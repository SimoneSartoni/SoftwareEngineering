package model.utility;

import model.board.GameManager;
import model.enums.TurnState;
import model.player.Player;
import model.powerup.PowerUp;

/**
 * class with only static method, is used to handle queues and turnstates after particular actions
 *
 */
public class TurnStateHandler {

    /**
     * it handles what a player needs to do after having discarded a powerUp
     * @param player the player that has discarded the powerUp
     * @param gameManager
     */
    public static void handleAfterDiscardPowerUp(Player player, GameManager gameManager) {
        switch (player.getTurnState()) {
            case DISCARD_POWERUP_FOR_COST_EFFECT: {
                if (player.getPowerUpToDiscardForCost(gameManager.getCurrentTurn().topEffect().getCost(), gameManager).isEmpty()) {
                    player.payCost(gameManager, gameManager.getCurrentTurn().topEffect().getCost());
                    gameManager.getCurrentTurn().topEffect().startingExecution(gameManager.getCurrentTurn().topActions().get(0), gameManager, player);
                }
                else{
                    gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getPowerUpToDiscardForCost(gameManager.getCurrentTurn().topEffect().getCost(), gameManager));
                    player.notifyOnLog("Choose another powerUp to discard for the cost");
                    player.notifyPowerUps(gameManager);
                }
                break;
            }
            case DISCARD_POWERUP_FOR_COST_POWERUP: {
                if (player.getPowerUpToDiscardForCost(player.getInUsePowerUp().getPowerUpEffect().getCost(), gameManager).isEmpty()) {
                    player.payCost(gameManager, player.getInUsePowerUp().getPowerUpEffect().getCost());
                    player.getInUsePowerUp().startEffect(gameManager, player);
                }
                else {
                    gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getPowerUpToDiscardForCost(player.getInUsePowerUp().getPowerUpEffect().getCost(),player.getInUsePowerUp(), gameManager));
                    player.notifyOnLog("Choose another powerUp to discard for the cost");
                    player.notifyPowerUps(gameManager);
                }
                break;
            }
            case DISCARD_POWERUP_FOR_COST_WEAPON:
            case DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION:{
                if (gameManager.getCurrentTurn().getCurrentWeapon()!=null) {
                    if (player.getPowerUpToDiscardForCost(gameManager.getCurrentTurn().getCurrentWeapon().getPartiallyLoadedCost(), gameManager).isEmpty()) {
                        player.getCurrentTile().removeWeapon(gameManager.getCurrentTurn().getCurrentWeapon());
                        player.loadWeaponGrabbed(gameManager.getCurrentTurn().getCurrentWeapon(), gameManager,gameManager.getCurrentTurn().getCurrentWeaponToDrop());
                        gameManager.getCurrentTurn().setCurrentWeaponToDrop(null);
                    }
                    else{
                        gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getPowerUpToDiscardForCost(gameManager.getCurrentTurn().getCurrentWeapon().getPartiallyLoadedCost(), gameManager));
                        player.notifyOnLog("Choose another powerUp to discard for the cost");
                        player.notifyPowerUps(gameManager);
                    }
                } else {
                    if (player.getPowerUpToDiscardForCost(gameManager.getCurrentTurn().getCurrentWeaponToReload().getReloadCost(), gameManager).isEmpty()) {
                        player.reloadWeapon(gameManager, gameManager.getCurrentTurn().getCurrentWeaponToReload());
                        handleAfterReload(gameManager,player);
                    }
                    else{
                        gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getPowerUpToDiscardForCost(gameManager.getCurrentTurn().getCurrentWeaponToReload().getReloadCost(), gameManager));
                        player.notifyOnLog("Choose another powerUp to discard for the cost");
                        player.notifyPowerUps(gameManager);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * it handles what a player needs to do after having reloaded a weapon (in action or at the end of the turn)
     * @param player the player that has discarded the powerUp
     * @param gameManager
     */
    public static void handleAfterReload(GameManager gameManager,Player player){
        gameManager.getCurrentTurn().setCurrentWeaponToReload(null);
       switch(player.getTurnState()){
           case DISCARD_POWERUP_FOR_COST_WEAPON:
           case CHOOSE_WEAPON_TO_RELOAD:
               if (player.getPossibleWeaponsToReload().isEmpty()) {
                   player.setTurnState(TurnState.END_OF_TURN);
                   gameManager.changeTurn();
               }
               else {
                   player.setTurnState(TurnState.CHOOSE_WEAPON_TO_RELOAD);
                   gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(player.getPossibleWeaponsToReload());
                   player.notifyOnLog("Choose another weapon to reload");
                   player.notifyWeapons(gameManager);
               }
               break;
           case DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION:
           case CHOOSE_WEAPON_TO_RELOAD_IN_ACTION:
            if (player.getPossibleWeaponsToReload().isEmpty()) {
                player.setTurnState(TurnState.CHOOSE_WEAPON_HAND);
                player.notifyOnLog("Choose a weapon you wanna shoot with");
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(player.getValidWeapons(gameManager));
                player.notifyWeapons(gameManager);
            }
            else {
                player.setTurnState(TurnState.CHOOSE_WEAPON_TO_RELOAD_IN_ACTION);
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(player.getPossibleWeaponsToReload());
                player.notifyOnLog("Choose another weapon to reload");
                player.notifyWeapons(gameManager);
            }
            break;
            default:break;
        }
    }

    /**
     * method to handle the counter attacking queue after a player has no other possible counter attack to
     * use (or he has chosen not to use them).
     * @param gameManager
     * @param player the current player of the counter attacking queue
     */
    public static void manageCounterAttackingQueue(GameManager gameManager,Player player) {
        int cont = 0;
        CurrentTurn temp=gameManager.getCurrentTurn();
        player.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
        player.notifyOnLog("Waiting for other players now..");
        temp.getCounterAttackingPlayers().remove(player);
        if (temp.getCounterAttackingPlayers().size() > 0) {
            cont = gameManager.getPlayerOrderTurn().indexOf(player);
            for (int i = 0; i < 3; i++) {
                if (cont == gameManager.getPlayerOrderTurn().size() - 1) {
                    cont = 0;}
                else cont++;
                if (temp.getCounterAttackingPlayers().containsKey(gameManager.getPlayerOrderTurn().get(cont))){
                    gameManager.getPlayerOrderTurn().get(cont).setTurnState(TurnState.CHOOSE_COUNTER_ATTACK);
                    gameManager.getPlayerOrderTurn().get(cont).notifyPossibleCommands();
                    gameManager.getPlayerOrderTurn().get(cont).notifyPrintHelp(gameManager);
                    temp.getPossibleChoices().setSelectablePowerUps(gameManager.getPlayerOrderTurn().get(cont).getValidPowerUps(gameManager));
                    gameManager.getPlayerOrderTurn().get(cont).notifyOnLog("Choose a counter attack");
                    gameManager.getPlayerOrderTurn().get(cont).notifyPowerUps(gameManager);
                    return;
                }
            }
        }
        else {
            gameManager.getCurrentPlayerTurn().setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
            if(gameManager.getCurrentPlayerTurn().hasValidPowerUp(gameManager)) {
                gameManager.getCurrentPlayerTurn().notifyPossibleCommands();
                gameManager.getCurrentPlayerTurn().notifyPrintHelp(gameManager);
                temp.getPossibleChoices().setSelectablePowerUps(gameManager.getCurrentPlayerTurn().getValidPowerUps(gameManager));
                gameManager.getCurrentPlayerTurn().notifyOnLog("Choose an after-damage powerUp");
                gameManager.getCurrentPlayerTurn().notifyPowerUps(gameManager);
            }
            else{
                gameManager.getCurrentPlayerTurn().notifyPossibleCommands();
                gameManager.getCurrentPlayerTurn().notifyPrintHelp(gameManager);
                temp.topEffect().afterExecution(temp.topActions().get(0),gameManager, gameManager.getCurrentPlayerTurn());
            }

        }
    }

    /**
     * method to add a player to the counterattacking queue. If the queue is empty then
     * the current player is the one that he's going to choose first the possible counter attack. If it's not then
     * the player is simply queued
     * @param player
     * @param gameManager
     */
    public static void manageAddCounterAttackingQueue(Player player,GameManager gameManager) {
            player.setTurnState(TurnState.CHOOSE_COUNTER_ATTACK);
            if (gameManager.getCurrentTurn().getCounterAttackingPlayers().isEmpty()) {
                if (player.hasValidPowerUp(gameManager)) {
                    gameManager.getCurrentTurn().getCounterAttackingPlayers().putIfAbsent(player, player.getValidPowerUps(gameManager));
                    gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getValidPowerUps(gameManager));
                    player.notifyPossibleCommands();
                    player.notifyPrintHelp(gameManager);
                    player.notifyOnLog("Choose a counter attack");
                    player.notifyPowerUps(gameManager);
                } else {
                    player.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
                }
            } else {
                if (player.hasValidPowerUp(gameManager)) {
                    gameManager.getCurrentTurn().getCounterAttackingPlayers().putIfAbsent(player, player.getValidPowerUps(gameManager));

                } else {
                    player.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
                }
            }
        }

    /**
     * method to deal with the end of the powerUp execution
     * @param player the player that has used the powerUp
     * @param gameManager
     * @param powerUp the current powerUp, whose execution is finished
     */
    public static void afterPowerUpExecution(Player player, GameManager gameManager, PowerUp powerUp){
        switch(powerUp.getTypeOfUse()){
            case AFTER_TAKEN_DAMAGE:{
                player.setTurnState(TurnState.CHOOSE_COUNTER_ATTACK);
                if(player.hasValidPowerUp(gameManager)){
                    gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getValidPowerUps(gameManager));
                    player.notifyOnLog("Choose another powerUp to play");
                    player.notifyPowerUps(gameManager);}
                else {
                    TurnStateHandler.manageCounterAttackingQueue(gameManager,player);
                }
                break;}
            case AFTER_DAMAGE:{
                player.setTurnState(TurnState.CHOOSE_AFTER_DAMAGE_POWERUP);
                if(player.hasValidPowerUp(gameManager)) {
                    gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getValidPowerUps(gameManager));
                    player.notifyOnLog("Choose another powerUp to play");
                    player.notifyPowerUps(gameManager);
                }else {
                    gameManager.getCurrentTurn().topEffect().afterExecution(gameManager.getCurrentTurn().topActions().get(0),gameManager,player);
                }
                break;}
            case SEPARATED:{
                player.setTurnState(TurnState.READY_FOR_ACTION);
                gameManager.getCurrentTurn().setNOfActionMade(gameManager.getCurrentTurn().getNOfActionMade()-1);
                player.handleActions(gameManager);
                break;
            }
            default:break;
        }
    }

    public static void handleSpawnQueue(Player player,GameManager gameManager){
        gameManager.getCurrentGameMode().setTurnAction(player,gameManager.getPlayerOrderTurn());
                if(gameManager.getPlayersInAState(TurnState.READY_TO_RESPAWN).isEmpty()){
                    gameManager.nextPlayer();
                    if(gameManager.getCurrentPlayerTurn().getTurnState()==TurnState.READY_TO_SPAWN) {
                        gameManager.getCurrentPlayerTurn().notifyPossibleCommands();
                        gameManager.spawnDrawPhase(gameManager.getCurrentPlayerTurn(), 2);
                    }
                    else {
                        gameManager.getCurrentPlayerTurn().setTurnState(TurnState.READY_FOR_ACTION);
                        gameManager.getCurrentPlayerTurn().notifyPossibleCommands();
                        gameManager.getCurrentPlayerTurn().notifyOnActions(gameManager);
                    }
                }
                else{
                    gameManager.spawnDrawPhase(gameManager.getPlayersInAState(TurnState.READY_TO_RESPAWN).get(0),1);
                }

    }
}
