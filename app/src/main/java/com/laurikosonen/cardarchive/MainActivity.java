package com.laurikosonen.cardarchive;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int minDisplayedCards = 1;
    private final int maxDisplayedCards = 10;
    private final int cardsToChooseFromAmount = 3;

    private Menu menu;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;
    private ProgressBar progressBar;
    private TextView categoryCardCountText;

    private List<List<Card>> decks;
    private List<Card> allCards;
    private List<Card> allCardsShuffled;
    //private List<Card> displayedDeck;
    private List<Card> chooseOneCards;
    private List<Card> chosenCards;
    private List<TextView> cardSlots;
    private int displayedCategory = -1;
    private int displayedCardCount = 10;
    private int nextShownCard = 0;
    private DisplayMode displayMode;
    private MenuItem currentDisplayedCatItem;
    private MenuItem currentDisplayedDisplayModeItem;
    private int[] textColors = new int[cardsToChooseFromAmount + 1];
    private boolean choosingActive;

    private enum DisplayMode {
        classic,
        list,
        splice,
        chooseOne
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDecks();
        initCardSlots();
        displayMode = DisplayMode.classic;
        displayCards(displayedCardCount, displayedCategory);

        textColors[0] = R.color.colorGray;
        textColors[1] = R.color.colorPrimary;
        textColors[2] = R.color.colorBlueLight;
        textColors[3] = R.color.colorBlue;

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // TODO: Have card count info in its own screen
        categoryCardCountText = (TextView) findViewById(R.id.cardCountText);
        updateCategoryCardCountText();

        fab1 = (FloatingActionButton) findViewById(R.id.fab_main);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!choosingActive) {
                    displayCards(displayedCardCount, displayedCategory);

                    // Snackbar example:
//                    Snackbar.make(view, String.format(getString(R.string.cardCountTip), displayedCards.size()), Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
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

    private void setTextColors(int colorId) {
        for (TextView text : cardSlots) {
            text.setTextColor(getResources().getColor(colorId));
        }
    }

    private void setTextColor(int textIndex, int colorId) {
        cardSlots.get(textIndex).setTextColor(getResources().getColor(colorId));
    }

    private void updateCategoryCardCountText() {
        int categoryCardCount = 0;
        if (displayedCategory < 0) {
            categoryCardCount = allCards.size();
        }
        else if (decks.size() > 0) {
            categoryCardCount = decks.get(displayedCategory).size();
        }

        categoryCardCountText.setText(
            String.format(getString(R.string.cardCountTip), categoryCardCount));
    }

    private void initDecks() {
        decks = new ArrayList<>();
        allCards = new ArrayList<>();
        CustomXmlResourceParser.parseCards(getResources(), R.xml.game_elements, decks, allCards);

        allCardsShuffled = new ArrayList<>(allCards);
        shuffleDeck(allCardsShuffled);

        chooseOneCards = new ArrayList<>(cardsToChooseFromAmount);
        chosenCards = new ArrayList<>();

//        for (Card card : allCardsShuffled){
//            Log.d("CAGE", "[" + card.categoryName + "] " + card.name);
//        }
//        for (Card card : allCards){
//            Log.d("CAGE", "[" + card.categoryName + "] " + card.name);
//        }
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

    private void displayCards(int shownCardCount, int category) {
        List<Card> displayedCards = allCards;

        boolean anyCategory = category < 0;
        if (anyCategory) {
            if (displayMode != DisplayMode.list) {
                displayedCards = allCardsShuffled;
            }
        }
        else if (category < decks.size()) {
            // TODO: In List mode, previously shuffled categories should be unshuffled
            displayedCards = decks.get(category);
        }
        else {
            Log.e("CAGE", "Invalid category ID: " + category);
            //return null;
            return;
        }

        if (shownCardCount > displayedCards.size()) {
            shownCardCount = displayedCards.size();
        }

        if (displayMode != DisplayMode.list) {
            shuffleDeck(displayedCards);
        }

        switch (displayMode) {
            case list:
                displayCardList(shownCardCount, displayedCards);
                break;
            case chooseOne:
                displayCardsToChooseFrom(displayedCards);
                break;
            default:
                setCardsSlotTexts(shownCardCount, displayedCards);
                break;
        }
    }

    private void setCardSlotText(int index, List<Card> cards, TextView cardSlot, boolean empty) {
        String text = "";

        if (!empty) {
            if (displayMode == DisplayMode.splice) {
                index = index * 2;
                if (index < cards.size() - 1) {
                    text = getCombineCardDisplayText(index, cards);
                }
            }
            else {
                Card card = cards.get(index);
                text = String.format(getString(R.string.cardSlot),
                    card.categoryShortName, card.name);
            }
        }

        cardSlot.setText(text);
    }

    private void setCardsSlotTexts(int shownCardCount, List<Card> displayedCards) {
        for (int i = 0; i < cardSlots.size(); i++) {
            boolean emptySlot = i >= shownCardCount || i >= displayedCards.size();
            setCardSlotText(i, displayedCards, cardSlots.get(i), emptySlot);
        }
    }

    private void displayCardList(int shownCardCount, List<Card> displayedCards) {
        boolean outOfCards = nextShownCard >= displayedCards.size();
        if (outOfCards) {
            nextShownCard = 0;
            outOfCards = false;
        }

        for (int i = 0; i < cardSlots.size(); i++) {
            boolean emptySlot = i >= shownCardCount;
            int deckIndex = nextShownCard + i;

            if (deckIndex >= displayedCards.size()) {
                emptySlot = true;
                outOfCards = true;
            }

            setCardSlotText(deckIndex, displayedCards, cardSlots.get(i), emptySlot);
        }

        if (outOfCards) {
            nextShownCard = 0;
        }
        else {
            nextShownCard += shownCardCount;
        }
    }

    private String getCombineCardDisplayText(int index, List<Card> cards) {
        Card card1 = cards.get(index);
        Card card2 = cards.get(index + 1);

        Card.NameHalfType secondHalfType = Card.NameHalfType.singular;
        if (card1.firstHalfType != null && card1.secondHalfPreference == null) {
            switch (card1.firstHalfType) {
                case verb:
                    secondHalfType = Card.NameHalfType.plural;
                    break;
                case adjective:
                    secondHalfType = Card.NameHalfType.plural;
                    break;
                case noun:
                    break;
            }
        }

        if (card1.secondHalfPreference != null) {
            switch (card1.secondHalfPreference) {
                case singular:
                    secondHalfType = Card.NameHalfType.singular;
                    break;
                case plural:
                    secondHalfType = Card.NameHalfType.plural;
                    break;
            }
        }

        return String.format(
            getString(R.string.cardSlotCombine),
            card1.categoryShortName,
            card2.categoryShortName,
            card1.getNameHalf(true, null),
            card2.getNameHalf(false, secondHalfType));
    }

    private void displayCardsToChooseFrom(List<Card> displayedCards) {
        if (cardsToChooseFromAmount <= displayedCards.size()) {
            if (chosenCards.size() == 0) {
                updateProgressBar(true, 0);
            }
            else if (chosenCards.size() >= displayedCardCount) {
                resetChooseOneMode(true);
                updateProgressBar(false, 0);
            }

            chooseOneCards.clear();
            choosingActive = true;
            fab2.show();
            fab3.show();

            setTextColor(0, textColors[1]);
            setTextColor(1, textColors[2]);
            setTextColor(2, textColors[3]);

            for (int i = 0; i < cardSlots.size(); i++) {
                boolean emptySlot = i >= cardsToChooseFromAmount;
                setCardSlotText(i, displayedCards, cardSlots.get(i), emptySlot);
                chooseOneCards.add(displayedCards.get(i));
            }
        }
    }

    private void chooseCard(int choice) {
        chosenCards.add(chooseOneCards.get(choice));

        setTextColors(textColors[0]);

        for (int i = 0; i < cardSlots.size(); i++) {
            boolean emptySlot = i >= chosenCards.size();
            setCardSlotText(i, chosenCards, cardSlots.get(i), emptySlot);
        }

        if (chosenCards.size() >= displayedCardCount) {
            resetChooseOneMode(true);
            updateProgressBar(false, displayedCardCount);
        }
        else {
            updateProgressBar(false, chosenCards.size());
            choosingActive = false;
            fab2.hide();
            fab3.hide();
        }
    }

    private void resetListMode() {
        nextShownCard = 0;
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
    private void updateProgressBar(boolean initialize, int progress) {
        Log.d("CAGE", "Updating progress: " + progress);
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
            progressBar.setProgress(progress);
        }
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
                    setTitle(String.format(getString(R.string.action_displayCategory), decks.get(i).get(0).categoryName));
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
                updateProgressBar(true, -1);
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

            // TODO: Change the displayed deck here (and also where the mode is set)
            // so the List mode could have unshuffled decks

            item.setEnabled(false);
            currentDisplayedCatItem.setEnabled(true);
            currentDisplayedCatItem = item;
            updateCategoryCardCountText();
            resetListMode();
            return true;
        }

        return false;
    }

    private boolean setDisplayMode(MenuItem item, DisplayMode mode) {
        displayMode = mode;
        item.setEnabled(false);
        currentDisplayedDisplayModeItem.setEnabled(true);
        currentDisplayedDisplayModeItem = item;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (isSetModeId(id)) {
            return handleSetDisplayModeOptions(id, item);
        }
        else if (isSetCardCountId(id)) {
            return handleSetCardCountOptions(id);
        }
        else {
            if (handleDisplayedCategoryOptions(id, item))
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isSetModeId(int id) {
        return id == R.id.action_setMode_classic
            || id == R.id.action_setMode_combine
            || id == R.id.action_setMode_list
            || id == R.id.action_setMode_chooseOne;
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
        if (displayMode == DisplayMode.list) {
            resetListMode();
        }
        else if (displayMode == DisplayMode.chooseOne) {
            resetChooseOneMode(false);
        }

        switch (id) {
            case R.id.action_setMode_classic: {
                return setDisplayMode(item, DisplayMode.classic);
            }
            case R.id.action_setMode_combine: {
                return setDisplayMode(item, DisplayMode.splice);
            }
            case R.id.action_setMode_list: {
                return setDisplayMode(item, DisplayMode.list);
            }
            case R.id.action_setMode_chooseOne: {
                return setDisplayMode(item, DisplayMode.chooseOne);
            }
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
