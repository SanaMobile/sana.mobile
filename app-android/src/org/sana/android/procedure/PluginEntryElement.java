package org.sana.android.procedure;

import org.w3c.dom.Node;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Plug-in element for activities which do not nicely return data through the
 * Android IPC API and must have data manually reentered as text.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b>Wherever external applications are used to collect
 * data but will not return it automatically.</li>
 * <li><b>Collects </b> Determined by plug-in activity but must be in a format
 * that is can be entered manually by the user as a string.</li>
 * </ul>
 * 
 * @author Sana Development Team
 */
public class PluginEntryElement extends PluginElement {
	private static final String TAG = PluginEntryElement.class.getSimpleName();

    protected EditText et;
    
    /**
     * Constructs a new PluginEntryElement
     * 
     * @param id The value of the "id" attribute
     * @param question The value of the "question" attribute
     * @param answer The value of the "answer" attribute
     * @param concept The value of the "concept" attribute
     * @param figure The value of the "figure" attribute
     * @param audio The value of the "audio" attribute
     * @param action The value of the "action" attribute
     * @param mimeType The content type which the plug-in will collect. Used to
     * 		construct a launch intent. 
     */
	protected PluginEntryElement(String id, String question, String answer,
			String concept, String figure, String audioPrompt, String action,
			String mimeType) {
		super(id, question, answer, concept, figure, audioPrompt,action,mimeType);
	}
	
	
	
	/** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static PluginEntryElement fromXML(String id, String question, 
    	String answer, String concept, String figure, String audio, Node node)  
		throws ProcedureParseException  
    {
        String action = node.getAttributes().getNamedItem("action")
        				.getNodeValue();
        String pkg = node.getAttributes().getNamedItem("mimeType")
						.getNodeValue();
    	return new PluginEntryElement(id, question, answer, concept, figure, 
    			audio, action,pkg);
    }
    
    /** {@inheritDoc} */
	@Override
	protected View createView(Context c) {
		Log.d(TAG, "");
		// New Layout
    	LinearLayout container = new LinearLayout(c);
        container.setOrientation(LinearLayout.VERTICAL);
        View plug = getContentView(c);
        container.addView(plug, new LinearLayout.LayoutParams(-1,-1,0.1f));
        
        // Add text entry
        et = new EditText(c);
        et.setText(answer);
        et.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        		LayoutParams.WRAP_CONTENT));
        container.addView(et, new LinearLayout.LayoutParams(-1,-1,0.1f));
        
        container.setWeightSum(1.0f);        
        return encapsulateQuestion(c,container);
	}
	
	/** {@inheritDoc} */
	@Override
	public ElementType getType() {
		return ProcedureElement.ElementType.ENTRY_PLUGIN;
	}
	
	/** {@inheritDoc} */
    @Override
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        else if(et.getText().length() == 0)
            return "";
        return et.getText().toString();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setAnswer(String answer) {
    	this.answer = answer;
		Log.d(TAG, "Element: "+id + ", set answer:" + answer);
    	if(isViewActive()) {
    		et.setText(answer);
    	}
    }
}
