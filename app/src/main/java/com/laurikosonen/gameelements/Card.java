package com.laurikosonen.gameelements;

public class Card {
    public String name;
    public int id;
    public String categoryName;
    public String categoryShortName;
    public int categoryNum;
    private String firstHalf;
    public NameHalfType firstHalfType;
    private NameHalfType secondHalfPreference;
    private String secondHalfSingular;
    private String secondHalfPlural;
    protected boolean keepCaps;

    public enum NameHalfType {
        verb,
        adjective,
        noun,
        singular,
        plural
    }

    public Card(String name,
                int id,
                String categoryName,
                String categoryShortName,
                int categoryNum) {
        this.name = name;
        this.id = id;
        this.categoryName = categoryName;
        this.categoryShortName = categoryShortName;
        this.categoryNum = categoryNum;
    }

    public String getNameHalf(boolean first, NameHalfType secondHalfType) {
        String result = null;

        if (first) {
            if (firstHalf != null)
                result = firstHalf;
            else if (keepCaps)
                result = name;
            else
                result = name.charAt(0) + name.substring(1).toLowerCase();
        }
        else if (secondHalfSingular != null || secondHalfPlural != null) {
            // Singular, no other option
            if (secondHalfPlural == null) {
                result = secondHalfSingular;
            }
            // Plural, no other option
            else if (secondHalfSingular == null) {
                result = secondHalfPlural;
            }
            // Singular, requested
            else if (secondHalfType == NameHalfType.singular) {
                result = secondHalfSingular;
            }
            // Plural, requested or default
            else {
                result = secondHalfPlural;
            }
        }
        else {
            if (keepCaps)
                result = name;
            else
                result = name.toLowerCase();
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

    public NameHalfType getSecondHalfPreference() {
        NameHalfType result = NameHalfType.singular;

        if (secondHalfPreference != null) {
            result = secondHalfPreference;
        }
        else if (firstHalfType != null) {
            switch (firstHalfType) {
                case verb:
                case adjective:
                    result = NameHalfType.plural;
                    break;
                case noun:
                    break;
            }
        }

        return result;
    }
}
