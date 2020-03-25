package com.laurikosonen.cardarchive;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int minDisplayedCards = 1;
    private final int maxDisplayedCards = 10;
    private final int cardsToChooseFromAmount = 3;

    private View mainView;
    private Menu menu;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;
    private ProgressBar progressBar;
    private TextView headerInfoText;

    private List<List<Card>> decks;
    private List<Card> allCards;
    private List<Card> allCardsShuffled;
    private List<Card> displayedCards;
    private List<Card> chooseOneCards;
    private List<Card> chosenCards;
    private List<Card> fundamentals;
    private List<TextView> cardSlots;
    private int displayedCategory = -1;
    private int displayedCardCount = 10;
    private int deckStartIndex = 0;
    private int nextCardInDeck = 0;
    private int listSize = 0;
    private DisplayMode displayMode;
    private MenuItem currentDisplayedCatItem;
    private MenuItem currentDisplayedDisplayModeItem;
    private int[] textColors = new int[cardsToChooseFromAmount + 1];
    private boolean listModeJustStarted;
    private boolean spliceAltModeJustStarted;
    private boolean choosingActive;

    private Card spliceBeginning;

    private enum DisplayMode {
        classic,
        list,
        splice,
        spliceAlt,
        chooseOne,
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
        displayCards(displayedCardCount, displayedCategory, false);

        textColors[0] = R.color.colorGray;
        textColors[1] = R.color.colorPrimary;
        textColors[2] = R.color.colorCyan;
        textColors[3] = R.color.colorGreen;

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        headerInfoText = (TextView) findViewById(R.id.pageNum);
        headerInfoText.setText(String.format(getString(R.string.allCatAndCardCount), "" + allCards.size()));

        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handleCardSlotTouch(y);
//                        Log.d("CAGE", "touched down");
                        break;
                    case MotionEvent.ACTION_MOVE:
//                        Log.d("CAGE", "moving: (" + x + ", " + y + ")");
                        break;
                    case MotionEvent.ACTION_UP:
//                        Log.d("CAGE", "touched up");
                        break;
                }

                return true;
            }
        });

        fab1 = (FloatingActionButton) findViewById(R.id.fab_main);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!choosingActive) {
                    displayCards(displayedCardCount, displayedCategory, false);
                }
                else {
                    chooseCard(0);
                }
            }
        });

        fab2 = (FloatingActionButton) findViewById(R.id.fab_secondary);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (displayMode == DisplayMode.chooseOne && choosingActive) {
                    chooseCard(1);
                }
                else if (displayMode == DisplayMode.list) {
                    prevPageInCardList();
                }
                else if (displayMode == DisplayMode.spliceAlt) {
                    takeNewSpliceAltBeginning();
                    displayCards(displayedCardCount, displayedCategory, false);
                }
            }
        });

        fab3 = (FloatingActionButton) findViewById(R.id.fab_tertiary);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (displayMode == DisplayMode.chooseOne && choosingActive) {
                    chooseCard(2);
                }
            }
        });

        fab2.hide();
        fab3.hide();
    }

    // Alternate way of checking touch. Not as powerful as OnTouchListener.
    // The other necessary part is in content_main: android:onClick="onTouch
    //public void onTouch(View view) {
    //}

    private int getTouchedCardSlotIndex(int touchY) {
        for (int i = 0; i < cardSlots.size(); i++) {
            // The y-coordinate increases when going down and vice versa
            if (touchY >= cardSlots.get(i).getTop() && touchY <= cardSlots.get(i).getBottom())
                return i;
        }

        return -1;
    }

    private TextView getTouchedCardSlot(int touchY, boolean allowEmpty) {
        int index = getTouchedCardSlotIndex(touchY);
        if (index < 0)
            return null;
        else if (allowEmpty || cardSlots.get(index).length() > 0)
            return cardSlots.get(index);

        return null;
    }

    private TextView getFirstEmptyCardSlot(int y) {
        for (TextView cardSlot : cardSlots) {
            if (cardSlot.getText().length() == 0)
                return cardSlot;
        }

        return null;
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
        startActivity(i);
    }

    private String getCategoryName(int categoryIndex, boolean shortName) {
        if (shortName)
            return decks.get(categoryIndex).get(0).categoryShortName;
        else
            return decks.get(categoryIndex).get(0).categoryName;
    }

    private void setTextColors(int colorId) {
        for (TextView text : cardSlots) {
            text.setTextColor(getResources().getColor(colorId));
        }
    }

    private void setTextColor(int textIndex, int colorId) {
        cardSlots.get(textIndex).setTextColor(getResources().getColor(colorId));
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
            // TODO: If the displayed card count is changed in a specific way,
            //  if shows the second page as the first. Fix this.

            String currentPage = "1";
            if (nextCardInDeck > deckStartIndex)
                currentPage = "" + ((int)(0.5f + ((nextCardInDeck - deckStartIndex) / displayedCardCount)) + 1);

            String maxPage = "" + ((listSize / displayedCardCount) + (listSize % displayedCardCount > 0 ? 1 : 0));

            if (usingAllCards)
                newString = String.format(getString(R.string.allCatAndPageNum), currentPage, maxPage);
            else
                newString = String.format(getString(R.string.catAndPageNum), categoryName, currentPage, maxPage);
        }
        else if (displayMode == DisplayMode.chooseOne) {
            String currentSelection = "" + chosenCards.size();
            String maxSelection = "" + displayedCardCount;

            if (usingAllCards)
                newString = String.format(getString(R.string.allCatAndSelNum), currentSelection, maxSelection);
            else
                newString = String.format(getString(R.string.catAndSelNum), categoryName, currentSelection, maxSelection);
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

        chooseOneCards = new ArrayList<>(cardsToChooseFromAmount);
        chosenCards = new ArrayList<>();
    }

    private void initCardSlots() {
        cardSlots = new ArrayList<>();
        cardSlots.add((TextView) findViewById(R.id.cardSlot01));
        cardSlots.add((TextView) findViewById(R.id.cardSlot02));
        cardSlots.add((TextView) findViewById(R.id.cardSlot03));
        cardSlots.add((TextView) findViewById(R.id.cardSlot04));
        cardSlots.add((TextView) findViewById(R.id.cardSlot05));
        cardSlots.add((TextView) findViewById(R.id.cardSlot06));
        cardSlots.add((TextView) findViewById(R.id.cardSlot07));
        cardSlots.add((TextView) findViewById(R.id.cardSlot08));
        cardSlots.add((TextView) findViewById(R.id.cardSlot09));
        cardSlots.add((TextView) findViewById(R.id.cardSlot10));
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

    private void displayCards(int shownCardCount, int category, boolean shownCardCountChanged) {
        if (category >= decks.size()) {
            Log.e("CAGE", "Invalid category ID: " + category);
            return;
        }

        updateDisplayedDeck(category);

        // Checks if the amount of cards we want to show is too large
        // (more than there are cards to show)
        if (shownCardCount > displayedCards.size()) {
            shownCardCount = displayedCards.size();
        }

        if (displayMode != DisplayMode.list && displayedCards != allCards) {
            shuffleDeck(displayedCards);

            if (displayMode == DisplayMode.fundamentalElem) {
                shuffleDeck(fundamentals);
            }
        }

        switch (displayMode) {
            case list:
                startOrNextPageInCardList(shownCardCount, shownCardCountChanged);
                break;
            case spliceAlt:
                startOrUpdateSpliceAltMode(displayedCards, shownCardCount);
                break;
            case chooseOne:
                displayChooseOneCards(displayedCards);
                break;
            default:
                setAllCardSlotTexts(displayedCards, shownCardCount);
                break;
        }
    }

    private void setAllCardSlotTexts(List<Card> displayedCards, int shownCardCount) {
        for (int i = 0; i < cardSlots.size(); i++) {
            boolean emptySlot = i >= shownCardCount || i >= displayedCards.size();
            setCardSlotText(cardSlots.get(i), displayedCards, i, emptySlot);
        }

        nextCardInDeck = displayedCardCount + 1;
    }

    private void setCardSlotText(TextView cardSlot, List<Card> cards, int index, boolean empty) {
        String text = "";

        if (!empty) {
            if (displayMode == DisplayMode.splice) {
                // Index is doubled because every other card in the deck
                // is used as the end part of the splice
                index = index * 2;
                if (index < cards.size() - 1) {
                    text = getSpliceCardDisplayText(index, cards);
                }
            }
            else if (displayMode == DisplayMode.spliceAlt) {
                // Index must be one larger, otherwise the beginning part's card is used twice;
                // see takeNewSpliceAltBeginning()
                text = getSpliceAltCardDisplayText(index + 1, cards);
            }
            else if (displayMode == DisplayMode.fundamentalElem) {
                Card fundamental = fundamentals.get(index);
                Card card = cards.get(index);
                text = String.format(getString(R.string.cardSlotFundElem),
                    card.categoryShortName, fundamental.name.toUpperCase(), card.name);
            }
            else {
                Card card = cards.get(index);
                text = String.format(getString(R.string.cardSlot),
                    card.categoryShortName, card.name);
            }
        }

        cardSlot.setText(text);
    }

    private void updateFab2Alpha() {
        // This is needed because fab2 is really reluctant to keep its transparency if it's hidden
        fab2.setAlpha(0.8f);
        //Log.d("CAGE", "Fab2 transparency: " + fab2.getAlpha());
    }

    private void handleCardSlotTouch(int touchY) {
        // TODO: Interesting things with selecting individual cards;
        //  maybe use getFirstEmptyCardSlot()

        if (displayMode == DisplayMode.chooseOne) {
            if (choosingActive) {
                int chosenCardNum = getTouchedCardSlotIndex(touchY);
                if (chosenCardNum >= 0 && chosenCardNum <= 2) {
                    chooseCard(chosenCardNum);
                }
            }
        }
        else if (displayMode == DisplayMode.classic) {
            addOrSwitchCard(touchY);
        }
        else if (displayMode != DisplayMode.list) {
            switchCard(touchY);
        }
    }

    private void addOrSwitchCard(int touchY) {
        TextView cardSlot = getTouchedCardSlot(touchY, true);
        TextView firstEmptySlot = getFirstEmptyCardSlot(touchY);

        if (cardSlot == null && firstEmptySlot != null && touchY >= firstEmptySlot.getTop()) {
            cardSlot = firstEmptySlot;
        }
        else if (cardSlot != null && cardSlot.length() == 0) {
            cardSlot = firstEmptySlot;
        }
        else if (cardSlot == null)
            return;

        drawNewCard(cardSlot);
    }

    private void switchCard(int touchY) {
        TextView cardSlot = getTouchedCardSlot(touchY, false);
        if (cardSlot == null)
            return;

        //makeSnackbar(v, "Switched " + cardSlot.getText().toString());
        drawNewCard(cardSlot);
    }

    private void drawNewCard(TextView cardSlot) {
        nextCardInDeck++;
        if (nextCardInDeck >= displayedCards.size())
            nextCardInDeck = 0; // TODO: No card duplication

        setCardSlotText(cardSlot, displayedCards, nextCardInDeck, false);
    }

    private void initListMode() {
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

    private void startOrNextPageInCardList(int shownCardCount, boolean shownCardCountChanged) {

        // Uses allCards deck with the start index and list size set in initListMode()

        boolean outOfCards = nextCardInDeck + shownCardCount >= deckStartIndex + listSize;
        if (listModeJustStarted) {
            //Log.d("CAGE", "LIST MODE. Just started");
            nextCardInDeck = deckStartIndex;
            listModeJustStarted = false;
            fab2.show();
            updateFab2Alpha();
        }
        else if (outOfCards) {
            //Log.d("CAGE", "LIST MODE. Out of cards");
            nextCardInDeck = deckStartIndex;
        }
        else if (!shownCardCountChanged) {
            //Log.d("CAGE", "LIST MODE. Next page");
            nextCardInDeck += shownCardCount;
        }

        //Log.d("CAGE", "LIST MODE. nextShownCardInList: " + nextShownCardInList +
        //      ". listStartIndex: " + listStartIndex +
        //      ". listSize: " + listSize);

        updateHeaderInfoText();
        updateCardList(shownCardCount);
    }

    private void prevPageInCardList() {
        boolean atListFirstElement = nextCardInDeck == deckStartIndex;

        nextCardInDeck -= displayedCardCount;
        if (nextCardInDeck < deckStartIndex) {
            nextCardInDeck = deckStartIndex;

            // Looping around to the last page
            if (atListFirstElement) {
                int pagesForward = listSize / displayedCardCount;
                if (listSize % displayedCardCount == 0)
                    pagesForward--;

                nextCardInDeck += pagesForward * displayedCardCount;
            }
        }

        updateHeaderInfoText();
        updateCardList(displayedCardCount);
    }

    private void updateCardList(int shownCardCount) {
        for (int i = 0; i < cardSlots.size(); i++) {
            boolean emptySlot = i >= shownCardCount;
            int deckIndex = nextCardInDeck + i;

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

    private String getSpliceCardDisplayText(int index, List<Card> cards) {
        Card card1 = cards.get(index);
        Card card2 = cards.get(index + 1);
        Card.NameHalfType secondHalfPreference = getSecondHalfPreference(card1);

        return String.format(
            getString(R.string.cardSlotSplice),
            card1.categoryShortName,
            card2.categoryShortName,
            card1.getNameHalf(true, null),
            card2.getNameHalf(false, secondHalfPreference));
    }

    private Card.NameHalfType getSecondHalfPreference(Card card) {
        Card.NameHalfType secondHalfPreference = Card.NameHalfType.singular;

        if (card.secondHalfPreference != null) {
            secondHalfPreference = card.secondHalfPreference;
        }
        else if (card.firstHalfType != null) {
            switch (card.firstHalfType) {
                case verb:
                case adjective:
                    secondHalfPreference = Card.NameHalfType.plural;
                    break;
                case noun:
                    break;
            }
        }

        return secondHalfPreference;
    }

    private void initSpliceAltMode() {
        spliceAltModeJustStarted = true;
    }

    private void takeNewSpliceAltBeginning() {
        spliceBeginning = displayedCards.get(0);
    }

    private void startOrUpdateSpliceAltMode(List<Card> displayedCards, int shownCardCount) {
        if (spliceAltModeJustStarted) {
            spliceAltModeJustStarted = false;
            takeNewSpliceAltBeginning();
            fab2.show();
        }

        setAllCardSlotTexts(displayedCards, shownCardCount);
    }

    private String getSpliceAltCardDisplayText(int index, List<Card> cards) {
        Card card = cards.get(index);
        Card.NameHalfType secondHalfPreference = getSecondHalfPreference(spliceBeginning);

        return String.format(
            getString(R.string.cardSlotSplice),
            spliceBeginning.categoryShortName,
            card.categoryShortName,
            spliceBeginning.getNameHalf(true, null),
            card.getNameHalf(false, secondHalfPreference));
    }

    private void displayChooseOneCards(List<Card> displayedCards) {
        if (cardsToChooseFromAmount <= displayedCards.size()) {
            if (chosenCards.size() == 0) {
                updateProgressBar(true, false, 0);
            }
            else if (chosenCards.size() >= displayedCardCount) {
                finishChooseOneMode(0);
            }

            chooseOneCards.clear();
            choosingActive = true;
            fab2.show();
            fab3.show();

            setTextColor(0, textColors[1]);
            setTextColor(1, textColors[2]);
            setTextColor(2, textColors[3]);

            // Sets up three cards from which the user can choose one
            for (int i = 0; i < cardSlots.size(); i++) {
                boolean emptySlot = i >= cardsToChooseFromAmount;
                setCardSlotText(cardSlots.get(i), displayedCards, i, emptySlot);
                chooseOneCards.add(displayedCards.get(i));
            }
        }
    }

    private void chooseCard(int choice) {
        chosenCards.add(chooseOneCards.get(choice));
        setTextColors(textColors[0]);

        if (chosenCards.size() >= displayedCardCount) {
            finishChooseOneMode(displayedCardCount);
        }
        else {
            updateProgressBar(false, false, chosenCards.size());

            // Choosing continues until all choices have been made
            displayCards(displayedCardCount, displayedCategory, false);
        }
    }

    private void finishChooseOneMode(int chosenCardCount) {
        if (chosenCardCount > 0) {
            // Shows the chosen cards
            for (int i = 0; i < cardSlots.size(); i++) {
                boolean emptySlot = i >= chosenCards.size();
                setCardSlotText(cardSlots.get(i), chosenCards, i, emptySlot);
            }
        }

        updateProgressBar(false, true, 0);
        resetChooseOneMode(true);
    }

    private void resetChooseOneMode(boolean keepProgressBarVisible) {
        choosingActive = false;
        fab2.hide();
        fab3.hide();
        chooseOneCards.clear();
        chosenCards.clear();
        setTextColors(textColors[0]);

        if (!keepProgressBarVisible)
            setProgressBarActive(false);
    }

    @TargetApi(26)
    private void updateProgressBar(boolean initialize, boolean finish, int progress) {
        //Log.d("CAGE", "Updating progress: " + progress);
        if (initialize) {
            progressBar.setMin(0);
            progressBar.setMax(displayedCardCount);

            if (progress == -1)
                progress = progressBar.getProgress();

            if (progress > displayedCardCount)
                progress = displayedCardCount;

            progressBar.setProgress(progress);
            setProgressBarActive(true);
        } else {
            if (finish) {
                progressBar.setProgress(progressBar.getMax());
            }
            else {
                progressBar.setProgress(progress);
            }
        }

        updateHeaderInfoText();
    }

    @TargetApi(26)
    private void setProgressBarActive(boolean active) {
        int visibility = View.INVISIBLE;
        if (active)
            visibility = View.VISIBLE;

        progressBar.setVisibility(visibility);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        MenuItem menuItem = menu.findItem(R.id.submenu_cardCount);
        menuItem.setTitle(String.format(getString(R.string.action_cardCount), displayedCardCount));

        currentDisplayedDisplayModeItem = menu.findItem(R.id.action_setMode_classic);
        currentDisplayedDisplayModeItem.setEnabled(false);

        currentDisplayedCatItem = menu.findItem(R.id.action_displayAll);
        currentDisplayedCatItem.setEnabled(false);

        if (decks.size() > 0) {
            int[] displayCategories = new int[]
                {
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

    private boolean setDisplayedCardCount(boolean increase, int min, int max, MenuItem item) {
        if (increase) {
            displayedCardCount++;
            if (displayedCardCount > max) {
                displayedCardCount = min;
            }
        }
        else {
            displayedCardCount--;
            if (displayedCardCount < min) {
                displayedCardCount = max;
            }
        }

        item.setTitle(String.format(getString(R.string.action_cardCount), displayedCardCount));
        return true;
    }

    private boolean setDisplayedCardCount(int value, int min, int max) {
        if (value >= min && value <= max) {
            displayedCardCount = value;

            if (menu != null) {
                menu.findItem(R.id.submenu_cardCount).
                    setTitle(String.format(getString(R.string.action_cardCount), displayedCardCount));
            }

            if (displayMode == DisplayMode.chooseOne) {
                if (choosingActive) {
                    if (displayedCardCount <= chosenCards.size())
                        finishChooseOneMode(displayedCardCount);
                    else
                        updateProgressBar(true, false, -1);
                }
            }
            else {
                updateHeaderInfoText();
                displayCards(displayedCardCount, displayedCategory, true);
            }

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

            if (displayMode == DisplayMode.list)
                initListMode();

            if (displayMode != DisplayMode.fundamentals)
                displayCards(displayedCardCount, displayedCategory, false);

            return true;
        }

        return false;
    }

    private boolean setDisplayMode(MenuItem item, DisplayMode mode) {

        // Hides fab2 only if the mode it's used in is ending
        if ((displayMode == DisplayMode.list && mode != DisplayMode.list)
            || (displayMode == DisplayMode.spliceAlt && mode != DisplayMode.spliceAlt))
            hideFab2();

        displayMode = mode;
        item.setEnabled(false);
        currentDisplayedDisplayModeItem.setEnabled(true);
        currentDisplayedDisplayModeItem = item;

        if (displayMode == DisplayMode.list) {
            // List mode updates the header info text when the cards are drawn
            initListMode();
        }
        else if (displayMode == DisplayMode.spliceAlt) {
            // Splice Alt mode updates the header info text differently
            initSpliceAltMode();
        }
        else {
            // All other modes do it here
            updateHeaderInfoText();
        }

        displayCards(displayedCardCount, displayedCategory, false);

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
        else if (isSetModeId(id)) {
            return handleSetDisplayModeOptions(id, item);
        }
        else if (isSetCardCountId(id)) {
            return handleSetCardCountOptions(id);
        }
        else if (handleDisplayedCategoryOptions(id, item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isSetModeId(int id) {
        return id == R.id.action_setMode_classic
            || id == R.id.action_setMode_list
            || id == R.id.action_setMode_splice
            || id == R.id.action_setMode_spliceAlt
            || id == R.id.action_setMode_chooseOne
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
        if (displayMode == DisplayMode.chooseOne) {
            resetChooseOneMode(false);
        }

        switch (id) {
            case R.id.action_setMode_classic:
                return setDisplayMode(item, DisplayMode.classic);
            case R.id.action_setMode_list:
                return setDisplayMode(item, DisplayMode.list);
            case R.id.action_setMode_splice:
                return setDisplayMode(item, DisplayMode.splice);
            case R.id.action_setMode_spliceAlt:
                return setDisplayMode(item, DisplayMode.spliceAlt);
            case R.id.action_setMode_chooseOne:
                return setDisplayMode(item, DisplayMode.chooseOne);
            case R.id.action_setMode_fundamentals:
                return setDisplayMode(item, DisplayMode.fundamentals);
            case R.id.action_setMode_fundamentalElem:
                return setDisplayMode(item, DisplayMode.fundamentalElem);
        }

        return false;
    }

    private boolean handleSetCardCountOptions(int id) {
        switch (id) {
            case R.id.action_setCardCount_1: {
                return setDisplayedCardCount(1, minDisplayedCards, maxDisplayedCards);
            }
            case R.id.action_setCardCount_2: {
                return setDisplayedCardCount(2, minDisplayedCards, maxDisplayedCards);
            }
            case R.id.action_setCardCount_3: {
                return setDisplayedCardCount(3, minDisplayedCards, maxDisplayedCards);
            }
            case R.id.action_setCardCount_4: {
                return setDisplayedCardCount(4, minDisplayedCards, maxDisplayedCards);
            }
            case R.id.action_setCardCount_5: {
                return setDisplayedCardCount(5, minDisplayedCards, maxDisplayedCards);
            }
            case R.id.action_setCardCount_6: {
                return setDisplayedCardCount(6, minDisplayedCards, maxDisplayedCards);
            }
            case R.id.action_setCardCount_7: {
                return setDisplayedCardCount(7, minDisplayedCards, maxDisplayedCards);
            }
            case R.id.action_setCardCount_8: {
                return setDisplayedCardCount(8, minDisplayedCards, maxDisplayedCards);
            }
            case R.id.action_setCardCount_9: {
                return setDisplayedCardCount(9, minDisplayedCards, maxDisplayedCards);
            }
            case R.id.action_setCardCount_10: {
                return setDisplayedCardCount(10, minDisplayedCards, maxDisplayedCards);
            }
        }

        return false;
    }

    private boolean handleDisplayedCategoryOptions(int id, MenuItem item) {
        switch (id) {
            case R.id.action_displayAll: {
                return setDisplayedCategory(item, -1);
            }
            case R.id.action_displayCategory0: {
                return setDisplayedCategory(item, 0);
            }
            case R.id.action_displayCategory1: {
                return setDisplayedCategory(item, 1);
            }
            case R.id.action_displayCategory2: {
                return setDisplayedCategory(item, 2);
            }
            case R.id.action_displayCategory3: {
                return setDisplayedCategory(item, 3);
            }
            case R.id.action_displayCategory4: {
                return setDisplayedCategory(item, 4);
            }
            case R.id.action_displayCategory5: {
                return setDisplayedCategory(item, 5);
            }
            case R.id.action_displayCategory6: {
                return setDisplayedCategory(item, 6);
            }
            case R.id.action_displayCategory7: {
                return setDisplayedCategory(item, 7);
            }
            case R.id.action_displayCategory8: {
                return setDisplayedCategory(item, 8);
            }
            case R.id.action_displayCategory9: {
                return setDisplayedCategory(item, 9);
            }
            case R.id.action_displayCategory10: {
                return setDisplayedCategory(item, 10);
            }
            case R.id.action_displayCategory11: {
                return setDisplayedCategory(item, 11);
            }
            case R.id.action_displayCategory12: {
                return setDisplayedCategory(item, 12);
            }
        }

        return false;
    }
}
