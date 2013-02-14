package org.sana.android.task;

import org.sana.R;
import org.sana.android.util.SanaUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Clears the application database.
 * 
 * @author Sana Development Team
 *
 */
public class ClearDatabaseTask extends AsyncTask<Context, Void, Integer> {
	private static final String TAG = ClearDatabaseTask.class.getSimpleName();
	
	private ProgressDialog progressDialog;
	private Context mContext = null; // TODO context leak?
	
	/**
	 * Instantiates a new task for clearing the database.
	 * @param c the current Context
	 */
	public ClearDatabaseTask(Context c) {
		this.mContext = c;
	}

	/** {@inheritDoc} */
	@Override
	protected Integer doInBackground(Context... params) {
		Log.i(TAG, "Executing ResetDatabaseTask");
		Context c = params[0];
		try{
			SanaUtil.clearDatabase(c);
		} catch(Exception e){
			SanaUtil.errorAlert(c.getApplicationContext(), 
				c.getApplicationContext().getString(R.string.msg_err_reset_db));
		}
		return 0;
	}


	/** {@inheritDoc} */
	@Override
	protected void onPreExecute() {
		Log.i(TAG, "About to execute ResetDatabaseTask");
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
		if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
	}

}
