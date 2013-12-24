package org.sana.android.procedure;

import java.util.ArrayList;
import java.util.List;

import org.sana.R;
import org.sana.android.util.SanaUtil;
import org.w3c.dom.Node;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

/**
 * RadioElement is a ProcedureElement that can display a question along with 
 * multiple-choice radio box answers. Unlike a MultiSelectElement, only one 
 * answer can be selected at a time.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b> Wherever the CHW needs to be prompted to select
 * one, and only one, value from a predefined set.</li>
 * <li><b>Collects </b>Zero or more string values representing an item in the 
 * list of available answers delimited by TOKEN_DELIMITER.</li>
 * </ul>
 * 
 * @author Sana Development Team
 */
public class RadioElement extends ProcedureElement {
    private List<String> choicelist;
    private String[] choices;
    ArrayList<RadioButton> rblist;

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.RADIO;
    }
    
    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        
        
        ScrollView radioView = new ScrollView(c);
        RadioGroup rg = new RadioGroup(c);
        rg.setOrientation(LinearLayout.VERTICAL);
        choicelist = java.util.Arrays.asList(choices);
        rblist = new ArrayList<RadioButton>();
        
        if(answer == null)
        	answer = "";
        RadioButton checked = null;
        for(Object choice : choicelist) {
            RadioButton rb = new RadioButton(c);
            rb.setText((String)choice);
            rg.addView(rb);
            if(answer.equals(choice)) {
                checked = rb;
            }
            rblist.add(rb);
        }
        if(checked != null)
            checked.setChecked(true);
        radioView.addView(rg, new ViewGroup.LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT));
        
        return encapsulateQuestion(c, radioView);
    }
    

    /** {@inheritDoc} */
    @Override
    public void setAnswer(String answer) {
    	if(!isViewActive()) {
    		this.answer = answer;
    	} else { 
	    	for(RadioButton r : rblist) {
	    		if(r.getText().toString().equals(answer))
	    			r.setChecked(true);
	    		else
	    			r.setChecked(false);
	    	}
    	}
    }

    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        String s = "";
        for (RadioButton r : rblist) {
            if (r.isChecked())
                s += r.getText().toString();
        }
        return s;
    }
    
    /** Appends <code>choices</code> attribute */
    @Override
    protected void appendOptionalAttributes(StringBuilder sb){
        sb.append("\" choices=\"" + TextUtils.join(ProcedureElement.CHOICE_DELIMITER, choices)+ "\"");
    }
    
    /** Default constructor */
    private RadioElement(String id, String question, String answer, 
    		String concept, String figure, String audio, String[] choices) 
    {
        super(id,question,answer, concept, figure, audio);
        this.choices = choices;
    }

    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static RadioElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, 
    		Node node) throws ProcedureParseException  
    {
        String choicesStr = SanaUtil.getNodeAttributeOrDefault(node, 
        		"choices", "");
        return new RadioElement(id, question, answer, concept, figure, audio, 
        		choicesStr.split(ProcedureElement.CHOICE_DELIMITER));
    }
    
    
}
