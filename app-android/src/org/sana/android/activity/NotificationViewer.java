package org.sana.android.activity;

import org.sana.R;
import org.sana.android.db.SanaDB.NotificationSQLFormat;

import android.app.NotificationManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * NotificationViewer builds the interface for viewing a single received 
 * notification. This displays the diagnosis, pertaining patient, and allows the
 * user to dismiss the notification if desired.
 * 
 * @author Sana Dev Team
 */
public class NotificationViewer extends SherlockActivity implements OnClickListener {
	
	private static String TAG = NotificationViewer.class.getSimpleName();

	private static final String[] PROJECTION = new String[] { 
		NotificationSQLFormat._ID, NotificationSQLFormat.PROCEDURE_ID, 
		NotificationSQLFormat.PATIENT_ID, NotificationSQLFormat.FULL_MESSAGE};
	private Button dismiss, save;
	private Uri notification;
	
	/** {@inheritDoc} */
	@Override
	public void onCreate(Bundle instance) {
        super.onCreate(instance);
        notification = getIntent().getData();
        
        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null,
                null, NotificationSQLFormat.DEFAULT_SORT_ORDER);
        
        cursor.moveToFirst();
        String procedureIdentifier = cursor.getString(cursor.getColumnIndex(
        		NotificationSQLFormat.PROCEDURE_ID));
        String patientId = cursor.getString(cursor.getColumnIndex(
        		NotificationSQLFormat.PATIENT_ID));
        String message = cursor.getString(cursor.getColumnIndex(
        		NotificationSQLFormat.FULL_MESSAGE));
        cursor.close();
        /*
        cursor = managedQuery(SavedProcedureSQLFormat.CONTENT_URI, new String[] {SavedProcedureSQLFormat._ID, SavedProcedureSQLFormat.PROCEDURE_ID, SavedProcedureSQLFormat.PROCEDURE_STATE},
        		SavedProcedureSQLFormat.GUID + " = ?",
        		new String[] { procedureIdentifier }, null);
        
        if(cursor.getCount() == 0) {
        	// doh
        	
        }
        cursor.moveToFirst();
        int spId = cursor.getInt(0);
        int procedureId = cursor.getInt(1);
        String answers = cursor.getString(2);
        cursor.close();
        Uri savedUri = ContentUris.withAppendedId(SavedProcedureSQLFormat.CONTENT_URI, spId);
        
        Uri procedureUri = ContentUris.withAppendedId(Procedures.CONTENT_URI, procedureId);
        Log.i(TAG, "Getting procedure " + procedureUri.toString());
        cursor = getContentResolver().query(procedureUri, new String[] { Procedures.PROCEDURE }, null, null, null);
        cursor.moveToFirst();
        String procedureXml = cursor.getString(0);
        cursor.deactivate();
        
        Map<String, Map<String,String>> questionsAnswers = new HashMap<String, Map<String,String>>();
        try{
	        Procedure p = Procedure.fromXMLString(procedureXml);
	        p.setInstanceUri(savedUri);
	        
	        JSONTokener tokener = new JSONTokener(answers);
	        JSONObject answersDict = new JSONObject(tokener);
	
	        Map<String,String> answersMap = new HashMap<String,String>();
	        Iterator it = answersDict.keys();
	        while(it.hasNext()) {
	        	String key = (String)it.next();
	        	answersMap.put(key, answersDict.getString(key));
	        	Log.i(TAG, "onCreate() : answer '" + key + "' : '" + answersDict.getString(key) +"'");
	        }
	        Log.i(TAG, "onCreate() : restoreAnswers");
	        p.restoreAnswers(answersMap);
	        questionsAnswers = p.toQAMap();
        } catch(ProcedureParseException e) {
        	
        } catch(JSONException e) {
        	
        } catch (IOException e) {

		} catch (ParserConfigurationException e) {

		} catch (SAXException e) {

		}
        */
        LinearLayout notifView = new LinearLayout(this);
        notifView.setOrientation(LinearLayout.VERTICAL);
        TextView tv1 = new TextView(this);
        tv1.setText(getString(R.string.note_pt_diagnosis)+" " + patientId);
        tv1.setTextAppearance(this, android.R.style.TextAppearance_Large);
        tv1.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView tv2 = new TextView(this);
        tv2.setText("");
        tv2.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        TextView tv4 = new TextView(this);
        tv4.setText("");
        tv4.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        TextView tv3 = new TextView(this);
        tv3.setText(message);
        tv3.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        dismiss = new Button(this);
        dismiss.setText(getString(R.string.general_dismiss));
        dismiss.setOnClickListener(this);
        save = new Button(this);
        save.setText(getString(R.string.general_save));
        save.setOnClickListener(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);      
        ll.addView(dismiss,new LinearLayout.LayoutParams(-2,-1, 0.5f));
        ll.addView(save,new LinearLayout.LayoutParams(-2,-1, 0.5f));
        notifView.setWeightSum(1.0f);
        ll.setGravity(Gravity.BOTTOM);
        notifView.addView(tv1);
        notifView.addView(tv2);
        notifView.addView(tv3);
        notifView.addView(tv4);
        notifView.addView(ll);
        this.setContentView(notifView);
	}

	/**
	 * If a user elects to dismiss the notification, the status bar will be 
	 * cleared of ALL notification alerts. This employs a "only the latest 
	 * notification in status bar" policy.
	 */
	@Override
	public void onClick(View v) {
		if (v == dismiss) {
			this.getContentResolver().delete(notification, null, null);
			((NotificationManager)this.getSystemService(NOTIFICATION_SERVICE))
				.cancelAll();
			this.finish();
		} else if (v == save) {
			((NotificationManager)this.getSystemService(NOTIFICATION_SERVICE))
				.cancelAll();
			this.finish();
		}
	}
}
