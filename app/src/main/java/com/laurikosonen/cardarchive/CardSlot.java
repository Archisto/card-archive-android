package com.laurikosonen.cardarchive;

import android.widget.TextView;

public class CardSlot {
    public Card card1;
    public Card card2;
    public boolean locked;
    public boolean avoided;

    private TextView textView;

    public CardSlot(TextView textView) {
        this.textView = textView;
    }

    public boolean isEmpty() {
        return getText().isEmpty();
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

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void copyFrom(CardSlot cardSlot) {
        card1 = cardSlot.card1;
        card2 = cardSlot.card2;
        locked = cardSlot.locked;
        avoided = cardSlot.avoided;
        setText(cardSlot.getText());
    }

    public void clear() {
        card1 = null;
        card2 = null;
        locked = false;
        avoided = false;
        setText("");
    }
}
