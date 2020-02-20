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

public class MovementAction implements Action {
    /**
     * min amount of movement requested
     */
    private int minAmount;
    /**
     * max amount of movement requested
     */
    private int maxAmount;
    /**
     * true if the target of the movement is an enemy
     */
    private boolean targetEnemy;
    /**
     * type of movment
     */
    private ForcedMovement toTile;
    /**
     * if the action in not mandatory
     */
    private boolean optional;
    /**
     * true if the action has been executed
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


    /**
     * Class constructor
     *
     * @param minAmount   indicates the minimum number of movement to perform
     * @param maxAmount   indicates the maximum number of movement to perform
     * @param targetEnemy indicates if the movement has to be applied on player or enemy
     * @param toTile      indicates if the enemy has to be moved exactly on your tile (1) or to a specified tile (2), otherwise 0
     */
    public MovementAction(int minAmount, int maxAmount, boolean targetEnemy, ForcedMovement toTile,
                          Action linkedToNext, Effect linkedEffect, Effect hitInTheEffect,boolean mandatoryChoice) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.targetEnemy = targetEnemy;
        this.toTile = toTile;
        this.actionExecuted = false;
        this.linkedToNext = linkedToNext;
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

    public int getMaxAmount() {
        return maxAmount;
    }

    public boolean isTargetEnemy() {
        return targetEnemy;
    }

    public ForcedMovement getToTile() {
        return toTile;
    }


    public boolean isActionExecuted() {
        return actionExecuted;
    }

    @Override
    public Effect getLinkedEffect() {
        return linkedEffect;
    }

    @Override
    public Effect getHitInTheEffect() {
        return hitInTheEffect;
    }

    public void setActionExecuted(boolean actionExecuted) {
        this.actionExecuted = actionExecuted;
    }

    public void initialExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer/*, Player targetPlayer*/) {
        effectVisitor.startAction(this, gameManager, currentPlayer/*, targetPlayer*/);
    }

    @Override
    public void afterChooseDirectionExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, Direction direction) {
        effectVisitor.afterChooseDirectionAction(this, gameManager, direction, currentPlayer);
    }

    ;

    @Override
    public void afterChooseTargetExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, Player targetPlayer) {
        gameManager.getCurrentTurn().setCurrentAction(this);
        effectVisitor.afterChooseTargetAction(this, gameManager, currentPlayer, targetPlayer);
    }

    @Override
    public void afterChooseTileExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, Tile tile) {
        effectVisitor.afterChooseTileAction(this, gameManager, currentPlayer, tile);
    }



    @Override
    public void afterChooseRoomExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, RoomColor roomColor) {
    }

    /**
     * method called after the end of an action to perform a linked action (referred to target previously chosen)
     * It allows to have a movement related to the previously selected target
     * @param effectVisitor the effect that has called the method
     * @param gameManager
     * @param currentPlayer the player that is shooting
     */
    @Override
    public void linkedToNextExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer) {
        effectVisitor.getAlreadyInteractedPlayers().add(new ArrayList<>());
        List<Player> targetPlayer = effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 2);
        if(targetPlayer.isEmpty()){
           effectVisitor.afterExecution(this,gameManager,currentPlayer);
           return;
        }
        afterChooseTargetExecute(effectVisitor,gameManager,currentPlayer,targetPlayer.get(0));
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

    @Override
    public void afterCounterAttackedExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer) {

    }

    public boolean isMandatoryChoice() {
        return mandatoryChoice;
    }
}
