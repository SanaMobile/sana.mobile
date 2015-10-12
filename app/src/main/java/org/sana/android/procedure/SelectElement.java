package org.sana.android.procedure;

import java.util.ArrayList;
import java.util.List;

import org.sana.R;
import org.sana.android.util.SanaUtil;
import org.w3c.dom.Node;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;

/**
 * SelectElement is a ProcedureElement that creates a question and roller-type 
 * selection drop box so that a user can answer the question. 
 * <p/>
 * <ul type="none">
 * <li><b>Clinical Use </b>This element type is well suited for questions that 
 * may have one response out of many possible responses.</li>
 * <li><b>Collects </b>A single string representing one of the available 
 * choices.</li>
 * </ul>
 * 
 * @author Sana Development Team
 */
public class SelectElement extends SelectionElement {
    private Spinner spin;
    private ArrayAdapter<String> adapter;

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.SELECT;
    }

    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {

        View v = ((Activity)c).getLayoutInflater().inflate(R.layout.widget_element_select, null);
        spin = (Spinner)v.findViewById(R.id.answer);
        //new Spinner(c);
        adapter = new ArrayAdapter<String>(c,
                R.layout.simple_spinner_item,
                labels);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        int selected =  adapter.getPosition(getLabelFromValue(answer));
        if(selected != -1)
            spin.setSelection(selected);
        return encapsulateQuestion(c, v);
    }

    @Override
    public void setAnswer(String answer) {
        Log.i(TAG,"["  + id +"]setAnswer() --> " + answer);
        this.answer = answer;
        if(isViewActive()) {
            int index = adapter.getPosition(getLabelFromValue(answer));
            if(index > -1)
                spin.setSelection(index);
            spin.refreshDrawableState();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        Log.i(TAG, "[" + id + "]getAnswer()");
        if(!isViewActive())
            return answer;
        return this.getValueFromLabel(adapter.getItem(spin
                .getSelectedItemPosition()));
    }

    /** Default constructor */
    protected SelectElement(String id, String question, String answer,
                            String concept, String figure, String audio, String[] choices)
    {
        super(id,question,answer, concept, figure, audio, choices);
    }

    protected SelectElement(String id, String question, String answer,
                            String concept, String figure, String audio,
                            String[] choices, String[] values)
    {
        super(id, question, answer, concept, figure, audio, choices, values);
    }

    
    /** @see SelectionElement#fromXML(String, String, String, String, String,
     * String, Node) */
    public static SelectElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, 
    		Node node) throws ProcedureParseException  
    {
        String choicesStr = SanaUtil.getNodeAttributeOrDefault(node, "choices",
                "");
        String valuesStr = SanaUtil.getNodeAttributeOrDefault(node, "values",
                choicesStr);
        return new SelectElement(id, question, answer, concept, figure, audio, 
        		choicesStr.split(SelectionElement.TOKEN_DELIMITER),
                valuesStr.split(SelectionElement.TOKEN_DELIMITER)
        );
    }
}
