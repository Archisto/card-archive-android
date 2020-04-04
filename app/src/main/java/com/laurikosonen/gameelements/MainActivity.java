package com.laurikosonen.gameelements;

import android.content.Context;
import android.content.Intent;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int minDisplayedCards = 1;
    private final int maxDisplayedCards = 10;
    private final float maxTouchDuration = 350;
    private final float buttonAlpha = 0.6f;

    private View mainView;
    private Menu optionsMenu;
    private Menu cardSlotContextMenu;
    private FloatingActionButton fab;
    private Button nextButton;
    private Button prevButton;
    private TextView headerInfoText;

    private List<List<Card>> decks;
    private List<Card> allCards;
    private List<Card> allCardsShuffled;
    private List<Card> displayedCards;
    private List<Card> fundamentals;
    private List<CardSlot> cardSlots;
    private int displayedCategory = -1;
    private int userSetCardCount = 10;
    private int deckStartIndex = 0;
    private int nextCardInDeck = 0;
    private int listSize = 0;
    private int touchStartX;
    private int touchStartY;
    private int lockedCardCount;
    private int selectedCardSlotIndex;
    private float touchStartTime;
    private DisplayMode displayMode;
    private MenuItem currentDisplayedCatItem;
    private MenuItem currentDisplayedDisplayModeItem;
    private MenuItem mergeSlotToggle;
    //private MenuItem keepResultCategoriesToggle;
    private MenuItem autoSortWhenLockingToggle;
    private MenuItem autoUpdateSettingChangesToggle;
    private MenuItem addFundamentalsToggle;
    private MenuItem showCategoryTagsToggle;
    //private int mainTextColor;
    private int mergeSlotColor;
    private boolean listModeJustStarted;
    private boolean mergeSlotEnabled;
    private boolean addFundamentalsEnabled;
    private boolean showCategoryTags = true;
    //private boolean keepResultCategories;
    private boolean autoSortWhenLocking;
    private boolean autoUpdateSettingChanges = true;
    private boolean locksChanged;
    private boolean touching;
    private boolean touchHandled;
    private boolean openingCardSlotContextMenu;

    CardSlot tempSlot;
    private Card mergeBeginning;
    private Card tipCard;
    private Card copiedFundamental;

    private enum DisplayMode {
        classic,
        list,
        merge,
        fundamentals
    }

    //    private void makeSnackbar(View view, String text) {
    //        Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
    //            .setAction("Action", null).show();
    //    }

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

        initTouch();

        fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawCards(displayedCategory, false);
            }
        });

        nextButton = (Button) findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOrNextPageInCardList(false);
            }
        });

        prevButton = (Button) findViewById(R.id.button_prev);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevPageInCardList();
            }
        });

        nextButton.setAlpha(buttonAlpha);
        prevButton.setAlpha(buttonAlpha);
        enableNextAndPrevButtons(false);

        registerForContextMenu(mainView);
        registerForContextMenu(fab);
    }

    private void openCardSlotContextMenu(int cardSlotIndex) {
        selectedCardSlotIndex = cardSlotIndex;
        openingCardSlotContextMenu = true;
        openContextMenu(mainView);
        openingCardSlotContextMenu = false;
    }

    public void onCreateContextMenu(ContextMenu menu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //MenuCompat.setGroupDividerEnabled(menu, true);

        if (openingCardSlotContextMenu)
            createCardSlotContextMenu(menu);
        else
            createFabContextMenu(menu);
    }

    private void createCardSlotContextMenu(ContextMenu menu) {
        menu.setHeaderTitle(cardSlots.get(selectedCardSlotIndex).getText());
        getMenuInflater().inflate(R.menu.contextmenu_cardslot, menu);
        cardSlotContextMenu = menu;

        if (isMergeSlot(selectedCardSlotIndex)) {
            menu.findItem(R.id.action_createCopy).setVisible(true);
            menu.findItem(R.id.action_disableMergeSlot).setVisible(true);
            menu.findItem(R.id.action_lockOrUnlock).setVisible(false);
            menu.findItem(R.id.action_remove).setVisible(false);
            menu.findItem(R.id.action_moveToTop).setVisible(false);
            menu.findItem(R.id.action_moveUp).setVisible(false);
            menu.findItem(R.id.action_moveDown).setVisible(false);
        }
        else if (cardSlots.get(selectedCardSlotIndex).isLocked()) {
            menu.findItem(R.id.action_lockOrUnlock).
                setTitle(getString(R.string.action_unlock));
        }
        else {
            menu.findItem(R.id.action_lockOrUnlock).
                setTitle(getString(R.string.action_lock));
        }

        if (cardSlots.get(selectedCardSlotIndex).card2 == null) {
            menu.findItem(R.id.action_undoMergeLeft).setVisible(false);
            menu.findItem(R.id.action_undoMergeRight).setVisible(false);
        }

        initFundamentalCopyMenuItems(menu);

        if (!canMoveUp(selectedCardSlotIndex)) {
            menu.findItem(R.id.action_moveToTop).setEnabled(false);
            menu.findItem(R.id.action_moveUp).setEnabled(false);
        }

        if (!canMoveDown(selectedCardSlotIndex))
            menu.findItem(R.id.action_moveDown).setEnabled(false);

        //Log.d("CAGE", "Card slot context menu created");
    }

    private void createFabContextMenu(ContextMenu menu) {
        menu.setHeaderTitle(getString(R.string.generate));
        getMenuInflater().inflate(R.menu.contextmenu_generate, menu);

        int possibleCardIncrease = getEmptyCardSlotCount();
        if (possibleCardIncrease > 0) {
            menu.findItem(R.id.action_add).
                setTitle(String.format(getString(R.string.action_add), Math.min(userSetCardCount, possibleCardIncrease)));
        }
        else {
            menu.findItem(R.id.action_add).
                setTitle(String.format(getString(R.string.action_add), userSetCardCount));
            menu.findItem(R.id.action_add).setEnabled(false);
        }

        //Log.d("CAGE", "FAB context menu created");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //Log.d("CAGE", "Selected cardSlot at index " + selectedCardSlotIndex);
        //Log.d("CAGE", "Item selected: " + item);

        CardSlot selectedCardSlot = cardSlots.get(selectedCardSlotIndex);

        boolean done = onContextItemSelectedFab(item, selectedCardSlot);
        if (!done) done = onContextItemSelectedCardSlot(item, selectedCardSlot);
        return done || super.onContextItemSelected(item);
    }

    private boolean onContextItemSelectedFab(MenuItem item, CardSlot selectedCardSlot) {
        switch (item.getItemId()) {
            case R.id.action_replace:
                drawCards(displayedCategory, false);
                return true;
            case R.id.action_add:
                for (int i = 0; i < userSetCardCount; i++)
                    addCard();
                return true;
            case R.id.action_clearCardsFab:
                clearCards();
                return true;
            case R.id.action_sortCardsFab:
                sortCardsFull();
                return true;
            default:
                return false;
        }
    }

    private boolean onContextItemSelectedCardSlot(MenuItem item, CardSlot selectedCardSlot) {
        switch (item.getItemId()) {
            case R.id.action_createCopy:
                createCopy(selectedCardSlotIndex);
                return true;
            case R.id.action_disableMergeSlot:
                enableMergeSlot(false);
                return true;
            case R.id.action_lockOrUnlock:
                toggleCardLock(selectedCardSlot, false);
                return true;
            case R.id.action_remove:
                removeCard(selectedCardSlotIndex);
                return true;
            case R.id.action_undoMergeLeft:
                if (selectedCardSlot.card2 != null) {
                    selectedCardSlot.setCards(selectedCardSlot.card1, null);
                    updateCardSlotText(selectedCardSlot);
                }
                return true;
            case R.id.action_undoMergeRight:
                if (selectedCardSlot.card2 != null) {
                    selectedCardSlot.setCards(selectedCardSlot.card2, null);
                    updateCardSlotText(selectedCardSlot);
                }
                return true;
            case R.id.action_randomFundamental:
                selectedCardSlot.fundamental = getRandomFundamental();
                updateCardSlotText(selectedCardSlot);
                return true;
            case R.id.action_copyFundamental1:
                copiedFundamental = getCopyableFundamental(selectedCardSlot, false);
                return true;
            case R.id.action_copyFundamental2:
                copiedFundamental = getCopyableFundamental(selectedCardSlot, true);
                return true;
            case R.id.action_pasteFundamental:
                selectedCardSlot.fundamental = copiedFundamental;
                updateCardSlotText(selectedCardSlot);
                return true;
            case R.id.action_moveToTop:
                handleMoveElementToTop(selectedCardSlotIndex);
                return true;
            case R.id.action_moveUp:
                handleMoveElementUp(selectedCardSlotIndex);
                return true;
            case R.id.action_moveDown:
                handleMoveElementDown(selectedCardSlotIndex);
                return true;
            default:
                return false;
        }
    }

    private void initFundamentalCopyMenuItems(Menu menu) {
        if (!addFundamentalsEnabled) {
            menu.setGroupVisible(R.id.group_fundamentalManagement, false);
            return;
        }

        Card copyableFundamental1 = getCopyableFundamental(cardSlots.get(selectedCardSlotIndex), false);
        Card copyableFundamental2 = getCopyableFundamental(cardSlots.get(selectedCardSlotIndex), true);

        MenuItem menuItem = menu.findItem(R.id.action_copyFundamental1);
        if (copyableFundamental1 != null) {
            menuItem.
                setTitle(String.format(getString(R.string.action_copyFundamental), copyableFundamental1.name.toUpperCase()));
        }
        else {
            if (copyableFundamental2 != null) {
                menuItem.setVisible(false);
            }
            else {
                menuItem.setTitle(getString(R.string.action_copyFundamental_disabled));
                menuItem.setEnabled(false);
            }
        }

        if (copyableFundamental2 != null) {
            menuItem = menu.findItem(R.id.action_copyFundamental2);
            menuItem.setVisible(true);
            menuItem.
                setTitle(String.format(getString(R.string.action_copyFundamental), copyableFundamental2.name.toUpperCase()));
        }

        menuItem = menu.findItem(R.id.action_pasteFundamental);
        if (copiedFundamental != null) {
            menuItem.
                setTitle(String.format(getString(R.string.action_pasteFundamental), copiedFundamental.name.toUpperCase()));
        }
        else {
            menuItem.setTitle(getString(R.string.action_pasteFundamental_disabled));
            menuItem.setEnabled(false);
        }
    }

    private Card getCopyableFundamental(CardSlot cardSlot, boolean onlyFundamentalCard) {
        Card result = null;

        if (onlyFundamentalCard && cardSlot.card1.isFundamental() && cardSlot.card2 == null)
            result = cardSlot.card1;
        else if (!onlyFundamentalCard && cardSlot.fundamental != null)
            result = cardSlot.fundamental;

        return result;
    }

    private boolean canMoveUp(int cardSlotIndex) {
        return cardSlotIndex > (mergeSlotEnabled ? 1 : 0);
    }

    private boolean canMoveDown(int cardSlotIndex) {
        return cardSlotIndex < cardSlots.size() - 1;
    }

    private void handleMoveElementToTop(int cardSlotIndex) {
        int firstAvailableSlotIndex = mergeSlotEnabled ? 1 : 0;
        if (canMoveUp(cardSlotIndex)) {
            tempSlot.copyFromLite(cardSlots.get(cardSlotIndex));
            cardSlots.get(cardSlotIndex).clear(false);
            moveCardsDown(firstAvailableSlotIndex, true);
            cardSlots.get(firstAvailableSlotIndex).copyFrom(tempSlot);
        }
    }

    private void handleMoveElementUp(int cardSlotIndex) {
        if (canMoveUp(cardSlotIndex)) {
            tempSlot.copyFromLite(cardSlots.get(cardSlotIndex - 1));
            cardSlots.get(cardSlotIndex - 1).copyFrom(cardSlots.get(cardSlotIndex));
            cardSlots.get(cardSlotIndex).copyFrom(tempSlot);
        }
    }

    private void handleMoveElementDown(int cardSlotIndex) {
        if (canMoveDown(cardSlotIndex)) {
            tempSlot.copyFromLite(cardSlots.get(cardSlotIndex + 1));
            cardSlots.get(cardSlotIndex + 1).copyFrom(cardSlots.get(cardSlotIndex));
            cardSlots.get(cardSlotIndex).copyFrom(tempSlot);
        }
    }

    // Alternate way of checking touch. Not as powerful as OnTouchListener.
    // The other necessary part is in content_main: android:onClick="onTouch
    //public void onTouch(View view) {
    //}

    private void initTouch() {
        mainView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (longTouchDetected(x, y))
                            onLongTouch(touchStartY);
                        else if (swipeDetected(true, x))
                            onSwipe(true, touchStartY);
                        else if (swipeDetected(false, x))
                            onSwipe(false, touchStartY);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!touchHandled)
                            onCardSlotTouch(y);
                        endTouch();
                        mainView.performClick();
                        break;
                }

                return true;
            }
        });
    }

    private boolean longTouchDetected(int touchX, int touchY) {
        if (touchHandled)
            return false;

        if (!touching) {
            touching = true;
            touchStartX = touchX;
            touchStartY = touchY;
            touchStartTime = SystemClock.elapsedRealtime();
        }
        else {
            if (SystemClock.elapsedRealtime() - touchStartTime >= maxTouchDuration) {
                touchHandled = true;

                int touchPosMaxDifference = 15;
                return Math.abs(touchY - touchStartY) <= touchPosMaxDifference
                    && Math.abs(touchX - touchStartX) <= touchPosMaxDifference;
            }
        }

        return false;
    }

    private boolean swipeDetected(boolean right, int touchX) {
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
    }

    private void enableNextAndPrevButtons(boolean enable) {
        if (enable) {
            fab.hide();
            nextButton.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.VISIBLE);
        }
        else {
            fab.show();
            nextButton.setVisibility(View.GONE);
            prevButton.setVisibility(View.GONE);
        }
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

    private void updateLockedCardCount() {
        lockedCardCount = 0;
        for (CardSlot cardSlot : cardSlots) {
            if (cardSlot.isLocked())
                lockedCardCount++;
        }
    }

    private int getUnlockedCardCount() {
        int result = 0;
        for (CardSlot slot : cardSlots) {
            if (!slot.isLocked() && !slot.isEmpty())
                result++;
        }

        return  result;
    }

    private int getMaxUnlockedCardCount() {
        return Math.min(userSetCardCount, maxDisplayedCards - lockedCardCount);
    }

    private int getEmptyCardSlotCount() {
        int result = 0;
        for (CardSlot slot : cardSlots) {
            if (slot.isEmpty())
                result++;
        }

        return  result;
    }

    private int getLastUnlockedListCardId() {
        for (int i = cardSlots.size() - 1; i >= 0; i--) {
            if (!cardSlots.get(i).isEmpty()
                  && !cardSlots.get(i).isLocked()
                  && cardSlots.get(i).card2 == null)
                return cardSlots.get(i).card1.id;
        }

        return -1;
    }

    private boolean isLocked(CardSlot cardSlot, boolean allowSecondaryLock) {
        return cardSlot.isLocked() && (allowSecondaryLock || !cardSlot.secondaryLockEnabled());
    }

    private boolean isMergeSlot(int cardSlotIndex) {
        return mergeSlotEnabled && cardSlotIndex == 0;
    }

    private void setTextColors(int color) {
        for (CardSlot slot : cardSlots) {
            slot.setTextColor(color);
        }
    }

    private void setTextColor(int textIndex, int color) {
        cardSlots.get(textIndex).setTextColor(color);
    }

    private void copyElementsToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        StringBuilder allElementsText = new StringBuilder();
        int elementCount = 0;

        for (int i = 0; i < cardSlots.size(); i++) {
            if (cardSlots.get(i).isEmpty())
                continue;
            else if (i > 0)
                allElementsText.append("\n");

            if (cardSlots.get(i).secondaryLockEnabled())
                allElementsText.append("- ");

            allElementsText.append(cardSlots.get(i).getText());
            elementCount++;
        }

        if (elementCount > 0) {
            ClipData clip = ClipData.newPlainText(getString(R.string.app_name), allElementsText.toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, getString(R.string.copiedToClipboard), Toast.LENGTH_LONG).show();
            Log.d("CAGE", elementCount + " element(s) copied to clipboard");
        }
        else {
            Toast.makeText(this, getString(R.string.notCopiedToClipboard), Toast.LENGTH_LONG).show();
        }
    }

    private void updateHeaderInfoText() {
        String newString;
        String categoryName = "";

        boolean usingAllCards = displayedCategory < 0;
        if (!usingAllCards)
            categoryName = getCategoryName(displayedCategory, false);

        if (displayMode == DisplayMode.list) {
            newString = getHeaderInfoListMode(usingAllCards, categoryName);
        }
        else if (displayMode == DisplayMode.fundamentals) {
            newString = getHeaderInfoListMode(false, "");
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

        int displayedListCardCount = getMaxUnlockedCardCount();
        if (displayedListCardCount <= 0) {
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
        else if (displayMode == DisplayMode.fundamentals)
            return String.format(getString(R.string.fundamentalPageNum), currentPage, maxPage);
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

        // Tip card for the merge slot
        tipCard = new Card(getString(R.string.mergeSlotTip_full), -1, getString(R.string.tip), getString(R.string.tip), -2);
        tipCard.setNameFirstHalf(getString(R.string.mergeSlotTip_left), null, null);
        tipCard.setNameSecondHalf(getString(R.string.mergeSlotTip_right), Card.NameHalfType.verb, false);

        // Temporary slot for moving cards around
        tempSlot = new CardSlot(-1,  new TextView(this), 0, 0);
    }

    private void shuffleDeck(List<Card> deck) {
        double rand;
        Card tempCard;
        for (int i = 0; i < deck.size(); i++) {
            rand = Math.random();
            int randCardIndex = (int) (rand * deck.size());
            tempCard = deck.get(randCardIndex);
            deck.set(randCardIndex, deck.get(i));
            deck.set(i, tempCard);
        }
    }

    private void updateDisplayedDeck(int category) {
        displayedCards = allCards;

        boolean anyCategory = category < 0;
        if (displayMode != DisplayMode.list) {
            // List mode uses allCards deck with the start and end indexes
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

        if (displayMode != DisplayMode.list
            && displayMode != DisplayMode.fundamentals
            && displayedCards != allCards) {
            shuffleDeck(displayedCards);
        }

        switch (displayMode) {
            case list:
            case fundamentals:
                startOrNextPageInCardList(shownCardCountChanged);
                break;
            default:
                setAllCardSlotTexts(displayedCards);
                break;
        }
    }

    private void setAllCardSlotTexts(List<Card> displayedCards) {
        int locksPassed = 0;
        for (int i = 0; i < cardSlots.size(); i++) {
            if (cardSlots.get(i).isLocked()) {
                locksPassed++;
                continue;
            }

            boolean emptySlot =
                (i >= userSetCardCount + locksPassed) || (i - locksPassed >= displayedCards.size());
            setCardSlotText(cardSlots.get(i), displayedCards, i, emptySlot);
        }

        nextCardInDeck = userSetCardCount;
    }

    private void setCardSlotText(CardSlot cardSlot, List<Card> cards, int index, boolean empty) {
        if (cardSlot.isLocked()) {
            return;
        }
        else if (empty) {
            cardSlot.clear(false);
            return;
        }

        cardSlot.setCards(cards.get(index), null);
        StringBuilder text = new StringBuilder();
        //Log.d("CAGE", "Card1: " + cardSlot.card1.name);

        if (addFundamentalsEnabled) {
            cardSlot.fundamental = getRandomFundamental();
            appendFundamental(text, cardSlot);
        }

        if (displayMode == DisplayMode.merge) {
            text.append(getMergeCardDisplayText(cardSlot, cards, index));
        }
        else {
            appendCardName(text, cardSlot);
        }

        int categoryTagLength = 0;
        if (showCategoryTags)
            categoryTagLength = insertCategoryTag(text, cardSlot, displayMode == DisplayMode.merge);

        cardSlot.setText(text.toString(), categoryTagLength);
    }

    private void updateCardSlotText(CardSlot cardSlot) { //, boolean onlyFirstHalf) {
        if (cardSlot == null)
            return;

        StringBuilder text = new StringBuilder();
        boolean mergeElements = cardSlot.card2 != null;

        if (addFundamentalsEnabled) {
            appendFundamental(text, cardSlot);
        }

//        if (onlyFirstHalf) {
//            appendCardHalf(text, cardSlot, true);
//        }
        if (mergeElements) {
            text.append(getMergeCardDisplayText(cardSlot));
        }
        else {
            appendCardName(text, cardSlot);
        }

        int categoryTagLength = 0;
        if (showCategoryTags)
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

    private void addCard() {
        CardSlot cardSlot = getFirstEmptyCardSlot();
        if (cardSlot != null) {
            drawNewCard(cardSlot);
        }
    }

    private void showOrHideFundamentals(boolean showFundamentals) {
        for (CardSlot cardSlot : cardSlots) {
            showOrHideFundamental(cardSlot, showFundamentals);
        }
    }

    private void showOrHideFundamental(CardSlot cardSlot, boolean showFundamental) {
        if (cardSlot.isEmpty())
            return;

        if (!cardSlot.isLocked() || cardSlot.fundamental == null) {
            if (showFundamental)
                cardSlot.fundamental = getRandomFundamental();
            else
                cardSlot.fundamental = null;
        }

        updateCardSlotText(cardSlot);
    }

    private Card getRandomFundamental() {
        return fundamentals.get((int)(Math.random() * fundamentals.size()));
    }

    private void appendFundamental(StringBuilder sb, CardSlot cardSlot) {
        if (cardSlot.fundamental != null) {
            sb.append(String.format(getString(R.string.cardSlotFundamental), cardSlot.fundamental.name.toUpperCase())).append(" ");

//            boolean useUpperCase =
//                displayMode != DisplayMode.fundamentals
//                || !cardSlot.card1.isFundamental()
//                || cardSlot.card2 != null;
//            sb.append(String.format(getString(R.string.cardSlotFundamental),
//                useUpperCase ? cardSlot.fundamental.name.toUpperCase() : cardSlot.fundamental.name))
//                .append(" ");
        }
    }

    private void showOrHideCategoryTags(boolean showCategories) {
        for (CardSlot cardSlot : cardSlots) {
            showOrHideCategoryTag(cardSlot, showCategories);
        }
    }

    private void showOrHideCategoryTag(CardSlot cardSlot, boolean showCategories) {
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
        if (showCategoryTags) {
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

    private void appendCardName(StringBuilder sb, CardSlot cardSlot) {
        sb.append(cardSlot.card1.name);

        //        boolean useUpperCase =
        //            addFundamentalsEnabled
        //            && displayMode == DisplayMode.fundamentals
        //            && cardSlot.card1.isFundamental()
        //            && cardSlot.card2 == null;
        //        sb.append(useUpperCase ? cardSlot.card1.name.toUpperCase() : cardSlot.card1.name);
    }

    private void appendCardHalf(StringBuilder sb, CardSlot cardSlot, boolean firstHalf) {
        if (firstHalf) {
            sb.append(cardSlot.card1.getNameHalf(true, null, null));
        }
        else {
            if (cardSlot.card2 == null)
                sb.append(cardSlot.card1.getNameHalf(false, cardSlot.card1.firstHalfType, cardSlot.card1.getSecondHalfPreference()));
            else
                sb.append(cardSlot.card2.getNameHalf(false, cardSlot.card1.firstHalfType, cardSlot.card1.getSecondHalfPreference()));
        }
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
            cardSlot.card1.getNameHalf(true, null, null),
            cardSlot.card2.getNameHalf(
                false,
                cardSlot.card1.firstHalfType,
                cardSlot.card1.getSecondHalfPreference()));
    }

    private void onCardSlotTouch(int touchY) {
        CardSlot cardSlot = getTouchedCardSlot(touchY, true, true);
        if (cardSlot != null && isMergeSlot(cardSlot.id)) {
            createCopy(0);
            return;
        }

        cardSlot = getAvailableCardSlot(cardSlot, touchY, false);
        if (cardSlot != null) {
            if (cardSlot.isEmpty()) {
                if (displayMode == DisplayMode.list || displayMode == DisplayMode.fundamentals)
                    refreshList();
                else
                    drawNewCard(cardSlot);
            }
            else {
                toggleCardLock(cardSlot, true);
            }
        }
    }

    private void onLongTouch(int touchY) {
        int cardSlotIndex = getTouchedCardSlotIndex(touchY);
        if (cardSlotIndex >= 0 && cardSlotIndex < cardSlots.size()) {
            if (!isMergeSlot(cardSlotIndex)) {
                if (cardSlots.get(cardSlotIndex).isEmpty()) {
                    sortCardsFull();
                }
                else {
                    openCardSlotContextMenu(cardSlotIndex);
                }
            }
            else {
                openCardSlotContextMenu(cardSlotIndex);
            }
        }
        else {
            sortCardsFull();
        }
    }

    private void onSwipe(boolean right, int touchY) {
        CardSlot cardSlot = getTouchedCardSlot(touchY, false, false);
        if (cardSlot == null)
            return;

        if (mergeSlotEnabled || displayMode == DisplayMode.merge)
            manualMerge(cardSlot, right);

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
                cardSlots.get(0).setCards(cardSlots.get(0).card1, card2);
                updateCardSlotText(cardSlots.get(0));
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
                String.format(getString(R.string.mergeSelected_rightSide),
                    card2.getNameHalf(false, null, null)),
                    Toast.LENGTH_SHORT)
                .show();
        }
        else {
            // Takes the merge origin's card1 and gives it to all unlocked card slots
            // If merge slot is enabled, only it is changed.

            mergeBeginning = mergeOrigin.card1;
            Card card2;

            if (mergeSlotEnabled) {
                card2 = cardSlots.get(0).card2 == null ?
                    cardSlots.get(0).card1 : cardSlots.get(0).card2;
                cardSlots.get(0).setCards(mergeBeginning, card2);
                updateCardSlotText(cardSlots.get(0));
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
                String.format(getString(R.string.mergeSelected_leftSide),
                    mergeBeginning.getNameHalf(true, null, null)),
                    Toast.LENGTH_SHORT)
                .show();
        }
    }

    private int getTouchedCardSlotIndex(int touchY) {
        for (int i = 0; i < cardSlots.size(); i++) {
            if (cardSlots.get(i).touchHit(touchY))
                return i;
            else if (i > 0 && touchY < cardSlots.get(i).getTop())
                return i - 1;
        }

        return -1;
    }

    private CardSlot getTouchedCardSlot(int touchY, boolean allowEmpty, boolean allowMergeSlot) {
        int index = getTouchedCardSlotIndex(touchY);
        if (index < 0 || (!allowMergeSlot && isMergeSlot(index)))
            return null;
        else if (allowEmpty || !cardSlots.get(index).isEmpty())
            return cardSlots.get(index);

        return null;
    }

    private int getLastUnlockedCardSlotIndex(int fromIndex) {
        if (fromIndex < 0)
            fromIndex = 0;

        for (int i = cardSlots.size() - 1; i >= fromIndex; i--) {
            if (!cardSlots.get(i).isLocked())
                return i;
        }

        return -1;
    }

    private int getFirstEmptyCardSlotIndex(int fromIndex) {
        if (fromIndex < 0)
            fromIndex = 0;

        for (int i = fromIndex; i < cardSlots.size(); i++) {
            if (cardSlots.get(i).isEmpty())
                return i;
        }

        return -1;
    }

    private CardSlot getFirstEmptyCardSlot() {
        int index = getFirstEmptyCardSlotIndex(0);
        if (index >= 0)
            return cardSlots.get(index);
        else
            return null;
    }

    private CardSlot getAvailableCardSlot(int touchY) {
        CardSlot cardSlot = getTouchedCardSlot(touchY, true, false);
        return getAvailableCardSlot(cardSlot, touchY, true);
    }

    private CardSlot getAvailableCardSlot(CardSlot cardSlot, int touchY, boolean obeyCardCount) {
        if (cardSlot != null && isMergeSlot(cardSlot.id))
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
        if (cardSlot == null) {
            return null;
        }
        // There's a possibly available empty card slot
        else if (cardSlot.isEmpty()
                 && obeyCardCount
                 && getUnlockedCardCount() >= userSetCardCount) {
            // Card count cap is reached
            return null;
        }

        // Returns an available (empty or non-empty) card slot
        return cardSlot;
    }

    private void addOrSwitchCard(int touchY) {
        CardSlot cardSlot = getAvailableCardSlot(touchY);
        if (cardSlot != null)
            drawNewCard(cardSlot);
    }

    private void switchCard(int touchY) {
        CardSlot cardSlot = getTouchedCardSlot(touchY, false, false);
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
        if (cardSlots.get(cardSlotIndex).isLocked())
            lockedCardCount--;

        if (cardSlotIndex == cardSlots.size() - 1) {
            cardSlots.get(cardSlotIndex).clear(false);
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

    private void sortCardsFull() {
        sortCards(true);
        sortCards(false);
    }

    private void sortCards(boolean allowSecondaryLock) {
        if (lockedCardCount == 0 || (allowSecondaryLock && lockedCardCount == maxDisplayedCards))
            return;

        // Latter card can be an unlocked card in any case or
        // a secondary locked card if secondary lock is not allowed
        int firstLatterCardIndex = -1;
        int lastFormerCardIndex = -1;

        for (int i = 0; i < cardSlots.size(); i++) {
            if (isLocked(cardSlots.get(i), allowSecondaryLock)) {
                lastFormerCardIndex = i;
            }
            else if (firstLatterCardIndex == -1) {
                firstLatterCardIndex = i;
            }
        }

        // Cards are already sorted; returns
        if (firstLatterCardIndex == -1
            || lastFormerCardIndex == -1
            || lastFormerCardIndex < firstLatterCardIndex)
            return;

        tempSlot.copyFromLite(cardSlots.get(firstLatterCardIndex));

        int lockedCardsMoved = 0;
        for (int i = firstLatterCardIndex + 1; i < cardSlots.size(); i++) {
            if (isLocked(cardSlots.get(i), allowSecondaryLock)) {
                // Moves a locked card to the position of the first latter card
                cardSlots.get(firstLatterCardIndex).copyFrom(cardSlots.get(i));
                lockedCardsMoved++;
                //Log.d("CAGE", "Locked card got position " + firstLatterCardIndex);

                // Moves down all latter cards in between
                for (int j = i; j > firstLatterCardIndex; j--) {
                    if (j == firstLatterCardIndex + 1) {
                        cardSlots.get(j).copyFrom(tempSlot);
                        firstLatterCardIndex = j;
                    }
                    else {
                        cardSlots.get(j).copyFrom(cardSlots.get(j - 1));
                    }
                    //Log.d("CAGE", "Unlocked card got position " + j);
                }

                if (lockedCardsMoved == lockedCardCount)
                    break;
            }
        }
    }

    private void moveCardsDown(int fromIndex, boolean updateLockedCardCount) {
        if (fromIndex < 0)
            fromIndex = 0;

        // If all card slots have a locked card, the last one is lost
        if (lockedCardCount == cardSlots.size()) {
            for (int i = cardSlots.size() - 1; i > fromIndex; i--) {
                cardSlots.get(i).copyFrom(cardSlots.get(i - 1));
            }
        }
        // Otherwise the first empty slot following fromIndex is filled
        // or the last unlocked card is lost
        else {
            int firstEmptyCardSlotIndex = getFirstEmptyCardSlotIndex(fromIndex + 1);
            int lastUnlockedCardSlotIndex = getLastUnlockedCardSlotIndex(fromIndex + 1);

            int toIndex;
            if (firstEmptyCardSlotIndex > fromIndex)
                toIndex = firstEmptyCardSlotIndex;
            else if (lastUnlockedCardSlotIndex > fromIndex)
                toIndex = lastUnlockedCardSlotIndex;
            else
                toIndex = cardSlots.size() - 1;

            for (int i = toIndex; i > fromIndex; i--) {
                cardSlots.get(i).copyFrom(cardSlots.get(i - 1));
            }
        }

        if (fromIndex > 0 || !mergeSlotEnabled)
            cardSlots.get(fromIndex).clear(false);

        if (updateLockedCardCount)
            updateLockedCardCount();
    }

    private void toggleCardLock(CardSlot cardSlot, boolean allowSecondaryLock) {
        if (cardSlot != null) {
            if (cardSlot.isLocked() && allowSecondaryLock && !cardSlot.secondaryLockEnabled()) {
                cardSlot.enableSecondaryLock(true);
                if (autoSortWhenLocking)
                    sortCards(false);
            }
            else {
                cardSlot.lock(!cardSlot.isLocked());
                if (cardSlot.isLocked())
                    lockedCardCount++;
                else
                    lockedCardCount--;

                locksChanged = true;

                if (autoSortWhenLocking)
                    sortCardsFull();
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

    private void initFundamentalListMode() {
        clearCards();
        deckStartIndex = 0;
        listSize = fundamentals.size();
        listModeJustStarted = true;
    }

    private void startOrNextPageInCardList(boolean shownCardCountChanged) {
        // List: Uses allCards deck with the start index and list size set in initListMode()
        // Fundamentals: Uses fundamentals deck with the start index and list size set in initFundamentalListMode()

        int lastUnlockedCardId = getLastUnlockedListCardId();
        if (lastUnlockedCardId < 0)
            lastUnlockedCardId = nextCardInDeck;

        boolean restartFromCategoryFirstCard =
            lastUnlockedCardId < deckStartIndex
            || lastUnlockedCardId >= deckStartIndex + listSize - 1;

        if (listModeJustStarted) {
            //Log.d("CAGE", "LIST/FND MODE. Just started");
            nextCardInDeck = deckStartIndex;
            listModeJustStarted = false;
            enableNextAndPrevButtons(true);
            locksChanged = false;
        }
        else if (restartFromCategoryFirstCard) {
            //Log.d("CAGE", "LIST/FND MODE. Restarting");
            nextCardInDeck = deckStartIndex;
        }
        else if (!shownCardCountChanged) {
            //Log.d("CAGE", "LIST/FND MODE. Next page. Moving forward " + (lastUnlockedCardId + 1 - nextCardInDeck));
            nextCardInDeck = lastUnlockedCardId + 1;
        }

        //Log.d("CAGE", "LIST/FND MODE. nextShownCardInList: " + nextShownCardInList +
        //      ". listStartIndex: " + listStartIndex +
        //      ". listSize: " + listSize);

        refreshList();
    }

    private void prevPageInCardList() {
        boolean atListFirstElement = nextCardInDeck == deckStartIndex;
        int shownListCardCount = getMaxUnlockedCardCount();

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

        //Log.d("CAGE", "LIST/FND MODE. Previous page. Moving back " + shownListCardCount);

        refreshList();
    }

    private void refreshList() {
        if (locksChanged) {
            sortCardsFull();
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

            int unlockedCardSlotIndex = i - lockedCardsPassed;
            boolean emptySlot = unlockedCardSlotIndex >= getMaxUnlockedCardCount();
            int deckIndex = nextCardInDeck + unlockedCardSlotIndex;

            if (deckIndex - deckStartIndex >= listSize) {
                // Out of cards
                emptySlot = true;
            }

            setCardSlotText(cardSlots.get(i), displayedCards, deckIndex, emptySlot);
        }
    }

    private void updateMergeSlot() {
        if (mergeSlotEnabled) {
            moveCardsDown(0, false);
            cardSlots.get(0).setCards(tipCard, null);

            if (addFundamentalsEnabled) {
                cardSlots.get(0).fundamental = getRandomFundamental();
            }

            updateCardSlotText(cardSlots.get(0));
            cardSlots.get(0).lock(true);
            cardSlots.get(0).setBackgroundColor(mergeSlotColor);
        }
        else {
            cardSlots.get(0).lock(false);
        }

        updateLockedCardCount();
    }

    private void createCopy(int cardSlotIndex) {
        int copySlotIndex = cardSlotIndex + 1;
        if (cardSlotIndex < cardSlots.size() - 1) {
            // Tries to free the next card slot and then copies the slot's content to it
            moveCardsDown(copySlotIndex, false);
            cardSlots.get(copySlotIndex).copyFrom(cardSlots.get(cardSlotIndex));
            updateLockedCardCount();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);

        optionsMenu = menu;
        MenuItem menuItem = menu.findItem(R.id.submenu_cardCount);
        menuItem.setTitle(String.format(getString(R.string.action_cardCount), userSetCardCount));

        currentDisplayedDisplayModeItem = menu.findItem(R.id.action_setMode_classic);
        currentDisplayedDisplayModeItem.setEnabled(false);

        currentDisplayedCatItem = menu.findItem(R.id.action_displayAll);
        currentDisplayedCatItem.setEnabled(false);

        mergeSlotToggle = menu.findItem(R.id.action_mergeSlotToggle);
        mergeSlotToggle.setChecked(mergeSlotEnabled);

        addFundamentalsToggle = menu.findItem(R.id.action_addFundamentalsToggle);
        addFundamentalsToggle.setChecked(addFundamentalsEnabled);

        showCategoryTagsToggle = menu.findItem(R.id.action_showCategoryTagsToggle);
        showCategoryTagsToggle.setChecked(showCategoryTags);

//        keepResultCategoriesToggle = menu.findItem(R.id.action_keepResultCategoriesToggle);
//        keepResultCategoriesToggle.setChecked(keepResultCategories);

        autoSortWhenLockingToggle = menu.findItem(R.id.action_autoSortWhenLocking);
        autoSortWhenLockingToggle.setChecked(autoSortWhenLocking);

        autoUpdateSettingChangesToggle = menu.findItem(R.id.action_autoUpdateSettingChangesToggle);
        autoUpdateSettingChangesToggle.setChecked(autoUpdateSettingChanges);

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
                    setTitle(String.format(getString(R.string.action_useCategory),
                        getCategoryName(i, true),
                        getCategoryName(i, false)));
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
        else if (id == R.id.action_mergeSlotToggle) {
            enableMergeSlot(!mergeSlotEnabled);
            return true;
        }
        else if (id == R.id.action_copyToClipboard) {
            copyElementsToClipboard();
            return true;
        }
        else if (handleClearCardsAction(id))
            return true;
        else if (handleClearLocksAction(id))
            return true;
        else if (id == R.id.action_sortCards) {
            sortCardsFull();
            return true;
        }
        else if (id == R.id.action_changeFundamentals) {
            enableFundamentals(true);
            return true;
        }
        else if (id == R.id.action_addFundamentalsToggle) {
            enableFundamentals(!addFundamentalsEnabled);
            return true;
        }
        else if (handleAutoSortWhenLockingActivation(id))
            return true;
        else if (handleAutoUpdateSettingChangesActivation(id))
            return true;
        else if (handleShowCategoryTagsActivation(id))
            return true;
        //else if (handleKeepResultCategoriesActivation(id))
        //    return true;

        return super.onOptionsItemSelected(item);
    }

    private boolean setCardCount(boolean increase, int min, int max, MenuItem item) {
        if (increase) {
            userSetCardCount++;
            if (userSetCardCount > max) {
                userSetCardCount = min;
            }
        }
        else {
            userSetCardCount--;
            if (userSetCardCount < min) {
                userSetCardCount = max;
            }
        }

        item.setTitle(String.format(getString(R.string.action_cardCount), userSetCardCount));
        return true;
    }

    private boolean setCardCount(int value, int min, int max) {
        if (value >= min && value <= max) {
            userSetCardCount = value;

            if (optionsMenu != null) {
                optionsMenu.findItem(R.id.submenu_cardCount).
                    setTitle(String.format(getString(R.string.action_cardCount), userSetCardCount));
            }

            updateHeaderInfoText();

            if (autoUpdateSettingChanges
                || displayMode == DisplayMode.list
                || displayMode == DisplayMode.fundamentals)
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
            else if (displayMode == DisplayMode.fundamentals) {
                initFundamentalListMode();
                drawCards(displayedCategory, false);
            }
            else {
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

        // Hides Next and Prev buttons if List or Fundamentals mode is ending
        if ((displayMode == DisplayMode.list || displayMode == DisplayMode.fundamentals)
            && mode != displayMode) {
            enableNextAndPrevButtons(false);
        }

        displayMode = mode;
        item.setEnabled(false);
        currentDisplayedDisplayModeItem.setEnabled(true);
        currentDisplayedDisplayModeItem = item;

        if (displayMode == DisplayMode.list) {
            // List mode updates the header info text when the cards are drawn
            initListMode();
        }
        else if (displayMode == DisplayMode.fundamentals) {
            // Fundamentals mode updates the header info text when the cards are drawn
            initFundamentalListMode();
        }
        else {
            // All other modes do it here
            updateHeaderInfoText();
        }

        drawCards(displayedCategory, false);

        return true;
    }

    private boolean isSetModeId(int id) {
        return id == R.id.action_setMode_classic
            || id == R.id.action_setMode_list
            || id == R.id.action_setMode_merge
            || id == R.id.action_setMode_fundamentals;
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
            case R.id.action_setMode_fundamentals:
                return setDisplayMode(item, DisplayMode.fundamentals);
        }

        return false;
    }

    private boolean handleSetCardCountOptions(int id) {
        switch (id) {
            case R.id.action_setCardCount_1:
                return setCardCount(1, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_2:
                return setCardCount(2, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_3:
                return setCardCount(3, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_4:
                return setCardCount(4, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_5:
                return setCardCount(5, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_6:
                return setCardCount(6, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_7:
                return setCardCount(7, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_8:
                return setCardCount(8, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_9:
                return setCardCount(9, minDisplayedCards, maxDisplayedCards);
            case R.id.action_setCardCount_10:
                return setCardCount(10, minDisplayedCards, maxDisplayedCards);
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

    private void enableMergeSlot(boolean enable) {
        mergeSlotEnabled = enable;
        mergeSlotToggle.setChecked(mergeSlotEnabled);
        updateMergeSlot();
    }

    private boolean handleClearCardsAction(int id) {
        if (id == R.id.action_clearCards) {
            clearCards();
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

    private void enableFundamentals(boolean enable) {
        addFundamentalsEnabled = enable;
        addFundamentalsToggle.setChecked(enable);
        showOrHideFundamentals(enable);

        if (cardSlotContextMenu != null)
            cardSlotContextMenu.findItem(R.id.action_randomFundamental).setEnabled(enable);
    }

    private boolean handleAutoSortWhenLockingActivation(int id) {
        if (id == R.id.action_autoSortWhenLocking) {
            autoSortWhenLocking = !autoSortWhenLocking;
            autoSortWhenLockingToggle.setChecked(autoSortWhenLocking);
            return true;
        }

        return false;
    }

    private boolean handleAutoUpdateSettingChangesActivation(int id) {
        if (id == R.id.action_autoUpdateSettingChangesToggle) {
            autoUpdateSettingChanges = !autoUpdateSettingChanges;
            autoUpdateSettingChangesToggle.setChecked(autoUpdateSettingChanges);
            return true;
        }

        return false;
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

    private boolean handleShowCategoryTagsActivation(int id) {
        if (id == R.id.action_showCategoryTagsToggle) {
            showCategoryTags = !showCategoryTags;
            showCategoryTagsToggle.setChecked(showCategoryTags);
            showOrHideCategoryTags(showCategoryTags);
            return true;
        }

        return false;
    }
}
