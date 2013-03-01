
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.provider.Procedures;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.SherlockListActivity;

/** Displays a list of Procedures.
 * 
 * @author Sana Development Team */
public class ProceduresList extends SherlockListActivity {

    /** Intent extra for a procedure. */
    public static final String EXTRA_PROCEDURE_URI = "uri_procedure";
    
    private static final String TAG = ProceduresList.class.toString();
    private static final String[] PROJECTION = new String[] {
            Procedures.Contract._ID, Procedures.Contract.TITLE,
            Procedures.Contract.AUTHOR
    };

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();

        if (uri == null) {
            uri = Procedures.CONTENT_URI;
        }

        Cursor cursor = managedQuery(uri, PROJECTION, null, null,
                Procedures.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.procedure_list_row, cursor,
                new String[] {
                        Procedures.Contract.TITLE,
                        Procedures.Contract.AUTHOR
                },
                new int[] {
                        R.id.toptext, R.id.bottomtext
                });
        setListAdapter(adapter);
    }

    /** {@inheritDoc} */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) ||
                Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return a note selected by
            // the user. They have clicked on one, so return it now.
            Log.d(TAG, "URI selected: " + uri);
            Intent intent = getIntent();
            intent.setData(uri);
            intent.putExtra(EXTRA_PROCEDURE_URI, uri);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}
