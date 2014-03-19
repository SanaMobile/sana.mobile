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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.activity.EncounterList;
import org.sana.android.app.Locales;
import org.sana.android.app.NotificationFactory;
import org.sana.android.content.DispatchResponseReceiver;
import org.sana.android.content.ModelContext;
import org.sana.android.content.Uris;
import org.sana.android.db.ModelWrapper;
import org.sana.android.net.MDSInterface;
import org.sana.android.net.MDSInterface2;
import org.sana.android.provider.EncounterTasks;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.QueueManager;
import org.sana.android.util.Logf;
import org.sana.android.util.SanaUtil;
import org.sana.api.task.EncounterTask;

import org.sana.core.Patient;
import org.sana.core.Procedure;
import org.sana.net.Response;
import org.sana.net.http.HttpTaskFactory;
import org.sana.net.http.handler.EncounterResponseHandler;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
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
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

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
	public static final int UPLOAD_RESPONSE = 1;
	public static final String RESPONSE_NOTIFICATION_ID = "notification_id";
	public static final String RESPONSE_CODE = "code";
	
	
	//TODO Refactor this out.
	abstract static class SyncHandler<T> {
		SyncHandler(){}

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
		
		public abstract ContentValues[] values(T t);

	}
	
	final SyncHandler<List<Procedure>> procedureListHandler = new SyncHandler<List<Procedure>>(){

		@Override
		public List<ContentValues> values(Response<List<Procedure>> response) {
			
			List<ContentValues> list = new ArrayList<ContentValues>();
			for(Procedure p: response.getMessage()){
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

		@Override
		public ContentValues[] values(List<Procedure> t) {
			// TODO Auto-generated method stub
			return null;
		}
		
		
	};
	
	final SyncHandler<List<Patient>> patientListHandler = new SyncHandler<List<Patient>>(){
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
				Log.d(TAG, p.system_id);
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
						File dir = ModelContext.getExternalFilesDir(Subjects.CONTENT_URI);
						//String path = media.getAbsolutePath() + "/" +  p.getImage().toASCIIString();
						File f = new File(dir, p.getImage().toASCIIString());
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
							if(f.exists() && f.isDirectory())
								f.delete();
							f.getParentFile().mkdirs();
							boolean result = MDSInterface2.getFile(DispatchService.this, "media/" + p.getImage().toASCIIString(), f);
							Log.d(TAG, "Download success: " + result);
							vals.put(Patients.Contract.IMAGE, Uri.fromFile(f).toString());
						
						}
					} catch(Exception e){
						Log.e(TAG, e.getMessage());
						e.printStackTrace();
					}
				} else {
					try{ 
						File dir = ModelContext.getExternalFilesDir(Subjects.CONTENT_URI);
						Log.d(TAG, dir.getAbsolutePath());
						File f = new File(dir, p.getImage().toASCIIString());
						if(f.isDirectory() && f.delete()){
							Log.d(TAG, "deleting erroneous directory " + f.getAbsolutePath());
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

		@Override
		public ContentValues[] values(List<Patient> t) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	final SyncHandler<Collection<EncounterTask>> encounterTasksHandler = new SyncHandler<Collection<EncounterTask>>(){

		@Override
		public List<ContentValues> values(Response<Collection<EncounterTask>> response) {
			List<ContentValues> values = new ArrayList<ContentValues>(response.message.size());
			for(EncounterTask task:response.message){
				ContentValues value = new ContentValues();
				value.put(EncounterTasks.Contract.DUE_DATE , task.due_on);
				value.put(EncounterTasks.Contract.PROCEDURE , task.procedure.uuid);
				value.put(EncounterTasks.Contract.SUBJECT , task.subject.uuid );
				value.put(EncounterTasks.Contract.ENCOUNTER, task.encounter.uuid);
				value.put(EncounterTasks.Contract.OBSERVER , task.assigned_to.uuid);
				value.put(EncounterTasks.Contract.STATUS , task.getStatus());
				values.add(value);
			}
			return values;
		}

		@Override
		public ContentValues[] values(Collection<EncounterTask> t) {
			ContentValues[] values = new ContentValues[t.size()];
			Iterator<EncounterTask> iterator =  t.iterator();
			int index = 0;
			while(iterator.hasNext()){
				EncounterTask task = iterator.next();
				ContentValues value = new ContentValues();
				value.put(EncounterTasks.Contract.DUE_DATE , task.due_on);
				value.put(EncounterTasks.Contract.PROCEDURE , task.procedure.uuid);
				value.put(EncounterTasks.Contract.SUBJECT , task.subject.uuid );
				if(task.encounter != null)
					value.put(EncounterTasks.Contract.ENCOUNTER, task.encounter.uuid);
				value.put(EncounterTasks.Contract.OBSERVER , task.assigned_to.uuid);
				value.put(EncounterTasks.Contract.STATUS , task.getStatus());
				values[index] = value;
				index++;
			}
			return values;
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
	static final AtomicInteger sNotificationCount = new AtomicInteger(0);
	
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
				if(msg != null)
					Logf.D(TAG, "handleMessage()", String.format(
						"Message what: %d, arg1: %d, arg2: %d", 
						msg.what,msg.arg1,msg.arg2));
				else{
					Logf.W(TAG, "handleMessage()", "Null message. Was it supposed to be?");
					return true;
				}
				Bundle data = new Bundle();
				try{
					data.putAll(msg.getData());
				} catch (Exception e){
					e.printStackTrace();
				}
				// set up for results
				ContentValues[] values = null;
				int index = 0;
				Cursor c = null;
				
				
				try {
					switch (msg.arg2) {
					case REQUEST:
						Logf.I(TAG, "handleMessage(Message)", "Got a REQUEST");
						HttpUriRequest request = null;
						HttpResponse httpResponse = null;
						String responseString = null;
						HttpClient client = HttpTaskFactory.CLIENT_FACTORY.produce();

						Intent intent = Intent.parseUri(msg.obj.toString(), 0);
						String action = intent.getAction();
						// set the request method.
						String method = "GET";
						if (action.contains("CREATE"))
							method = "POST";
						else if (action.contains("READ"))
							method = "GET";
						else if (action.contains("UPDATE"))
							method = "PUT";
						else if (action.contains("DELTE"))
							method = "DELETE";

						Logf.D(TAG, "handleMessage()",
								String.format("Method: %s", method));

						String root = getString(R.string.cfg_mds_url);
						String path = (intent.getData() != null) ? intent
								.getData().getPath() : "";
						String query = (intent.getData() != null) ? intent
								.getData().getEncodedQuery() : "";
						if (root.endsWith("/") && path.startsWith("/")) {
							path.replaceFirst("/", "");
						}
						if(!TextUtils.isEmpty(query))
							path = path + "?" + query;
						URI uri = URI.create(root + path);
						Logf.D(TAG, "handleMessage()", "method: " + method
								+ ", uri: " + uri);
						
						// Set up for results
						ContentValues update = new ContentValues();
						
						switch (msg.what) {
						case Uris.ENCOUNTER_DIR:
							// TODO implement as a query from msg.data
							break;
						case Uris.ENCOUNTER_UUID:
						case Uris.ENCOUNTER_ITEM:
									
							// TODO Allows GET or POST
							if (method.equals("GET"))
								request = new HttpGet(uri);
							else if (method.equals("POST")) {
								// TODO
								boolean encounterPost = MDSInterface2
										.postProcedureToDjangoServer(
												intent.getData(),
												DispatchService.this);
								// Send notification to notification bar

								// Notification intent
								Intent notifyIntent = new Intent(
										DispatchService.this,
										EncounterList.class);
								// Sets the Activity to start in a new, empty
								// task
								notifyIntent
										.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

								if (encounterPost) {
									update.put(
											Encounters.Contract.UPLOAD_STATUS,
											QueueManager.UPLOAD_STATUS_SUCCESS);
									DispatchService.this.getContentResolver()
											.update(intent.getData(), update,
													null, null);
									//DispatchService.this.notify(
									//		R.string.upload_success,
									//		notifyIntent);
									DispatchService.this.notifyForeground(
											UPLOAD_RESPONSE, 
											R.string.upload_success, 
											notifyIntent);
									Intent broadcast = new Intent(DispatchResponseReceiver.BROADCAST_RESPONSE);//,intent.getData());
									Locales.updateLocale(DispatchService.this, getString(R.string.force_locale));
									broadcast.putExtra(DispatchResponseReceiver.KEY_RESPONSE_MESSAGE, getString(R.string.upload_success));
									broadcast.putExtra(RESPONSE_CODE, 200);
									LocalBroadcastManager.getInstance(DispatchService.this.getApplicationContext()).sendBroadcast(broadcast);
								} else {
									update.put(
											Encounters.Contract.UPLOAD_STATUS,
											QueueManager.UPLOAD_STATUS_FAILURE);
									DispatchService.this.getContentResolver()
											.update(intent.getData(), update,
													null, null);
									DispatchService.this.notifyForeground(
											UPLOAD_RESPONSE, 
											R.string.upload_fail, 
											notifyIntent);
									Intent broadcast = new Intent(DispatchResponseReceiver.BROADCAST_RESPONSE);//,intent.getData());
									Locales.updateLocale(DispatchService.this, getString(R.string.force_locale));
									broadcast.putExtra(DispatchResponseReceiver.KEY_RESPONSE_MESSAGE, getString(R.string.upload_fail));
									broadcast.putExtra(RESPONSE_CODE, 400);
									LocalBroadcastManager.getInstance(DispatchService.this.getApplicationContext()).sendBroadcast(broadcast);
									//DispatchService.this.notify(
									//		R.string.upload_fail, notifyIntent);
								}
								/*
								 * responseString =
								 * String.valueOf(encounterPost); Cursor c =
								 * null; try{ c =
								 * DispatchService.this.getContentResolver
								 * ().query(Uri.parse(uri.toString()),
								 * null,null,null,null); if(c != null &&
								 * c.moveToFirst()){ String uuid =
								 * c.getString(c.
								 * getColumnIndex(Encounters.Contract.UUID));
								 * String subject =
								 * c.getString(c.getColumnIndex(
								 * Encounters.Contract.SUBJECT));
								 * 
								 * } } catch(Exception e){
								 * 
								 * }
								 */
								// request = new HttpPost(uri);

							}
							break;
						case Uris.OBSERVATION_DIR:
							// TODO implement as a query from msg.data
							break;
						case Uris.OBSERVATION_UUID:
							// TODO Allows GET or POST
							if (method.equals("GET"))
								request = new HttpGet(uri);
							else if (method.equals("POST")) {
								request = new HttpPost(uri);
							}
							break;
						case Uris.PROCEDURE_DIR:
							// TODO Allows GET only for updating procedures from
							// repository
							uri = URI.create(getString(R.string.cfg_mds_core)
									+ "/procedure/");
							// only allows get
							request = new HttpGet(uri);
							break;
						case Uris.PROCEDURE_UUID:
							// TODO Allows GET. This should pull raw xml
							if (method.equals("GET"))
								request = new HttpGet(uri);
							break;
						case Uris.SUBJECT_DIR:
							// only alows GET for pulling the patient list.
							// TODO switch over to newer methods
							request = MDSInterface
									.createSubjectRequest(DispatchService.this);
							break;
						case Uris.SUBJECT_UUID:
							// TODO Allows get or post
							if (method.equals("GET"))
								request = new HttpGet(uri);
							else if (method.equals("POST")) {
								request = new HttpPost(uri);
							}
							break;
						case Uris.ENCOUNTER_TASK:
						case Uris.ENCOUNTER_TASK_DIR:
							if (method.equals("GET")){
								EncounterResponseHandler handler = new EncounterResponseHandler();
								Collection<EncounterTask> objs = Collections.emptyList();
								try {
									SharedPreferences preferences = PreferenceManager
											.getDefaultSharedPreferences(DispatchService.this);
									String username = preferences.getString(
											Constants.PREFERENCE_EMR_USERNAME, Constants.DEFAULT_USERNAME);
									String password = preferences.getString(
											Constants.PREFERENCE_EMR_PASSWORD, Constants.DEFAULT_PASSWORD);
									Response<Collection<EncounterTask>> response = MDSInterface2.apiGet(uri,username,password,handler);
									objs = response.message;
							        Log.i(TAG, "GET EncounterTask: " + objs.size());
									List<EncounterTask> updates = Collections.emptyList();
									Iterator<EncounterTask> it = objs.iterator();
									while(it.hasNext()){
										EncounterTask task = it.next();
										Log.i(TAG, "due date: " + task.due_on);
										try{
											c = ModelWrapper.getOneByUuid(EncounterTasks.CONTENT_URI, getContentResolver(), task.uuid);
											if(c != null && c.moveToFirst()){
												if(c.getCount() > 1)
													Logf.W(TAG, "CRITICAL! > 1 task exists uuid:" + task.uuid);
												it.remove();
												updates.add(task);
											}
										} catch (Exception e){
											e.printStackTrace();
										} finally {
											if (c != null){
												c.close();
											}
										}
										
									}

							        Log.i(TAG, "GET EncounterTask: new=" + objs.size());
									values = encounterTasksHandler.values(objs);
							        Log.i(TAG, "GET EncounterTask: cv=" + values.length);
									int inserted = getContentResolver().bulkInsert(EncounterTasks.CONTENT_URI, values);

							        Log.i(TAG, "GET EncounterTask: inserted=" + inserted);
									values = encounterTasksHandler.values(updates);
									for(ContentValues vals:values){
										String uuid = vals.getAsString(EncounterTasks.Contract.UUID);
										vals.remove(EncounterTasks.Contract.UUID);
										int updated = getContentResolver().update(Uris.withAppendedUuid(EncounterTasks.CONTENT_URI, uuid),vals,null,null);
								        Log.i(TAG, "GET EncounterTask: updated=" + updated);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							break;
						case Uris.PACKAGE_DIR:
							Logf.D(TAG, "handleMessage(Message)",
									"PACKAGE update request");
							uri = URI.create(DispatchService.this
									.getString(R.string.cfg_app_url));
							request = new HttpGet(uri);
							break;
						default:
						}
						// wrap it up and send it back to self as a response
						Message rsp = Message.obtain(msg);
						rsp.arg2 = RESPONSE;
						if (request != null) {
							Logf.I(TAG, "handleMessage(Message)",
									"sending to url: " + request.getURI());
							// TODO Reimplement the HTTPS layer
							httpResponse = client.execute(request);
							responseString = EntityUtils.toString(httpResponse
									.getEntity());
						} else {

						}
						rsp.obj = responseString;
						rsp.sendToTarget();
						break;

					case RESPONSE:
						Logf.I(TAG, "handleResponse(Message)",
								"Got a RESPONSE: " + msg.obj);
						Response<?> response;
						List<ContentValues> content = new ArrayList<ContentValues>();
						if (msg.obj == null) {
							Logf.W(TAG, "handleResponse(Message)",
									"NULL RESPONSE");
							break;
						}
						switch (msg.what) {
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
							// TODO Allows GET or POST
							break;
						case Uris.PROCEDURE_DIR:
							/*
							 Response<List<Procedure>> p =
							 procedureListHandler.fromJson(msg); 
							 content = procedureListHandler.values(p); 
							 for(ContentValues v: content){
							 ModelWrapper.insertOrUpdate(Procedures.CONTENT_URI, v, getContentResolver()); 
							 
							 }
							 */
							break;
						case Uris.PROCEDURE_ITEM:
						case Uris.PROCEDURE_UUID:
							// TODO Allows GET. This should pull raw xml
							break;
						case Uris.SUBJECT_DIR:
							Response<List<Patient>> patientListResponse = patientListHandler
									.fromJson(msg);
							content = patientListHandler
									.values(patientListResponse);

							Logf.D(TAG, "Returned list patients: " + content.size());
							// Validate new patients
							ListIterator<ContentValues> it = content.listIterator();
							c = null;
							while(it.hasNext()){
								ContentValues next = it.next();
								String uuid = next.getAsString(Patients.Contract.UUID);
								if(!TextUtils.isEmpty(uuid)){
									try{
										c = ModelWrapper.getOneByUuid(Patients.CONTENT_URI, getContentResolver(), uuid);
										if(c != null && c.moveToFirst() && c.getCount() == 1){
											it.remove();
										}
									} catch (Exception e){
										e.printStackTrace();
									} finally {
										if (c != null){
											c.close();
										}
									}
								}
							}
							Logf.D(TAG, "New patients: " + content.size());
							// insert new patients 
							index = 0;
							values = new ContentValues[content.size()];
							for (ContentValues v : content) {
								values[index] = content.get(index);
								index++;
							}
							if(values != null && values.length > 0)
								getContentResolver().bulkInsert(Subjects.CONTENT_URI, values);
							/*
							for (ContentValues v : content) {
								ModelWrapper.insertOrUpdate(
										Subjects.CONTENT_URI, v,
										getContentResolver());
							}
							*/
							// getContentResolver().notifyChange(Subjects.CONTENT_URI,
							// null);
							break;
						case Uris.SUBJECT_ITEM:
						case Uris.SUBJECT_UUID:
							// TODO
							Response<Patient> patientResponse;
							break;
						case Uris.PACKAGE_DIR:
							Logf.D(TAG, "handleMessage(Message)",
									"PACKAGE update response: " + msg.obj);
							// This will download and install new apk
							break;
						default:
						}
						break;
					default:

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				//stopSelf(msg.arg1);
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
	private AtomicInteger numNotifications = new AtomicInteger(0);
	
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
	    mNotificationFactory = NotificationFactory.getInstance(this);
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
		Log.i(TAG, "onStartCommand()" + ((intent != null)? intent.getAction(): null));
		if (intent != null) {
			// handleCommand(intent);
			// We want this service to continue running until it is explicitly
			// stopped, so return sticky.
			int matchesPackage = pkgFilter.match(getContentResolver(), intent,
					false, TAG);
			int matchesModel = filter.match(getContentResolver(), intent,
					false, TAG);
			int what = Uris.getDescriptor(intent.getData());
			Log.e(TAG, String.format("Message what: %d, match: %d, pkg: %d",
					what, matchesModel, matchesPackage));

			// msg.what <=> [REQUEST|RESPONSE] and msg.arg1 <=> startId
			if (matchesModel >= 0) {
				Log.w(TAG, String.format(
						"Obtaining message what: %d, match: %d", what,
						matchesModel));
				Message msg = mHandler.obtainMessage(what,
						intent.toUri(Intent.URI_INTENT_SCHEME));
				msg.arg1 = startId;
				msg.arg2 = REQUEST;
				mHandler.sendMessage(msg);
			} else if (matchesPackage >= 0) {
				Log.w(TAG, String.format(
						"Obtaining message what: %d, match: %d", what,
						matchesPackage));
				Message msg = mHandler.obtainMessage(what,
						intent.toUri(Intent.URI_INTENT_SCHEME));
				msg.arg1 = startId;
				msg.arg2 = REQUEST;
				mHandler.sendMessage(msg);
			} else {
				Log.e(TAG, String.format(
						"Unrecognized message. what: %d, match: %d", what,
						matchesPackage));
				//stopSelf(startId);
			}
					
		} else {
			Log.w(TAG, "onStartCommand(): Null intent. Are we resuming?");
			//stopSelf(startId);
		}
		mStartMode = START_STICKY;
	    return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		Logf.D(TAG, "onDestroy()", "...finishing");
		try{
			mNotificationFactory.cancelAll();
		} catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}
	
	@Override
	public boolean stopService(Intent intent){
		if(intent != null){
			int notification = intent.getIntExtra(RESPONSE_NOTIFICATION_ID, 0);
			if (notification != 0){
				mNotificationFactory.cancel(notification);
				if(sNotificationCount.decrementAndGet() == 0){
					mNotificationFactory.cancelAll();
					this.stopForeground(true);
					return super.stopService(intent);
				}
			}
		} else {
			return super.stopService(intent);
		} 
		return false;
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
	
	protected final void notifyForeground(int id, int resID, Intent notifyIntent){
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
		Notification notification = mNotificationFactory
				.setContentIntent(actionIntent)
				.setContentText(resID)
				.setNumber(numNotifications.incrementAndGet())
				.build();
		sNotificationCount.incrementAndGet();
		startForeground(id, notification);
	}
	
	protected final void notify(int resID, Intent notifyIntent){
		Log.d(TAG, "notify(...) " + resID +", " + notifyIntent.toUri(Intent.URI_INTENT_SCHEME));
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