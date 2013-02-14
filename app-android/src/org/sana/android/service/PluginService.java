package org.sana.android.service;

import java.io.File;

import org.sana.android.Constants;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

/**
 * Handles preparing Intents used for launching data capture Activities through
 * the ProcedureRunner class as well as post processing the returned data into
 * a standardized Intent.
 *
 * @author Sana Development Team
 */
public class PluginService{
	public static final String TAG = PluginService.class.getSimpleName();
	
	/** Handles extracting the actual data, if available, when an intent is
	 * returned from startActivityForResult with code RESULT_OK. The data will 
	 * be returned in an Intent with
	 * 
	 * 1. data - Uri encoded as either:
	 * 		inline-data: data:[mime-type]#[data] Actual data can be retrieved by 
	 * 			calling Uri.getFragment();
	 * 		content uri: content:[authority][path] Actual data can be retrieved
	 * 			using Android content resolution API
	 * 2. type - as determined by ContentResolver.getType() or text/plain
	 * 
	 * Activity accepting the rendered Intent should call Intent.getData() and
	 * Intent.getType() to extract the information.
	 * 
	 * Handles these situations:
	 * 1. Data is in result.getData()
	 * 2. Intent action is set to "inline-data" and data is present in extras
	 * 		through the "data" key-default for many Camera apps.
	 * 3. Data is returned as Intent.EXTRA_STREAM
	 * 4. Data is returned as Intent.EXTRA_TEXT
	 * 
	 * @param cr //Deprecated as soon as we change this over to a Service class.
	 * @param result The result that came back to the original launching 
	 * 	Activity
	 * @param obs The observation this was collected for
	 * @param hack String flag indicating this may require a hack. 
	 * @return
	 */
	public static Intent renderPluginActivityResult(ContentResolver cr, 
			Intent result, Uri obs, String hack)
	{
		Log.d(TAG, "Rendering activity result.");
		String type = "";
		String text = "";
		Intent rResult = new Intent();
		Uri stream = null;

		if (result != null){
			logIntent(TAG, result);
			Bundle extras = result.getExtras();
			Uri data = result.getData();
			String action = result.getAction();
			type = result.getType();

			// handle various returned data scenarios
			// Standard
			if(data != null){
				// hack for applications which don't return type
				if(TextUtils.isEmpty(type)){
					type = cr.getType(data);
					type = (TextUtils.isEmpty(type))? "application/octet-stream": type;
				}
				stream = data;
				Log.d(TAG, "....getData() : " + stream.toString());
			} 
			// Many camera apps return data as inline-data
			else if(action != null && action.equals("inline-data")){
				Bitmap b = result.getParcelableExtra("data");
				Log.d(TAG, "inline-data : " + b);
				// TODO fix this
				File extDir = Environment.getExternalStorageDirectory();
				File path = new File(extDir, Constants.PATH_OBSERVATION);
				path.mkdirs();
				File f = new File(path, obs.hashCode() + ".jpg");
				stream = Uri.fromFile(f);
				// TODO Write the bitmap to the File
				type = (TextUtils.isEmpty(type))? "image/jpg": type;
				Log.d(TAG, "....inline-data : " + stream);
			} 
			// Scanner hack
			else if(action != null && action.equals("com.google.zxing.client.android.SCAN")){
				text = result.getStringExtra("SCAN_RESULT");
				type = "text/plain";
				stream = Uri.fromParts("data", type, text);
				Log.d(TAG, "....com.google.zxing.client.android.SCAN : " + stream);
			} else if(extras != null){
				// What we really want for content
				if(extras.containsKey(Intent.EXTRA_STREAM)){
					stream = extras.getParcelable(Intent.EXTRA_STREAM);
					type = cr.getType(stream);
					type = (TextUtils.isEmpty(type))? 
							"application/octet-stream": type;
					Log.d(TAG, "....EXTRA_STREAM : " + stream);
				}else if(extras.containsKey(Intent.EXTRA_TEXT)){
					text = extras.getString(Intent.EXTRA_TEXT);
					type = "text/plain";
					stream = Uri.fromParts("data", type, text);
					Log.d(TAG, "....EXTRA_TEXT : " + text);
				}
			}
		} else if(hasHack(hack)){
			Log.d(TAG, "Attemptig hack");
			type = renderHackType(hack);
			stream = renderHackStream(hack, obs);
		} else {
			// To get here the Activity would have had to return a status
			// RESULT_OK and null data Intent
			throw new NullPointerException("NULL data returned by intent");
		} 
		// set return properly
		rResult.setDataAndType(stream, type);

		Log.d(TAG, "Rendered intent: ");
		logIntent(TAG, rResult);
		return rResult;	
	}

	// Infamous image hack for the Camera app. Put more here if we need it.
	private static final boolean hasHack(String hack){
		return hack.contains("image/");
	}
	
	// More hack
	private static final String renderHackType(String hack){
		return "image/jpg";
	}
	
	// Makes sure the obs file exists 
	private static final Uri renderHackStream(String hack, Uri obs){
		File extDir = Environment.getExternalStorageDirectory();
		File path = new File(extDir, Constants.PATH_OBSERVATION);
		File f = new File(path, obs.hashCode() + ".jpg");
		if(f.exists())
			return Uri.fromFile(f);
		else
			throw new NullPointerException("NULL data. File Not Found");
	}
	
	/**
	 * Renders plug in intents for launching data capture activities. This 
	 * method handles any of the odd behavior or parameters that need to 
	 * be set such as with the stock Android Camera app. For most Activities
	 * this does nothing.
	 * 
	 * @param intent The raw launch intent.
	 * @param obs The observation.
	 * @return
	 */
	public static Intent renderPluginLaunchIntent(Intent intent, Uri obs){
		Intent rIntent = new Intent();
		Log.d(TAG, "Rendering launch Intent....");
		logIntent(TAG, intent);
		String action = intent.getAction();
		String mime = intent.getType();

		if(!TextUtils.isEmpty(action)){
			rIntent.setAction(action);
			if(action.equals(Intent.ACTION_GET_CONTENT))
				rIntent.setType(mime);
		} else {
			rIntent.setAction(Intent.ACTION_GET_CONTENT);
			rIntent.setType(mime);
		}
		
		Log.d(TAG, "..obs: " + obs);
		if(rIntent.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE)){
			Log.d(TAG, "Got an ACTION_IMAGE_CAPTURE" );
			rIntent = new Intent(intent.getAction());
			File extDir = Environment.getExternalStorageDirectory();
			File path = new File(extDir, Constants.PATH_OBSERVATION);
			path.mkdirs();
			File f = new File(path, obs.hashCode() + ".jpg");
			Uri fUri = Uri.fromFile(f);
			Log.d(TAG, "..output: " + fUri);
			rIntent.putExtra(MediaStore.EXTRA_OUTPUT, fUri);
		}
		
		Log.d(TAG, "Rendered launch Intent....");
		logIntent(TAG, rIntent);
		return rIntent;
	}
	
	/**
	 * Log helper for pulling information out of Intents.
	 * 
	 * @param tag The tag that should be associated with the log entry.
	 * @param intent The intent to analyze.
	 */
	public static void logIntent(String tag, Intent intent){
		Log.d(tag, "Intent: "  + intent.toUri(Intent.URI_INTENT_SCHEME));
		Log.d(tag, "..action : " + intent.getAction());
		Log.d(tag, "..data   : " + intent.getData());
		Log.d(tag, "..type   : " + intent.getType());
		Log.d(tag, "..scheme : " + intent.getScheme());
		Bundle extras = intent.getExtras();
		// Purely for debugging
		if(extras != null){
			Log.d(tag, "..extras : ");
			for(String key:extras.keySet())
				Log.d(tag, "...." + key + " : " + extras.get(key));
		} else 
			Log.d(TAG, "..extras : null");
		
	}
	
}
