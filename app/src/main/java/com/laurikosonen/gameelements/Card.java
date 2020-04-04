package com.laurikosonen.gameelements;

public class Card {
    public String name;
    public int id;
    public String categoryName;
    public String categoryShortName;
    public int categoryNum;
    private String firstHalf;
    public NameHalfType firstHalfType;
    public NameHalfType secondHalfType;
    private boolean secondHalfPrefPlural;
    private String secondHalfSingular;
    private String secondHalfPlural;
    protected boolean keepCaps;

    public enum NameHalfType {
        verb,
        adjective,
        noun,
        modifier,
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

    public boolean isFundamental() {
        return false;
    }

    public String getNameHalf(boolean first,
                              NameHalfType otherFirstHalfType,
                              NameHalfType secondHalfTypePref) {
        String result;

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
            // Singular, driven by both half types being verbs
            else if (otherFirstHalfType == NameHalfType.verb
                     && secondHalfType == NameHalfType.verb) {
                result = secondHalfSingular;
            }
            // Singular, requested
            else if (secondHalfTypePref == NameHalfType.singular) {
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

    public void setNameFirstHalf(String nameHalf, NameHalfType type, NameHalfType secondHalfPrefType) {
        firstHalf = nameHalf;

        if (type != null)
            firstHalfType = type;
        else
            firstHalfType = NameHalfType.verb;

        if (secondHalfPrefType == NameHalfType.singular || secondHalfPrefType == NameHalfType.plural)
            secondHalfPrefPlural = secondHalfPrefType == NameHalfType.plural;
        else if (firstHalfType == NameHalfType.verb || firstHalfType == NameHalfType.adjective)
            secondHalfPrefPlural = true;
    }

    public void setNameSecondHalf(String nameHalf, NameHalfType type, boolean plural) {
        if (type != null)
            secondHalfType = type;
        else
            secondHalfType = NameHalfType.noun;

        if (plural)
            secondHalfPlural = nameHalf;
        else
            secondHalfSingular = nameHalf;
    }

    public NameHalfType getSecondHalfPreference() {
        return secondHalfPrefPlural ? NameHalfType.plural : NameHalfType.singular;
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
        else if (typeString.equalsIgnoreCase("modifier"))
            return NameHalfType.modifier;
        else if (typeString.equalsIgnoreCase("singular"))
            return NameHalfType.singular;
        else if (typeString.equalsIgnoreCase("plural"))
            return NameHalfType.plural;
        else
            return null;
    }
}
