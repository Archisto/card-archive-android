package com.laurikosonen.gameelements;

public class Card {
    public String name;
    public int id;
    public String categoryName;
    public String categoryShortName;
    public int categoryNum;
    private String firstHalf;
    private String secondHalfSingular;
    private String secondHalfPlural;
    public NameHalfType firstHalfType;
    public NameHalfType secondHalfType;
    private boolean firstHalfEndsInPreposition;
    private boolean secondHalfPrefPlural;
    protected boolean keepCaps; // unused?

    public enum NameHalfType {
        none,
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
                              NameHalfType secondHalfTypePref,
                              NameHalfType secondHalfCardType) {
        String result;

        if (first) {
            if (firstHalf != null) {
                // Default
                result = firstHalf;

                // Remove ending preposition if the second half's type is verb or modifier
                if (firstHalfEndsInPreposition
                    && (secondHalfCardType == NameHalfType.verb
                        || secondHalfCardType == NameHalfType.modifier)) {
                    result = result.substring(0, result.lastIndexOf(" "));
                }
            }
            else if (keepCaps) {
                result = name;
            }
            else {
                // Only the first letter can remain capitalized
                result = name.charAt(0) + name.substring(1).toLowerCase();
            }
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
            // Singular because both halves' type is verb
            else if (otherFirstHalfType == NameHalfType.verb
                     && secondHalfType == NameHalfType.verb) {
                result = secondHalfSingular;
            }
            // Plural, requested
            else if (secondHalfTypePref == NameHalfType.plural) {
                result = secondHalfPlural;
            }
            // Singular, requested or default
            else {
                result = secondHalfSingular;
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

    public void setNameFirstHalf(String nameHalf,
                                 NameHalfType type,
                                 NameHalfType secondHalfPrefType,
                                 boolean endsInPreposition) {
        firstHalf = nameHalf;

        if (type != null)
            firstHalfType = type;
        else
            firstHalfType = NameHalfType.verb;

        if (secondHalfPrefType != null && secondHalfPrefType != NameHalfType.none)
            secondHalfPrefPlural = secondHalfPrefType == NameHalfType.plural;
        else if (firstHalfType == NameHalfType.verb || firstHalfType == NameHalfType.adjective)
            secondHalfPrefPlural = true;

        this.firstHalfEndsInPreposition = endsInPreposition;
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
            return NameHalfType.none;
    }
}
