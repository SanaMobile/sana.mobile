package org.sana.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.ViewSwitcher;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.sana.R;
import org.sana.android.app.Locales;
import org.sana.android.app.Preferences;
import org.sana.android.util.Dates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class DateOfBirthWidget extends LinearLayout {
    public static final String TAG = DateOfBirthWidget.class.getSimpleName();
    public static final int MAX_AGE = 120;

    DatePicker mDatePicker;
    Spinner mSpinner;
    CheckBox mCheckBox;
    ViewSwitcher mSwitcher;

    LocalDate mDate = LocalDate.now();

    public DateOfBirthWidget(Context context) {
        this(context, null);
    }

    public DateOfBirthWidget(Context context, AttributeSet attrs) {
        this(context, attrs, new Date());
    }

    public DateOfBirthWidget(Context context, AttributeSet attrs, Date value) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DateOfBirthWidget, 0, 0);
        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        String locale = Preferences.getString(getContext(),
                getContext().getString(R.string.setting_locale), "en");
        Locales.updateLocale(getContext(), locale);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_date_of_birth, this, true);

        mSpinner = (Spinner)findViewById(R.id.spinner);
        mSpinner.setAdapter((SpinnerAdapter) getYearsAdapter());
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onAgeSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                onAgeSelected(0);
            }
        });

        // Set the date
        if(value != null){
            mDate = new LocalDate(value);
        }
        mDatePicker = (DatePicker)findViewById(R.id.datePicker);
        init(mDate, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                onDateSelected(year, monthOfYear, dayOfMonth);
            }
        });

        mSwitcher = (ViewSwitcher)findViewById(R.id.viewSwitcher);
        mCheckBox = (CheckBox)findViewById(R.id.checkbox);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onDefaultSelectorChecked(isChecked);
            }
        });
    }

    protected Adapter getYearsAdapter(){
        List<Integer> years = new ArrayList<Integer>(MAX_AGE + 1);
        for(int i = 0; i < years.size(); i++){
                years.add(i,i);
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(getContext(),
                android.R.layout.simple_spinner_item, years);
        return adapter;

    }

    protected final void onAgeSelected(int age){
        mDate = LocalDate.now().withMonthOfYear(6).withDayOfMonth(15)
            .minusYears(age);
    }

    protected final void onDateSelected(int year, int month, int day){
        mDate = new LocalDate(year,month,day);
    }

    protected final void onDefaultSelectorChecked(boolean isChecked){
        mDatePicker.updateDate(mDate.getYear(), mDate.getMonthOfYear(), mDate.getDayOfMonth());
        int age = Years.yearsBetween(mDate, LocalDate.now()).getYears();
        mSpinner.setSelection(age);
        mSwitcher.showNext();
    }

    public String getValue(){
        return Dates.toSQL(mDate.toDate());
    }

    public void setValue(String value){
        try {
            mDate = new LocalDate(Dates.fromSQL(value));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        updateDate(mDate.getYear(),mDate.getMonthOfYear(),mDate.getDayOfMonth());
    }

    public void init(LocalDate date, DatePicker.OnDateChangedListener mListener){
        mDatePicker.init(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), mListener);
        int age = Years.yearsBetween(date, LocalDate.now()).getYears();
        mSpinner.setSelection(age);
    }

    public void updateDate(int year, int month, int day){
        mDatePicker.updateDate(mDate.getYear(), mDate.getMonthOfYear(), mDate.getDayOfMonth());
        int age = Years.yearsBetween(mDate, LocalDate.now()).getYears();
        mSpinner.setSelection(age);
    }

    public void setDefaultText(String text){
        mCheckBox.setText(text);
    }

    public void setDefaultText(int resId){
        String locale = Preferences.getString(getContext(),
                getContext().getString(R.string.setting_locale), "en");
        Locales.updateLocale(getContext(), locale);
        mCheckBox.setText(resId);
    }
}
