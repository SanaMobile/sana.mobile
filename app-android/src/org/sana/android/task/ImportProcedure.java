package org.sana.android.task;

import java.io.IOException;

import org.sana.R;
import org.sana.android.procedure.ProcedureParseException;
import org.sana.android.util.SanaUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/** 
 * A task for importing Procedure into the database.
 * 
 * @author Sana Development Team
 *
 */
public class ImportProcedure extends AsyncTask<Context, Void, Integer> {
	private static final String TAG = ImportProcedure.class.getSimpleName();
	
	private ProgressDialog progressDialog;
	private Context mContext = null; // TODO context leak?
	private String location = "";
	private String fname = "Undefined";
	
	/**
	 * A new task instance.
	 * @param c The enclosing context.
	 * @param location the Procedure source
	 */
	public ImportProcedure(Context c, String location) {
		this.mContext = c;
		this.location = location;
	}
	
	/** {@inheritDoc} */
	@Override
	protected Integer doInBackground(Context... params) {
		Log.i(TAG, "Executing Import from SD");
		Context c = params[0];
		int result = 2;
		try{
			String[] f = location.split("//");
			fname = f[f.length - 1];
			result = SanaUtil.insertProcedureFromSd(c, location);
		} catch (IOException e){
			// FIle open errors
            err = "Error opening file: " + fname;
        } catch(IllegalArgumentException e) {
            // Catches bad type attribute values
            err = "Bad type attribute value: "
            		+ e.getMessage().split(" ")[0];
		} catch(ProcedureParseException e){
            // Catches other xml errors
			//TODO
            err = "Parse Error: " + e.getMessage();
		} catch(Exception e){
            // Catches other xml errors
			//TODO
            err = e.getMessage();
		}
		return result;
	}
	
	private String err= "";

	/** {@inheritDoc} */
	@Override
	protected void onPreExecute() {
		Log.i(TAG, "About to execute ImportProcedure");
		if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    	progressDialog = new ProgressDialog(mContext);
    	progressDialog.setMessage(mContext.getString(R.string.import_proc) 
    			+ " " + location);
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	progressDialog.show();
	}
	

	/** {@inheritDoc} */
	@Override
	protected void onPostExecute(Integer result) {
		Log.i(TAG, "Completed ImportProcedure");
		if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
		switch (result) {
		case 0:
			Toast.makeText(mContext, "Procedure inserted into Database.",
                   Toast.LENGTH_LONG).show();
			break;
		case 1:
			SanaUtil.errorAlert(mContext, "Duplicate Procedure Found!");
			break;
		case 2:
			SanaUtil.errorAlert(mContext, 
					"Something's wrong with the file!" +
					"\n" + err);
			break;
		}		
	}

}
