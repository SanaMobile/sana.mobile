package org.sana.android.procedure;


import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.widget.LinearLayout;import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.sana.R;
import org.w3c.dom.Node;

/**
 * Displays a single checkbox which returns "true" when checked as the answer.
 */
public class TruthElement extends ProcedureElement{
    public static final String TAG = TruthElement.class.getSimpleName();

    boolean value = false;
    CheckBox mCheckbox;

    /**
     * Constructs a new Instance.
     *
     * @param id          The unique identifier of this element within its procedure.
     * @param question    The text that will be displayed to the user as a question
     * @param answer      The result of data capture.
     * @param concept     A required categorization of the type of data captured.
     * @param figure      An optional figure to display to the user.
     * @param audioPrompt An optional audio prompt to play for the user.
     */
    protected TruthElement(String id, String question, String answer, String concept, String figure, String audioPrompt) {
        super(id, question, answer, concept, figure, audioPrompt);
    }

    @Override
    protected View createView(Context c) {

        LayoutInflater inflater = ((Activity)c).getLayoutInflater();
        LinearLayout v = (LinearLayout)inflater.inflate(R.layout.widget_element_truth, null);
        mCheckbox = (CheckBox)v.findViewById(R.id.question);
        //int id = Resources.getSystem().getIdentifier("btn_check_holo_dark", "drawable", "android");
        //mCheckbox.setButtonDrawable(id);
        //mCheckbox = new CheckBox(c);
        mCheckbox.setText(question);
        mCheckbox.setChecked(value);
        /*
        mCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Log.d(TAG, "...click checkbox checked = " + cb.isChecked());
                //cb.setChecked(!TruthElement.this.getValue());
                TruthElement.this.setValue(cb.isChecked());
            }
        });
        */
        /*
        mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "...checkbox checked = " + isChecked);
                TruthElement.this.setValue(isChecked);
            }
        });
        */
        //return mCheckbox;
        return v;
    }

    @Override
    public ElementType getType() {
        return ElementType.TRUTH;
    }

    @Override
    public String getAnswer(){
        if(isViewActive()){
            value = mCheckbox.isChecked();
            answer = String.valueOf(value);
        }
        return answer;
    }


    @Override
    public void setAnswer(String answer){
        value = (TextUtils.isEmpty(answer))?
                Boolean.valueOf(answer): Boolean.valueOf(defaultValue);
        if(isViewActive()){
            mCheckbox.setChecked(value);
        }
    }

    @Override
    public String getDefault(){
        return String.valueOf(defaultValue);
    }


    @Override
    public void setDefault(String defaultValue){
        value = Boolean.valueOf(defaultValue);
        if(isViewActive()){
            mCheckbox.setChecked(value);
        }
    }

    private final boolean getValue(){
        return value;
    }
    private final void setValue(boolean value){
        this.value = value;
        answer = String.valueOf(value);
    }

    public static TruthElement fromXML(String id, String question,
                                       String answer, String concept, String figure, String audio, Node node)
            throws ProcedureParseException
    {
        TruthElement el = new TruthElement(id,question,answer,concept,figure,audio);
        return el;
    }
}
