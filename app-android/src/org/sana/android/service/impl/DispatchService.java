/**
 * Copyright (c) 2013, Sana
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sana nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL Sana BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sana.android.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.RequestLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import org.sana.R;
import org.sana.android.activity.EncounterList;
import org.sana.android.app.Locales;
import org.sana.android.app.NotificationFactory;
import org.sana.android.content.Intents;
import org.sana.android.content.Uris;
import org.sana.android.db.ModelWrapper;
import org.sana.android.net.HttpTask;
import org.sana.android.net.MDSInterface;
import org.sana.android.net.NetworkTaskListener;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.QueueManager;
import org.sana.android.util.Logf;
import org.sana.android.util.SanaUtil;
import org.sana.android.util.UriUtil;

import org.sana.core.Patient;
import org.sana.core.Procedure;
import org.sana.net.Response;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Implementation of the dispatch server as an Android service.
 * 
 * @author Sana Development
 *
 */
public class DispatchService extends Service{
	public static final String TAG = DispatchService.class.getSimpleName();

	
	static final int REQUEST = 0;
	static final int RESPONSE = 1;
	
	//TODO Refactor this out.
	abstract static class JSONHandler<T> {
		JSONHandler(){}

		public abstract List<ContentValues> values(Response<T> response);
		
		public Response<T> fromJson(Message msg){
    		Log.d("JSONHandler<T>", msg.obj.toString());
			Type type = new TypeToken<Response<T>>(){}.getType();


			Gson gson = new GsonBuilder()
				     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				     .setDateFormat("yyyy-MM-dd HH:mm:ss")
				     .create();
			Response<T> response = gson.fromJson(msg.obj.toString(), type);
    		Log.d("JSONHandler<T>", msg.obj.toString());
    		return response;
		}

	}
	
	final JSONHandler<List<Procedure>> procedureListHandler = new JSONHandler<List<Procedure>>(){

		@Override
		public List<ContentValues> values(Response<List<Procedure>> response) {
			
			List<ContentValues> list = new ArrayList<ContentValues>();
			for(Procedure p: response.getMessage()){
				for(Field f:p.getClass().getDeclaredFields()){
					Log.d(TAG, f.toGenericString());
				}
				ContentValues vals = new ContentValues();
				vals.put(Procedures.Contract.UUID, p.getUuid());
				vals.put(Procedures.Contract.TITLE, p.getDescription());
				vals.put(Procedures.Contract.AUTHOR, p.getAuthor());
				vals.put(Procedures.Contract.VERSION, p.getVersion());
				vals.put(Procedures.Contract.PROCEDURE, p.getSrc());
				list.add(vals);
			}
			return list;
		}
		
		String handleSrc(String src, Uri uri) throws IOException{			
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO)
				     System.setProperty("http.keepAlive", "false");
		
			StringBuilder builder = new StringBuilder();
			URL url = null;
			HttpURLConnection con = null;
			BufferedReader in = null;
			Writer out = null;
			try {
				url = new URL(src);
				con = (HttpURLConnection) url.openConnection();
				out = new OutputStreamWriter(getContentResolver().openOutputStream(uri));
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String str;
			    while ((str = in.readLine()) != null) {
			    	out.write(str);
			    }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if(in != null)
					in.close();
				if(out != null)
					out.close();
				if(con != null)
					con.disconnect();
			}
			
			return builder.toString();
		}
		
	};
	
	final JSONHandler<List<Patient>> patientListHandler = new JSONHandler<List<Patient>>(){
		public  Response<List<Patient>> fromJson(Message msg){
			Type type = new TypeToken<Response<List<Patient>>>(){}.getType();
			Gson gson = new GsonBuilder()
				     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				     .setDateFormat(DispatchService.this.getString(R.string.cfg_format_date_value))
				     .create();
			Response<List<Patient>> response = gson.fromJson(msg.obj.toString(), type);
    		return response;
		}

		@Override
		public List<ContentValues> values(Response<List<Patient>> response) {
			List<ContentValues> list = new ArrayList<ContentValues>();
			for(Patient p: response.getMessage()){
				Log.d(TAG, p.toString());
				ContentValues vals = new ContentValues();
				vals.put(Patients.Contract.GIVEN_NAME, p.getGiven_name());
				vals.put(Patients.Contract.FAMILY_NAME, p.getFamily_name());
				vals.put(Patients.Contract.GENDER, p.getGender());
				vals.put(Patients.Contract.LOCATION, p.getLocation().toString());
				vals.put(Patients.Contract.UUID, p.getUuid());
				vals.put(Patients.Contract.PATIENT_ID, p.system_id);
				vals.put(Patients.Contract.DOB, p.getDob().toString());
				if(p.getImage() != null && (p.getImage().getPath().endsWith("jpg") || p.getImage().getPath().endsWith("png"))){
					try{ 
						File root = Environment.getExternalStorageDirectory();
						File media = new File(root, "/media/sana/");
						String path = media.getAbsolutePath() + "/" +  p.getImage().toASCIIString();
						Log.d(TAG, path);
						File f = new File(path);
						if(f.isDirectory()){
							Log.d(TAG, "deleting erroneous directory " + f.getAbsolutePath());
							f.delete();
						}
						if(f.exists()){
							// no need to download again
							Log.d(TAG, "File exists " + f.getAbsolutePath());
							if(f.isDirectory())
								f.delete();
							else
								vals.put(Patients.Contract.IMAGE, Uri.fromFile(f).toString());
						} else {
							// TODO Add image download here
							Log.d(TAG, "Need to download file to: " + f.getAbsolutePath());
							
							//f.getParentFile().mkdirs();
							URL url = new URL("http://ec2-23-23-147-197.compute-1.amazonaws.com/mds/media/" + p.getImage().toASCIIString());
							Log.d(TAG, "Downloading from: " + url);
							
							HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
							urlConnection.setRequestMethod("GET");
							urlConnection.setDoOutput(true);
							urlConnection.connect();
							FileOutputStream fileOutput = new FileOutputStream(f);
							InputStream inputStream = urlConnection.getInputStream();
							byte[] buffer = new byte[1024];
							int bufferLength = 0;
							while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
								fileOutput.write(buffer, 0, bufferLength);
								Log.d(TAG, "... wrote bytes:" + bufferLength);
							}
							fileOutput.close();
							vals.put(Patients.Contract.IMAGE, Uri.fromFile(f).toString());
						
						}
					} catch(Exception e){
						Log.e(TAG, e.getMessage());
						e.printStackTrace();
					}
				} else {
					try{ 
						File root = Environment.getExternalStorageDirectory();
						File media = new File(root, "/media/sana/");
						String path = media.getAbsolutePath() + "/" +  p.getImage().toASCIIString();
						Log.d(TAG, path);
						File f = new File(path);
						if(f.isDirectory()){
							Log.d(TAG, "deleting erroneous directory " + f.getAbsolutePath());
							f.delete();
							//f.createNewFile();
						}
					} catch(Exception e){
						Log.e(TAG, e.getMessage());
						e.printStackTrace();
					}
				}
				list.add(vals);
			}
			return list;
		}
	};
	
	static final int create = 0x00000001;
	static final int read = 0x00000010;
	static final int update = 0x00000100;
	static final int delete = 0x000001000;
	
	static final int CREATE = new Intent("org.sana.android.intent.action.CREATE").filterHashCode();
	static final int READ = new Intent("org.sana.android.intent.action.READ").filterHashCode();
	static final int UPDATE = new Intent("org.sana.android.intent.action.UPDATE").filterHashCode();
	static final int DELETE = new Intent("org.sana.android.intent.action.DELETE").filterHashCode();
	
	static final IntentFilter filter = new IntentFilter();
	static{
		filter.addAction("org.sana.android.intent.action.CREATE");
		filter.addAction("org.sana.android.intent.action.READ");
		filter.addAction("org.sana.android.intent.action.UPDATE");
		filter.addAction("org.sana.android.intent.action.DELETE");
		filter.addDataScheme("content");
		filter.addDataAuthority("org.sana.provider", null);
	}

	static final IntentFilter pkgFilter = new IntentFilter();
	static{
		pkgFilter.addAction("org.sana.android.intent.action.READ");
		pkgFilter.addAction("org.sana.android.intent.action.UPDATE");
		pkgFilter.addDataScheme("package");
	}
	
	public static final String PKG = "application/vnd.android.package-archive";
	public static final int PKG_MASK = 0x00000000;
	
	// Callback we pass to the Handler Thread to execute the requests.
	protected Handler.Callback getHandlerCallback(){
		return new Handler.Callback() {
			
			/*
			 * (non-Javadoc)
			 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
			 */
			/**
			 * Message passed should have the following values
			 * what - REQUEST(0) or RESPONSE(1)
			 * arg1 - startId passed to startService()
			 * arg2 - Uri descriptor
			 * obj - Uri string of the intent
			 * data - additional query/post parameters
			 * 
			 * Request will be sent from initial call to handle message which 
			 * will trigger a second callback placed on the Handler for the 
			 * response. Hence, Response may not be handled in the same order as
			 * the original request.
			 */
			@Override
			public boolean handleMessage(Message msg) {
				Logf.D(TAG, "handleMessage()", String.format(
						"Message what: %d, arg1: %d, arg2: %d", 
						msg.what,msg.arg1,msg.arg2));
				try{
				switch(msg.arg2){
				case REQUEST:
					Logf.I(TAG, "handleMessage(Message)", "Got a REQUEST");
					HttpUriRequest request = null;
					HttpResponse httpResponse = null;
					String responseString = null;
					HttpClient client = new DefaultHttpClient();
					
					Intent intent = Intent.parseUri(msg.obj.toString(), 0);
					String action = intent.getAction();
					// set the request method.
					String method = "GET";
					if(action.contains("CREATE"))
						method = "POST";
					else if(action.contains("READ"))
						method = "GET";
					else if(action.contains("UPDATE"))
						method = "PUT";
					else if(action.contains("DELTE"))
						method = "DELETE";

					Logf.D(TAG, "handleMessage()", String.format("Method: %s", method));
					
					String root = getString(R.string.cfg_mds_core);
					String path = (intent.getData() != null)?
							intent.getData().getPath(): "";
				    if (root.endsWith("/") && path.startsWith("/")){
				    	path.replaceFirst("/", "");
				    }
					URI uri = URI.create(root + path);
					Logf.D(TAG, "handleMessage()", "method: " + method+", uri: " + uri);
					ContentValues update = new ContentValues();		
					switch(msg.what){
					case Uris.ENCOUNTER_DIR:
						// TODO implement as a query from msg.data
						break;
					case Uris.ENCOUNTER_UUID:
					case Uris.ENCOUNTER_ITEM:
						//TODO Allows GET or POST
						if(method.equals("GET"))
							request = new HttpGet(uri);
						else if(method.equals("POST")){
							// TODO 
							boolean encounterPost = MDSInterface.postProcedureToDjangoServer(intent.getData(), DispatchService.this);
							// Send notification to notification bar

							// Notification intent
							Intent notifyIntent = new Intent();
							notifyIntent.setClass(getApplicationContext(), EncounterList.class);
							// Sets the Activity to start in a new, empty task
							notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
							
							if(encounterPost){
								DispatchService.this.notify(R.string.upload_success,notifyIntent);
								update.put(Encounters.Contract.UPLOAD_STATUS, QueueManager.UPLOAD_STATUS_SUCCESS);
								DispatchService.this.getContentResolver().update(intent.getData(), update, null, null);
							}else{
								DispatchService.this.notify(R.string.upload_fail, notifyIntent);
								update.put(Encounters.Contract.UPLOAD_STATUS, QueueManager.UPLOAD_STATUS_FAILURE);
								DispatchService.this.getContentResolver().update(intent.getData(), update, null, null);
							}
							/*
							responseString = String.valueOf(encounterPost);
							Cursor c = null;
							try{
								c = DispatchService.this.getContentResolver().query(Uri.parse(uri.toString()), null,null,null,null);
								if(c != null && c.moveToFirst()){
									String uuid = c.getString(c.getColumnIndex(Encounters.Contract.UUID));
									String subject = c.getString(c.getColumnIndex(Encounters.Contract.SUBJECT));
									
								}
							} catch(Exception e){
								
							}
							*/
							//request = new HttpPost(uri);
							
						}
						break;
					case Uris.OBSERVATION_DIR:
						// TODO implement as a query from msg.data
						break;
					case Uris.OBSERVATION_UUID:
						//TODO Allows GET or POST
						if(method.equals("GET"))
							request = new HttpGet(uri);
						else if(method.equals("POST")){
							request = new HttpPost(uri);
						}
						break;
					case Uris.PROCEDURE_DIR:
						//TODO Allows GET only for updating procedures from repository
						uri = URI.create(getString(R.string.cfg_mds_core) 
								+ "procedure/");
						// only allows get
						request = new HttpGet(uri);
						break;
					case Uris.PROCEDURE_UUID:
						//TODO Allows GET. This should pull raw xml
						if(method.equals("GET"))
							request = new HttpGet(uri);
						break;
					case Uris.SUBJECT_DIR:
						// only alows GET for pulling the patient list.
						// TODO switch over to newer methods
						request = MDSInterface.createSubjectRequest(DispatchService.this);
						break;
					case Uris.SUBJECT_UUID:
						//TODO Allows get or post
						if(method.equals("GET"))
							request = new HttpGet(uri);
						else if(method.equals("POST")){
							request = new HttpPost(uri);
						}
						break;
					case Uris.PACKAGE_DIR:
						Logf.D(TAG, "handleMessage(Message)", "PACKAGE update request");
						uri = URI.create(DispatchService.this.getString(R.string.cfg_app_url));
						request = new HttpGet(uri);
						break;
					default:
					}	
					// wrap it up and send it back to self as a response
					Message rsp = Message.obtain(msg);
					rsp.arg2 = RESPONSE;
					if(request != null){
						Logf.I(TAG, "handleMessage(Message)", "sending to url: " + request.getURI());
						//TODO Reimplement the HTTPS layer
						httpResponse = client.execute(request);
						responseString = EntityUtils.toString(httpResponse.getEntity());
					} else {
						
					}
					rsp.obj = responseString;
					rsp.sendToTarget();
					break;
					
					
				case RESPONSE:
					Logf.I(TAG, "handleResponse(Message)", "Got a RESPONSE: " + msg.obj);
					Response<?> response;
					List<ContentValues> content = new ArrayList<ContentValues>();
					if(msg.obj == null){
						Logf.W(TAG, "handleResponse(Message)", "NULL RESPONSE");
						break;
					}
					switch(msg.what){
					case Uris.ENCOUNTER_DIR:
						// TODO implement as a query from msg.data
						break;
					case Uris.ENCOUNTER_ITEM:
					case Uris.ENCOUNTER_UUID:
						Logf.D(TAG, String.valueOf(msg.obj));
						break;
					case Uris.OBSERVATION_DIR:
						// TODO implement as a query from msg.data
						break;
					case Uris.OBSERVATION_ITEM:
					case Uris.OBSERVATION_UUID:
						//TODO Allows GET or POST
						break;
					case Uris.PROCEDURE_DIR:
						/*
						Response<List<Procedure>> p = procedureListHandler.fromJson(msg);
						content = procedureListHandler.values(p);
						for(ContentValues v: content){
							ModelWrapper.insertOrUpdate(Procedures.CONTENT_URI, v, getContentResolver());
						}
						*/
						break;
					case Uris.PROCEDURE_ITEM:
					case Uris.PROCEDURE_UUID:
						//TODO Allows GET. This should pull raw xml
						break;
					case Uris.SUBJECT_DIR:
						Response<List<Patient>> patientListResponse = patientListHandler.fromJson(msg);
						content = patientListHandler.values(patientListResponse);
						for(ContentValues v: content){
							ModelWrapper.insertOrUpdate(Subjects.CONTENT_URI, v, getContentResolver());
						}
						getContentResolver().notifyChange(Subjects.CONTENT_URI, null);
						break;
					case Uris.SUBJECT_ITEM:
					case Uris.SUBJECT_UUID:
						//TODO
						Response<Patient> patientResponse;
						break;
					case Uris.PACKAGE_DIR:
						Logf.D(TAG, "handleMessage(Message)", "PACKAGE update response: " + msg.obj);
						// This will download and install new apk
						break;
					default:
					}
					break;
				default:
					
				}

				} catch (Exception e){
					e.printStackTrace();
				}
				stopSelf(msg.arg1);
				return true;
			}
		};
	}
	
	////////////////////////////////////////////////////////////////////////////
	//	
	////////////////////////////////////////////////////////////////////////////
	int mStartMode;       						// indicates how to behave if the service is killed
	final IBinder mBinder = new Binder();       // interface for clients that bind
	boolean mAllowRebind; 						// indicates whether onRebind should be used
	private Looper mServiceLooper;
	private Handler mHandler;
	private boolean initialized = false;
	private NotificationFactory mNotificationFactory;
	
	////////////////////////////////////////////////////////////////////////////
	//	Begin Overridden methods
	////////////////////////////////////////////////////////////////////////////
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate()");
	    HandlerThread thread = new HandlerThread("dispatcher",
	            Process.THREAD_PRIORITY_BACKGROUND);
	    thread.start();
	    // Get the HandlerThread's Looper and use it for our Handler 
	    mServiceLooper = thread.getLooper();
	    mHandler = new Handler(mServiceLooper, getHandlerCallback());
	    if(!initialized)
	    	initialized = checkInit();
	    mNotificationFactory = NotificationFactory.getInstance(getApplicationContext());
	    mNotificationFactory.setContentTitle(R.string.network_alert);
	  }
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand()" + intent.getAction());
	    //handleCommand(intent);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
		int matchesPackage = pkgFilter.match(getContentResolver(), intent, false, TAG);
		int matchesModel = filter.match(getContentResolver(), intent, false, TAG);
		int what = Uris.getDescriptor(intent.getData());
		Log.e(TAG, String.format("Message what: %d, match: %d, pkg: %d",what,matchesModel, matchesPackage));
		
		// msg.what <=> [REQUEST|RESPONSE] and  msg.arg1 <=> startId 
		if(matchesModel >= 0){
			Log.w(TAG, String.format("Obtaining message what: %d, match: %d",what,matchesModel));
			Message msg = mHandler.obtainMessage(what, intent.toUri(Intent.URI_INTENT_SCHEME));
			msg.arg1 = startId;
			msg.arg2 = REQUEST;
			mHandler.sendMessage(msg);
		} else if(matchesPackage >= 0){
			Log.w(TAG, String.format("Obtaining message what: %d, match: %d",what,matchesPackage));
			Message msg = mHandler.obtainMessage(what, intent.toUri(Intent.URI_INTENT_SCHEME));
			msg.arg1 = startId;
			msg.arg2 = REQUEST;
			mHandler.sendMessage(msg);
		} else {
			Log.e(TAG, String.format("Unrecognized message. what: %d, match: %d",what,matchesPackage));
			stopSelf(startId);
		}
		mStartMode = START_STICKY;
	    return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		Logf.D(TAG, "onDestroy()", "...finishing");
		super.onDestroy();
	}
	
	private final boolean checkInit(){
		Logf.D(TAG, "initialize()", "Entering");
	    SharedPreferences preferences = 
        		PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    int version = 0;
	    String dbKey = getString(R.string.cfg_db_init);
	    String dbVersion = getString(R.string.cfg_db_version);
	    // check whether the db is initialized and create if not
	    boolean doInit = preferences.getBoolean(dbKey, false);
	    Logf.D(TAG, "initialize()", "dbs initialized: " + doInit);
	    if(!doInit){
	    	getContentResolver().acquireContentProviderClient(Procedures.CONTENT_URI).release();
	    	getContentResolver().delete(Procedures.CONTENT_URI, null, null);
	    	SanaUtil.loadDefaultDatabase(getBaseContext());
	    	preferences.edit().putBoolean(dbKey, true)
	    		.putInt(dbVersion, getResources().getInteger(R.integer.cfg_db_version_value))
	    		.putBoolean(dbKey, true)
	    		.commit();
	    }
	    return true;
	}
	
	//TODO Refactor these out
	/**
	 * Returns the number of ms until the next sync should occur
	 * 
	 * @param uri
	 * @return
	 */
	private final long nextSync(Uri uri){
		final String METHOD = "delta(Uri)";
		
		SharedPreferences preferences = 
    			PreferenceManager.getDefaultSharedPreferences(DispatchService.this);
		String key = "last_sync";
		
		switch(Uris.getDescriptor(uri)){
		default:
			key = "last_sync";
		}
		long now = new Date().getTime();
		long tdelta = Long.valueOf(getString(R.string.sync_delta));
		long delta = now - preferences.getLong(key, 0);
		return delta;
	}
	
	private final void resetSync(Uri uri){
		final String METHOD = "resetSync(Uri)";
		
		SharedPreferences preferences = 
    			PreferenceManager.getDefaultSharedPreferences(DispatchService.this);

		String key = null;
		switch(Uris.getDescriptor(uri)){
		default:
				key = "last_sync";
		}
		preferences.edit().putLong(key, new Date().getTime()).commit();
	}
	
	// The dispatch server URI
	URI uHost = null;
	HttpClient mClient = null;
	HttpHost mHost = null;
	

	
	protected void build() throws URISyntaxException{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String scheme = prefs.getString("_scheme", getString(R.string.cfg_mds_scheme));
		String host = prefs.getString("_host", getString(R.string.cfg_mds_host));
		int port = prefs.getInt("_port", R.integer.cfg_mds_port);
		
		mHost = new HttpHost(host, port, scheme);

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		AuthScope authScope = new AuthScope(mHost.getHostName(), mHost.getPort());
		Credentials creds = new UsernamePasswordCredentials("username", "password");
		credsProvider.setCredentials(authScope,creds);
		
		mClient = new DefaultHttpClient();
		((AbstractHttpClient) mClient).getCredentialsProvider().setCredentials(
		        authScope, creds);
	}
	
	protected final void notify(int resID, Intent notifyIntent){
		
		// Pending intent used to launch the notification intent 
		PendingIntent actionIntent =
		        PendingIntent.getActivity(
		        getBaseContext(),
		        0,
		        notifyIntent,
		        PendingIntent.FLAG_UPDATE_CURRENT
		);
		// Always force the locale before we send the notification
		Locales.updateLocale(getBaseContext(), getString(R.string.force_locale));
		mNotificationFactory
			.setContentIntent(actionIntent)
			.setContentText(resID)
			.doNotify();
	}
	
	static <T> T readJSONStream(HttpURLConnection url) throws IOException{
		T t = null;
		JsonReader reader = new JsonReader(new InputStreamReader(url.getInputStream()));
		Type type = new TypeToken<T>(){}.getType();
		t = new Gson().fromJson(reader, type);
		return t;
	}
	
	static <T> T readJSONStream(HttpURLConnection url, Context context) throws IOException{
		T t = null;
		JsonReader reader = new JsonReader(new InputStreamReader(url.getInputStream()));
		final Gson gson = new GsonBuilder()
	     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
	     .setDateFormat(context.getString(R.string.cfg_format_date_value))
	     .create();
		Type type = new TypeToken<T>(){}.getType();
		return gson.fromJson(reader, type);
	}
	
	static <T> T readJSONStream(String string, Context context) throws IOException{
		final Gson gson = new GsonBuilder()
	     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
	     .setDateFormat(context.getString(R.string.cfg_format_date_value))
	     .create();
		Type type = new TypeToken<T>(){}.getType();
		return gson.fromJson(string, type);
	}	    
	
	
	
	
	public static String readCharStream(HttpURLConnection url) throws IOException{
		InputStreamReader in = null;
		StringBuilder builder = new StringBuilder();
		char[] buf = new char[80];
		in = new InputStreamReader(url.getInputStream()); 
		while(in.read(buf) > -1){
			builder.append(buf);
		}
		return builder.toString().trim();
		
	}
		    
	public static Uri readByteStream(HttpURLConnection url, String output) throws IOException{
		File file = new File(output);
		OutputStream out = null;
		InputStream in = null;
		try{
			in = new BufferedInputStream(url.getInputStream());
			out = (BufferedOutputStream) new BufferedOutputStream(new FileOutputStream(file));
			byte[] buffer = new byte[1024];
			while(in.read(buffer) > -1){
				out.write(buffer);
			}
		} finally {
			in.close();
			out.close();
		}
		return Uri.fromFile(new File(output));
	}
	
	private final void notifyPackageManager(final Context context, Uri apk){
		Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apk, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
        context.startActivity(intent);
	}
	
	
	final Handler.Callback updateCheckRunnable(Handler handler){

		Intent intent = new Intent(getString(R.string.intent_action_read));
        intent.setType("application/vnd.android.package-archive");
        
        
	    final URI uri = URI.create(this.getString(R.string.cfg_app_url));
	    final Message message = handler.obtainMessage(RESPONSE);
		try {
			final URL url = uri.toURL();
	    
			return new Handler.Callback() {
				
				@Override
				public boolean handleMessage(Message msg) {
					try {
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						Response<String> response = readJSONStream(connection);
						message.obj = response;
						return true;
					} catch (IOException e) {
						e.printStackTrace();
					}
					return false;
		    }};

		} catch (MalformedURLException e1) {
			throw new IllegalArgumentException(e1);
		}
	}

	final MessageFactory MSG_FACTORY = new MessageFactory();
	
	public static class MessageFactory{
		
		public MessageFactory(){}
		
		public Message obtainRequest(Handler handler, String action, Uri uri){
			Message msg = handler.obtainMessage(REQUEST, action.hashCode(), Uris.getDescriptor(uri));
			return msg;
		}
		
		public Message obtainRequest(Handler handler, int who, Intent intent){
			int what =  Uris.getDescriptor(intent.getData());
			Message msg = handler.obtainMessage(what, 
					intent.toUri(Intent.URI_INTENT_SCHEME));
			msg.arg1 = intent.filterHashCode();
			msg.arg2 = REQUEST;
			msg.obj = intent.toUri(Intent.URI_INTENT_SCHEME);
			return msg;
		}
		
		public Message obtainResponse(Message orig, Object obj){
			Message msg = Message.obtain(orig);
			msg.what = RESPONSE;
			msg.obj = obj;
			return msg;
		}
		
	}
}