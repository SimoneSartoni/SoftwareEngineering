package model.weapon.actions;

import model.board.GameManager;
import model.board.Tile;
import model.enums.Direction;
import model.enums.ForcedMovement;
import model.enums.RoomColor;
import model.enums.TurnState;
import model.player.Player;
import model.utility.TurnStateHandler;
import model.weapon.Effect;

import java.util.ArrayList;
import java.util.List;


public class DamageAction implements Action {
    /**
     * the min amount of damage
     */
    private int minAmount;
    /**
     * the max amount of damage
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


    /**
     * Class constructor: craete a damage actions
     * @param minAmount indicates the minimun amoutn of damage a player can give with this actions
     * @param maxAmount as before, but the maximum
     * @param aoe indicates if the actions envolves all the tile or only a player, it modifies damage multiplying it by -1
     *            so that when the amount si less than 0 then aoe is true
     *
     * */
    public DamageAction(int minAmount, int maxAmount, boolean aoe, int minDist, int maxDist,
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

    public int getMinDist() {
        return minDist;
    }

    public int getMaxDist() {
        return maxDist;
    }

    public Action getLinkedToNext() {
        return linkedToNext;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public boolean isAoe() {
        return aoe;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public ForcedMovement getToTile() {
        return ForcedMovement.NO_MOVEMENT;
    }

    public boolean isOptional() {
        return false;
    }

    public boolean isActionExecuted() {
        return actionExecuted;
    }

    public void setActionExecuted(boolean actionExecuted) {
        this.actionExecuted = actionExecuted;
    }

    @Override
    public Effect getLinkedEffect() {
        return linkedEffect;
    }

    @Override
    public Effect getHitInTheEffect() {
        return hitInTheEffect;
    }

    public void initialExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer){
        gameManager.getCurrentTurn().setCurrentAction(this);
        effectVisitor.startAction(this, gameManager, currentPlayer);
    }

    public void afterChooseDirectionExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, Direction direction) {
        effectVisitor.afterChooseDirectionAction(this,gameManager,direction,currentPlayer);
    }
    @Override
    public void afterChooseTargetExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer,Player  targetPlayer){
        effectVisitor.afterChooseTargetAction(this, gameManager, currentPlayer, targetPlayer); }

    public void afterChooseTileExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer,Tile tile){
        effectVisitor.afterChooseTileAction(this, gameManager, currentPlayer, tile); }

    public void afterChooseRoomExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer, RoomColor roomColor){
        effectVisitor.afterChooseRoomAction(this, gameManager, currentPlayer, roomColor);};

    /**
     * method called after the end of an action to perform a linked action (referred to target previously chosen)
     * It deals damage to the target already chosen in the former action
     * @param effectVisitor the effect that has called the method
     * @param gameManager
     * @param currentPlayer the player that's shooting
     */
    @Override
    public void linkedToNextExecute(Effect effectVisitor, GameManager gameManager, Player currentPlayer) {
        effectVisitor.getAlreadyInteractedPlayers().add(new ArrayList<>());
        List<Tile> tempTile = new ArrayList<>();
        if (effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 2).isEmpty()) {
            initialExecute(effectVisitor, gameManager, currentPlayer);
            return;
        }
        if (aoe) {
            for (Player p : effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 2)) {
                if (!tempTile.contains(p.getCurrentTile())) {
                    tempTile.add(p.getCurrentTile());
                    for (Player p2 : p.getCurrentTile().getPlayers()) {
                        p2.addDamageTaken(currentPlayer, maxAmount, gameManager);
                        gameManager.setPlayerState(p2);
                        gameManager.getCurrentTurn().getAlreadyHitPlayer().get(effectVisitor).add(p2);
                        effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 1).add(p2);
                    }
                }
            }
        } else {
            for (Player p : effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 2)) {
                p.addDamageTaken(currentPlayer, maxAmount, gameManager);
                gameManager.setPlayerState(p);
                gameManager.getCurrentTurn().getAlreadyHitPlayer().get(effectVisitor).add(p);
                effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 1).add(p);
            }
        }
        for (Player p : effectVisitor.getAlreadyInteractedPlayers().get(effectVisitor.getAlreadyInteractedPlayers().size() - 1)) {
            if (!gameManager.getCurrentTurn().getAlreadyHitTile().get(effectVisitor).contains(currentPlayer.getCurrentTile())) {
                gameManager.getCurrentTurn().getAlreadyHitTile().get(effectVisitor).add(currentPlayer.getCurrentTile());
            }
            currentPlayer.setTurnState(TurnState.CAN_BE_COUNTER_ATTACKED);
            TurnStateHandler.manageAddCounterAttackingQueue(p, gameManager);
        }
        if (gameManager.getCurrentTurn().getCounterAttackingPlayers().isEmpty()) {
            currentPlayer.setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
            if (currentPlayer.hasValidPowerUp(gameManager))
                effectVisitor.notifyPowerUps(gameManager, currentPlayer, TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
            else {
                currentPlayer.setTurnState(TurnState.CHOOSE_TARGET);
                effectVisitor.afterExecution(this, gameManager, currentPlayer);
            }
        }
        else {
            currentPlayer.notifyOnLog("Waiting for counter Attack...");
        }
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
