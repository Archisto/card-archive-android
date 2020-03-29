package com.laurikosonen.cardarchive;

import android.widget.TextView;

public class CardSlot {
    public int id;
    public Card card1;
    public Card card2;

    private TextView textView;
    private boolean locked;
    private int defaultColor;
    private int lockColor;

    public CardSlot( int id,TextView textView, int lockColor) {
        this.id = id;
        this.textView = textView;
        defaultColor = getTextColor();
        this.lockColor = lockColor;
    }

    public boolean isEmpty() {
        return getText().isEmpty();
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean touchHit(int touchY) {
        // The y-coordinate increases when going down and vice versa
        return touchY >= textView.getTop() && touchY <= textView.getBottom();
    }

    public int getTop() {
        return textView.getTop();
    }

    public String getText() {
        return textView.getText().toString();
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public int getTextColor() {
        return textView.getTextColors().getDefaultColor();
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void lock(boolean enable) {
        locked = enable;
        if (locked)
            setTextColor(lockColor);
        else
            setTextColor(defaultColor);
    }

    public void copyFrom(CardSlot cardSlot) {
        card1 = cardSlot.card1;
        card2 = cardSlot.card2;
        locked = cardSlot.locked;
        setText(cardSlot.getText());
        setTextColor(cardSlot.getTextColor());
    }

    public boolean clear(boolean obeyLock) {
        if (!obeyLock || !locked) {
            card1 = null;
            card2 = null;
            locked = false;
            setText("");
            setTextColor(defaultColor);
            return true;
        }

        return false;
    }
}
