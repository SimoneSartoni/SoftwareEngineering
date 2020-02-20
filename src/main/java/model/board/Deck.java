package model.board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck<C> implements Serializable {
    /**
     * ArrayList of Generic C representing the cards in the deck that che be drawn.
     */
    private List<C> pile;
    /**
     * ArrayList of Generic C representing the discard pile. A card in this list has already been used.
     */
    private List<C> discardPile;

    /**
     * Deck constructor with a given list of cards.
     * @param pile the list of the card of the deck.
     */
    public Deck(List<C> pile) {
        this.pile = new ArrayList<>();
        if(pile != null)
            this.pile.addAll(pile);
        this.discardPile = new ArrayList<>();
    }

    /**
     * Method to draw a card from the deck.
     * @return the Generic C at position 0 of the deck removing from it.
     */
    public C draw () {
        C returnCard = pile.get(0);
        pile.remove(0);
        return returnCard;
    }

    public void addToPile(C card) {
        pile.add(card);
    }

    public void addToDiscardPile(C card) {
        discardPile.add(card);
    }

    public void addToPile(List<C> cards) {
        pile.addAll(cards);
    }

    public void addToDiscardPile(List<C> cards) {
        discardPile.addAll(cards);
    }

    /**
     * Shuffle only the pile of the deck.
     * */
    public void shuffle() {
        Collections.shuffle(pile);
    }

    /**
     * Clear discard pile adding all of the card to the pile, and then shuffle.
     */
    public void reShuffleAll() {
        pile.addAll(discardPile);
        discardPile.clear();
        Collections.shuffle(pile);
    }

    public boolean isEmpty(){
        return pileSize()==0;
    }

    public int pileSize() {
        return pile.size();
    }

    public int discardPileSize() {
        return discardPile.size();
    }

    public void removeAll(){
        pile.clear();
    }

    public void removeDiscardPile(){
        discardPile.clear();
    }
}
