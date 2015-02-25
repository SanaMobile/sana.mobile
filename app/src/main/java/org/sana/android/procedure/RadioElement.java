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
public class RadioElement extends SelectionElement {
    protected static final String TAG = RadioElement.class.getSimpleName();

    ArrayList<RadioButton> rblist;
    RadioGroup mRadioGroup;

    /** {@inheritDoc} */
    @Override
    public ElementType getType() {
        return ElementType.RADIO;
    }
    
    /** {@inheritDoc} */
    @Override
    protected View createView(Context c) {
        Log.i(TAG, "[" + id + "]createView()");
        ScrollView radioView = new ScrollView(c);
        mRadioGroup = new RadioGroup(c);
        mRadioGroup.setOrientation(LinearLayout.VERTICAL);
        rblist = new ArrayList<RadioButton>(values.length);

        if(answer == null)
            answer = "";
        for(String value : values) {
            Log.d(TAG, "..." + value +":" + getLabelFromValue(value));
            RadioButton rb = new RadioButton(c);
            rb.setText(getLabelFromValue(value));
            rb.setTag(value);
            if(value.equals(answer)) {
                rb.setChecked(true);
            }
            rblist.add(rb);
            mRadioGroup.addView(rb);
        }
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setAnswer(String.valueOf(group.findViewById(checkedId).getTag
                        ()));
            }
        });
        radioView.addView(mRadioGroup, new ViewGroup.LayoutParams(-1,-1));
        return encapsulateQuestion(c, radioView);
    }

    @Override
    public void setAnswer(String answer) {
        Log.i(TAG, "[" + id + "]setAnswer() --> " + answer);
        this.answer = answer;

        if(isViewActive()) {
            for(RadioButton r : rblist) {
                if(TextUtils.isEmpty(answer))
                    continue;
                r.setChecked((String.valueOf(r.getTag()).equals(this.answer))?
                    true:false);
            }
        }

    }

    @Override
    public String getAnswer() {
        Log.i(TAG, "[" + id + "]getAnswer()");
        /*
        boolean active = isViewActive();
        // If visible get pressed item
        if (active) {
            Log.d(TAG, "...checked id " + mRadioGroup.getCheckedRadioButtonId
                    ());
            String value = "";
            for (RadioButton r : rblist) {
                value = (r.isChecked()) ? String.valueOf(r.getTag()) : value;
            }
            answer = value;
        }
        */
        return answer;
    }
    
    /** Default constructor */
    private RadioElement(String id, String question, String answer, 
    		String concept, String figure, String audio, String[] choices) 
    {
        super(id,question,answer, concept, figure, audio, choices);
    }

    private RadioElement(String id, String question, String answer,
                         String concept, String figure, String audio,
                         String[] choices, String[] values)
    {
        super(id,question,answer, concept, figure, audio, choices, values);
    }

    /** @see ProcedureElement#fromXML(String, String, String, String, String, String, Node) */
    public static RadioElement fromXML(String id, String question, 
    		String answer, String concept, String figure, String audio, 
    		Node node) throws ProcedureParseException  
    {
        String choicesStr = SanaUtil.getNodeAttributeOrDefault(node, 
        		"choices", "");
        String valuesStr = SanaUtil.getNodeAttributeOrDefault(node, "values",
                choicesStr);
        return new RadioElement(id, question, answer, concept, figure, audio, 
        		choicesStr.split(SelectionElement.TOKEN_DELIMITER),
                valuesStr.split(SelectionElement.TOKEN_DELIMITER));
    }
    
    
}
