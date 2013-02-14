package org.sana.android.procedure;

import java.util.ArrayList;
import java.util.List;

import org.sana.android.util.SanaUtil;
import org.w3c.dom.Node;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
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
public class SelectElement extends ProcedureElement {
    private Spinner spin;
    private List<String> choicelist;
    private String[] choices;
    private ArrayAdapter<String> adapter;

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.SELECT;
    }

    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        spin = new Spinner(c);
    
        if(choices == null) 
            choicelist = new ArrayList<String>();
        else
            choicelist = java.util.Arrays.asList(choices);

        adapter = new ArrayAdapter<String>(c,
                android.R.layout.simple_spinner_item,
                choicelist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        int selected =  choicelist.indexOf(answer);
        if(selected != -1)
            spin.setSelection(selected);
        return encapsulateQuestion(c, spin);
    }
    

    /** {@inheritDoc} */
    @Override
    public void setAnswer(String answer) {
    	if(!isViewActive()) {
    		this.answer = answer;
    	} else {
    		this.answer = answer;
    		// TODO: Fix this so that the adapter has the correct selected item.
    		int index = choicelist.indexOf(answer);
    		spin.setSelection(index);
    		spin.refreshDrawableState();
    	}
    }

    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        if(!isViewActive())
            return answer;
        return adapter.getItem(spin.getSelectedItemPosition()).toString(); 
    }

    /** {@inheritDoc} */
    @Override
    public void buildXML(StringBuilder sb) {
        sb.append("<Element type=\"" + getType().name() + "\" id=\"" + id);
        sb.append("\" question=\"" + question);
        sb.append("\" choices=\"" + TextUtils.join(",", choices));
        sb.append("\" answer=\"" + getAnswer()); 
        sb.append("\" concept=\"" + getConcept());
        sb.append("\"/>\n");
    }

    /** Default constructor */
    private SelectElement(String id, String question, String answer, 
    		String concept, String figure, String audio, String[] choices) 
    {
        super(id,question,answer, concept, figure, audio);
        this.choices = choices;
    }
    
    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static SelectElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, 
    		Node node) throws ProcedureParseException  
    {
        String choicesStr = SanaUtil.getNodeAttributeOrDefault(node, "choices", 
        		"");
        return new SelectElement(id, question, answer, concept, figure, audio, 
        		choicesStr.split(","));
    }

}
