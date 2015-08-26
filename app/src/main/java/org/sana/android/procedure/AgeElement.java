package org.sana.android.procedure;

import android.content.Context;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.sana.util.DateUtil;
import org.sana.util.Functions;
import org.w3c.dom.Node;

import java.text.ParseException;
import java.util.Date;

/**
 * Element which accepts an integer as input and calculates an estimated date. This
 * is useful for entering approximate dates of birth when precise value is not
 * known.
 */
public class AgeElement  extends TextEntryElement {
    public static final String TAG = AgeElement.class.getSimpleName();
    Date dateAnswer = new Date();
    long age = 0;
    /**
     * Default constructor
     *
     * @param id
     * @param question
     * @param answer
     * @param concept
     * @param figure
     * @param audio
     */
    protected AgeElement(String id, String question, String answer, String concept, String figure,
                         String audio) {
        super(id, question, answer, concept, figure, audio, NumericType.INTEGER);
        setAnswer(answer);
    }

    @Override
    public ElementType getType() {
        return ElementType.AGE;
    }

    @Override
    public void setAnswer(String answer) {
        Log.i(TAG,"["+id+"]setAnswer(String)");
        this.answer = new String(answer);
        if(isViewActive()) {
            try {
                dateAnswer = DateUtil.parseDate(answer);
                age = Functions.age(dateAnswer);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            et.setText(String.valueOf(age));
        }
        Log.d(TAG, "...answer='" + this.answer + "'");
        Log.d(TAG, "...calculated age='" + age + "'");
    }

    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        Log.i(TAG,"["+id+"]getAnswer()");
        String tempAnswer = null;
        if(isViewActive()) {
            // Need to be certain the answer value is stored
            tempAnswer = (et.getText().length() == 0)? "":et.getText().toString();
            int age = Integer.parseInt(tempAnswer);
            dateAnswer = Functions.dateFromAge(age);
            answer = DateUtil.format(dateAnswer);
        }
        Log.d(TAG,"...returning answer='" + answer + "'");
        return answer;
    }

    @Override
    protected View createView(Context c) {
        et = new EditText(c);
        et.setText(String.valueOf(age));
        et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        et.setKeyListener(new DigitsKeyListener());
        return encapsulateQuestion(c, et);
    }

    public static AgeElement fromXML(String id, String question,
                                            String answer, String concept, String figure, String audio, Node n)
            throws ProcedureParseException
    {
        return new AgeElement(id, question, answer, concept, figure,
                audio);
    }
}
