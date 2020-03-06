package com.laurikosonen.cardarchive;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XmlResourceParser_Fundamentals {

    private static final String elementStr = "element";
    private static final String nameStr = "name";

    public static void parseFundamentals(Resources resources,
                                         int resourceID,
                                         List<Card> fundamentals) {
        XmlResourceParser parser = resources.getXml(resourceID);

        try {
            parser.next();
            int eventType = parser.getEventType();
            String startTagName = "_";
            String name = "_";
            Fundamental fundamental = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    startTagName = parser.getName();
                    //Log.d("CAGE", "startTagName: " + startTagName);

                    if (startTagName.equalsIgnoreCase(elementStr)) {
                        name = parser.getAttributeValue(null, nameStr);
                        fundamental = new Fundamental(name);
                        fundamentals.add(fundamental);
                    }
                }

                eventType = parser.next();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        finally {
            Log.d("CAGE", "Fundamental parsing complete");
        }
    }
}
