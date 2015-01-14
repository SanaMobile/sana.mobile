package org.sana.android.task;

import org.sana.android.net.MDSInterface;
import org.sana.android.util.SanaUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * A Task for validating authorization.
 * 
 * @author Sana Development Team
 *
 */
public class CheckCredentialsTask extends AsyncTask<Context, Void, Integer> {
	public static final String TAG = CheckCredentialsTask.class.getSimpleName();
	
	/** Indicates a connection could not be established to validate. */
	public static final int CREDENTIALS_NO_CONNECTION = 0;
	
	/** 
	 * Indicates a connection was established but that the credentials were
	 * not valid.
	 */
	public static final int CREDENTIALS_INVALID = 1;
	
	/** 
	 * Indicates a connection was established but and the credentials were 
	 * valid.
	 */
	public static final int CREDENTIALS_VALID = 2;
	
	private ValidationListener validationListener = null;
	
	/**
	 * Sets the current listener
	 * @param listener the new ValidationListener
	 */
	public void setValidationListener(ValidationListener listener) {
		this.validationListener = listener;
	}

	/** {@inheritDoc} */
	@Override
	protected Integer doInBackground(Context... params) {
		Log.i(TAG, "Executing CheckCredentialsTask");
		Context c = params[0];
		Integer result = CREDENTIALS_NO_CONNECTION;
		
		if (SanaUtil.checkConnection(c)) {
			try {
				boolean credentialsValid = MDSInterface.validateCredentials(c);
				result = credentialsValid ? 
						CREDENTIALS_VALID : CREDENTIALS_INVALID;
			} catch (Exception e) {
				Log.e(TAG, "Got exception while validating credentials: " + e);
				e.printStackTrace();
				result = CREDENTIALS_NO_CONNECTION;
			}
		}
		return result;
	}
	
	/** {@inheritDoc} */
	@Override
	protected void onPostExecute(Integer result) {
		Log.i(TAG, "Completed CheckCredentialsTask");
		if (validationListener != null) {
			validationListener.onValidationComplete(result);
			// Free the reference to help prevent leaks.
			validationListener = null;
		}
    }
}
