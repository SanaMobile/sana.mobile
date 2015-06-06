package org.sana.android.procedure;

import org.sana.R;
import org.w3c.dom.Node;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * ProcedureElement which collects and displays a patient ID.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b> For registering and looking up patients by id.</li>
 * <li><b>Collects </b> A patient identifier string. This class makes no guarantee
 * of the validity with any backing emr.</li>
 * </ul>
 * 
 * @author Sana Development Team
 */
public class PatientIdElement extends ProcedureElement implements 
	OnClickListener 
{
    public static final String TAG = PatientIdElement.class.getSimpleName();
    private EditText et;
    private Button barcodeButton;

    private static final int BARCODE_INTENT_REQUEST_CODE = 2;

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.PATIENT_ID;
    }

    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
    	
        et = new EditText(c);
        et.setPadding(10,5,10,5);
        et.setText(answer);
        et.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        		LayoutParams.WRAP_CONTENT));
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setKeyListener(new DialerKeyListener());
        
    	LinearLayout ll = new LinearLayout(c);
    	ll.setOrientation(LinearLayout.VERTICAL);

    	ll.addView(et, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 
    			LayoutParams.WRAP_CONTENT));
    	ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 
    			LayoutParams.WRAP_CONTENT));
    	
    	//SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
    	boolean barcodeEnable = true; //sp.getBoolean(Constants.PREFERENCE_BARCODE_ENABLED, false);
    	
    	if (barcodeEnable) {
	    	barcodeButton = new Button(c);
	        barcodeButton.setText(c.getResources().getString(
	        		R.string.procedurerunner_scan_id));
	        barcodeButton.setOnClickListener(this);
	        barcodeButton.setGravity(Gravity.CENTER_HORIZONTAL);
	    	ll.addView(barcodeButton, new LinearLayout.LayoutParams(
	    			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    	}
        return encapsulateQuestion(c, ll);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setAnswer(String answer) {
        Log.i(TAG,"["+id+"]setAnswer(String)");
        this.answer = new String(answer);
        if(isViewActive()) {
            et.setText(this.answer);
        }
        Log.d(TAG,"...answer='"+this.answer+"'");
    }
    
    /**
     * Sets the answer and refreshes the view
     * @param answer the new answer
     */
    public void setAndRefreshAnswer(String answer) {
    	this.answer = answer;
    	if (et != null) {
    		et.setText(answer);
    		et.refreshDrawableState();
    	}
    }

    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        Log.i(TAG,"["+id+"]getAnswer()");
        if(isViewActive()) {
            // Need to be certain the answer value is stored
            answer = (et.getText().length() == 0)? "":et.getText().toString();
        }
        Log.d(TAG,"...returning answer='" + answer + "'");
        return answer;
    }

    /** Default constructor */
    private PatientIdElement(String id, String question, String answer, 
    		String concept, String figure, String audio) 
    {
        super(id, question, answer, concept, figure, audio);
    }
    
    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static PatientIdElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, Node n) 
			throws ProcedureParseException 
    {
        return new PatientIdElement(id, question, answer, concept, figure, audio);
    }
    
    /** Launches the barcode reader if available */
    @Override
    public void onClick(View v) {
    	if (v == barcodeButton) {
    		String procedureId = 
    			getProcedure().getInstanceUri().getPathSegments().get(1); 
    		String[] params = {procedureId, id};
    		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
    		try {
    			((Activity)this.getContext()).startActivityForResult(intent, 
    					BARCODE_INTENT_REQUEST_CODE);
    		} catch (Exception e) {
    			Log.e(TAG, "Exception opening barcode reader, probably not "
    					+"installed, " + e.toString());
    			new AlertDialog.Builder(getContext())
    			.setTitle("Error")
    			.setMessage("Barcode reader not installed. install "
    					+"\"ZXing Barcode Scanner\" from the Android Market.")
    			.setPositiveButton("Ok", null)
    			.show();
    		}
    	}
    }
}
