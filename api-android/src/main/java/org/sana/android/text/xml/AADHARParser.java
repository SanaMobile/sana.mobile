package org.sana.android.text.xml;

import android.os.Bundle;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 */
public class AADHARParser {
    public static final String TAG = AADHARParser.class.getSimpleName();
    public static final String PRINT_LETTER_BARCODE_DATA = "PrintLetterBarcodeData";
    public static final String UID = "uid";
    public static final String NAME = "name";
    public static final String GENDER = "gender";
    public static final String YOB = "yob";
    public static final String CO = "co";
    public static final String HOUSE = "house";
    public static final String STREET = "street";
    public static final String LM = "lm";
    public static final String LOC = "loc";
    public static final String VTC = "vtc";
    public static final String PO = "po";
    public static final String DIST = "dist";
    public static final String STATE = "state";
    public static final String PC = "pc";

    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static final int FATHER_SPOUSE_NAME_FIRST_PART_REMOVAL = 4;

    static final Bundle parseAttr(XmlPullParser parser){
        Bundle data = new Bundle();
        data.putString(UID, parser.getAttributeValue(null, UID));
        data.putString(NAME, (parser.getAttributeValue(null, NAME)));
        data.putString(GENDER, parser.getAttributeValue(null, GENDER));

        String yob = parser.getAttributeValue(null, YOB);
        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(yob), 1, 1);
        data.putString(YOB, sdf.format(c.getTime()));

        data.putString(CO, getAttributeValue(parser, CO).trim().substring(FATHER_SPOUSE_NAME_FIRST_PART_REMOVAL));
        data.putString(LM, getAttributeValue(parser, LM));
        data.putString(LOC, getAttributeValue(parser, LOC));
        data.putString(VTC, getAttributeValue(parser, VTC));
        data.putString(PO, getAttributeValue(parser, PO));
        data.putString(DIST, getAttributeValue(parser, DIST));
        data.putString(STATE, getAttributeValue(parser, STATE));
        data.putString(PC, getAttributeValue(parser, PC));
        return data;
    }

    public static Bundle parse(String xml) throws XmlPullParserException, IOException {
        XmlPullParserFactory xmlFactoryObject = null;
        xmlFactoryObject = XmlPullParserFactory.newInstance();
        XmlPullParser parser = xmlFactoryObject.newPullParser();
        parser.setInput(new StringReader(xml));
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, PRINT_LETTER_BARCODE_DATA);
        return parseAttr(parser);
        }

        private static String getAttributeValue(XmlPullParser parser, String name)
        {
            String value = parser.getAttributeValue(null, name);
            if(value== null)
                value = "";
            return value;
        }
}
