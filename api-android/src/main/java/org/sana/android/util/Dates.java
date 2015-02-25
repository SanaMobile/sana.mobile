package org.sana.android.util;

import org.sana.api.IModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Eric on 02/07/2015.
 */
public class Dates {
    private static final SimpleDateFormat sdf = new SimpleDateFormat(IModel
            .DATE_FORMAT,
            Locale.US);

    private Dates(){}

    public static String toSQL(Date date){
       return sdf.format(date);
    }

    public static Date fromSQL(String date) throws ParseException {
        return sdf.parse(date);
    }

    public static String formatForDisplay(Date date, String format){
        SimpleDateFormat formatter = new SimpleDateFormat(format,
                Locale.getDefault());
        return formatter.format(date);
    }
}
