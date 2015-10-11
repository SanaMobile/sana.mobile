package org.sana.android.procedure;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import org.joda.time.DateTime;
import org.sana.R;
import org.sana.util.DateUtil;
import org.sana.util.Functions;
import org.w3c.dom.Node;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Element which accepts an integer as input and calculates an estimated date. This
 * is useful for entering approximate dates of birth when precise value is not
 * known.
 */
public class AgeElement  extends TextEntryElement {
    public static final String TAG = AgeElement.class.getSimpleName();
    Date dateAnswer = new Date();
    NumberPicker mWidget;
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
        try {
            dateAnswer = DateUtil.parseDate(answer);
            age = Functions.age(dateAnswer);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(isViewActive()) {
            mWidget.setValue((int)age);
            //et.setText(String.valueOf(age));
        }
        Log.d(TAG, "...answer='" + this.answer + "'");
        Log.d(TAG, "...calculated age='" + age + "'");
    }

    /** {@inheritDoc} */
    @Override
    public String getAnswer() {
        Log.i(TAG, "[" + id + "]getAnswer()");
        if(isViewActive()) {

        }
        Log.d(TAG,"...returning answer='" + answer + "'");
        return answer;
    }

    @Override
    protected View createView(Context c) {
        LayoutInflater inflater = ((Activity)c).getLayoutInflater();
        LinearLayout v = (LinearLayout)inflater.inflate(R.layout.widget_element_age, null);
        mWidget = (NumberPicker) v.findViewById(R.id.answer);
        mWidget.setMaxValue(120);
        mWidget.setMinValue(0);
        mWidget.setValue((int) age);
        mWidget.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    setAge(newVal);
            }
        });
        /*
        et = new EditText(c);
        et.setText(String.valueOf(age));
        et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        et.setKeyListener(new DigitsKeyListener());
        */
        //return encapsulateQuestion(c, et);
        return encapsulateQuestion(c, v);
    }

    public void setAge(long age){
        Log.i(TAG, "[" + id + "]setAge()");
        Log.d(TAG, "....age="+age);
        this.age = age;
        DateTime dateTime = new DateTime();
        int month = dateTime.getMonthOfYear();
        int day = dateTime.getDayOfMonth();
        if(month < 6 || (month == 6 && day < 15)) {
            dateAnswer = dateTime.minusYears((int)age + 1).withMonthOfYear(6)
                    .withDayOfMonth(15).toLocalDate().toDate();
        } else {
            dateAnswer = dateTime.minusYears((int)age).withMonthOfYear(6)
                    .withDayOfMonth(15).toLocalDate().toDate();
        }
        answer = DateUtil.format(dateAnswer);
        Log.d(TAG, "....answer="+answer);
    }

    public static AgeElement fromXML(String id, String question,
                                            String answer, String concept, String figure, String audio, Node n)
            throws ProcedureParseException
    {
        return new AgeElement(id, question, answer, concept, figure,
                audio);
    }
}
