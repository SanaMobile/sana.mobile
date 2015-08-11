/**
 *
 */
package org.sana.android.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.content.Uris;
import org.sana.android.db.ModelWrapper;
import org.sana.android.db.SanaDB.BinarySQLFormat;
import org.sana.android.db.SanaDB.ImageSQLFormat;
import org.sana.android.db.SanaDB.SoundSQLFormat;
import org.sana.android.procedure.Procedure;
import org.sana.android.procedure.ProcedureParseException;
import org.sana.android.procedure.ProcedureElement.ElementType;
import org.sana.android.provider.Encounters;
import org.sana.android.provider.Observations;
import org.sana.android.provider.Patients;
import org.sana.android.provider.Procedures;
import org.sana.android.provider.Subjects;
import org.sana.android.service.QueueManager;
import org.sana.android.service.impl.DispatchService;
import org.sana.android.util.Dates;
import org.sana.core.Patient;
import org.sana.net.MDSResult;
import org.sana.net.Response;
import org.sana.net.http.HttpTaskFactory;
import org.sana.util.UUIDUtil;
import org.xml.sax.SAXException;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * @author Sana Development
 *
 */
public class MDSInterface2 {

	public static final String TAG = MDSInterface2.class.getSimpleName();

	/**
	 * Gets the value in the MDS url setting and add the correct scheme, i.e.
	 * http or https, depending on the value of the use secure transmission
	 * setting.
	 * @param context The current context.
	 * @param path an additional path to append if not null
	 * @return The mds url with correct scheme as a String.
	 */
	public static String getMDSUrl(Context context, String path){
		String host = context.getString(R.string.host_mds);
		String root = context.getString(R.string.path_root);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		host = preferences.getString(Constants.PREFERENCE_MDS_URL, host);
		// Realistacally should never use http
		boolean useSecure = preferences.getBoolean(
				Constants.PREFERENCE_SECURE_TRANSMISSION, true);
		String scheme = (useSecure)? "https": "http";
		String url = scheme + "://" + host +"/"+ root;
		return (TextUtils.isEmpty(path)? url: url + path);
	}

	/**
	 * Gets the value in the MDS url setting and add the correct scheme, i.e.
	 * http or https, depending on the value of the use secure transmission
	 * setting.
	 * @param context The application context.
	 * @return The mds url with correct scheme.
	 */
	public static String getMDSUrl(Context context){
		return getMDSUrl(context, null);
	}

	private static String constructProcedureSubmitURL(String mdsURL) {
		return mdsURL + Constants.PROCEDURE_SUBMIT_PATTERN;
	}

	protected static MDSResult doPost(String scheme, String host, int port,
			String path,
			List<NameValuePair> postData) throws UnsupportedEncodingException{
		URI uri = null;
		try {
			uri = URIUtils.createURI(scheme, host, port, path, null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(String.format("Can not post to mds: %s, %s, %d, %s", scheme,host,port,path),e);
		}
		Log.d(TAG, "doPost() uri: " + uri.toASCIIString());
		HttpPost post = new HttpPost(uri);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData, "UTF-8");
		post.setEntity(entity);
		return MDSInterface2.doExecute(post);

	}

	/**
	 * Executes a POST method. Provides a wrapper around doExecute by
	 * preparing the PostMethod.
	 *
	 * @param url the request url
	 * @param postData the form data.
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected static MDSResult doPost(String url,
			List<NameValuePair> postData) throws UnsupportedEncodingException
	{

		HttpPost post = new HttpPost(url);
		Log.d(TAG, "doPost(): " + url + ", " + postData.size());
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData, "UTF-8");
		post.setEntity(entity);
		return MDSInterface2.doExecute(post);
	}

	protected static MDSResult doPost(String scheme,
			String host,
			int port,
			String path,
			HttpEntity entity)
	{
		URI uri = null;
		try {
			uri = URIUtils.createURI(scheme, host, port, path, null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(String.format("Can not post to mds: %s, %s, %d, %s", scheme,host,port,path),e);
		}
		Log.d(TAG, "doPost() uri: " + uri.toASCIIString());
		HttpPost post = new HttpPost(uri);
		post.setEntity(entity);
		return MDSInterface2.doExecute(post);
	}

	/**
	 * Executes a POST method. Provides a wrapper around doExecute by
	 * preparing the PostMethod.
	 *
	 * @param url the request url
	 * @param entity the form data.
	 * @return
	 */
	protected static MDSResult doPost(String url, HttpEntity entity)
	{
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		return MDSInterface2.doExecute(post);
	}

	/**
	 * Executes a client HttpMethod.
	 *
	 * @param method The Http
	 * @return
	 */
	protected static MDSResult doExecute(HttpUriRequest method){
		HttpClient client = HttpTaskFactory.CLIENT_FACTORY.produce();
		MDSResult response = null;
		HttpResponse httpResponse = null;
		String responseString = null;
		try {
			httpResponse = client.execute(method);
			Log.d(TAG, "doExecute() got response code " +  httpResponse.getStatusLine().getStatusCode());

			char buf[] = new char[20560];
			responseString = EntityUtils.toString(httpResponse.getEntity());
			Log.d(TAG, "doExecute() Received from MDS:" + responseString);//.length()+" chars");
			Gson gson = new Gson();
			response = gson.fromJson(responseString, MDSResult.class);

		} catch (IOException e1) {
			Log.e(TAG, e1.toString());
			e1.printStackTrace();
		} catch (JsonParseException e) {
			Log.e(TAG, "doExecute(): Error parsing MDS JSON response: "
					+ e.getMessage());
		}
		return response;
	}

	private static boolean postResponses(Context c,
			String savedProcedureGuid,
			String procedureUUID,
			String subjectUUID,
			String jsonResponses,
			String username,
			String password) throws UnsupportedEncodingException
	{

		SharedPreferences preferences = PreferenceManager
											.getDefaultSharedPreferences(c);
		String mdsURL = getMDSUrl(c);
		Log.d(TAG, "mds url: " + mdsURL);
		//mdsURL = checkMDSUrl(mdsURL);
		String mUrl = constructProcedureSubmitURL(mdsURL);
		String phoneId = preferences.getString("s_phone_name",
				Constants.PHONE_ID);
		return MDSInterface2.postResponses(c, savedProcedureGuid,
				procedureUUID, subjectUUID, jsonResponses, username, password, phoneId);

	}

	static boolean postResponses(Context context,
			String savedProcedureGuid,
			String procedureUUID,
			String subjectUUID,
			String jsonResponses,
			String username,
			String password,
			String phoneId) throws UnsupportedEncodingException
	{
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		String scheme = MDSInterface.getScheme(preferences);
		String host = MDSInterface.getHost(preferences, context.getString(R.string.host_mds));
		String path = context.getString(R.string.path_root) + context.getString(R.string.path_encounter);
		int port = MDSInterface.getPort(context);
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("savedproc_guid", savedProcedureGuid));
		postData.add(new BasicNameValuePair("procedure_guid", procedureUUID));
		postData.add(new BasicNameValuePair("phone", phoneId));
		postData.add(new BasicNameValuePair("username", username));
		postData.add(new BasicNameValuePair("password", password));
		postData.add(new BasicNameValuePair("responses", jsonResponses));
		postData.add(new BasicNameValuePair("subject", subjectUUID));

		String mdsURL = getMDSUrl(context);
		Log.d(TAG, "mds url: " + mdsURL);
		//mdsURL = checkMDSUrl(mdsURL);
		String mUrl = constructProcedureSubmitURL(mdsURL);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData, "UTF-8");
		MDSResult postResponse = MDSInterface2.doPost(mUrl, entity);
		if(postResponse != null){
			Log.d(TAG, "postResponses(...): " +postResponse.toString());
			int code = Integer.valueOf(postResponse.getCode());
			return (code == 200)? postResponse.succeeded(): false;
		} else {
			Log.d(TAG, "postResponses(...): null response");
			return false;
		}


	}

	static class NetworkConfig{
		String host;
		int port;
		int proxy;
		String path;
	}
	/*
	public static Uri getInfo(Context c, Uri uri){
		SharedPreferences preferences = PreferenceManager
			.getDefaultSharedPreferences(c);

		String scheme = ((preferences.getBoolean(
			Constants.PREFERENCE_SECURE_TRANSMISSION, true))? "https":"http");

		// If there's a proxy enabled, use it.
		String proxyHost = preferences.getString(Constants.PREFERENCE_PROXY_HOST, "");
		String sProxyPort = null;
		int proxyPort = 0;
		try {
			sProxyPort = preferences.getString(Constants.PREFERENCE_PROXY_PORT, "0");
			if (!TextUtils.isEmpty(sProxyPort));
				proxyPort = Integer.parseInt(sProxyPort);
		} catch(NumberFormatException e) {
			Log.w(TAG, "Invalid proxy port: " + sProxyPort);
		}

		URI uri = null;
		try {
			uri = URIUtils.createURI(scheme, host, port, path, null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(String.format("Can not post to mds: %s, %s, %d, %s", scheme,host,port,path),e);
		}
		*/

	/**
	 *
	 * @author Sana development
	 *
	 */
	static class EncounterCompat{

		String uuid = null;
		String subject = null;
		String observer = null;
		String procedure = null;
		String concept = "521b0825-14c9-49e5-a95e-462a01e2ae05";
		String device = null;
		boolean finished = false;
		boolean uploaded = false;
		String observations = null;

		public static EncounterCompat readFromCursor(Context ctx, Uri uri){
			EncounterCompat ec = new EncounterCompat();

			Cursor c = null;

			c = ctx.getContentResolver().query(
					uri, savedProcedureProjection, null,
					null, null);
			ec.uuid = c.getString(c.getColumnIndex(Encounters.Contract.UUID));
			ec.subject = c.getString(c.getColumnIndex(Encounters.Contract.SUBJECT));
			ec.observer = c.getString(c.getColumnIndex(Encounters.Contract.OBSERVER));
			ec.procedure = c.getString(c.getColumnIndex(Encounters.Contract.PROCEDURE));
			ec.uploaded = c.getInt(c.getColumnIndex(Encounters.Contract.UPLOADED)) > 0;
			return ec;
		}
	}

	public static String[] savedProcedureProjection = new String[] {
		Encounters.Contract._ID,
		Encounters.Contract.PROCEDURE,
		Encounters.Contract.STATE,
		Encounters.Contract.FINISHED,
		Encounters.Contract.UUID,
		Encounters.Contract.UPLOADED,
		Encounters.Contract.SUBJECT,
		Encounters.Contract.OBSERVER};

	public static boolean postProcedureToDjangoServer(Uri uri, Context context, String user, String password) throws UnsupportedEncodingException {
		Log.i(TAG, "In Post procedure to Django server for background uploading service.");
		Log.i(TAG, "Attempting to upload: " + uri);
		QueueManager.setProcedureUploadStatus(context, uri, QueueManager.UPLOAD_STATUS_IN_PROGRESS);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String mdsURL = getMDSUrl(context);
		Log.d(TAG, "mds url: " + mdsURL);
		//mdsURL = checkMDSUrl(mdsURL);
		String mUrl = constructProcedureSubmitURL(mdsURL);

		// get the tel number - default to ten-digit all zero's if null
		TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String device = tMgr.getLine1Number();
		device = (TextUtils.isEmpty(device))? "9999999999":device;

		Cursor cursor = context.getContentResolver().query(
				uri, savedProcedureProjection, null,
				null, null);

		// First get the saved procedure...
		int savedProcedureId = -1;
		String answersJson = null;
		boolean finished = false;
		String savedProcedureGUID = null;
		boolean savedProcedureUploaded = false;
		String observerUUID = null;
		String subjectUUID = null;
		String phoneId =  null;
		String procedureId = null;
		//todo rEMOVE THIS AND REPLACE
		try{
			cursor.moveToFirst();
			procedureId = cursor.getString(cursor.getColumnIndex(Encounters.Contract.PROCEDURE));
			savedProcedureId = cursor.getInt(0);
			answersJson = cursor.getString(2);
			finished = cursor.getInt(3) != 0;
			savedProcedureGUID = cursor.getString(cursor.getColumnIndex(Encounters.Contract.UUID));
			savedProcedureUploaded = cursor.getInt(5) != 0;
			observerUUID = cursor.getString(
				cursor.getColumnIndex(Encounters.Contract.OBSERVER));
			subjectUUID = cursor.getString(
				cursor.getColumnIndex(Encounters.Contract.SUBJECT));
		} finally {
			if(cursor != null) cursor.close();
		}

		Log.i(TAG, "...encounter " + savedProcedureGUID);
		Log.i(TAG, "...procedure" + procedureId );
		Log.i(TAG, "...subject" + subjectUUID);
		Log.i(TAG, "...device" + device);
		Log.i(TAG, "...observer " + observerUUID);
		//if(savedProcedureUploaded)
		//	return true;

		// TODO Remove this entirely and replace
		Uri procedureUri = null;
		if(UUIDUtil.isValid(procedureId))
			procedureUri = Uris.withAppendedUuid(Procedures.CONTENT_URI, procedureId);
		else{
			procedureUri = ContentUris.withAppendedId(
					Procedures.CONTENT_URI, Long.parseLong(procedureId));
		}
		Log.i(TAG, "Getting procedure uuid "+ ModelWrapper.getUuid(procedureUri, context.getContentResolver()));
		Log.i(TAG, "Getting procedure " + procedureUri.toString());

		String procedureTitle = null;
		String procedureXml = null;
		String procedureUUID = null;
		cursor = context.getContentResolver().query(procedureUri,
				new String[] { Procedures.Contract.TITLE,
							   Procedures.Contract.PROCEDURE,
							   Procedures.Contract.UUID},
				null, null, null);

		try{
			cursor.moveToFirst();
			procedureTitle = cursor.getString(
				cursor.getColumnIndex(Procedures.Contract.TITLE));
			procedureXml = cursor.getString(
				cursor.getColumnIndex(Procedures.Contract.PROCEDURE));
			procedureUUID = cursor.getString(
				cursor.getColumnIndex(Procedures.Contract.UUID));
		} finally {
			if(cursor != null) cursor.close();
		}


		Log.i(TAG, "...encounter " + savedProcedureGUID);
		Log.i(TAG, "...procedure" + procedureUUID + " '" + procedureTitle +"'");
		Log.i(TAG, "...subject" + subjectUUID);
		Log.i(TAG, "...device" + device);
		Log.i(TAG, "...observer " + observerUUID);

		if(!finished) {
			Log.i(TAG, "Not finished. Not uploading. (just kidding)"
					+ uri.toString());
			//return false;
		}




		// Map of all of the Procedure Elements; i.e. observation data
		// binaries get parsed out later
		Map<String, Map<String,String>> elementMap = null;
		try {

			Procedure p = Procedure.fromXMLString(procedureXml);

			p.setInstanceUri(uri);

			JSONTokener tokener = new JSONTokener(answersJson);
			JSONObject answersDict = new JSONObject(tokener);

			Map<String,String> answersMap = new HashMap<String,String>();
			Iterator<?> it = answersDict.keys();
			while(it.hasNext()) {
				String key = (String)it.next();
				answersMap.put(key, answersDict.getString(key));
			}
			Log.i(TAG, "restoreAnswers");
			p.restoreAnswers(answersMap);
			elementMap = p.toElementMap();

		} catch (IOException e2) {
			Log.e(TAG, e2.toString());
		} catch (ParserConfigurationException e2) {
			Log.e(TAG, e2.toString());
		} catch (SAXException e2) {
			Log.e(TAG, e2.toString());
		} catch (ProcedureParseException e2) {
			Log.e(TAG, e2.toString());
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
		}
		// check that we don't have empty map
		if(elementMap == null) {
			Log.i(TAG, "Could not encounter text " + uri + ". Not uploading.");
			return false;
		}
		Map<String, Map<String, String>> observations = getObservationsCompat(context,savedProcedureGUID, elementMap);

		// Add in procedureTitle as a fake answer
		/*
		Map<String,String> titleMap = new HashMap<String,String>();
		titleMap.put("answer", procedureTitle);
		titleMap.put("id", "procedureTitle");
		titleMap.put("type", "HIDDEN");
		elementMap.put("procedureTitle", titleMap);
		*/
		class ElementAnswer {
			public String id;
			public String answer;
			public String type;
			public ElementAnswer(String id, String answer, String type) {
				this.id = id;
				this.answer = answer;
				this.type = type;
			}
		}
		// Convert saved procedure to JSON
		JSONObject jsono = new JSONObject();
		int totalBinaries = 0;
		ArrayList<ElementAnswer> binaries = new ArrayList<ElementAnswer>();
		for(Entry<String,Map<String,String>> e : observations.entrySet()) {
			try {
				jsono.put(e.getKey(), new JSONObject(e.getValue()));
			} catch (JSONException e1) {
				Log.e(TAG, "JSON conversion fail: " + e1.getMessage());
			}

			String id = e.getKey();
			String type = e.getValue().get("type");
			String answer = e.getValue().get("answer");

			if (id == null || type == null || answer == null)
				continue;

			// Find elements that require binary uploads
			if(type.equals(ElementType.PICTURE.toString()) ||
					type.equals(ElementType.BINARYFILE.toString()) ||
					type.equals(ElementType.SOUND.toString()) ||
					type.equals(ElementType.PLUGIN.toString())){
				binaries.add(new ElementAnswer(id, answer, type));
				if(!"".equals(answer)) {
					String[] ids = answer.split(",");
					totalBinaries += ids.length;
				}
			}
		}

		Log.i(TAG, "About to post responses.");
		// check if it is already uploaded
		if(savedProcedureUploaded) {
			Log.i(TAG, "Responses have already been sent to MDS, not posting.");
		} else {
             QueueManager.setProcedureUploadStatus(context, uri, QueueManager.UPLOAD_STATUS_IN_PROGRESS);
			// upload the question and answer pairs text, without packetization
			String json = jsono.toString();
			Log.i(TAG, "json string: " + json.length());

			// try repeating upload on fail to some preset number
			final int MAX_TRIES = 3;
			int tries = 0;
			while(tries < MAX_TRIES) {
				if (MDSInterface2.postResponses(context,
						savedProcedureGUID, procedureTitle, subjectUUID,json,
						user, password,device)) {
					// Mark the procedure text as uploaded in the database
					ContentValues cv = new ContentValues();
					cv.put(Encounters.Contract.UPLOADED, true);
					context.getContentResolver().update(uri, cv, null, null);
					Log.i(TAG, "Responses were uploaded successfully.");
					break;
				}
				tries++;
			}
			// if tries >= maximum we bail and try again later
			if(tries == MAX_TRIES) {
				Log.e(TAG, "Could not post responses, bailing.");
                                QueueManager.setProcedureUploadStatus(context, uri, QueueManager.UPLOAD_STATUS_FAILURE);
				return false;
			}

		}
		Log.i(TAG, "Posted responses, now sending " + totalBinaries
				+ " binaries.");
		// lookup starting packet size
		int newPacketSize;
		try {
			newPacketSize = Integer.parseInt(
					PreferenceManager.getDefaultSharedPreferences(context)
						.getString("s_packet_init_size",
						Integer.toString(Constants.DEFAULT_INIT_PACKET_SIZE)));
		} catch (NumberFormatException e) {
			newPacketSize = Constants.DEFAULT_INIT_PACKET_SIZE;
		}
		// adjust from KB to bytes
		newPacketSize *= 1000;

		int totalProgress = 1+totalBinaries;
		int thisProgress = 2;
		// upload each binary file where each binary should be represented by
		// one value in a comma separated list of ints starting
		
		final int MAXBINARY_POST_ATTEMPT = 5;
		for(ElementAnswer e : binaries) {
			if(TextUtils.isEmpty(e.answer)){
				Log.w(TAG, "Got empty answer! Element: " + e.id);
				continue;
			}
				
			// parse csv list
			String[] ids = e.answer.split(",");
			// loop over each value
			for(String binaryId : ids) {
				Uri binUri = null;
				ElementType type = ElementType.INVALID;
				try {
					type = ElementType.valueOf(e.type);
				} catch(IllegalArgumentException ex) {
					Log.e(TAG, ex.getMessage());
				}

				if (type == ElementType.PICTURE) {
					binUri = ContentUris.withAppendedId(
								ImageSQLFormat.CONTENT_URI,
								Long.parseLong(binaryId));
				} else if (type == ElementType.SOUND) {
					binUri = ContentUris.withAppendedId(
								SoundSQLFormat.CONTENT_URI,
								Long.parseLong(binaryId));
				} else if (type == ElementType.PLUGIN) {
					binUri = ContentUris.withAppendedId(
							BinarySQLFormat.CONTENT_URI,
							Long.parseLong(binaryId));
				} else if (type == ElementType.BINARYFILE) {
					binUri = Uri.fromFile(new File(e.answer));
					// We can't tell if a BINARYFILE has been uploaded before.
					// Maybe if we grab the mtime/filesize on the file and store
					// it when we upload it.
				}
				
				
				int binaryPostCount = 0;
				while(binaryPostCount < MAXBINARY_POST_ATTEMPT){
					binaryPostCount++;
					try {
						Log.i(TAG, "Uploading " + binUri);
						// reset the new packet size each time to the last
						// successful transmission size
						newPacketSize = MDSInterface.transmitBinary(context, savedProcedureGUID,
												   e.id, binaryId, type, binUri,
												   newPacketSize);
						// Delete the file!
						switch(type) {
						case PICTURE:
						case SOUND:
							//This was deleting the pictures after upload - should
							// not happen, leave commented out!
							//context.getContentResolver().delete(binUri,null,null);
							break;
						default:
						}
					} catch (Exception x) {
						Log.e(TAG, "Uploading " + binUri + " failed : "
							+ x.toString());
						x.printStackTrace();
						QueueManager.setProcedureUploadStatus(context, uri, QueueManager.UPLOAD_STATUS_FAILURE);
						return false;
					}
					thisProgress++;
				}
				
			}
		}
		// TODO Tag entire procedure in db as done transmitting
		QueueManager.setProcedureUploadStatus(context, uri, QueueManager.UPLOAD_STATUS_SUCCESS);
		return true;
	}

	public static boolean postProcedureToDjangoServer(Uri uri, Context context) throws UnsupportedEncodingException {


		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String username = preferences.getString(
				Constants.PREFERENCE_EMR_USERNAME, Constants.DEFAULT_USERNAME);
		String password = preferences.getString(
				Constants.PREFERENCE_EMR_PASSWORD, Constants.DEFAULT_PASSWORD);
		return MDSInterface2.postProcedureToDjangoServer(uri,context, username,password);
	}
	/*
		String mdsUuid = null;
		String uuid = null;
		String subject = null;
		String observer = null;
		String procedure = null;
		String encConcept = "521b0825-14c9-49e5-a95e-462a01e2ae05";

		// get the tel number - default to ten-digit all zero's if null
		TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String device = tMgr.getLine1Number();
		device = (TextUtils.isEmpty(device))? "0000000000":device;

		// Fetch the encounter information
		Cursor c = null;
		c = context.getContentResolver().query(uri, null, null, null, null);
		c.moveToFirst();
		uuid = c.getString(c.getColumnIndex(Encounters.Contract.UUID));
		subject = c.getString(c.getColumnIndex(Encounters.Contract.SUBJECT));
		observer = c.getString(c.getColumnIndex(Encounters.Contract.OBSERVER));
		procedure = c.getString(c.getColumnIndex(Encounters.Contract.PROCEDURE));
		if( c!= null)
			c.close();
		// TODO Remove this at release
		Log.i(TAG, "uuid: " + uuid);
		Log.i(TAG, "subject: " + subject);
		Log.i(TAG, "observer: " + observer);
		Log.i(TAG, "procedure: " + procedure);
		Log.i(TAG, "concept: " + encConcept);
		Log.i(TAG, "device: " + device);

		// build out the observations
		String[] obsProjection =  new String[]{
				Observations.Contract.ID,
				Observations.Contract.CONCEPT,
				Observations.Contract.VALUE
		};

		Cursor cursor = context.getContentResolver().query(
				Observations.CONTENT_URI,
				obsProjection,
				Observations.Contract.ENCOUNTER + " = ?",
				new String[]{ uuid } ,
				Observations.Contract.ID + " ASC");
		List<Map<String,String>> observations = new ArrayList<Map<String,String>>(cursor.getCount());
		while(cursor.moveToNext()){
			Map<String,String> obs = new HashMap<String,String>();
			String id = cursor.getString(cursor.getColumnIndex(Observations.Contract.ID));
			String concept = cursor.getString(cursor.getColumnIndex(Observations.Contract.CONCEPT));
			String value = cursor.getString(cursor.getColumnIndex(Observations.Contract.VALUE));
			//TODO handle complex observations
			obs.put(Observations.Contract.ID, id);
			obs.put(Observations.Contract.CONCEPT, concept);
			obs.put(Observations.Contract.VALUE, value);
			observations.add(obs);
		}

		return mdsUuid;

	}
	*/

	public static List<Map<String, String>> getObservations(Context context, String uuid){

		final String[] obsProjection = new String[] {
				Observations.Contract.UUID, Observations.Contract.ID,
				Observations.Contract.CONCEPT, Observations.Contract.VALUE, };
		List<Map<String, String>> observations = Collections.EMPTY_LIST;
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(
					Observations.CONTENT_URI, obsProjection,
					Observations.Contract.ENCOUNTER + " = ?",
					new String[] { uuid }, Observations.Contract.ID + " ASC");
			observations = new ArrayList<Map<String, String>>(cursor.getCount());
			while (cursor.moveToNext()) {
				Map<String, String> obs = new HashMap<String, String>(4);
				// TODO handle complex observations
				obs.put(Observations.Contract.UUID, cursor.getString(0));
				obs.put(Observations.Contract.ID, cursor.getString(1));
				obs.put(Observations.Contract.CONCEPT, cursor.getString(2));
				obs.put(Observations.Contract.VALUE, cursor.getString(3));
				observations.add(obs);
				Log.d(TAG, DatabaseUtils.dumpCurrentRowToString(cursor));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return observations;
	}

	/**
	 * Returns the observations for a single encounter in a format that can be passed to the mds 1.x
	 * responses post
	 * @param context The current context
	 * @param uuid The encounter uuid String.
	 * @param elementMap A 1.x style map of elements which includes the element type.
	 * @return
	 */
	public static Map<String, Map<String, String>> getObservationsCompat(Context context, String uuid, Map<String, Map<String, String>> elementMap){
		final String[] obsProjection = new String[] {
				Observations.Contract.UUID,
				Observations.Contract.ID,
				Observations.Contract.CONCEPT,
				Observations.Contract.VALUE,
				Observations.Contract.PARENT};
		Map<String, Map<String, String>> observations = null;
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(
					Observations.CONTENT_URI, obsProjection,
					Observations.Contract.ENCOUNTER + " = ?",
					new String[] { uuid }, Observations.Contract.ID + " ASC");
			observations = new HashMap<String, Map<String, String>>(cursor.getCount());

			while (cursor.moveToNext()) {
				Map<String, String> obs = new HashMap<String, String>(4);
				// TODO handle complex observations
				String eID = cursor.getString(1);
				Map<String,String> element = elementMap.get(eID);
				if(element == null){
					Log.w(TAG, "No element with id: " + eID);
					String[] subID = eID.split("_");
					// Complex node so we inherit from parent
					Log.w(TAG, "Trying parent id: " + subID[0]);
					Map<String,String> parent = elementMap.get(subID[0]);
					element = new HashMap<String,String>();
					element.put("type", parent.get("type"));
				}
				element.put(Observations.Contract.UUID, cursor.getString(0));
				element.put("id", eID);
				element.put(Observations.Contract.CONCEPT, cursor.getString(2));
				element.put("answer", cursor.getString(3));
				observations.put(cursor.getString(1), element);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return observations;
	}

	public static List<String> postObservations(Context context, String uuid) throws UnsupportedEncodingException {
		Cursor cursor = null;

		List<Map<String, String>> observations = null;
		cursor = context.getContentResolver().query(
				Observations.CONTENT_URI,
				new String[]{
						Observations.Contract.UUID,
						Observations.Contract.ENCOUNTER,
						Observations.Contract.CONCEPT,
						Observations.Contract.ID,
						Observations.Contract.PARENT,
						Observations.Contract.VALUE
				},
				Observations.Contract.ENCOUNTER + " = ?",
				new String[]{ uuid } ,
				Observations.Contract.ID + " ASC");



		return null;
	}
	public static HttpPost createSessionRequest(Context context, String username,
			String password) throws URISyntaxException
	{
		String url = getMDSUrl(context, context.getString(R.string.path_session));
		Log.d(TAG, "createSessionRequest() "+url);
		URI uri = getURI(context,context.getString(R.string.path_session));
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("username", username));
		postData.add(new BasicNameValuePair("password", password));
		return HttpRequestFactory.getPostRequest(uri, postData);
	}

	public static boolean getFile(Context context, Uri file) throws IOException {
		String path = "media/" + file.getLastPathSegment();
		return getFile(context,path, new File(file.getPath()));
	}

	public static boolean getFile(Context context, String path, File outfile) throws IOException{
		String url = getMDSUrl(context, path);
		Log.d(TAG, "getFile() "+url +", outfile:" + outfile);
		URI uri = URI.create(url);
		HttpGet get = new HttpGet(uri);
		HttpClient client = HttpTaskFactory.CLIENT_FACTORY.produce();
		HttpResponse response;
		FileOutputStream fileOutput = null;
		InputStream inputStream = null;
		boolean result = false;
		try {
			response = client.execute(get);
			fileOutput = new FileOutputStream(outfile);
			inputStream = response.getEntity().getContent();
			byte[] buffer = new byte[1024];
			int bufferLength = 0;
			while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
				fileOutput.write(buffer, 0, bufferLength);
				Log.d(TAG, "... wrote bytes:" + bufferLength);
			}
			result = true;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(fileOutput != null) fileOutput.close();
			if(inputStream != null) inputStream.close();
		}
		return result;
	}

	/**
	 * Builds and returns an HttpClient with Basic Authorization headers initialized
	 * with a BasicCredentialsProvider
	 *
	 * @param username The username credential.
	 * @param password The password credential.
	 * @return a new HttpClient
	 */

	public static HttpClient buildBasicAuthClient(String username, String password){
		HttpClient client = HttpTaskFactory.CLIENT_FACTORY.produce();
		if(!(TextUtils.isEmpty(username) || TextUtils.isEmpty(password))){
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
	        credsProvider.setCredentials(
	                new AuthScope(AuthScope.ANY_HOST,AuthScope.ANY_PORT),
	                new UsernamePasswordCredentials(username,password));
	        ((DefaultHttpClient)client).setCredentialsProvider(credsProvider);
		}
		return client;
	}

	public static  Response<String> apiGet(URI uri) throws UnsupportedEncodingException
	{
		return apiGet(uri, null, null);
	}

	public static  Response<String> apiGet(URI uri, String username, String password) throws UnsupportedEncodingException
			{
		HttpClient client = buildBasicAuthClient(username,password);
		HttpGet request = new HttpGet(uri);
		request.setHeader("Accept", "application/json");
		HttpResponse httpResponse = null;
		Response<String> response = Response.empty();
		try {
			httpResponse = client.execute(request);
			response.message = EntityUtils.toString(httpResponse.getEntity());
			response.code = httpResponse.getStatusLine().getStatusCode();
			response.status = Response.SUCCESS;
		} catch (ClientProtocolException e) {
			response.setCode(500);
			response.setStatus(Response.FAILURE);
			e.printStackTrace();
		} catch (IOException e) {
			response.setCode(501);
			response.setStatus(Response.FAILURE);
			e.printStackTrace();
		}
		return response;
	}

	public static synchronized <T> Response<T> apiGet(URI uri, String username, String password,
			ResponseHandler<Response<T>> handler) throws UnsupportedEncodingException
			{
		HttpClient client = buildBasicAuthClient(username,password);
		HttpGet request = new HttpGet(uri);
		request.setHeader("Accept", "application/json");
		HttpResponse httpResponse = null;
		Response<T> response = Response.empty();
		try {
			httpResponse = client.execute(request);
			response = handler.handleResponse(httpResponse);
		} catch (ClientProtocolException e) {
			response.setCode(500);
			response.setStatus(Response.FAILURE);
            response.errors = new String[]{
                    "ClientProtocolException executing request",
                    e.toString()
            };
			e.printStackTrace();
		} catch (IOException e) {
			response.setCode(501);
			response.setStatus(Response.FAILURE);
            response.errors = new String[]{
                    "IOException executing request",
                    e.toString()
            };
			e.printStackTrace();
		} catch (OutOfMemoryError e){
			response.setCode(500);
			response.setStatus(Response.FAILURE);
            response.errors = new String[]{
                    "OutOfMemoryError reading server response",
                    e.toString()
            };
			e.printStackTrace();
		} catch (Exception e) {
            response.setCode(501);
            response.setStatus(Response.FAILURE);
            response.errors = new String[]{
                "Exception",
                e.toString()
        };
        e.printStackTrace();
    }
		return response;
	}

	public static <T> Response<T> apiGet(URI uri, ResponseHandler<Response<T>> handler) throws UnsupportedEncodingException
	{
		return apiGet(uri,"","",handler);
	}

	public static synchronized <T> Response<T> apiPost(URI uri, String username, String password,
			Map<String, String> values,
			ResponseHandler<Response<T>> handler)
	{
                Log.i(TAG, "apiPost()" + uri);
		HttpClient client = buildBasicAuthClient(username,password);
		HttpPost request = new HttpPost(uri);
		HttpResponse httpResponse = null;
		Response<T> response = Response.empty();
		try {
                        HttpEntity entity = new UrlEncodedFormEntity(mapToPost(values), "UTF-8");
                        request.setEntity(entity);
                        Log.i(TAG, "apiPost(): executing" + request.getMethod());
			httpResponse = client.execute(request);
			response = handler.handleResponse(httpResponse);
		} catch (ClientProtocolException e) {
			response.setCode(500);
			response.setStatus(Response.FAILURE);
			e.printStackTrace();
		} catch (IOException e) {
			response.setCode(501);
			response.setStatus(Response.FAILURE);
			e.printStackTrace();
		}
		return response;
	}

	/**
	 *
	 * @param uri
	 * @param username
	 * @param password
	 * @param form
	 * @param handler
	 * @return
	 */
	public static synchronized <T> Response<T> apiPut(URI uri, String username, String password,
			Map<String, String> form,
			Map<String, String> files,
			ResponseHandler<Response<T>> handler){
		HttpClient client = buildBasicAuthClient(username,password);
		HttpPut request = new HttpPut(uri);
		HttpResponse httpResponse = null;
		Response<T> response = Response.empty();
		try{
                    Log.d(TAG,"apiPut() BUILDING ENTITY");
                    //UrlEncodedFormEntity
                    HttpEntity entity;
                    if(files == null){
                        entity = new UrlEncodedFormEntity(mapToPost(form), "UTF-8");
                    } else {
                        entity = new MultipartEntity();

                        Log.d(TAG,"apiPut() updating n = " + form.entrySet().size() + "values");
                    }
                    request.setEntity(entity);
		    Log.d(TAG,"apiPut() executing" + request.getMethod());
		    httpResponse = client.execute(request);
		    Log.d(TAG,"apiPut() reading response");
		    response = handler.handleResponse(httpResponse);
		    Log.d(TAG,"apiPut() --> " + response.status + ": " + response.code);
		} catch (ClientProtocolException e) {
			response.setCode(500);
			response.setStatus(Response.FAILURE);
			e.printStackTrace();
		} catch (IOException e) {
			response.setCode(501);
			response.setStatus(Response.FAILURE);
			e.printStackTrace();
		}
		return response;
	}

	public static synchronized <T> Response<T> apiDelete(URI uri, String username, String password,
			Map<String, Object> values,
			ResponseHandler<Response<T>> handler){
		HttpClient client = buildBasicAuthClient(username,password);
		HttpDelete request = new HttpDelete(uri);
		HttpResponse httpResponse = null;
		Response<T> response = Response.empty();
		try {
			httpResponse = client.execute(request);
			response = handler.handleResponse(httpResponse);
		} catch (ClientProtocolException e) {
			response.setCode(500);
			response.setStatus(Response.FAILURE);
			e.printStackTrace();
		} catch (IOException e) {
			response.setCode(501);
			response.setStatus(Response.FAILURE);
			e.printStackTrace();
		}
		return response;
	}

	/**
     * Converts Android "content" style resource identifiers to URIs to use with the
     * new MDS REST API. Only works for those objects whose path components in the new
     * REST API are consistent with MDS. Requires the <code>host_mds</code> and
     * <code>path_root</code> strings be declared as resources, <code>cfg_mds_port</code>
     * integer be declared in the resources as well as the PREFERENCE_MDS_URL setting
     * value declared in the settings.xml.
     *
	 * @param context
	 * @param uri
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static URI iriToURI(Context context, Uri uri) throws
		MalformedURLException, URISyntaxException
	{
		String host = context.getString(R.string.host_mds);
		String root = context.getString(R.string.path_root);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		host = preferences.getString(Constants.PREFERENCE_MDS_URL, host);
		boolean useSecure = preferences.getBoolean(
				Constants.PREFERENCE_SECURE_TRANSMISSION, true);
		String scheme = (useSecure)? "https": "http";
		String path = "/mds" + uri.getPath();
                if(!path.endsWith("/"))
                    path = path + "/";
		int port = 443;
		try{
		    port = Integer.valueOf(preferences.getString(Constants.PREFERENCE_MDS_PORT, "443"));
                } catch (Exception e){
                    e.printStackTrace();
                }
		return Uris.iriToURI(uri, scheme, host, port, root);
	}

	static String getScheme(SharedPreferences preferences){
		if(preferences.getBoolean(Constants.PREFERENCE_SECURE_TRANSMISSION, true))
			return "https";
		else
			return "http";
	}

	static int getPort(Context c){
		SharedPreferences preferences = PreferenceManager
											.getDefaultSharedPreferences(c);
		if(preferences.getBoolean(Constants.PREFERENCE_SECURE_TRANSMISSION, true))
			return 443;
		else
			return c.getResources().getInteger(R.integer.port_mds);
	}

	public static URI getRoot(Context c) throws URISyntaxException{
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(c);

		String scheme = getScheme(preferences);
		String host = preferences.getString(Constants.PREFERENCE_MDS_URL,
				c.getString(R.string.host_mds));
		int port = getPort(c);
		String path = c.getString(R.string.path_root);
		return new URI(scheme, null, host, port,path, null, null);
	}

	public static URI getURI(Context c, String path) throws URISyntaxException{
		return getURI(c,path,null);
	}

	public static URI getURI(Context c, String path, String query) throws URISyntaxException{
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(c);

		String scheme = getScheme(preferences);
		String host = preferences.getString(Constants.PREFERENCE_MDS_URL,
				c.getString(R.string.host_mds));
		int port = getPort(c);
		String root = c.getString(R.string.path_root);
		Log.i(TAG," path:"+  root + path);
		URI uri =  new URI(scheme, null, host, port,root + path, query, null);
		Log.i(TAG, "uri: " + uri.toString() +", path:" + path);
		return uri;
	}

	public static void logObservations(Context context, String uuid){
		Cursor cursor = context.getContentResolver().query(
				Observations.CONTENT_URI,
				null,
				Observations.Contract.ENCOUNTER + " = ?",
				new String[]{ uuid } ,
				Observations.Contract.ID + " ASC");
		StringBuilder obs = new StringBuilder("{ 'encounter': "+ uuid + ", 'observations' : [");
		if(cursor != null){
			while(cursor.moveToNext()){
				obs.append("{");
				obs.append("'"+ Observations.Contract._ID+ "': " + cursor.getLong(cursor.getColumnIndex(Observations.Contract._ID)) +", ");
				obs.append("'"+ Observations.Contract.ID+ "': " + cursor.getString(cursor.getColumnIndex(Observations.Contract.ID)) +", ");
				obs.append("'"+ Observations.Contract.CONCEPT+ "': " + cursor.getString(cursor.getColumnIndex(Observations.Contract.CONCEPT)) +", ");
				obs.append("'"+ Observations.Contract.VALUE+ "': " + cursor.getString(cursor.getColumnIndex(Observations.Contract.VALUE)) +", ");
				obs.append("}");
			}
		}
		obs.append("]}");
		Log.i(TAG, obs.toString());
	}

    public synchronized static final <T> Response<T> syncUpdate(Context ctx, Uri uri,
        String username, String password,
        Bundle form,
        Bundle files,
        ResponseHandler<Response<T>> handler)
    {
        Log.i(TAG, "syncUpdate()");
        Map<String, String> data = new HashMap<String, String>();
        Cursor c = null;
        Response<T> response = Response.empty();
        // Should have at least one field that need to be updated
        if(form != null){
            Iterator<String> keys = form.keySet().iterator();
            while(keys.hasNext()){
                String key = keys.next();
                data.put(key, form.getString(key));
                Log.d(TAG, "key: " + key +" --> value: "+ form.getString(key));
            }
        }
        try{
            URI target = iriToURI(ctx,uri);
            response = apiPost(target,username,password,data,handler);
        } catch(Exception e){
            e.printStackTrace();
        }
        return response;
    }

    public static List<NameValuePair> mapToPost(Map<String, ? extends Object> map){
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        map = (map != null)?map: new HashMap<String,Object>();
        for(String key: map.keySet()){
            Log.i(TAG, "    " + key + " : " + map.get(key));
            postData.add(new BasicNameValuePair(key, String.valueOf(map.get(key))));
        }
        return postData;
    }

    public static HttpEntity buildEntity(Map<String, ? extends Object> form, Map<String,File> files)
        throws UnsupportedEncodingException
    {
        if(files  == null){
            return new UrlEncodedFormEntity(mapToPost(form),"UTF-8");
        } else {
            MultipartEntity entity = new MultipartEntity();
            for(String key: form.keySet()){
                entity.addPart(key,
                        new StringBody(String.valueOf(form.get(key))));
            }
            for(Entry<String, File> entry: files.entrySet()){
                //entity.addPart(entry.getKey(),
                //        new ByteArrayBody(entry.getValue()));
            }
            return entity;
        }
    }

    public static Response<Collection<Patient>> postPatient(Context context,
            Patient patient, String username, String password,
            ResponseHandler<Response<Collection<Patient>>> handler){
        Log.i(TAG, "postPatient(Context,Patient,String,String, " +
                "ResponseHandler<Response<Collection<Patient>>>)");
        Response<Collection<Patient>> response = Response.empty();
        try {
            URI target = getURI(context, Subjects.CONTENT_URI.getPath() + "/");
            Map<String,String> values = new HashMap<String,String>();
            values.put(Patients.Contract.GIVEN_NAME, patient.getGiven_name());
            values.put(Patients.Contract.FAMILY_NAME, patient.getFamily_name());
            values.put(Patients.Contract.GENDER, patient.getGender());
            if(patient.getLocation() != null &&
                    !TextUtils.isEmpty(patient.getLocation().getUuid())) {
                values.put(Patients.Contract.LOCATION,
                        patient.getLocation().getUuid());
            } else {
                values.put(Patients.Contract.LOCATION,
                        context.getString(R.string.cfg_default_location));
            }
            values.put(Patients.Contract.UUID, patient.getUuid());
            values.put(Patients.Contract.PATIENT_ID, patient.system_id);
            values.put(Patients.Contract.DOB, Dates.toSQL(patient.getDob()));
            response = MDSInterface2.apiPost(target, username, password,
                    values, handler);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            response.code = 500;
            response.message = Collections.EMPTY_LIST;
        } catch (Exception e) {
            e.printStackTrace();
            response.code = 500;
            response.message = Collections.EMPTY_LIST;
        }
        return response;
    }

    public static Response<Collection<Patient>> updatePatient(Context context,
         Patient patient, String username, String password,
         ResponseHandler<Response<Collection<Patient>>> handler){
        Log.i(TAG, "postPatient(Context,Patient,String,String, " +
                "ResponseHandler<Response<Collection<Patient>>>)");
        Response<Collection<Patient>> response = Response.empty();
        try {
            URI target = getURI(context, Subjects.CONTENT_URI.getPath() + "/"
            + patient.getUuid());
            Map<String,String> values = new HashMap<String,String>();
            values.put(Patients.Contract.GIVEN_NAME, patient.getGiven_name());
            values.put(Patients.Contract.FAMILY_NAME, patient.getFamily_name());
            values.put(Patients.Contract.GENDER, patient.getGender());
            if(patient.getLocation() != null &&
                    !TextUtils.isEmpty(patient.getLocation().getUuid())) {
                values.put(Patients.Contract.LOCATION + "__uuid",
                        patient.getLocation().getUuid());
            } else {
                values.put(Patients.Contract.LOCATION + "__uuid",
                        context.getString(R.string.cfg_default_location));
            }
            values.put(Patients.Contract.PATIENT_ID, patient.system_id);
            values.put(Patients.Contract.DOB, Dates.toSQL(patient.getDob()));
            response = MDSInterface2.apiPut(target, username, password,
                    values, null, handler);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            response.code = 500;
            response.message = Collections.EMPTY_LIST;
        } catch (Exception e) {
            e.printStackTrace();
            response.code = 500;
            response.message = Collections.EMPTY_LIST;
        }
        return response;
    }
}
