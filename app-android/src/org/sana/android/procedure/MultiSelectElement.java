package org.sana.android.procedure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.sana.android.util.SanaUtil;
import org.w3c.dom.Node;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * MultiSelectElement is a ProcedureElement that creates a procedure page with
 * multiple check-box items that can be selected.
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b> Defined by subclasses.</li>
 * <li><b>Collects </b>Zero or more string values representing items in the 
 * list of available answers delimited by TOKEN_DELIMITER.</li>
 * </ul>
 * 
 * @author Sana Development Team
 */
public class MultiSelectElement extends ProcedureElement {
    private List<String> choicelist;
    private String[] choices;
    private ArrayList<CheckBox> cblist;
    public static final String TOKEN_DELIMITER = ",";
    
    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.MULTI_SELECT;
    }

    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        ScrollView sv = new ScrollView(c);
        LinearLayout ll = new LinearLayout(c);
        HashSet<String> selectedSet = new HashSet<String>();

        if(answer == null)
        	answer = "";
        // we've got a problem if there are TOKEN_DELIMITERs in the value!
        // since getAnswer separates responses using TOKEN_DELIMITER
        String[] values = answer.split(TOKEN_DELIMITER);
        for(String val : values) {
            selectedSet.add(val);
        }
        
        ll.setOrientation(LinearLayout.VERTICAL);
        choicelist = java.util.Arrays.asList(choices);
        cblist = new ArrayList<CheckBox>();
        for(Object choice : choicelist) {
            CheckBox cb = new CheckBox(c);
            cb.setText((String)choice);
            cb.setChecked(selectedSet.contains(choice));
            cblist.add(cb);
            ll.addView(cb);
        }
        sv.addView(ll, new ViewGroup.LayoutParams(-1,-1));
        return encapsulateQuestion(c, sv);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setAnswer(String answer) {
    	this.answer = answer;
    	    		
    	if(isViewActive()) {
    		String[] answers = answer.split(TOKEN_DELIMITER);
    		HashSet<String> answerSet = new HashSet<String>();
    		for(String a : answers) {
    			answerSet.add(a);
    			Log.i(TAG, "SetAnswer a:" + a + ":");
    		}
    		for (CheckBox c : cblist) {
    			Log.i(TAG, "SetAnswer - :" + c.getText().toString() + ":");
    			if(answerSet.contains(c.getText().toString())) {
    				c.setChecked(true);
    			} else{
    				c.setChecked(false);
    			}
    		}
    	}
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        String s = "";
        boolean any = false;
        for (CheckBox c : cblist) {
            if (c.isChecked()) {
                s += c.getText().toString() + TOKEN_DELIMITER;
                any = true;
            }
        }
        if(any)
            s = s.substring(0, s.length()-1);
        return s;
    }
    
    /** {@inheritDoc} */
    @Override
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" question=\"" + question);
        sb.append("\" choices=\"" + TextUtils.join(TOKEN_DELIMITER, choices));
        sb.append("\" answer=\"" + getAnswer());
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }

    /** Default constructor */
    private MultiSelectElement(String id, String question, String answer, 
    		String concept, String figure, String audio, String[] choices) 
    {
        super(id, question, answer, concept, figure, audio);
        this.choices = choices;
    }
    
    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static MultiSelectElement fromXML(String id, String question, 
    	String answer, String concept, String figure, String audio, Node node) 
		throws ProcedureParseException  
    {
        String choicesStr = SanaUtil.getNodeAttributeOrDefault(node, "choices",
        		"");
        return new MultiSelectElement(id, question, answer, concept, figure, 
        		audio, choicesStr.split(","));
    }
}
