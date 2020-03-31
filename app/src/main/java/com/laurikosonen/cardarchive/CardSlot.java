package com.laurikosonen.cardarchive;

import android.widget.TextView;

public class CardSlot {
    public int id;
    public Card card1;
    public Card card2;
    public Card fundamental;

    private TextView textView;
    private boolean locked;
    private boolean secondaryLock;
    //private int defaultTextColor;
    //private int lockTextColor;
    private int lockBackgroundColor1;
    private int defaultBackgroundColor;
    private int lockBackgroundColor2;
    private int categoryTagLength;

    public CardSlot( int id,
                     TextView textView,
                     int lockBackgroundColor1,
                     int lockBackgroundColor2) {
        this.id = id;
        this.textView = textView;
        //defaultTextColor = getTextColor();
        //this.lockTextColor = lockTextColor;
        defaultBackgroundColor = 0;
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

    public void setText(String text, int categoryTagLength) {
        textView.setText(text);
        this.categoryTagLength = categoryTagLength;
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

    public int getCategoryTagLength() {
        return categoryTagLength;
    }

    public void lock(boolean enable) {
        if (locked == enable)
            return;

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
        fundamental = cardSlot.fundamental;
        lock(cardSlot.locked);
        enableSecondaryLock(cardSlot.secondaryLock);
        setText(cardSlot.getText(), cardSlot.getCategoryTagLength());
        //setTextColor(cardSlot.getTextColor());
    }

    public void copyFromLite(CardSlot cardSlot) {
        setCards(cardSlot.card1, cardSlot.card2);
        locked = cardSlot.locked;
        secondaryLock = cardSlot.secondaryLock;
        setText(cardSlot.getText(), cardSlot.getCategoryTagLength());
    }

    public boolean clear(boolean obeyLock) {
        if (!obeyLock || !locked) {
            card1 = null;
            card2 = null;
            fundamental = null;
            locked = false;
            secondaryLock = false;
            setText("", 0);
            //setTextColor(defaultTextColor);
            setBackgroundColor(defaultBackgroundColor);
            return true;
        }

        return false;
    }
}
