package org.sana.android.procedure;

import java.util.ArrayList;
import java.util.Arrays;
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
public class MultiSelectElement extends SelectionElement {
    private String[] choices;
    private ArrayList<CheckBox> cblist;
    
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
        cblist = new ArrayList<CheckBox>();
        for(String choice : labels()) {
            CheckBox cb = new CheckBox(c);
            cb.setTag(getValueFromLabel(choice));
            cb.setText(choice);
            cb.setChecked(selectedSet.contains(String.valueOf(cb.getTag())));
            cblist.add(cb);
            ll.addView(cb);
        }
        sv.addView(ll, new ViewGroup.LayoutParams(-1,-1));
        return encapsulateQuestion(c, sv);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setAnswer(String answer) {
        Log.i(TAG,"["  + id +"]setAnswer() --> " + answer);
        this.answer = (answer == null)?"":answer;
        // Update UI if visible
        if(isViewActive()) {
            for (CheckBox c : cblist) {
                String label = c.getText().toString();
                String value = String.valueOf(c.getTag());
                if(answer.contains(value)) {
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
        Log.i(TAG,"["  + id +"]getAnswer()");
        String val = "";
        if(!isViewActive())
            val = (answer == null)?"":answer;
        else {
            boolean any = false;
            // loop over list and add to answer if checked
            for (CheckBox c : cblist) {
                if (c.isChecked()) {
                    val += c.getTag() + TOKEN_DELIMITER;
                    any = true;
                }
            }
            //Remove trailing
            if(any)
                val = val.substring(0, val.length()-1);
        }
        Log.d(TAG, "...returning " + val);
        return val;
    }

    /** Default constructor */
    private MultiSelectElement(String id, String question, String answer, 
    		String concept, String figure, String audio, String[] labels)
    {
        super(id, question, answer, concept, figure, audio, labels);
    }

    private MultiSelectElement(String id, String question, String answer,
                               String concept, String figure, String audio,
                               String[] labels, String[] values)
    {
        super(id, question, answer, concept, figure, audio, labels,values);
    }

    /** @see SelectionElement#fromXML(String, String, String, String, String,
     * String,
     *  Node) */
    public static MultiSelectElement fromXML(String id, String question, 
    	String answer, String concept, String figure, String audio, Node node) 
		throws ProcedureParseException  
    {
        String choicesStr = SanaUtil.getNodeAttributeOrDefault(node, "choices",
        		"");
        String valuesStr = SanaUtil.getNodeAttributeOrDefault(node, "values",
                choicesStr);
        return new MultiSelectElement(id, question, answer, concept, figure, 
        		audio, choicesStr.split(SelectionElement.TOKEN_DELIMITER),
                valuesStr.split(SelectionElement.TOKEN_DELIMITER));
    }
}
