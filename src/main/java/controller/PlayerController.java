package controller;

import model.board.GameManager;
import model.board.TileView;
import model.enums.AmmoColor;
import model.enums.TypeOfAction;
import model.exceptions.AmmoException;
import model.exceptions.GameException;
import model.player.Player;
import model.player.RunHandler;
import model.powerup.PowerUp;
import model.utility.TurnStateHandler;
import model.weapon.Weapon;

import java.io.Serializable;

public class PlayerController implements Serializable {

    /**
     *  method called when a player wants to shoot
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @throws GameException if the action is not valid
     */
    public void shoot(Player player, GameManager gameManager) throws GameException {
        if(player.isValidAction(TypeOfAction.SHOOT,gameManager))
            player.chooseTileShootingAction(gameManager);
        }


    /**
     * method called when a player wants to grab
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @throws GameException if the action is not valid
     */
    public void grab(Player player, GameManager gameManager) throws  GameException{
        if (player.isValidAction(TypeOfAction.GRAB,gameManager)) {
            player.chooseTileGrabAction(gameManager);
        }
    }


    /**
     * method called when a player wants to run
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @throws GameException if the action is not valid
     */
    public void run(Player player, GameManager gameManager) throws GameException {
        if (player.isValidAction(TypeOfAction.RUN,gameManager)) {
            player.chooseTileMovementAction(gameManager);
        }
    }


    /**
     * method called when a player decides to use a powerUp separated for action
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @throws GameException if the action is not valid
     */
    public void powerUpSeparated(Player player, GameManager gameManager) throws GameException {
        if (player.isValidAction(TypeOfAction.POWER_UP,gameManager))
            player.separatedPowerUpAction(gameManager);
    }

    /**
     * method called when a player chooses a tile to move in before shooting
     * @param t the tileView selected
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @throws GameException if the TileView is not valid
     */
    public void chooseTileShoot(Player player, TileView t, GameManager gameManager) throws GameException{
            player.shootingActionAfterTile(player.isAValidShootingAction(gameManager,t), gameManager);
    }

    /**
     * method called when a player wants to reload a weapon
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param w the weapon chosen
     * @throws GameException if the weapon is not valid
     */
    public void chooseWeaponToReload(Player player, GameManager gameManager, Weapon w) throws GameException {
            player.startReloading(gameManager,player.isAValidWeaponToReload(gameManager,w));
    }

    /**
     * method called when a player discards a powerUp for paying the cost of a powerUp
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param powerUp powerUp chosen
     * @throws GameException if the powerUp is not valid
     */
    public void discardPowerUpForPowerUp(Player player, GameManager gameManager, PowerUp powerUp) throws GameException {
        PowerUp p=player.isASelectablePowerUp(powerUp,gameManager);
            player.discardPowerUpForAmmos(p, gameManager);
        if(p.getPowerUpEffect().getCost().getRedValue()>=0)
            TurnStateHandler.handleAfterDiscardPowerUp(player, gameManager);
    }

    /**
     * method called when a player is choosing a powerUp to discard for paying the cost of a weapon
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param powerUp powerUp chosen
     * @throws GameException if the powerUp is not valid
     */
    public void discardPowerUpForWeapon(Player player,GameManager gameManager,PowerUp powerUp) throws GameException {
        player.discardPowerUpForAmmos(player.isASelectablePowerUp(powerUp,gameManager), gameManager);
        TurnStateHandler.handleAfterDiscardPowerUp(player,gameManager);
    }

    /**
     * method called when a player chooses a powerUp to discard for effect
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param powerUp the powerUp selected
     * @throws GameException if the powerUp is not valid
     */
    public void discardPowerUpForEffect(Player player,GameManager gameManager,PowerUp powerUp) throws GameException{
        player.discardPowerUpForAmmos(player.isASelectablePowerUp(powerUp,gameManager),gameManager);
        TurnStateHandler.handleAfterDiscardPowerUp(player,gameManager);
    }

    /**
     * method called when a player chooses a weapon to shoot
     * @param w the weapon selected
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @throws GameException if the weapon selected is not valid
     */
    public void chooseWeaponShoot(Player player,Weapon w,GameManager gameManager) throws GameException {
        player.startShootingWithAWeapon(gameManager, player.isAValidWeaponToShoot(gameManager,w));
    }

    /**
     * method called when a player chooses a tile to grab
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param t the TileView selected to grab
     * @throws GameException if the tileView selected is not valid
     */
    public void chooseTileGrab(Player player,GameManager gameManager,TileView t) throws GameException{
        player.grabActionAfterTile(gameManager,player.isAValidGrabAction(gameManager,t));
    }

    /**
     * method called when the player is grabbing a weapon
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param w the Weapon selected to grab
     * @throws GameException if the Weapon is not valid to grab
     */
    public void chooseWeaponToGrab(Player player,GameManager gameManager,Weapon w) throws GameException{
        player.pickWeapon(player.isAnAvailableWeapon(gameManager,w),gameManager);
    }

    /**
     * method called when the player is swapping weapons
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param w the Weapon selected to drop
     * @throws GameException if the Weapon is not valid to drop
     */
    public void chooseWeaponToSwap(Player player,GameManager gameManager,Weapon w) throws GameException{
        player.swapWeapon(player.isAValidWeaponToDrop(gameManager,w),gameManager);
    }

    /**
     * method called when  player chooses a TileView to run in
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param t1 the TileView selected
     * @throws GameException if the Tile selected is not valid
     */
    public void chooseTileRun(Player player,GameManager gameManager,TileView t1) throws GameException{
            RunHandler.movementActionAfterTile(gameManager,player.isAValidMovementAction(gameManager,t1),player);
    }

    /**
     * method called when a player chooses a powerUp for separated action
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param powerUp the powerUp selected for separated action
     * @throws GameException if the separated powerUp is not valid
     */
    public void chooseSeparatedPowerUp (Player player,GameManager gameManager, PowerUp powerUp) throws GameException{
            player.afterChosenPowerUp(gameManager,player.isASelectablePowerUp(powerUp,gameManager));
    }

    /**
     *
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param powerUp the powerUps selected after damage
     * @throws GameException if the after damage selected is not valid
     */
    public void chooseAfterDamagePowerUp(Player player,GameManager gameManager, PowerUp powerUp) throws GameException{
            player.afterChosenPowerUp(gameManager,player.isASelectablePowerUp(powerUp,gameManager));
    }

    /**
     * method called when a player uses a counter attack
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @param powerUp the powerUp selected for counter attack
     * @throws GameException if the counter attack selected is not valid
     */
    public void chooseCounterAttackPowerUp(Player player,GameManager gameManager,PowerUp powerUp) throws GameException{
        player.afterChosenPowerUp(gameManager,player.isASelectablePowerUp(powerUp,gameManager));
    }

    /**
     * method called when a player chooses an ammo to pay the cost
     * @param ammoColor the ammo color selected
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @throws GameException if the ammo selected is not valid
     */
    public void chooseAmmoForCost(Player player,AmmoColor ammoColor,GameManager gameManager) throws GameException{
        if(player.isAValidAmmo(gameManager,ammoColor))
            player.afterChooseAmmo(gameManager,ammoColor);
        else throw  new AmmoException("Wrong choice of ammo");
    }

    /**
     * method called when a player decides not to use a counter attack
     * @param player the player is playing
     * @param gameManager the game the player is in
     */
    public void noCounterAttackChosen(Player player,GameManager gameManager){
        TurnStateHandler.manageCounterAttackingQueue(gameManager,player);
    }

    /**
     * method called when the turn needs to change because no weapons were
     * selected to reload
     * @param gameManager the game the player is in
     */
    public void endOfTurn(GameManager gameManager){
        gameManager.changeTurn();
    }

    /**
     * method called when a player decides to shoot without reloading any weapon
     * @param player the player is playing
     * @param gameManager the game the player is in
     */
    public void noReloadInAction(Player player,GameManager gameManager){
        player.chooseWeaponToShoot(gameManager);
    }

    /**
     * method called when a player selects a powerUp to spawn
     * @param powerUp the powerUp select
     * @param player the player is playing
     * @param gameManager the game the player is in
     * @throws GameException if the powerUp selected is not valid
     */
    public void spawn(Player player,PowerUp powerUp,GameManager gameManager) throws GameException{
        gameManager.spawnSetPosition(player,player.isASelectablePowerUp(powerUp,gameManager));
    }

    /**
     * method called when no powerUp are selected to pay a cost of a Weapon
     * @param player the player is playing
     * @param gameManager the game the player is in
     */
    public void noPowerUpForCostWeapon(Player player,GameManager gameManager) {
        if (gameManager.getCurrentTurn().getCurrentWeapon() != null) {
            player.getCurrentTile().removeWeapon(gameManager.getCurrentTurn().getCurrentWeapon());
            player.loadWeaponGrabbed(gameManager.getCurrentTurn().getCurrentWeapon(), gameManager,gameManager.getCurrentTurn().getCurrentWeaponToDrop());
        }
        else {
            player.reloadWeapon(gameManager, gameManager.getCurrentTurn().getCurrentWeaponToReload());
            TurnStateHandler.handleAfterReload(gameManager,player);
        }
    }

    /**
     * method called when no powerUp are selected to pay a cost for a powerUp
     * @param player the player is playing
     * @param gameManager the game the player is in
     */
    public void noPowerUpForCostPowerUp(GameManager gameManager,Player player){
        if(player.getInUsePowerUp().getPowerUpEffect().getCost().getRedValue()<0)
            player.chooseAmmoToDiscard(gameManager);
        else player.payCost(gameManager,player.getInUsePowerUp().getPowerUpEffect().getCost());

    }

    /**
     *  method called if the player  chooses no powerUp when he's paying acost
     * @param player the player is playing
     * @param gameManager the game the player is in
     */
    public void noPowerUpForCostEffect(Player player,GameManager gameManager){
        player.payCost(gameManager,gameManager.getCurrentTurn().topEffect().getCost());
    }


    /**
     * method called if the player is ending his turn and needs to reload
     * @param player the player is playing
     * @param gameManager the game the player is in
     */
    public void handleReload(GameManager gameManager, Player player) {
        player.handleReload(gameManager);
    }
}
