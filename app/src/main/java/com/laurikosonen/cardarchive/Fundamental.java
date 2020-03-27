package com.laurikosonen.cardarchive;

public class Fundamental extends Card {
    private static final String categoryName = "Fundamentals";
    private static final String categoryShortName = "Fnd";

    public Fundamental(String name, int id) {
        super(name, id, categoryName, categoryShortName, -1);
    }
}
