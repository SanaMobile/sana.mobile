package org.sana.android.procedure;

import org.sana.R;
import org.sana.android.util.SanaUtil;
import org.w3c.dom.Node;

import android.content.Context;
import android.text.InputType;
import android.text.method.DialerKeyListener;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

/**
 * TextEntryElement is a ProcedureElement that contains a question and a text 
 * box for user response. Numeric inputs can be formatted by specifying the 
 * 'numeric' attribute in the xml.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b>Defined by subclasses.</li>
 * <li><b>Collects </b>A character sequence.</li>
 * </ul>
 * 
 * @author Sana Development Team
 */
public class TextEntryElement extends ProcedureElement {
    public static final String TAG = TextEntryElement.class.getSimpleName();
    protected EditText et;
    protected NumericType numericType = NumericType.NONE;
    /**
     * Available numeric types for entry. It is used to determine acceptable
     * values and an appropriate key listener.
     * 
     * @author Sana Development Team
     */
    public enum NumericType {
    	/** Any character */
    	NONE,
    	/** Any number that can be entered through the keypad */
    	DIALPAD,
    	/** Any integer. */
    	INTEGER,
    	/** Any signed integer. */
    	SIGNED,
    	/** Any signed decimal value. */
    	DECIMAL
    }

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.ENTRY;
    }
    
    /** gets the key listener by type */
    protected static KeyListener getKeyListenerForType(NumericType type) {
    	switch (type) {
    	case DIALPAD:
    		return new DialerKeyListener();
    	case INTEGER:
    		return new DigitsKeyListener();
    	case SIGNED:
    		return new DigitsKeyListener(true, false);
    	case DECIMAL:
    		return new DigitsKeyListener(true, true);
    	case NONE:
    	default:
    		return null;
    	}
    }
   
    public static final int TYPE_TEXT_FLAG_NO_SUGGESTIONS = 0x00080000;

    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        et = new EditText(c);
        et.setBackgroundResource(R.drawable.oval);
        et.setTextColor(c.getResources()
                .getColorStateList(R.color.primary_text_holo_light));
        et.setText(answer);
        et.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        		LayoutParams.WRAP_CONTENT));
        if (!NumericType.NONE.equals(numericType)) {
        	KeyListener listener = getKeyListenerForType(numericType);
        	if (listener != null)
        		et.setKeyListener(listener);
        } else {
        	et.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | 
        			TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
        return encapsulateQuestion(c, et);
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
    protected TextEntryElement(String id, String question, String answer,
    		String concept, String figure, String audio, 
    		NumericType numericType) {
        super(id, question, answer, concept, figure, audio);
        this.numericType = numericType;
    }
    
    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static TextEntryElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, Node n) 
    		throws ProcedureParseException 
    {
    	String numericStr = SanaUtil.getNodeAttributeOrDefault(n, "numeric", 
    			"NONE");
    	NumericType numericType = NumericType.NONE;
    	try {
    		numericType = NumericType.valueOf(numericStr);
    	} catch (Exception e) {
    		Log.e(TAG, "Could not parse numeric type: " + e.toString());
    		e.printStackTrace();
    	}
        return new TextEntryElement(id, question, answer, concept, figure, 
        		audio, numericType);
    }
}
