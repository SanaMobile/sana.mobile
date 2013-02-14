package org.sana.android.activity;

import org.sana.android.db.SanaDB.NotificationSQLFormat;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * NotificationList is the activity that allows a user to browse all of the 
 * current notifications (generally physician-issued diagnoses) that have yet to
 * be dismissed. It is a simple list view that displays notifications by Patient
 * ID number. Clicking on a notification launches the NotificationViewer.
 * 
 * @author Sana Dev Team
 */
public class NotificationList extends ListActivity implements 
	SimpleCursorAdapter.ViewBinder 
{
	private static final String TAG = NotificationList.class.getSimpleName();
	private static int MESSAGE_LENGTH_LIMIT = 35;
	private static final String[] PROJECTION = new String[] {
			NotificationSQLFormat._ID, NotificationSQLFormat.PATIENT_ID,
			NotificationSQLFormat.FULL_MESSAGE };
	
	/** 
	 * Binds the cursor to either the patient or message column.
	 */
	public boolean setViewValue(View v, Cursor cur, int columnIndex) {
		((TextView)v).setText(cur.getString(columnIndex));
		switch(columnIndex) {
		case 1:
			String patientId = cur.getString(columnIndex);
			((TextView)v).setText(patientId);
			break;
		case 2:
			// Limit the message to a fixed length.
			String message = cur.getString(columnIndex);
			if(message.length() > MESSAGE_LENGTH_LIMIT) {
				message = message.substring(0, MESSAGE_LENGTH_LIMIT) + "...";
			}
			((TextView)v).setText(message);
			break;
		
		}
		return true;
	}
	
	/** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri == null) {
            uri = NotificationSQLFormat.CONTENT_URI;
        }
        
        Cursor cursor = managedQuery(uri, PROJECTION, 
        		NotificationSQLFormat.DOWNLOADED + "=1", null, 
        		NotificationSQLFormat.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.two_line_list_item, cursor,
                new String[] { NotificationSQLFormat.PATIENT_ID, 
        					   NotificationSQLFormat.FULL_MESSAGE },
                new int[] { android.R.id.text1, android.R.id.text2 });
        adapter.setViewBinder(this);
        setListAdapter(adapter);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || 
        		Intent.ACTION_GET_CONTENT.equals(action)) 
        {
            setResult(RESULT_OK, new Intent().setData(uri));
            finish();
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}
