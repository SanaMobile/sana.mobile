package org.sana.android.text;

import android.content.Context;

import org.joda.time.DateTime;
import org.sana.android.app.Locales;
import org.sana.android.util.Dates;
import org.sana.api.R;

import java.util.Date;

/**
 */
public class LocalizedDateFormatter {

    private final String[] months;
    private final int format;

    public static final int FORMAT_LONG = 0;
    public static final int FORMAT_DAY_FIRST = 1;
    public static final int FORMAT_SHORT = 2;

    public LocalizedDateFormatter(Context context, int format, String[] months){
        this.months = months;
        this.format = format;
    }

    private String format(int year, int month, int day){
        return String.format("%02d %s %04d", day, months[month - 1], year);
    }

    public String format(Date date){
        DateTime dt = new DateTime(date);
        int month = dt.getMonthOfYear();
        int dayOfMonth = dt.getDayOfMonth();
        int year = dt.getYear();
        return format(year, month, dayOfMonth);
    }
}
