package com.laurikosonen.cardarchive;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int minDisplayedCards = 1;
    private final int maxDisplayedCards = 10;
    private int maxTouchDuration = 30;

    private View mainView;
    private Menu menu;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private TextView headerInfoText;

    private List<List<Card>> decks;
    private List<Card> allCards;
    private List<Card> allCardsShuffled;
    private List<Card> displayedCards;
    private List<Card> fundamentals;
    private List<CardSlot> cardSlots;
    private int displayedCategory = -1;
    private int cardCountCap = 10;
    private int deckStartIndex = 0;
    private int nextCardInDeck = 0;
    private int listSize = 0;
    private int touchStartX;
    private int touchStartY;
    private int touchDuration;
    private int lockedCardCount;
    private DisplayMode displayMode;
    private MenuItem currentDisplayedCatItem;
    private MenuItem currentDisplayedDisplayModeItem;
    private MenuItem mergeSlotToggle;
    //private MenuItem keepResultCategoriesToggle;
    private MenuItem autoUpdateSettingChangesToggle;
    private MenuItem showCategoriesToggle;
    //private int mainTextColor;
    private int mergeSlotColor;
    private boolean listModeJustStarted;
    private boolean mergeAltModeJustStarted;
    private boolean showCategories = true;
    private boolean mergeSlotEnabled;
    //private boolean keepResultCategories;
    private boolean autoUpdateSettingChanges = true;
    private boolean locksChanged;
    private boolean touching;
    private boolean touchHandled;

    private Card mergeBeginning;
    private Card tipCard;
    private CardSlot mergeSlot;


    private enum DisplayMode {
        classic,
        list,
        merge,
        mergeAlt,
        fundamentals,
        fundamentalElem
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainView = (View) findViewById(R.id.include);

        initDecks();
        initCardSlots();
        displayMode = DisplayMode.classic;
        drawCards(displayedCategory, false);

        //mainTextColor = ContextCompat.getColor(this, R.color.colorGray);
        mergeSlotColor = ContextCompat.getColor(this, R.color.colorMergeSlot);

        headerInfoText = (TextView) findViewById(R.id.headerInfo);
        headerInfoText.setText(String.format(getString(R.string.allCatAndCardCount), "" + allCards.size()));

        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (longTouch(x, y))
                            handleCardSlotLongTouch(touchStartY);
                        else if (swipe(true, x))
                            handleCardSlotSwipe(true, touchStartY);
                        else if (swipe(false, x))
                            handleCardSlotSwipe(false, touchStartY);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!touchHandled)
                            handleCardSlotTouch(y);
                        endTouch();
                        mainView.performClick();
                        break;
                }

                return true;
            }
        });

        fab1 = (FloatingActionButton) findViewById(R.id.fab_main);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawCards(displayedCategory, false);
            }
        });

        fab2 = (FloatingActionButton) findViewById(R.id.fab_secondary);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (displayMode == DisplayMode.list) {
                    prevPageInCardList();
                }
                else if (displayMode == DisplayMode.mergeAlt) {
                    takeNewMergeAltBeginning();
                    drawCards(displayedCategory, false);
                }
            }
        });

        fab2.hide();
    }

    // Alternate way of checking touch. Not as powerful as OnTouchListener.
    // The other necessary part is in content_main: android:onClick="onTouch
    //public void onTouch(View view) {
    //}

    private boolean longTouch(int touchX, int touchY) {
        if (touchHandled)
            return false;

        if (!touching) {
            touching = true;
            touchStartX = touchX;
            touchStartY = touchY;
        }
        else {
            touchDuration++;
            if (touchDuration > maxTouchDuration) {
                touchHandled = true;

                int touchPosMaxDifference = 10;
                return Math.abs(touchY - touchStartY) <= touchPosMaxDifference
                    && Math.abs(touchX - touchStartX) <= touchPosMaxDifference;
            }
        }

        return false;
    }

    private boolean swipe(boolean right, int touchX) {
        if (touching && !touchHandled) {
            int touchXRequiredDistance = 30;
            int difference = touchX - touchStartX;
            if (right && difference > touchXRequiredDistance) {
                touchHandled = true;
                return true;
            }
            else if (!right && difference < -1 * touchXRequiredDistance) {
                touchHandled = true;
                return true;
            }
        }

        return false;
    }

    private void endTouch() {
        touching = false;
        touchHandled = false;
        touchDuration = 0;
    }

//    private void makeSnackbar(View view, String text) {
//        Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
//            .setAction("Action", null).show();
//    }

    private void hideFab2() {
        fab2.hide();
    }

    private void goToHelp() {
        Intent i = new Intent(MainActivity.this, HelpActivity.class);
        i.putExtra("totalElemCount", "" + allCards.size());
        i.putExtra("wldElemCount", "" + decks.get(0).size());
        i.putExtra("chaElemCount", "" + decks.get(1).size());
        i.putExtra("grpElemCount", "" + decks.get(2).size());
        i.putExtra("infElemCount", "" + decks.get(3).size());
        i.putExtra("iactElemCount", "" + decks.get(4).size());
        i.putExtra("abiElemCount", "" + decks.get(5).size());
        i.putExtra("navElemCount", "" + decks.get(6).size());
        i.putExtra("comElemCount", "" + decks.get(7).size());
        i.putExtra("thiElemCount", "" + decks.get(8).size());
        i.putExtra("itmElemCount", "" + decks.get(9).size());
        i.putExtra("goElemCount", "" + decks.get(10).size());
        i.putExtra("avElemCount", "" + decks.get(11).size());
        i.putExtra("mscElemCount", "" + decks.get(12).size());
        i.putExtra("fundamentalCount", "" + fundamentals.size());
        startActivity(i);
    }

    private String getCategoryName(int categoryIndex, boolean shortName) {
        if (shortName)
            return decks.get(categoryIndex).get(0).categoryShortName;
        else
            return decks.get(categoryIndex).get(0).categoryName;
    }

    private void setTextColors(int color) {
        for (CardSlot slot : cardSlots) {
            slot.setTextColor(color);
        }
    }

    private void setTextColor(int textIndex, int color) {
        cardSlots.get(textIndex).setTextColor(color);
    }

    private void updateHeaderInfoText() {
        String newString;
        String categoryName = "";

        boolean usingAllCards = displayedCategory < 0;
        if (!usingAllCards)
            categoryName = getCategoryName(displayedCategory, false);

        if (displayMode == DisplayMode.fundamentals) {
            newString = String.format(getString(R.string.fundamentalCount), "" + fundamentals.size());
        }
        else if (displayMode == DisplayMode.list) {
            newString = getHeaderInfoListMode(usingAllCards, categoryName);
        }
        else {
            String cardCount = "" + allCards.size();
            if (!usingAllCards)
                cardCount = "" + decks.get(displayedCategory).size();

            if (usingAllCards)
                newString = String.format(getString(R.string.allCatAndCardCount), cardCount);
            else
                newString = String.format(getString(R.string.catAndCardCount), categoryName, cardCount);
        }

        headerInfoText.setText(newString);
    }

    private String getHeaderInfoListMode(boolean usingAllCards, String categoryName) {
        // TODO: If the displayed card count is changed in a specific way,
        //  if shows the second page as the first. Fix this.
        //  (This happens because the faux-first page's first card would be on the real first page)

        int displayedListCardCount = cardCountCap - lockedCardCount;
        if (displayedListCardCount <= 0) {
            if (cardCountCap < maxDisplayedCards)
                return getString(R.string.listUnavailable_elemCountCap);
            else
                return getString(R.string.listUnavailable_clearLocks);
        }

        String currentPage = "1";
        if (nextCardInDeck > deckStartIndex) {
            currentPage =
                "" + ((int) (0.5f + ((nextCardInDeck - deckStartIndex) / displayedListCardCount)) + 1);
            //Log.d("CAGE", "currentPage: " + currentPage);
        }

        String maxPage =
            "" + ((listSize / displayedListCardCount)
            + (listSize % displayedListCardCount > 0 ? 1 : 0));

        if (usingAllCards)
            return String.format(getString(R.string.allCatAndPageNum), currentPage, maxPage);
        else
            return String.format(getString(R.string.catAndPageNum), categoryName, currentPage, maxPage);
    }

    private void initDecks() {
        // Normal cards
        decks = new ArrayList<>();
        allCards = new ArrayList<>();
        CustomXmlResourceParser.parseCards(getResources(), R.xml.game_elements, decks, allCards);

        // Fundamentals
        fundamentals = new ArrayList<>();
        XmlResourceParser_Fundamentals.
            parseFundamentals(getResources(), R.xml.fundamentals, fundamentals);

        allCardsShuffled = new ArrayList<>(allCards);
        shuffleDeck(allCardsShuffled);
    }

    private void initCardSlots() {
        cardSlots = new ArrayList<>();
        int[] cardSlotTextViewIds = {
            R.id.cardSlot01,
            R.id.cardSlot02,
            R.id.cardSlot03,
            R.id.cardSlot04,
            R.id.cardSlot05,
            R.id.cardSlot06,
            R.id.cardSlot07,
            R.id.cardSlot08,
            R.id.cardSlot09,
            R.id.cardSlot10
        };

        for (int i = 0; i < cardSlotTextViewIds.length; i++) {
            CardSlot cardSlot = new CardSlot(
                i,
                (TextView) findViewById(cardSlotTextViewIds[i]),
                ContextCompat.getColor(this, R.color.colorLockBackground1),
                ContextCompat.getColor(this, R.color.colorLockBackground2));
            cardSlots.add(cardSlot);
        }

        mergeSlot = new CardSlot(
            0,
            (TextView) findViewById(cardSlotTextViewIds[0]),
            ContextCompat.getColor(this, R.color.colorAccent),
            ContextCompat.getColor(this, R.color.colorGreen));

        // Tip card for the merge slot
        tipCard = new Card(getString(R.string.mergeSlotTip_full), -1, getString(R.string.tip), getString(R.string.tip), -2);
        tipCard.setNameHalf(getString(R.string.mergeSlotTip_left), Card.NameHalfType.verb, Card.NameHalfType.singular);
        tipCard.setNameHalf(getString(R.string.mergeSlotTip_right), Card.NameHalfType.singular, null);
    }

    private void shuffleDeck(List<Card> deck) {
        double rand;
        Card temp;
        for (int i = 0; i < deck.size(); i++) {
            rand = Math.random();
            int randCardIndex = (int) (rand * deck.size());
            temp = deck.get(randCardIndex);
            deck.set(randCardIndex, deck.get(i));
            deck.set(i, temp);
        }
    }

    private void updateDisplayedDeck(int category) {
        displayedCards = allCards;

        boolean anyCategory = category < 0;
        if (displayMode != DisplayMode.list) {
            // In List mode, uses allCards deck with the start and end indexes
            // according to the used category

            if (displayMode == DisplayMode.fundamentals) {
                displayedCards = fundamentals;
            }
            else if (anyCategory) {
                displayedCards = allCardsShuffled;
            }
            else {
                displayedCards = decks.get(category);
            }
        }
    }

    private void drawCards(int category, boolean shownCardCountChanged) {
        if (category >= decks.size()) {
            Log.e("CAGE", "Invalid category ID: " + category);
            return;
        }

        updateDisplayedDeck(category);

        if (displayMode != DisplayMode.list && displayedCards != allCards) {
            shuffleDeck(displayedCards);

            if (displayMode == DisplayMode.fundamentalElem) {
                shuffleDeck(fundamentals);
            }
        }

        switch (displayMode) {
            case list:
                startOrNextPageInCardList(shownCardCountChanged);
                break;
            case mergeAlt:
                startOrUpdateMergeAltMode(displayedCards);
                break;
            default:
                setAllCardSlotTexts(displayedCards);
                break;
        }
    }

    private void setAllCardSlotTexts(List<Card> displayedCards) {
        for (int i = 0; i < cardSlots.size(); i++) {
            boolean emptySlot = i >= cardCountCap || i >= displayedCards.size();
            setCardSlotText(cardSlots.get(i), displayedCards, i, emptySlot);
        }

        nextCardInDeck = cardCountCap;
    }

    private void setCardSlotText(CardSlot cardSlot, List<Card> cards, int index, boolean empty) {
        // TODO: Possibility of adding a fundamental in any mode

        if (cardSlot.isLocked()) {
            return;
        }
        else if (empty) {
            cardSlot.clear(false);
            return;
        }

        cardSlot.setCards(cards.get(index), null);
        StringBuilder text = new StringBuilder();
        boolean mergeModeActive =
            displayMode == DisplayMode.merge || displayMode == DisplayMode.mergeAlt;
        //Log.d("CAGE", "Card1: " + cardSlot.card1.name);

        if (displayMode == DisplayMode.merge) {
            text.append(getMergeCardDisplayText(cardSlot, cards, index));
        }
        else if (displayMode == DisplayMode.mergeAlt) {
            cardSlot.setCards(mergeBeginning, cardSlot.card1);
            text.append(getMergeCardDisplayText(cardSlot));
        }
        else if (displayMode == DisplayMode.fundamentalElem) {
            text.append(String.format(getString(R.string.cardSlotFundElem),
                fundamentals.get(index).name.toUpperCase(),
                cardSlot.card1.name));
        }
        else {
            text.append(cardSlot.card1.name);
        }

        int categoryTagLength = 0;
        if (showCategories)
            categoryTagLength = insertCategoryTag(text, cardSlot, mergeModeActive);

        cardSlot.setText(text.toString(), categoryTagLength);
    }

    private void updateCardSlotText(CardSlot cardSlot) {
        if (cardSlot == null)
            return;

        StringBuilder text = new StringBuilder();
        boolean mergeElements = cardSlot.card2 != null;

        if (mergeElements) {
            text.append(getMergeCardDisplayText(cardSlot));
        }
        else if (displayMode == DisplayMode.fundamentalElem) {
            // TODO: Random fundamental, and only if the card slot had one before
            text.append(String.format(getString(R.string.cardSlotFundElem),
                fundamentals.get(0).name.toUpperCase(),
                cardSlot.card1.name));
        }
        else {
            text.append(cardSlot.card1.name);
        }

        int categoryTagLength = 0;
        if (showCategories)
            categoryTagLength = insertCategoryTag(text, cardSlot, mergeElements);

        cardSlot.setText(text.toString(), categoryTagLength);
    }

    private void drawNewCard(CardSlot cardSlot) {
        // Note: Skips one index (e.g. from 9 to 11) because
        // it's set here and not after setCardSlotText()
        nextCardInDeck++;
        if (nextCardInDeck >= displayedCards.size())
            nextCardInDeck = 0; // TODO: No card duplication

        setCardSlotText(cardSlot, displayedCards, nextCardInDeck, false);
    }

    private void showOrHideCategories(boolean showCategories) {
        if (mergeSlotEnabled)
            showOrHideCategory(mergeSlot, showCategories);

        for (CardSlot cardSlot : cardSlots) {
            showOrHideCategory(cardSlot, showCategories);
        }
    }

    private void showOrHideCategory(CardSlot cardSlot, boolean showCategories) {
        if (cardSlot.isEmpty())
            return;

        StringBuilder text = new StringBuilder(cardSlot.getText());

        int categoryTagLength = 0;
        if (showCategories)
            categoryTagLength = insertCategoryTag(text, cardSlot, cardSlot.card2 != null);
        else
            text.delete(0, cardSlot.getCategoryTagLength());

        cardSlot.setText(text.toString(), categoryTagLength);
    }

    private int insertCategoryTag(StringBuilder sb, CardSlot cardSlot, boolean mergedElements) {
        int categoryTagLength = 0;
        if (showCategories) {
            String categoryText = (mergedElements ?
                String.format(getString(R.string.cardSlotMergeCategory),
                    cardSlot.card1.categoryShortName, cardSlot.card2.categoryShortName)
                : String.format(getString(R.string.cardSlotCategory),
                    cardSlot.card1.categoryShortName))
                + " ";

            sb.insert(0, categoryText);
            categoryTagLength = categoryText.length();
        }

        return categoryTagLength;
    }

    private String getMergeCardDisplayText(CardSlot cardSlot,
                                           List<Card> cards,
                                           int index) {
        int index2 = index;
        while (index2 == index)
            index2 = (int) (Math.random() * cards.size());
        cardSlot.setCards(null, cards.get(index2));

        return getMergeCardDisplayText(cardSlot);
    }

    private String getMergeCardDisplayText(CardSlot cardSlot) {
        return String.format(getString(R.string.cardSlotMerge),
            cardSlot.card1.getNameHalf(true, null),
            cardSlot.card2.getNameHalf(false, cardSlot.card1.getSecondHalfPreference()));
    }

    private void updateFab2Alpha() {
        // This is needed because fab2 is really reluctant to keep its transparency if it's hidden
        fab2.setAlpha(0.8f);
        //Log.d("CAGE", "Fab2 transparency: " + fab2.getAlpha());
    }

    private void handleCardSlotTouch(int touchY) {
        addOrLockCard(touchY);
    }

    private void handleCardSlotLongTouch(int touchY) {
        int cardSlotIndex = getTouchedCardSlotIndex(touchY);
        if (mergeSlotEnabled && cardSlotIndex == 0) {
            enableMergeSlot(false);
        }
        else if (displayMode != DisplayMode.list) {
            if (cardSlotIndex >= 0 && cardSlotIndex < cardSlots.size())
                removeCard(cardSlotIndex);
        }
    }

    private void handleCardSlotSwipe(boolean right, int touchY) {
        CardSlot cardSlot = getTouchedCardSlot(touchY, false);
        if (cardSlot == null)
            return;

        boolean mergeModeActive =
            mergeSlotEnabled
            || displayMode == DisplayMode.merge
            || displayMode == DisplayMode.mergeAlt;

        if (mergeModeActive) {
            manualMerge(cardSlot, right);
        }

        //Log.d("CAGE", "Swiped " + right ? "right" : "left");
    }

    private void manualMerge(CardSlot mergeOrigin, boolean right) {
        if (right) {
            // Takes the merge origin's card2 and gives it to all unlocked card slots.
            // If merge slot is enabled, only it is changed.

            // If card2 is null, card1 is used instead
            Card card2 = mergeOrigin.card2 == null ? mergeOrigin.card1 : mergeOrigin.card2;

//            Log.d("CAGE", "Origin card1: " + mergeOrigin.card1 +
//                ", card2: " + mergeOrigin.card2);

            if (mergeSlotEnabled) {
                mergeSlot.setCards(mergeSlot.card1, card2);
                updateCardSlotText(mergeSlot);
            }
            else {
                for (CardSlot slot : cardSlots) {
                    if (!slot.isEmpty() && !slot.isLocked()) {
                        slot.setCards(slot.card1, card2);
                        updateCardSlotText(slot);
                    }
                }
            }

            Toast.makeText(this,
                String.format(getString(R.string.mergeSelected_rightSide), card2.getNameHalf(false, null)),
                Toast.LENGTH_SHORT)
                .show();
        }
        else {
            // Takes the merge origin's card1 and gives it to all unlocked card slots
            // If merge slot is enabled, only it is changed.

            mergeBeginning = mergeOrigin.card1;
            Card card2;

            if (mergeSlotEnabled) {
                card2 = mergeSlot.card2 == null ? mergeSlot.card1 : mergeSlot.card2;
                mergeSlot.setCards(mergeBeginning, card2);
                updateCardSlotText(mergeSlot);
            }
            else {
                for (CardSlot slot : cardSlots) {
                    if (!slot.isEmpty() && !slot.isLocked()) {
                        card2 = slot.card2 == null ? slot.card1 : slot.card2;
                        slot.setCards(mergeBeginning, card2);
                        updateCardSlotText(slot);
                    }
                }
            }

            Toast.makeText(this,
                String.format(getString(R.string.mergeSelected_leftSide), mergeBeginning.getNameHalf(true, null)),
                Toast.LENGTH_SHORT)
                .show();
        }
    }

    private int getTouchedCardSlotIndex(int touchY) {
        for (int i = 0; i < cardSlots.size(); i++) {
            if (cardSlots.get(i).touchHit(touchY))
                return i;
        }

        return -1;
    }

    private CardSlot getTouchedCardSlot(int touchY, boolean allowEmpty) {
        int index = getTouchedCardSlotIndex(touchY);
        if (index < 0 || (index == 0 && mergeSlotEnabled))
            return null;
        else if (allowEmpty || !cardSlots.get(index).isEmpty())
            return cardSlots.get(index);

        return null;
    }

    private int getFirstEmptyCardSlotIndex() {
        for (int i = 0; i < cardSlots.size(); i++) {
            if (i == 0 && mergeSlotEnabled)
                continue;

            if (cardSlots.get(i).isEmpty())
                return i;
        }

        return -1;
    }

    private CardSlot getFirstEmptyCardSlot() {
        int index = getFirstEmptyCardSlotIndex();
        if (index >= 0)
            return cardSlots.get(index);
        else
            return null;
    }

    private CardSlot getAvailableCardSlot(int touchY) {
        CardSlot cardSlot = getTouchedCardSlot(touchY, true);

        if (mergeSlotEnabled && cardSlot != null && cardSlot.id == mergeSlot.id)
            return null;

        CardSlot firstEmptySlot = getFirstEmptyCardSlot();

        // Touched nothing and there's an available card slot
        if (cardSlot == null && firstEmptySlot != null && touchY >= firstEmptySlot.getTop()) {
            cardSlot = firstEmptySlot;
        }
        // Touched an empty card slot
        else if (cardSlot != null && cardSlot.isEmpty()) {
            cardSlot = firstEmptySlot;
        }

        // Touched nothing and there aren't available card slots
        // or card count cap is reached
        if (cardSlot == null || (cardSlot.isEmpty() && cardSlot.id >= cardCountCap))
            return null;
        else
            // Otherwise touched an available (empty or non-empty) card slot
            return cardSlot;
    }

    private void addOrLockCard(int touchY) {
        CardSlot cardSlot = getAvailableCardSlot(touchY);
        if (cardSlot != null) {
            if (cardSlot.isEmpty() && displayMode != DisplayMode.list)
                drawNewCard(cardSlot);
            else if (!cardSlot.isEmpty())
                toggleCardLock(cardSlot);
        }
    }

    private void addOrSwitchCard(int touchY) {
        CardSlot cardSlot = getAvailableCardSlot(touchY);
        if (cardSlot != null)
            drawNewCard(cardSlot);
    }

    private void switchCard(int touchY) {
        CardSlot cardSlot = getTouchedCardSlot(touchY, false);
        if (cardSlot == null)
            return;

        //makeSnackbar(v, "Switched " + cardSlot.getText().toString());
        drawNewCard(cardSlot);
    }

    private void removeCardOrEditLock(int cardSlotIndex) {
        CardSlot cardSlot = cardSlots.get(cardSlotIndex);
        if (cardSlot.isLocked()) {
            cardSlot.enableSecondaryLock(!cardSlot.secondaryLockEnabled());
        }
        else {
            removeCard(cardSlotIndex);
        }
    }

    private void removeCard(int cardSlotIndex) {
        if (cardSlotIndex == cardSlots.size() - 1) {
            cardSlots.get(cardSlotIndex).clear(true);
        }
        else {
            for (int i = cardSlotIndex + 1; i < cardSlots.size(); i++) {
                if (cardSlots.get(i).isEmpty()) {
                    cardSlots.get(i - 1).clear(false);
                    break;
                }
                else {
                    cardSlots.get(i - 1).copyFrom(cardSlots.get(i));

                    if (i == cardSlots.size() - 1) {
                        cardSlots.get(i).clear(false);
                    }
                }
            }
        }
    }

    private void clearCards() {
        int clears = 0;
        for (int i = 0; i < cardSlots.size(); i++) {
            boolean cleared = cardSlots.get(i).clear(true);
            if (cleared) {
                clears++;
            }
            else if (clears > 0) {
                cardSlots.get(i - clears).copyFrom(cardSlots.get(i));
                cardSlots.get(i).clear(false);
            }
        }
    }

    private void sortCards() {
        if (lockedCardCount == 0
            || lockedCardCount == maxDisplayedCards
            || getFirstEmptyCardSlotIndex() >= lockedCardCount)
            return;

        int[] unlockedSlotIndexes = new int[10];
        short unlockedCards = 0;
        short unlockedCardsHandled = 0;

        //Log.d("CAGE", "Not sorted");

        for (int i = 0; i < cardSlots.size(); i++) {
            if (!cardSlots.get(i).isLocked()) {
                // Plus one because 0 means the index is unset
                unlockedSlotIndexes[unlockedCards] = i + 1;
                unlockedCards++;
            }
        }

        CardSlot temp = new CardSlot(-1,  new TextView(this), 0, 0);

        for (int i = 0; i < cardSlots.size(); i++) {
            if (cardSlots.get(i).isLocked()
                  && unlockedSlotIndexes[unlockedCardsHandled] > 0
                  && unlockedSlotIndexes[unlockedCardsHandled] - 1 < i) {
                int unlockedSlotIndex = unlockedSlotIndexes[unlockedCardsHandled] - 1;

                temp.copyFromLite(cardSlots.get(unlockedSlotIndex));
                cardSlots.get(unlockedSlotIndex).copyFrom(cardSlots.get(i));
                cardSlots.get(i).copyFrom(temp);

                unlockedCardsHandled++;
                if (unlockedCardsHandled >= lockedCardCount)
                    break;
            }
        }
    }

    private void moveCardsDown() {
        //if (cardSlots.get(0).isEmpty() || lockedCardCount >= cardCountCap)
        if (lockedCardCount >= cardCountCap)
            return;

        int lockedReached = 0;
        for (int i = cardSlots.size() - 1; i > 0; i--) {
            if (cardSlots.get(i).isLocked())
                lockedReached++;

            if (!cardSlots.get(i).isLocked() || i < cardSlots.size() - lockedReached)
                cardSlots.get(i).copyFrom(cardSlots.get(i - 1));
        }

        cardSlots.get(0).clear(false);
    }

    private void toggleCardLock(CardSlot cardSlot) {
        if (cardSlot != null) {
            if (cardSlot.isLocked() && !cardSlot.secondaryLockEnabled()) {
                cardSlot.enableSecondaryLock(true);
            }
            else {
                cardSlot.lock(!cardSlot.isLocked());
                if (cardSlot.isLocked())
                    lockedCardCount++;
                else
                    lockedCardCount--;

                locksChanged = true;
            }
        }
    }

    private void initListMode() {
        clearCards();

        deckStartIndex = 0;
        if (displayedCategory < 0) {
            listSize = allCards.size();
        }
        else {
            for (int i = 0; i < displayedCategory; i++) {
                deckStartIndex += decks.get(i).size();
            }

            listSize = decks.get(displayedCategory).size();
        }

        listModeJustStarted = true;
    }

    private void startOrNextPageInCardList(boolean shownCardCountChanged) {

        // Uses allCards deck with the start index and list size set in initListMode()

        boolean outOfCards =
            nextCardInDeck + cardCountCap - lockedCardCount >= deckStartIndex + listSize;

        if (listModeJustStarted) {
            //Log.d("CAGE", "LIST MODE. Just started");
            nextCardInDeck = deckStartIndex;
            listModeJustStarted = false;
            fab2.show();
            updateFab2Alpha();
            locksChanged = false;
        }
        else if (outOfCards) {
            //Log.d("CAGE", "LIST MODE. Out of cards");
            nextCardInDeck = deckStartIndex;
        }
        else if (!shownCardCountChanged) {
            //Log.d("CAGE", "LIST MODE. Next page");
            nextCardInDeck += cardCountCap - lockedCardCount;
        }

        //Log.d("CAGE", "LIST MODE. nextShownCardInList: " + nextShownCardInList +
        //      ". listStartIndex: " + listStartIndex +
        //      ". listSize: " + listSize);

        if (locksChanged) {
            sortCards();
            locksChanged = false;
        }

        updateHeaderInfoText();
        updateCardList();
    }

    private void prevPageInCardList() {
        boolean atListFirstElement = nextCardInDeck == deckStartIndex;
        int shownListCardCount = cardCountCap - lockedCardCount;

        nextCardInDeck -= shownListCardCount;
        if (nextCardInDeck < deckStartIndex) {
            nextCardInDeck = deckStartIndex;

            // Looping around to the last page
            if (atListFirstElement) {
                int pagesForward = listSize / shownListCardCount;
                if (listSize % shownListCardCount == 0)
                    pagesForward--;

                nextCardInDeck += pagesForward * shownListCardCount;
            }
        }

        if (locksChanged) {
            sortCards();
            locksChanged = false;
        }

        updateHeaderInfoText();
        updateCardList();
    }

    private void updateCardList() {
        int lockedCardsPassed = 0;

        for (int i = 0; i < cardSlots.size(); i++) {
            if (cardSlots.get(i).isLocked()) {
                lockedCardsPassed++;
                continue;
            }

            boolean emptySlot = i >= cardCountCap;
            int deckIndex = nextCardInDeck + i - lockedCardsPassed;

            if (deckIndex - deckStartIndex >= listSize) {
                // Out of cards
                emptySlot = true;
            }

            setCardSlotText(cardSlots.get(i), allCards, deckIndex, emptySlot);
        }

        // TODO: fab2 is not transparent before changing page if
        //  it had previously been hidden (outside onCreate()) and afterwards unhidden
        if (fab2.getAlpha() == 1f)
            updateFab2Alpha();
    }

    private void initMergeAltMode() {
        mergeAltModeJustStarted = true;
    }

    private void takeNewMergeAltBeginning() {
        int oldCardId = (mergeBeginning == null ? -1 : mergeBeginning.id);

        while (mergeBeginning == null || mergeBeginning.id == oldCardId) {
            int index = (int) (Math.random() * displayedCards.size());
            mergeBeginning = displayedCards.get(index);
        }
    }

    private void startOrUpdateMergeAltMode(List<Card> displayedCards) {
        if (mergeAltModeJustStarted) {
            mergeAltModeJustStarted = false;
            takeNewMergeAltBeginning();
            fab2.show();
        }

        setAllCardSlotTexts(displayedCards);
    }

    private void updateMergeSlot() {
        // TODO: Get rid of mergeSlot object and just use cardSlots.get(0)

        if (mergeSlotEnabled) {
            mergeSlot.copyFrom(cardSlots.get(0));
            mergeSlot.setCards(tipCard, null);
            moveCardsDown();
            updateCardSlotText(mergeSlot);
            cardSlots.get(0).lock(true);
            mergeSlot.setBackgroundColor(mergeSlotColor);
        }
        else {
            mergeSlot.lock(false);
            cardSlots.get(0).lock(false);
            cardSlots.get(0).copyFrom(mergeSlot);
            updateCardSlotText(cardSlots.get(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        MenuItem menuItem = menu.findItem(R.id.submenu_cardCount);
        menuItem.setTitle(String.format(getString(R.string.action_cardCount), cardCountCap));

        currentDisplayedDisplayModeItem = menu.findItem(R.id.action_setMode_classic);
        currentDisplayedDisplayModeItem.setEnabled(false);

        currentDisplayedCatItem = menu.findItem(R.id.action_displayAll);
        currentDisplayedCatItem.setEnabled(false);

        mergeSlotToggle = menu.findItem(R.id.action_mergeSlotToggle);
        mergeSlotToggle.setChecked(mergeSlotEnabled);

//        keepResultCategoriesToggle = menu.findItem(R.id.action_keepResultCategoriesToggle);
//        keepResultCategoriesToggle.setChecked(keepResultCategories);

        autoUpdateSettingChangesToggle = menu.findItem(R.id.action_autoUpdateSettingChangesToggle);
        autoUpdateSettingChangesToggle.setChecked(autoUpdateSettingChanges);

        showCategoriesToggle = menu.findItem(R.id.action_showCategoriesToggle);
        showCategoriesToggle.setChecked(showCategories);

        if (decks.size() > 0) {
            int[] displayCategories = new int[] {
                R.id.action_displayCategory0,
                R.id.action_displayCategory1,
                R.id.action_displayCategory2,
                R.id.action_displayCategory3,
                R.id.action_displayCategory4,
                R.id.action_displayCategory5,
                R.id.action_displayCategory6,
                R.id.action_displayCategory7,
                R.id.action_displayCategory8,
                R.id.action_displayCategory9,
                R.id.action_displayCategory10,
                R.id.action_displayCategory11,
                R.id.action_displayCategory12
            };

            for (int i = 0; i < displayCategories.length; i++) {
                menu.findItem(displayCategories[i]).
                    setTitle(String.format(getString(R.string.action_useCategory), getCategoryName(i, false)));
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_help) {
            goToHelp();
            return true;
        }
        else if (isSetModeId(id))
            return handleSetDisplayModeOptions(id, item);
        else if (isSetCardCountId(id))
            return handleSetCardCountOptions(id);
        else if (handleDisplayedCategoryOptions(id, item))
            return true;
        else if (isSetModeId(id))
            return handleSetDisplayModeOptions(id, item);
        else if (handleClearCardsAction(id))
            return true;
        else if (handleClearLocksAction(id))
            return true;
        else if (handleMergeSlotActivation(id))
            return true;
//        else if (handleKeepResultCategoriesActivation(id))
//            return true;
        else if (handleAutoUpdateSettingChangesActivation(id))
            return true;
        else if (handleShowCategoriesActivation(id))
            return true;

        return super.onOptionsItemSelected(item);
    }

    private boolean setDisplayedCardCount(boolean increase, int min, int max, MenuItem item) {
        if (increase) {
            cardCountCap++;
            if (cardCountCap > max) {
                cardCountCap = min;
            }
        }
        else {
            cardCountCap--;
            if (cardCountCap < min) {
                cardCountCap = max;
            }
        }

        item.setTitle(String.format(getString(R.string.action_cardCount), cardCountCap));
        return true;
    }

    private boolean setDisplayedCardCount(int value, int min, int max) {
        if (value >= min && value <= max) {
            cardCountCap = value;

            if (menu != null) {
                menu.findItem(R.id.submenu_cardCount).
                    setTitle(String.format(getString(R.string.action_cardCount), cardCountCap));
            }

            updateHeaderInfoText();

            if (autoUpdateSettingChanges || displayMode == DisplayMode.list)
                drawCards(displayedCategory, true);

            return true;
        }
        else {
            return false;
        }
    }

    private boolean setDisplayedCategory(MenuItem item, int categoryId) {
        if (categoryId < decks.size()) {
            displayedCategory = categoryId;

            updateHeaderInfoText();

            item.setEnabled(false);
            currentDisplayedCatItem.setEnabled(true);
            currentDisplayedCatItem = item;

            if (displayMode == DisplayMode.list) {
                initListMode();
                drawCards(displayedCategory, false);
            }
            else if (displayMode != DisplayMode.fundamentals) {
                if (autoUpdateSettingChanges) {
                    drawCards(displayedCategory, false);
                }
                else {
                    updateDisplayedDeck(displayedCategory);
                }
            }

            return true;
        }

        return false;
    }

    private boolean setDisplayMode(MenuItem item, DisplayMode mode) {

        // Hides fab2 only if the mode it's used in is ending
        if ((displayMode == DisplayMode.list && mode != DisplayMode.list)
            || (displayMode == DisplayMode.mergeAlt && mode != DisplayMode.mergeAlt))
            hideFab2();

        displayMode = mode;
        item.setEnabled(false);
        currentDisplayedDisplayModeItem.setEnabled(true);
        currentDisplayedDisplayModeItem = item;

        if (displayMode == DisplayMode.list) {
            // List mode updates the header info text when the cards are drawn
            initListMode();
        }
        else {
            // All other modes do it here
            updateHeaderInfoText();
        }

        if (displayMode == DisplayMode.mergeAlt) {
            initMergeAltMode();
        }

        drawCards(displayedCategory, false);

        return true;
    }

    private boolean isSetModeId(int id) {
        return id == R.id.action_setMode_classic
            || id == R.id.action_setMode_list
            || id == R.id.action_setMode_merge
            || id == R.id.action_setMode_mergeAlt
            || id == R.id.action_setMode_fundamentals
            || id == R.id.action_setMode_fundamentalElem;
    }

    private boolean isSetCardCountId(int id) {
        return id == R.id.action_setCardCount_1
            || id == R.id.action_setCardCount_2
            || id == R.id.action_setCardCount_3
            || id == R.id.action_setCardCount_4
            || id == R.id.action_setCardCount_5
            || id == R.id.action_setCardCount_6
            || id == R.id.action_setCardCount_7
            || id == R.id.action_setCardCount_8
            || id == R.id.action_setCardCount_9
            || id == R.id.action_setCardCount_10;
    }

    private boolean handleSetDisplayModeOptions(int id, MenuItem item) {
        switch (id) {
            case R.id.action_setMode_classic:
                return setDisplayMode(item, DisplayMode.classic);
            case R.id.action_setMode_list:
                return setDisplayMode(item, DisplayMode.list);
            case R.id.action_setMode_merge:
                return setDisplayMode(item, DisplayMode.merge);
            case R.id.action_setMode_mergeAlt:
                return setDisplayMode(item, DisplayMode.mergeAlt);
            case R.id.action_setMode_fundamentals:
                return setDisplayMode(item, DisplayMode.fundamentals);
            case R.id.action_setMode_fundamentalElem:
                return setDisplayMode(item, DisplayMode.fundamentalElem);
        }

        return false;
    }

    private boolean handleSetCardCountOptions(int id) {
        switch (id) {
            case R.id.action_setCardCount_1:
                return setDisplayedCardCount(1, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_2:
                return setDisplayedCardCount(2, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_3:
                return setDisplayedCardCount(3, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_4:
                return setDisplayedCardCount(4, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_5:
                return setDisplayedCardCount(5, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_6:
                return setDisplayedCardCount(6, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_7:
                return setDisplayedCardCount(7, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_8:
                return setDisplayedCardCount(8, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_9:
                return setDisplayedCardCount(9, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_10:
                return setDisplayedCardCount(10, minDisplayedCards, maxDisplayedCards);
        }

        return false;
    }

    private boolean handleDisplayedCategoryOptions(int id, MenuItem item) {
        switch (id) {
            case R.id.action_displayAll:
                return setDisplayedCategory(item, -1);
            case R.id.action_displayCategory0:
                return setDisplayedCategory(item, 0);
            case R.id.action_displayCategory1:
                return setDisplayedCategory(item, 1);
            case R.id.action_displayCategory2:
                return setDisplayedCategory(item, 2);
            case R.id.action_displayCategory3:
                return setDisplayedCategory(item, 3);
            case R.id.action_displayCategory4:
                return setDisplayedCategory(item, 4);
            case R.id.action_displayCategory5:
                return setDisplayedCategory(item, 5);
            case R.id.action_displayCategory6:
                return setDisplayedCategory(item, 6);
            case R.id.action_displayCategory7:
                return setDisplayedCategory(item, 7);
            case R.id.action_displayCategory8:
                return setDisplayedCategory(item, 8);
            case R.id.action_displayCategory9:
                return setDisplayedCategory(item, 9);
            case R.id.action_displayCategory10:
                return setDisplayedCategory(item, 10);
            case R.id.action_displayCategory11:
                return setDisplayedCategory(item, 11);
            case R.id.action_displayCategory12:
                return setDisplayedCategory(item, 12);
        }

        return false;
    }

    private boolean handleClearCardsAction(int id) {
        if (id == R.id.action_clearCards) {
            if (displayMode == DisplayMode.list) {
                updateCardList();
                updateHeaderInfoText();

                if (locksChanged) {
                    sortCards();
                    locksChanged = false;
                }
            }
            else {
                clearCards();
            }

            return true;
        }

        return false;
    }

    private boolean handleClearLocksAction(int id) {
        if (id == R.id.action_clearLocks) {
            for (int i = 0; i < cardSlots.size(); i++) {
                if (i > 0 || !mergeSlotEnabled)
                    cardSlots.get(i).lock(false);
            }

            lockedCardCount = 0;
            return true;
        }

        return false;
    }

    private boolean handleMergeSlotActivation(int id) {
        if (id == R.id.action_mergeSlotToggle) {
            enableMergeSlot(!mergeSlotEnabled);
            return true;
        }

        return false;
    }

    private void enableMergeSlot(boolean enable) {
        mergeSlotEnabled = enable;
        mergeSlotToggle.setChecked(mergeSlotEnabled);
        updateMergeSlot();
    }

//    private boolean handleKeepResultCategoriesActivation(int id) {
//        if (id == R.id.action_keepResultCategoriesToggle) {
//            keepResultCategories = !keepResultCategories;
//            keepResultCategoriesToggle.setChecked(keepResultCategories);
//            return true;
//        }
//
//        return false;
//    }

    private boolean handleAutoUpdateSettingChangesActivation(int id) {
        if (id == R.id.action_autoUpdateSettingChangesToggle) {
            autoUpdateSettingChanges = !autoUpdateSettingChanges;
            autoUpdateSettingChangesToggle.setChecked(autoUpdateSettingChanges);
            return true;
        }

        return false;
    }

    private boolean handleShowCategoriesActivation(int id) {
        if (id == R.id.action_showCategoriesToggle) {
            showCategories = !showCategories;
            showCategoriesToggle.setChecked(showCategories);
            showOrHideCategories(showCategories);
            return true;
        }

        return false;
    }
}
