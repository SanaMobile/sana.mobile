package org.sana.android.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.sana.R;
import org.sana.android.Constants;
import org.sana.android.task.ClearDatabaseTask;
import org.sana.android.task.ImportProcedure;
import org.sana.android.task.ImportProcedureAll;
import org.sana.android.task.ResetDatabaseTask;
import org.sana.android.util.EnvironmentUtil;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Activity for importing Procedures from the sdcard.
 * 
 * @author Sana Development Team
 *
 */
public class ProcedureSdImporter extends ListActivity {

    public static final String TAG = ProcedureSdImporter.class.getSimpleName();

	// Option menu codes
	private static final int OPTION_LOAD_ALL = 0;
	private static final int OPTION_RESET_DATABASE = 1;
	private static final int OPTION_RELOAD_DATABASE = 2;

	
	private List<String> procedures = new ArrayList<String>();

	/** {@inheritDoc} */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sd_import_list);
		updateProcedureList();
	}

	/**
	 * 	Refreshes the available procedures on the sdcard
	 */
	public void updateProcedureList() {
		File home = new File(EnvironmentUtil.getProcedureDirectory());
		if(!home.exists()){
			home.mkdirs();
		}
		File[] fileList = home.listFiles( new XmlFilter() );
		if (fileList != null && fileList.length > 0) {
			for (File file : fileList) {
				procedures.add(file.getName());
			}
			ArrayAdapter<String> procedureList = new ArrayAdapter<String>(
					this,R.layout.sd_item,procedures);
			setListAdapter(procedureList);
		}    	
	}

	// inserts a procedure into the database
	private void doInsertProcedure(String location) {
		// TODO: context leak
		new ImportProcedure(this, location).execute(this);
	}

	/**
	 * Clicking on an item will attempt to insert it into the database
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		try {
			String mount = Environment.getExternalStorageState();
			if(!mount.equals(Environment.MEDIA_MOUNTED))
				return;
			doInsertProcedure(EnvironmentUtil.getProcedureDirectory()+
                    procedures.get(position));
		} catch(Exception e) {
            e.printStackTrace();
			Log.e(TAG, e.getMessage());
		} 
	}

	/**
	 * Provides options to load all available, delete the procedure database,
	 * or reload the default procedures hard coded into the application.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_LOAD_ALL, 0, getString(R.string.import_load_all));
		menu.add(0, OPTION_RESET_DATABASE, 1, 
				getString(R.string.import_proc_delete_db));
		menu.add(0, OPTION_RELOAD_DATABASE, 2, 
				getString(R.string.import_proc_reload));
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		AlertDialog.Builder bldr = new AlertDialog.Builder(this);
		AlertDialog dialog = bldr.create();
		switch (item.getItemId()) {
		case OPTION_LOAD_ALL:
			// TODO: Dialog leak

			dialog.setMessage(getString(R.string.import_proc_warn_load_all));

			dialog.setCancelable(true);
			dialog.setButton(this.getString(R.string.general_yes), 
					new OnClickListener() {
				public void onClick(DialogInterface i, int v) {
					doLoadAllProcedures(procedures);
				}
			});
			dialog.setButton2(this.getString(R.string.general_no), 
					(OnClickListener)null);
			dialog.show();
			return true;
		case OPTION_RESET_DATABASE:
			// TODO: Dialog leak
			dialog.setMessage(getString(R.string.import_proc_warn_delete_all));
			dialog.setCancelable(true);
			dialog.setButton(getString(R.string.general_ok), 
				new OnClickListener() {
				public void onClick(DialogInterface i, int v) {
					doClearDatabase();
				}
			});
			dialog.setButton2(getString(R.string.general_no), 
					(OnClickListener)null);
			dialog.show();
			return true;
		case OPTION_RELOAD_DATABASE:
			// TODO: Dialog leak
			dialog.setMessage(getString(R.string.import_proc_warn_reload_all));
			dialog.setCancelable(true);
			dialog.setButton(getString(R.string.general_ok), new OnClickListener() {
				public void onClick(DialogInterface i, int v) {
					doResetDatabase();
				}
			});
			dialog.setButton2(getString(R.string.general_no), (OnClickListener)null);
			dialog.show();
			return true;
		}    
		return false;
	}

	/** loads all procedures on the from a list */
	private void doLoadAllProcedures(List<String> procedures) {
		// TODO: context leak
		new ImportProcedureAll(this, procedures).execute(this);
	}
	
	/** Clears and resets the Procedure database */
	private void doResetDatabase() {
		// TODO: context leak
		new ResetDatabaseTask(this).execute(this);
	}

	/** Clears but does not reset the database */
	private void doClearDatabase() {
		// TODO: context leak
		new ClearDatabaseTask(this).execute(this);
	}
	
	/** filters files in a directory based on the suffix xml */
	public static class XmlFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".xml"));
		}
	}
}

