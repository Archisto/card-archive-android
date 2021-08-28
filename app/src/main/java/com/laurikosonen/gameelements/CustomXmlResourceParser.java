package com.laurikosonen.gameelements;

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
    private static final String endsInPrepositionStr = "endsInPreposition";
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
            String typeStr = null;
            String secondHalfPreferenceStr = null;
            int categoryId = 0;
            Card card = null;
            boolean secondHalf = false;
            boolean firstHalfEndsInPreposition = false;

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
                            //Log.d("CAGE", "Created pool; name: " + categoryName);
                        }
                    }
                    // Gets first half type and attributes
                    else if (startTagName.equalsIgnoreCase(firstHalfStr)) {
                        typeStr = parser.getAttributeValue(null, CustomXmlResourceParser.typeStr);
                        secondHalfPreferenceStr =
                            parser.getAttributeValue(null, preferenceStr);
                        firstHalfEndsInPreposition =
                            parser.getAttributeValue(null, endsInPrepositionStr) != null;
                    }
                    // Gets second half type and sets second half mode on
                    else if (startTagName.equalsIgnoreCase(secondHalfStr)) {
                        typeStr = parser.getAttributeValue(null, CustomXmlResourceParser.typeStr);
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

                        // The card's id is determined by poolAll.size(): if the pool is empty,
                        // the id is 0, and so on
                        card = getParsedCard
                            (text, poolAll.size(), categoryName, categoryShortName, categoryId);
                        if (card != null) {
                            pools.get(categoryId).add(card);
                            poolAll.add(card);
                        }
                    }
                    // Adds name halves to the latest card
                    else if (card != null) {
                        Card.NameHalfType type = Card.parseNameHalfType(typeStr);

                        // First half of the card's name
                        if (startTagName.equalsIgnoreCase(firstHalfStr)) {
                            card.setNameFirstHalf(
                                text,
                                type,
                                Card.parseNameHalfType(secondHalfPreferenceStr),
                                firstHalfEndsInPreposition);
                        }
                        // Second half of the card's name
                        else if (secondHalf) {
                            if (startTagName.equalsIgnoreCase(singularStr)) {
                                card.setNameSecondHalf(text, type, false);
                            }
                            else if (startTagName.equalsIgnoreCase(pluralStr)) {
                                card.setNameSecondHalf(text, type, true);
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
                                      int id,
                                      String categoryName,
                                      String categoryShortName,
                                      int categoryNum) {
        if (text != null && !text.isEmpty()) {
            return new Card(text, id, categoryName, categoryShortName, categoryNum);
        }

        return null;
    }
}
