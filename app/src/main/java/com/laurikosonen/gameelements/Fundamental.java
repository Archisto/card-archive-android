package com.laurikosonen.gameelements;

public class Fundamental extends Card {
    private static final String categoryName = "Fundamentals";
    private static final String categoryShortName = "Fnd";

    public Fundamental(String name, int id) {
        super(name, id, categoryName, categoryShortName, -1);
    }

    @Override
    public boolean isFundamental() {
        return true;
    }

    public void parseAndSetKeepCaps(String keepCapsString) {
        keepCaps = Boolean.parseBoolean(keepCapsString);
    }
}
