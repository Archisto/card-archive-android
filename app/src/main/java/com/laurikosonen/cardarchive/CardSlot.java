package com.laurikosonen.cardarchive;

import android.widget.TextView;

public class CardSlot {
    public int id;
    public Card card1;
    public Card card2;

    private TextView textView;
    private boolean locked;
    private boolean secondaryLock;
    private int defaultTextColor;
    private int defaultBackgroundColor;
//    private int lockTextColor;
    private int lockBackgroundColor1;
    private int lockBackgroundColor2;

    public CardSlot( int id,
                     TextView textView,
                     int lockBackgroundColor1,
                     int lockBackgroundColor2) {
        this.id = id;
        this.textView = textView;
        defaultTextColor = getTextColor();
        defaultBackgroundColor = 0;
//        this.lockTextColor = lockTextColor;
        this.lockBackgroundColor1 = lockBackgroundColor1;
        this.lockBackgroundColor2 = lockBackgroundColor2;
    }

    public boolean isEmpty() {
        return getText().isEmpty();
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean secondaryLockEnabled() {
        return secondaryLock;
    }

    public boolean touchHit(int touchY) {
        // The y-coordinate increases when going down and vice versa
        return touchY >= textView.getTop() && touchY <= textView.getBottom();
    }

    public int getTop() {
        return textView.getTop();
    }

    public void setCards(Card card1, Card card2) {
        if (card1 != null)
            this.card1 = card1;
        this.card2 = card2;
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

    public void setBackgroundColor(int color) {
        textView.setBackgroundColor(color);
    }

    public void lock(boolean enable) {
        locked = enable;
        if (locked) {
            //setTextColor(lockTextColor);
            setBackgroundColor(lockBackgroundColor1);
        }
        else {
            secondaryLock = false;
            //setTextColor(defaultTextColor);
            setBackgroundColor(defaultBackgroundColor);
        }
    }

    public void enableSecondaryLock(boolean enable) {
        if (locked) {
            if (enable && !secondaryLock) {
                setBackgroundColor(lockBackgroundColor2);
                secondaryLock = true;
            }
            else if (!enable && secondaryLock) {
                setBackgroundColor(lockBackgroundColor1);
                secondaryLock = false;
            }
        }
    }

    public void copyFrom(CardSlot cardSlot) {
        setCards(cardSlot.card1, cardSlot.card2);
        lock(cardSlot.locked);
        enableSecondaryLock(cardSlot.secondaryLock);
        setText(cardSlot.getText());
        setTextColor(cardSlot.getTextColor());
    }

    public boolean clear(boolean obeyLock) {
        if (!obeyLock || !locked) {
            card1 = null;
            card2 = null;
            locked = false;
            secondaryLock = false;
            setText("");
            setTextColor(defaultTextColor);
            setBackgroundColor(defaultBackgroundColor);
            return true;
        }

        return false;
    }
}
