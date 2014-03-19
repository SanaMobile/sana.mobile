package org.sana.android.task;

import org.sana.android.util.SanaUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Task for resetting the application database.
 * 
 * @author Sana Development Team
 *
 */
public class ResetDatabaseTask extends AsyncTask<Context, Void, Integer> {
	private static final String TAG = ResetDatabaseTask.class.getSimpleName();
	
	private ProgressDialog progressDialog;
	private Context mContext = null; // TODO context leak?
	private boolean quiet = false;
	private Uri[] uris = new Uri[0];
	/**
	 * A new task for resetting the database.
	 * @param c the current Context.
	 */
	public ResetDatabaseTask(Context c) {
		this.mContext = c;
	}

	/**
	 * A new task for resetting the database.
	 * @param c the current Context.
	 */
	public ResetDatabaseTask(Context c, boolean quiet) {
		this.mContext = c;
		this.quiet = quiet;
		
	}
	
	/**
	 * A new task for resetting the database.
	 * @param c the current Context.
	 */
	public ResetDatabaseTask(Context c, boolean quiet, Uri[] uris) {
		this.mContext = c;
		this.quiet = quiet;
		this.uris = new Uri[uris.length];
		int index = 0;
		for(Uri uri:uris){
			this.uris[index] = uri; index++;
		}
		
	}
	
	/** {@inheritDoc} */
	@Override
	protected Integer doInBackground(Context... params) {
		Log.i(TAG, "Executing ResetDatabaseTask");
		Context c = params[0];
		try{
			SanaUtil.clearDatabase(c);
			for(Uri uri:uris){
				Log.d(TAG, "Clearing: " + uri);
				c.getContentResolver().delete(uri, null, null);
			}
			SanaUtil.loadDefaultDatabase(c);	
		} catch(Exception e){
			Log.e(TAG, "Could not sync. " + e.toString());
		}	
		return 0;
	}

	/** {@inheritDoc} */	
	@Override
	protected void onPreExecute() {
		Log.i(TAG, "About to execute ResetDatabaseTask");
		if(quiet) return;
		if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    	progressDialog = new ProgressDialog(mContext);
    	progressDialog.setMessage("Clearing Database"); // TODO i18n
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	progressDialog.show();
	}

	/** {@inheritDoc} */
	@Override
	protected void onPostExecute(Integer result) {
		Log.i(TAG, "Completed ResetDatabaseTask");
		if(quiet) return;
		if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
	}

}
