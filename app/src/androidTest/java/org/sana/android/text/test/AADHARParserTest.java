package org.sana.android.text.test;

import android.os.Bundle;

import junit.framework.TestCase;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.sana.android.text.xml.AADHARParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by winkler.em@gmail.com, on 06/10/2016.
 */
public class AADHARParserTest{
    public static final String TAG = AADHARParserTest.class.getSimpleName();
    final String XML = "<?xml version=\"1.0\" encoding = \"utf-8?\">\n" +
            "<PrintLetterBarcodeData" +
            "    uid=\"111111111111\"" +
            "    name=\"Someone\"" +
            "    gender=\"M\"" +
            "    yob=\"1991\"" +
            "    co=\"CO\"" +
            "    lm=\"LM\"" +
            "    loc=\"LOC\"" +
            "    vtc=\"VTC\"" +
            "    po=\"PO\"" +
            "    dist=\"DIST\"" +
            "    state=\"STATE\"" +
            "    pc=\"PC\"/>";



    @Test
    public void testParse(){
        try {
            Bundle data = AADHARParser.parse(XML);
            assertEquals(data.getString(AADHARParser.UID), "111111111111");
            assertEquals(data.getString(AADHARParser.NAME), "Someone");
            assertEquals(data.getString(AADHARParser.GENDER), "M");
            assertEquals(data.getString(AADHARParser.YOB), "1991-01-01");
            assertEquals(data.getString(AADHARParser.CO), "CO");
            assertEquals(data.getString(AADHARParser.LM), "LM");
            assertEquals(data.getString(AADHARParser.LOC), "LOC");
            assertEquals(data.getString(AADHARParser.VTC), "VTC");
            assertEquals(data.getString(AADHARParser.PO), "PO");
            assertEquals(data.getString(AADHARParser.DIST), "DIST");
            assertEquals(data.getString(AADHARParser.STATE), "STATE");
            assertEquals(data.getString(AADHARParser.PC), "PC");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            assert false;
        } catch (IOException e) {
            assert false;
            e.printStackTrace();
        }
    }
}
