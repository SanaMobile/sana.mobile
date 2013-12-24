package org.sana.android.procedure;

import java.net.URISyntaxException;

import org.sana.android.content.core.ObservationWrapper;
import org.sana.android.provider.Observations;
import org.w3c.dom.Node;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
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
        if(!isViewActive())
            return answer;
        return "";
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
