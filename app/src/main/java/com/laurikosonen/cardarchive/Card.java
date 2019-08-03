package com.laurikosonen.cardarchive;

import android.util.Log;

public class Card {
    public String name;
    public String categoryName;
    public String categoryShortName;
    public int categoryNum;
    private String firstHalf;
    public NameHalfType firstHalfType;
    public NameHalfType secondHalfPreference;
    private String secondHalfSingular;
    private String secondHalfPlural;

    public enum NameHalfType {
        verb,
        adjective,
        noun,
        singular,
        plural
    }

    public Card(String name, String categoryName, String categoryShortName, int categoryNum) {
        this.name = name;
        this.categoryName = categoryName;
        this.categoryShortName = categoryShortName;
        this.categoryNum = categoryNum;
    }

    public String getNameHalf(boolean first, NameHalfType secondHalfType) {
        String result = null;

        if (first) {
            if (firstHalf != null)
                result = firstHalf;
        }
        else if (secondHalfSingular != null || secondHalfPlural != null) {
            if (secondHalfPlural == null) {
                result = secondHalfSingular;
            }
            else if (secondHalfSingular == null) {
                result = secondHalfPlural;
            }
            else if (secondHalfType == NameHalfType.singular) {
                result = secondHalfSingular;
            }
            else {
                result = secondHalfPlural;
            }
        }

        return result;
    }

    public void setNameHalf(String nameHalf, NameHalfType type, NameHalfType secondHalfPreference) {
        if (type == null || type == NameHalfType.singular) {
            secondHalfSingular = nameHalf;
        }
        else if (type == NameHalfType.plural) {
            secondHalfPlural = nameHalf;
        }
        else {
            firstHalf = nameHalf;
            firstHalfType = type;
            this.secondHalfPreference = secondHalfPreference;
        }
    }

    public static NameHalfType parseNameHalfType(String typeString) {
        if (typeString == null)
            return null;

        if (typeString.equalsIgnoreCase("verb"))
            return NameHalfType.verb;
        else if (typeString.equalsIgnoreCase("adjective"))
            return NameHalfType.adjective;
        else if (typeString.equalsIgnoreCase("noun"))
            return NameHalfType.noun;
        else if (typeString.equalsIgnoreCase("singular"))
            return NameHalfType.singular;
        else if (typeString.equalsIgnoreCase("plural"))
            return NameHalfType.plural;
        else
            return null;
    }
}
