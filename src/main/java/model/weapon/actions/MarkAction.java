package model.weapon.actions;

import model.board.GameManager;
import model.board.Tile;
import model.enums.Direction;
import model.enums.ForcedMovement;
import model.enums.RoomColor;
import model.player.Player;
import model.weapon.Effect;

import java.util.ArrayList;
import java.util.List;

public class MarkAction implements Action {
    /**
     * the min amount of marks
     */
    private int minAmount;
    /**
     * the max amount of marks
     */
    private int maxAmount;
    /**
     * min distance to shoot in the action
     */
    private int minDist;
    /**
     * max distance to shoot in the action
     */
    private int maxDist;
    /**
     * true if the target is a tile, false if is a target
     */
    private boolean aoe;
    /**
     * true if the action is executed
     */
    private boolean actionExecuted;
    /**
     * if the action has a linked to next action
     */
    private Action linkedToNext;
    /**
     *  the linked effect (can be null) to this action
     */
    private Effect linkedEffect;
    /**
     * if he needs to deal additional damage to an already in player in the effect (can be null)
     */
    private Effect hitInTheEffect;
    /**
     * true if the player needs to choose a mandatory target
     */
    private boolean mandatoryChoice;



    public MarkAction(int minAmount, int maxAmount, boolean aoe, int minDist, int maxDist,
                      Action linkedToNext, Effect linkedEffect, Effect hitInTheEffect,boolean mandatoryChoice) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.minDist=minDist;
        this.maxDist=maxDist;
        this.aoe = aoe;
        this.actionExecuted = false;
        this.linkedToNext=linkedToNext;
        this.linkedEffect = linkedEffect;
        this.hitInTheEffect = hitInTheEffect;
        this.mandatoryChoice=mandatoryChoice;
    }

    public Action getLinkedToNext() {
        return linkedToNext;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMinDist() {
        return minDist;
    }

    public int getMaxDist() {
        return maxDist;
    }

    public boolean isAoe() {
        return aoe;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     *
     * @return always 0 (for this particular action) because this action doesn't include any movement
     */
    public ForcedMovement getToTile() {
        return ForcedMovement.NO_MOVEMENT;
    }

    public boolean isOptional() {
        return false;
    }

    public boolean isActionExecuted() {
        return actionExecuted;
    }

    public Effect getHitInTheEffect() {
        return hitInTheEffect;
    }

    @Override
    public Effect getLinkedEffect() {
        return linkedEffect;
    }


    /**
     * set true after this action has been executed
     * @param actionExecuted (true if action has been executed)
     */
    public void setActionExecuted(boolean actionExecuted) {
        this.actionExecuted = actionExecuted;
    }

    /**
     * this method is part of the visitor pattern,it calls visit() of a particular visitor (that deals with MarkActions)
     * @param effectVisitor
     */
    public void initialExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer/*, Player targetPlayer*/){
        effectVisitor.startAction(this, gameManager, currentPlayer/*, targetPlayer*/);
    }

    public void afterChooseDirectionExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, Direction direction){ };

    @Override
    public void afterChooseTargetExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer,Player targetPlayer){
        gameManager.getCurrentTurn().setCurrentAction(this);
        effectVisitor.afterChooseTargetAction(this, gameManager, currentPlayer, targetPlayer);
    }

    public void afterChooseTileExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, Tile tile){
        effectVisitor.afterChooseTileAction(this, gameManager, currentPlayer, tile);
    };

    public void afterChooseRoomExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, RoomColor roomColor){
        effectVisitor.afterChooseRoomAction(this, gameManager, currentPlayer, roomColor);
    };

    /**
     * method called after the end of an action to perform a linked action (referred to target previously chosen)
     * It deals marks to the prevoiusly selected target
     * @param effectVisitor the effect that has called the method
     * @param gameManager
     * @param currentPlayer the player that's shooting
     */
    @Override
    public void linkedToNextExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer) {
        gameManager.getCurrentTurn().setCurrentAction(this);
        effectVisitor.getAlreadyInteractedPlayers().add(new ArrayList<>());
        List<Tile> tempTile = new ArrayList<>();
        if(effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 2).isEmpty()) {
            effectVisitor.afterExecution(this, gameManager, currentPlayer);
            return;
        }
        if (aoe) {
            for (Player p : effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 2)) {
                if (!tempTile.contains(p.getCurrentTile())) {
                    tempTile.add(p.getCurrentTile());
                    for (Player p2 : p.getCurrentTile().getPlayers()) {
                        if(!p2.equals(currentPlayer)) {
                            gameManager.getCurrentTurn().addMark(p2, maxAmount);
                            effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 1).add(p2);
                        }
                    }
                }
            }
        }
        else {
            for (Player p : effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 2)) {
                gameManager.getCurrentTurn().addMark(p, maxAmount);
                effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 1).add(p);
            }
        }
        effectVisitor.afterExecution(this, gameManager, currentPlayer);
    }

    @Override
    public void afterCounterAttackedExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer) {

    }

    @Override
    public void setLinkedToNext(Action linkedTo) {
        linkedToNext = linkedTo;
    }

    @Override
    public void setLinkedEffect(Effect effect) {
        linkedEffect = effect;
    }

    @Override
    public void setHitInTheEffect(Effect effect) {
        hitInTheEffect = effect;
    }

    public boolean isMandatoryChoice() {
        return mandatoryChoice;
    }
}
