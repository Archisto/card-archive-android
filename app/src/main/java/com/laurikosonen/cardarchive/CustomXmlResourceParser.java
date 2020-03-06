package com.laurikosonen.cardarchive;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class CustomXmlResourceParser {

    private static final String categoryStr = "category";
    private static final String elementStr = "element";
    private static final String nameStr = "name";
    private static final String shortNameStr = "shortName";
    private static final String idStr = "id";
    private static final String typeStr = "type";
    private static final String preferenceStr = "preference";
    private static final String theStr = "the";
    private static final String firstHalfStr = "firstHalf";
    private static final String secondHalfStr = "secondHalf";
    private static final String singularStr = "singular";
    private static final String pluralStr = "plural";

    private static int parseInt(String str) {
        int result = -1;
        if (str != null && str.length() > 0) {
            try {
                result = Integer.parseInt(str);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void parseCards(Resources resources,
                                  int resourceID,
                                  List<List<Card>> pools,
                                  List<Card> poolAll) {
        XmlResourceParser parser = resources.getXml(resourceID);

        try {
            parser.next();
            int eventType = parser.getEventType();
            String startTagName = "_";
            String categoryName = "ERROR";
            String categoryShortName = "ERR";
            String firstHalfTypeStr = null;
            String secondHalfPreferenceStr = null;
            int categoryId = 0;
            Card card = null;
            boolean secondHalf = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    startTagName = parser.getName();
                    //Log.d("CAGE", "startTagName: " + startTagName);

                    // Parses the category
                    if (startTagName.equalsIgnoreCase(categoryStr)) {
                        String strId = parser.getAttributeValue(null, idStr);
                        categoryName = parser.getAttributeValue(null, nameStr);
                        categoryShortName = parser.getAttributeValue(null, shortNameStr);
                        categoryId = parseInt(strId);

                        // Increases the pool count if the ID is too large
                        if (pools.size() <= categoryId) {
                            List<Card> newPool = new ArrayList<>();
                            pools.add(newPool);
                            //Log.d("CAGE", "Created pool; new pool count: " + pools.size());
                        }
                    }
                    // Gets first half type
                    else if (startTagName.equalsIgnoreCase(firstHalfStr)) {
                        firstHalfTypeStr = parser.getAttributeValue(null, typeStr);
                        secondHalfPreferenceStr =
                            parser.getAttributeValue(null, preferenceStr);
                    }
                    // Sets second half mode on
                    else if (startTagName.equalsIgnoreCase(secondHalfStr)) {
                        secondHalf = true;
                    }

                }
                else if (eventType == XmlPullParser.END_TAG) {

                    // Sets second half mode off
                    if (parser.getName().equalsIgnoreCase(secondHalfStr)) {
                        secondHalf = false;
                    }
                }
                else if (eventType == XmlPullParser.TEXT) {
                    String text = parser.getText();

                    // Creates a new card
                    if (startTagName.equalsIgnoreCase(nameStr)
                          || startTagName.equalsIgnoreCase(elementStr)) {
                        card = getParsedCard(text, categoryName, categoryShortName, categoryId);
                        if (card != null) {
                            pools.get(categoryId).add(card);
                            poolAll.add(card);
                        }
                    }
                    // Adds name halves to the latest card
                    else if (card != null) {

                        // First half of the card's name
                        if (startTagName.equalsIgnoreCase(firstHalfStr)) {
                            card.setNameHalf(
                                text,
                                Card.parseNameHalfType(firstHalfTypeStr),
                                Card.parseNameHalfType(secondHalfPreferenceStr));
                        }
                        // Second half of the card's name
                        else if (secondHalf) {
                            if (startTagName.equalsIgnoreCase(singularStr)) {
                                card.setNameHalf(text, Card.NameHalfType.singular, null);
                            }
                            else if (startTagName.equalsIgnoreCase(pluralStr)) {
                                card.setNameHalf(text, Card.NameHalfType.plural, null);
                            }
                        }
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
            Log.d("CAGE", "Card parsing complete");
        }
    }

    private static Card getParsedCard(String text,
                                      String categoryName,
                                      String categoryShortName,
                                      int id) {
        if (text != null && !text.isEmpty()) {
            return new Card(text, categoryName, categoryShortName, id);
        }

        return null;
    }
}
