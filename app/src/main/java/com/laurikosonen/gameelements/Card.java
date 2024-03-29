package com.laurikosonen.gameelements;

public class Card {
    public String name;
    public int id;
    public String categoryName;
    public String categoryShortName;
    public int categoryNum;
    private String firstHalf;
    private String firstHalfNoEndPreposition;
    private String secondHalfSingular;
    private String secondHalfPlural;
    public NameHalfType firstHalfType;
    public NameHalfType secondHalfType;
    private boolean secondHalfPrefPlural;
    protected boolean keepCaps; // unused?

    public enum NameHalfType {
        none,
        verb,
        adjective,
        noun,
        nounPlural,
        specifier,
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

                // Use the version without ending preposition
                // if the second half's type is verb or modifier
                if (firstHalfNoEndPreposition != null
                    && !firstHalfNoEndPreposition.isEmpty()
                    && (secondHalfCardType == NameHalfType.verb
                        || secondHalfCardType == NameHalfType.modifier)) {
                    result = firstHalfNoEndPreposition;
                }

                // Pluralize a specifier which prefers plural second half
                // and the second half is a verb or a modifier
                if (firstHalfType == NameHalfType.specifier
                    && secondHalfPrefPlural
                    && (secondHalfCardType == NameHalfType.verb
                        || secondHalfCardType == NameHalfType.modifier)) {
                    result = result + 's';
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
            // Singular because both half's type is verb
            else if (otherFirstHalfType == NameHalfType.verb
                     && secondHalfType == NameHalfType.verb) {
                result = secondHalfSingular;
            }
            // Singular because the first half is a singular noun and the second a verb
            else if (otherFirstHalfType == NameHalfType.noun
                     && secondHalfType == NameHalfType.verb) {
                result = secondHalfSingular;
            }
            else if (otherFirstHalfType == NameHalfType.specifier) {
                // Plural because the first half is a specifier
                // and the second not a verb or the first half prefers plural
                if (secondHalfType != NameHalfType.verb
                      || secondHalfTypePref == NameHalfType.plural) {
                    result = secondHalfPlural;
                }
                // Otherwise singular for a specifier
                else {
                    result = secondHalfSingular;
                }
            }
            // Plural, preference
            else if (secondHalfTypePref == NameHalfType.plural) {
                result = secondHalfPlural;
            }
            // Singular, preference or default
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

        // The first half's type; default: verb
        if (type != null)
            firstHalfType = type;
        else
            firstHalfType = NameHalfType.verb;

        // The first half's preference for the second half being plural
        if (secondHalfPrefType != null && secondHalfPrefType != NameHalfType.none)
            secondHalfPrefPlural = secondHalfPrefType == NameHalfType.plural;
        else if (firstHalfType == NameHalfType.verb
                 || firstHalfType == NameHalfType.adjective
                 || firstHalfType == NameHalfType.nounPlural)
            secondHalfPrefPlural = true;

        // Set version without ending preposition
        if (endsInPreposition) {
            firstHalfNoEndPreposition = firstHalf.substring(0, firstHalf.lastIndexOf(" "));
        }
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
        else if (typeString.equalsIgnoreCase("noun pl"))
            return NameHalfType.nounPlural;
        else if (typeString.equalsIgnoreCase("specifier"))
            return NameHalfType.specifier;
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
