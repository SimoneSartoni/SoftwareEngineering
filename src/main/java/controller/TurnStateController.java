package controller;

import model.board.GameManager;
import model.board.TileView;
import model.enums.*;
import model.exceptions.GameException;
import model.exceptions.PowerUpException;
import model.exceptions.WrongCommandException;
import model.player.Player;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.weapon.Effect;
import model.weapon.Weapon;
import model.weapon.actions.Action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TurnStateController implements Serializable {
    private PlayerController playerController;
    private WeaponController weaponController;
    private EffectController effectController;
    private HashMap<TurnState,List<String>> commands;
    private PowerUpController powerUpController;


    public TurnStateController(){

    }
    public TurnStateController(String fileName) {
        powerUpController=new PowerUpController();
        playerController=new PlayerController();
        effectController=new EffectController();
        weaponController=new WeaponController();
        commands = new HashMap<>();
        try (
                BufferedReader buf = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)))
        ) {

            String line = buf.readLine();

            while(line != null) {
                //substitute with a switch case if more identifiers will occur
                if(!line.equals("") && ! line.startsWith("//")) {
                    String [] splitted = line.split("-");
                    String [] commandsToSplit = splitted[1].split("\\s");
                    List<String> commandsString = new ArrayList<>();

                    TurnState toPut = TurnState.ToTurnState(splitted[0]);
                    if(toPut != null) {
                        if (!commandsToSplit[0].equals(".")) {
                            commandsString = Arrays.asList(commandsToSplit);
                        }
                        commands.put(toPut, commandsString);
                    }
                }
                line = buf.readLine();
            }

        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "IO Exception throwed", e);
        }
    }

    //the  choice of nothing is like any other commands and simply makes game go to the next stage of the turn

    /**
     * method to return the possible commands of a player in a specific  turnstate
     * @param p is the player who's trying to execute a command
     * @return the list of possible commands that can be done by the player
     */
    public List<String> getValidCommands(GameManager gameManager,Player p){
        List<String> ret = commands.get(p.getTurnState());
        if(p.getTurnState() == null) {
            return new ArrayList<>();
        }
        switch(p.getTurnState()){
            case READY_FOR_ACTION:
                ret = analyzeActions(gameManager,ret, p);
                break;
            case CHOOSE_WEAPON_TO_RELOAD_MANDATORY:
                ret=analyzeReload(gameManager,ret,p);
                break;
            case CHOOSE_EFFECT:
                ret=analyzeEffects(gameManager,ret,p);
                break;
            case DISCARD_POWERUP_FOR_COST_POWERUP:
            case DISCARD_POWERUP_FOR_COST_EFFECT:
            case DISCARD_POWERUP_FOR_COST_WEAPON:
            case DISCARD_POWERUP_FOR_SPAWN:
            case DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION:
                ret=analyzeDiscardPowerUp(gameManager,ret,p);
                break;
            case CHOOSE_TARGET:
                ret=analyzeEffectActionTarget(gameManager,ret);
                break;
            case CHOOSE_TILE_FOR_WEAPON_ACTION:
                ret=analyzeEffectActionTile(gameManager,ret);
             break;
            default:
                break;
        }

        return ret;
    }

    /**
     * method to return  if a command is valid or not
     * @param gameManager the game in which he's in
     * @param command the command selected
     * @param p the player he's giving the command
     * @return true if the command is valid
     * @throws GameException if the command is not valid
     */
    public boolean isAValidCommand(GameManager gameManager,String command,Player p) throws GameException{
            if(getValidCommands(gameManager,p).contains(command)) {
                return true;
            }
            else{ throw new WrongCommandException(command);}

    }

    private List<String> analyzeEffects(GameManager gameManager,List<String> ret,Player p) {
        List<String> toReturn= new ArrayList<>(ret);
            if(!gameManager.getCurrentTurn().getCurrentWeapon().getBaseEffect().isExecuted())
                toReturn.remove("nothing");
            if(p.getTurnState()==TurnState.CHOOSE_LINKED_EFFECT)
                toReturn.add("nothing");
        return toReturn;

    }

    private List<String> analyzeReload(GameManager gameManager,List<String> ret,Player p){
        List<String> toReturn = new ArrayList<>(ret);
        if(!p.getMandatoryWeapon(gameManager).isEmpty()){
           toReturn.remove("nothing");
        }
        return  toReturn;
    }

    private List<String> analyzeEffectActionTarget(GameManager gameManager,List<String> ret) {
        List<String> toReturn = new ArrayList<>(ret);
        if (isLastValidAction(gameManager)) {
            for (List<Player> players : gameManager.getCurrentTurn().topEffect().getAlreadyInteractedPlayers())
                if (!players.isEmpty()) {
                    break;
                }
        }
        if (gameManager.getCurrentTurn().getPossibleChoices().getSelectableTargets().size() == gameManager.getCurrentTurn().topEffect().effectTargettable.size())
            toReturn.remove("nothing");
        return toReturn;
    }

    private List<String> analyzeEffectActionTile(GameManager gameManager,List<String> ret){
        List<String> toReturn = new ArrayList<>(ret);
        if((gameManager.getCurrentTurn().getAlreadyHitPlayer().containsKey(gameManager.getCurrentTurn().topEffect()))&&(gameManager.getCurrentTurn().getAlreadyHitPlayer().get(gameManager.getCurrentTurn().topEffect()).isEmpty()))
            toReturn.remove("nothing");
        return  toReturn;
    }

    private List<String> analyzeDiscardPowerUp(GameManager gameManager,List<String> ret,Player p){
        List<String> toReturn = new ArrayList<>(ret);
        switch (p.getTurnState()){
            case DISCARD_POWERUP_FOR_COST_EFFECT:
                if(!p.getMandatoryPowerUpsToDiscard(gameManager.getCurrentTurn().topEffect().getCost(),gameManager).isEmpty())
                    toReturn.remove("nothing");
                break;
            case DISCARD_POWERUP_FOR_COST_POWERUP:
                if(!p.getMandatoryPowerUpsToDiscard(p.getInUsePowerUp().getPowerUpEffect().getCost(),gameManager).isEmpty())
                    toReturn.remove("nothing");
                break;
            case DISCARD_POWERUP_FOR_COST_WEAPON:
                if(gameManager.getCurrentTurn().getCurrentWeaponToReload()==null){
                    if(!p.getMandatoryPowerUpsToDiscard(gameManager.getCurrentTurn().getCurrentWeapon().getPartiallyLoadedCost(),gameManager).isEmpty())
                        toReturn.remove("nothing");}
                else {
                    if(!p.getMandatoryPowerUpsToDiscard(gameManager.getCurrentTurn().getCurrentWeaponToReload().getReloadCost(),gameManager).isEmpty())
                        toReturn.remove("nothing");
                }
                break;
            case DISCARD_POWERUP_FOR_SPAWN:
                toReturn.remove("nothing");
                break;
            case DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION:
                if(!p.getMandatoryPowerUpsToDiscard(gameManager.getCurrentTurn().getCurrentWeaponToReload().getReloadCost(),gameManager).isEmpty())
                    toReturn.remove("nothing");
                break;
            default: break;
        }
        return toReturn;
    }

    private List<String> analyzeChoices(GameManager gameManager,List<String> ret,Player p){
        List<String> toReturn=new ArrayList<>(ret);
                if(isLastValidAction(gameManager))
                    toReturn.remove("nothing");
        return toReturn;
    }

    private boolean isLastValidAction(GameManager gameManager){
        Effect effect=gameManager.getCurrentTurn().topEffect();
        Action action=gameManager.getCurrentTurn().topActions().get(0);
        if((effect.getActions().indexOf(action)==effect.getActions().size()-1)||((effect.getActions().indexOf(action)==effect.getActions().size()-2)&&(action.getLinkedToNext()!=null)))
                return true;
        return false;
    }

    private List<String> analyzeActions(GameManager gameManager,List<String> ret, Player p) {
        List<String> toReturn = new ArrayList<>(ret);
        if(gameManager.getCurrentTurn().getNOfActionMade() < p.getCurrentTurnAction().getMaxNOfActions())
            toReturn.remove("nothing");
        return toReturn;
    }


    /**
     *  method to handle the choice of a tileView. Different methods called in different turnstates
     * @param tile the TileView selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the TileView selected is not valid
     */
    public void handleChooseTile(TileView tile, Player player, GameManager gameManager)throws  GameException{
        switch (player.getTurnState()){
            case CHOOSE_TILE_FOR_WEAPON_ACTION:{
                effectController.chooseTileAction(gameManager,player,tile);
                break;
            }
            case CHOOSE_TILE_FOR_SHOOT_ACTION:{
                playerController.chooseTileShoot(player,tile,gameManager);
                break;
            }
            case CHOOSE_POWERUP_TILE:{
                powerUpController.afterChoosingTile(player.getInUsePowerUp(),gameManager,player,tile);
                break;
            }
            case CHOOSE_TILE_FOR_GRAB_ACTION:{
                playerController.chooseTileGrab(player,gameManager,tile);
                break;
            }
            case CHOOSE_TILE_FOR_RUN_ACTION: {
                playerController.chooseTileRun(player,gameManager, tile);
                break;
            }
            default:break;
        }
    }

    /**
     *  method to handle the choice of a PlayerView. Different methods called in different turnstates
     * @param targetPlayer the PlayerView selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the PlayerView selected is not valid
     */
    public void handleChooseTarget(PlayerView targetPlayer, Player player, GameManager gameManager)throws GameException{
        switch (player.getTurnState()){
            case CHOOSE_TARGET: {
                effectController.chooseTargetAction(gameManager, player, targetPlayer);
                break;
            }
            case CHOOSE_POWERUP_TARGET: {
                powerUpController.afterChoosingTarget(player.getInUsePowerUp(),gameManager,player,targetPlayer);
                break;
            }
            default:break;
        }
    }

    /**
     *  method to handle the choice of a powerUp. Different methods called in different turnstates
     * @param powerUp the powerUp selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the powerUp selected is not valid
     */
    public void handlePowerUp(PowerUp powerUp,Player player,GameManager gameManager) throws GameException{
        switch ( player.getTurnState()){
            case DISCARD_POWERUP_FOR_SPAWN:
                playerController.spawn(player,powerUp,gameManager);
                break;

            case DISCARD_POWERUP_FOR_COST_POWERUP:
                playerController.discardPowerUpForPowerUp(player,gameManager, powerUp);
                break;

            case DISCARD_POWERUP_FOR_COST_WEAPON:
                playerController.discardPowerUpForWeapon(player,gameManager, powerUp);
                break;

            case DISCARD_POWERUP_FOR_COST_EFFECT:
                playerController.discardPowerUpForEffect(player,gameManager, powerUp);
                break;

            case DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION:
                playerController.discardPowerUpForWeapon(player,gameManager, powerUp);
                break;

            case CHOOSE_AFTER_DAMAGE_POWERUP:
                playerController.chooseAfterDamagePowerUp(player,gameManager, powerUp);
                break;

            case CHOOSE_POWERUP:
                playerController.chooseSeparatedPowerUp(player,gameManager,powerUp);
                break;

            case CHOOSE_COUNTER_ATTACK:
                try {
                    playerController.chooseCounterAttackPowerUp(player,gameManager, powerUp);
                }
                catch (PowerUpException e){
                    //nothing
                }
                break;
            default:break;
        }
    }

    /**
     *  method to handle the choice of a direction. Different methods called in different turnstates
     * @param direction the direction selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the direction selected is not valid
     */
    public void handleDirection(GameManager gameManager, Player player, Direction direction) throws GameException{
        effectController.chooseDirectionAction(gameManager,player,direction);
    }

    /**
     *  method to handle the choice of a room. Different methods called in different turnstates
     * @param roomColor the room selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the room selected is not valid
     */
    public void handleRoom(GameManager gameManager, Player player, RoomColor roomColor) throws GameException{
        effectController.chooseRoomAction(gameManager,player,roomColor);
    }

    /**
     *  method to handle the choice of an action. Different methods called in different turnstates
     * @param typeOfAction the action selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the action selected is not valid
     */
    public void handleActions(GameManager gameManager, TypeOfAction typeOfAction, Player player) throws GameException{
        switch (typeOfAction){
            case RUN:
                playerController.run(player,gameManager);
                break;
            case GRAB:
                playerController.grab(player,gameManager);
                break;
            case SHOOT:
                playerController.shoot(player,gameManager);
                break;
            case POWER_UP:
                playerController.powerUpSeparated(player,gameManager);
        }
    }

    /**
     *  method to handle the choice of nothing (player had an optional choice
     *  and decided to choose nothing among the possiblities.
     *  Different methods called in different turnstates
     * @param player the player is giving the command
     * @param gameManager the game the player is ind
     */
    public void handleNothing(GameManager gameManager,Player player){
        switch (player.getTurnState()){
            case CHOOSE_WEAPON_TO_RELOAD_IN_ACTION:
                playerController.noReloadInAction(player,gameManager);
                break;
            case DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION :
            case DISCARD_POWERUP_FOR_COST_WEAPON:
                playerController.noPowerUpForCostWeapon(player,gameManager);
                break;
            case DISCARD_POWERUP_FOR_COST_EFFECT:
                playerController.noPowerUpForCostEffect(player,gameManager);
                effectController.startingAction(gameManager,player);
                break;
            case DISCARD_POWERUP_FOR_COST_POWERUP:
                playerController.noPowerUpForCostPowerUp(gameManager,player);
                break;
            case CHOOSE_AFTER_DAMAGE_POWERUP:
                effectController.noPowerUpSelectedAfterDamage(player,gameManager);
                break;
            case CHOOSE_TYPE_OF_EFFECT:
                weaponController.clear(gameManager,player,gameManager.getCurrentTurn().getCurrentWeapon());
                break;
            case CHOOSE_EFFECT:
                weaponController.clear(gameManager,player,gameManager.getCurrentTurn().getCurrentWeapon());
                break;
            case CHOOSE_COUNTER_ATTACK:
                playerController.noCounterAttackChosen(player,gameManager);
                break;
            case CHOOSE_LINKED_EFFECT:
                for(Effect e:gameManager.getCurrentTurn().getPossibleChoices().getSelectableOptionalEffects())
                    if (e.isAfterBaseLinked())
                        e.setCanBeUsed(true);
                    else
                        e.setCanBeUsed(false);
                effectController.startingAction(gameManager,player);
                break;
            case CHOOSE_TARGET:
                effectController.afterExecution(gameManager,player);
                break;
            case CHOOSE_ROOM:
                effectController.afterExecution(gameManager,player);
                break;
            case CHOOSE_WEAPON_TO_RELOAD:
                playerController.endOfTurn(gameManager);
                break;
            case CHOOSE_TILE_FOR_WEAPON_ACTION:
                effectController.afterExecution(gameManager,player);
                break;
            case READY_FOR_ACTION:
		        playerController.handleReload(gameManager, player);
                break;
            default: break;

        }
    }

    /**
     *  method to handle the choice of an Effect. Different methods called in different turnstates
     * @param effect the Effect selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the selected effect is not valid
     */
    public void handleEffect(GameManager gameManager, Player player, Effect effect) throws  GameException{
        weaponController.optionalEffect(effect,gameManager,player,gameManager.getCurrentTurn().getCurrentWeapon());
    }

    /**
     *  method to handle the choice of an ammo. Different methods called in different turnstates
     * @param ammoColor the ammo selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the selected ammo is not valid
     */
    public void handleAmmo(GameManager gameManager, Player player, AmmoColor ammoColor) throws GameException{
        playerController.chooseAmmoForCost(player,ammoColor,gameManager);
    }

    /**
     *  method to handle the choice of a typeOfEffect. Different methods called in different turnstates
     * @param typeOfEffect the typeOfEffect selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the selected typeOfEffect is not valid
     */
    public void handleTypeOfEffect(GameManager gameManager,Player player,TypeOfEffect typeOfEffect) throws GameException {
        switch (typeOfEffect){
            case ALTERNATIVE:
                weaponController.alternativeEffect(gameManager,player,gameManager.getCurrentTurn().getCurrentWeapon());
                break;
            case OPTIONAL:
                weaponController.optionalTypeChosen(gameManager,player,gameManager.getCurrentTurn().getCurrentWeapon());
                break;
            case BASE:
                weaponController.baseEffect(gameManager,player,gameManager.getCurrentTurn().getCurrentWeapon());
                break;
        }
    }

    /**
     * method to handle the choice of a weapon. Different methods called in different turnstates
     * @param weapon the weapon selected
     * @param player the player is giving the command
     * @param gameManager the game the player is in
     * @throws GameException if the Weapon selected is not valid
     */
    public void handleWeapon(GameManager gameManager, Player player, Weapon weapon) throws GameException {
        switch (player.getTurnState()){
            case CHOOSE_WEAPON_HAND:
                playerController.chooseWeaponShoot(player,weapon,gameManager);
                break;
            case CHOOSE_WEAPON_TO_RELOAD:
                playerController.chooseWeaponToReload(player,gameManager,weapon);
                break;
            case CHOOSE_WEAPON_TO_TAKE:
                playerController.chooseWeaponToGrab(player,gameManager,weapon);
                break;
            case CHOOSE_WEAPON_TO_DROP:
                playerController.chooseWeaponToSwap(player,gameManager,weapon);
                break;
            case CHOOSE_WEAPON_TO_RELOAD_IN_ACTION:
            case CHOOSE_WEAPON_TO_RELOAD_MANDATORY:
                playerController.chooseWeaponToReload(player,gameManager,weapon);
                break;
        }
    }

}
