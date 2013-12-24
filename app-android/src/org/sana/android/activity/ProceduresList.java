
package org.sana.android.activity;

import org.sana.R;
import org.sana.android.app.Locales;
import org.sana.android.content.Intents;
import org.sana.android.provider.Procedures;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/** Displays a list of Procedures.
 * 
 * @author Sana Development Team */
public class ProceduresList extends ListActivity {

    /** Intent extra for a procedure. */
    public static final String EXTRA_PROCEDURE_URI = "uri_procedure";
    
    private static final String TAG = ProceduresList.class.toString();
    private static final String[] PROJECTION = new String[] {
            Procedures.Contract._ID, Procedures.Contract.TITLE,
            Procedures.Contract.AUTHOR
    };
    
    private static final String[] PROJECTION2 = new String[] {
        Procedures.Contract._ID, Procedures.Contract.TITLE,
        Procedures.Contract.VERSION
    };
    
    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();

        if (uri == null) {
            uri = Procedures.CONTENT_URI;
        }
        sync(this,uri);
        Cursor cursor = managedQuery(uri, PROJECTION2, null, null,
                Procedures.DEFAULT_SORT_ORDER);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.procedure_list_row, cursor,
                new String[] {
                        Procedures.Contract.TITLE,
                        Procedures.Contract.VERSION
                        //Procedures.Contract.AUTHOR
                },
                new int[] {
                        R.id.toptext, R.id.bottomtext
                });
    	Locales.updateLocale(this, getString(R.string.force_locale));
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
            intent.putExtra(Intents.EXTRA_PROCEDURE, uri);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
    

    final void sync(Context context, Uri uri){
    	Intent intent = new Intent(context.getString(R.string.intent_action_read),uri);
    	context.startService(intent);
    }
}
