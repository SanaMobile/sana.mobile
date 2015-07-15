package org.sana.android.procedure;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

import org.sana.android.Constants;
import org.sana.android.app.Preferences;
import org.sana.android.content.core.ObservationWrapper;
import org.sana.android.provider.Observations;
import org.w3c.dom.Node;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

/**
 * TextElement is an answer-less ProcedureElement that represents a block of 
 * text on a procedure page.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use</b>Wherever displaying a block of text may be useful.</li>
 * <li><b>Collects</b></li>Nothing. Displays only.</li>
 * </ul>
 * 
 * @author Sana Dev Team
 */
public class HiddenElement extends ProcedureElement {
	public static final String TAG = HiddenElement.class.getSimpleName();
    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        return encapsulateQuestion(c, null);
    }

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.HIDDEN;
    }

    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        Log.i(TAG, "getAnswer()");
        // set the default value if answer is empty
        if(TextUtils.isEmpty(answer) && hasDefault()){
            Log.d(TAG, "\tUsing default value");
            setAnswer(getDefault());
        }
        return answer;
    }

    public void setAnswer(String answer){
        Log.i(TAG, "setAnswer(String)");
        // set the default value if answer is empty
        if(TextUtils.isEmpty(answer) && hasDefault()){
            Log.w(TAG, "\tUsing default value");
            answer = getDefault();
        }
        this.answer = answer;
    }
    
    @Override
    public boolean isViewActive(){
    	return false;
    }
    
    public Intent getIntent(){

		Intent intent = null;
		try {
			intent = Intent.parseUri(action, Intent.URI_INTENT_SCHEME);
			intent.putExtra(Observations.Contract.ID, id);
			intent.putExtra(Observations.Contract.CONCEPT, concept);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return intent;
    }


    public String getDefault(){
        Log.i(TAG, "getDefault()");
        final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        String val = super.getDefault();
        for(String str:val.split(":")){
            if(str.length() > 1 && str.startsWith("@")){
                    str = str.substring(1);
                    if(str.compareTo("DEVICE") == 0){
                        // Get device id
                        str = Preferences.getString(getContext(),
                                Constants.PREFERENCE_PHONE_NAME);
                    } else if (str.compareTo("NOW") == 0){
                        // formats current date time as 'yyyyMMddHHmmss'
                        str = df.format(new java.util.Date());
                    }
            }
            sb.append(str);
        }
        return sb.toString();
    }

    /** Default constructor */
    private HiddenElement(String id, String question, String answer, 
    		String concept, String figure, String audio) 
    {
        super(id, question, answer, concept, figure, audio);
    }
    
    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static HiddenElement fromXML(String id, String question, String answer,
    	String concept, String figure, String audio, Node node)    
		throws ProcedureParseException 
    {
    	HiddenElement el = new HiddenElement(id, question, answer, concept, figure, audio);
    	ProcedureElement.parseOptionalAttributes(node, el);
    	return el;
    }
}
