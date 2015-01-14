package org.sana.android.procedure;

import org.w3c.dom.Node;

import android.content.Context;
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
public class TextElement extends ProcedureElement {
	
    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        return encapsulateQuestion(c, null);
    }

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.TEXT;
    }

    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        return "";
    }
    
    /** Default constructor */
    private TextElement(String id, String question, String answer, 
    		String concept, String figure, String audio) 
    {
        super(id, question, answer, concept, figure, audio);
    }
    
    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static TextElement fromXML(String id, String question, String answer,
    	String concept, String figure, String audio, Node node)    
		throws ProcedureParseException 
    {
    	TextElement el = new TextElement(id, question, answer, concept, figure, audio);
    	ProcedureElement.parseOptionalAttributes(node, el);
    	return el;
    }
}
