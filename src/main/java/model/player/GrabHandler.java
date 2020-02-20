package model.player;

import model.board.AmmoTile;
import model.board.GameManager;
import model.board.Tile;
import model.board.TileView;
import model.enums.LoadedState;
import model.enums.TurnState;
import model.exceptions.AmmoException;
import model.exceptions.GrabException;
import model.exceptions.WeaponsException;
import model.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

public class GrabHandler {
    /**
     * checks if the grab action is valid or not
     * @param gameManager
     * @param player
     * @return
     */
    public static boolean isValidGrab(GameManager gameManager,Player player) {
        for (Tile t : gameManager.getPossibleTiles(0, player.getCurrentTurnAction().getMovementBeforeGrabbing(), player.getCurrentTile()))
            if (((t.isSpawnPoint()) && (!t.getWeapons().isEmpty()) && (!player.getPossibleWeaponToGrab(t).isEmpty())) || (t.getAmmo() != null))
                return true;
       return false;
    }

    /**
     * checks if the selected tile is a valid tile for grab
     * @param gameManager
     * @param player the player who's grabbing
     * @param t1 tile
     * @return the Tile corresponding to the tile view selected
     * @throws GrabException if the tile selected is not valid. the error message depends on the type of constraint
     * that is not respected
     */
    public static Tile isAValidGrabAction(GameManager gameManager, Player player, TileView t1) throws GrabException {
        if (t1 == null) {
            throw new GrabException("Null Tile");
        }

        Tile toRet = GameManager.getTileFromList(gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles(), t1);
        if(toRet != null) {
            return toRet;
        }

        Tile tileToView = GameManager.getTileFromMap(gameManager.getTiles(), t1);

        if (gameManager.getDistanceBetweenTiles(tileToView, player.getCurrentTile()) <= player.getCurrentTurnAction().getMovementBeforeGrabbing()) {
            if (t1.isSpawnPoint()) {
                if ((t1.getWeapons().isEmpty()) || (player.getPossibleWeaponToGrab(tileToView).isEmpty())) {
                    throw new GrabException("No valid weapons to grab in this Tile");
                }
            } else {
                if (t1.getAmmo() == null) {
                    throw new GrabException("No ammo tile in this tile");
                }
            }
        }
        throw new GrabException("Not reachable tile");
    }

    /**
     * this method checks if the player has to draw an ammo card or if he has to choose a weapon to pick
     * @param gameManager
     * @param player the player that is grabbing
     * @param t1 the tile in which the player is grabbing
     */
    public static void grabActionAfterTile(GameManager gameManager,Player player, Tile t1) {
        Tile currentTile=player.getCurrentTile();
        if(! t1.equals(currentTile)) {
            currentTile.removePlayer(player);
            player.setCurrentTile(t1);
            t1.addPlayer(player);
            gameManager.notifyOnMovement(player, t1);
        }
        if (t1.isSpawnPoint()){
            player.notifyOnLog("Choose a Weapon to pick in your tile");
            player.setTurnState(TurnState.CHOOSE_WEAPON_TO_TAKE);
            gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(player.getPossibleWeaponToGrab(t1));
            player.notifyWeapons(gameManager);
        }
        else {
            player.pickAmmoTile(t1.getAmmo(), gameManager);
            AmmoTile ammoTile=t1.removeAmmo();
            gameManager.getAmmoTileDeck().addToDiscardPile(ammoTile);
            gameManager.notifyOnAmmoTileGrab(ammoTile,player);
            player.handleActions(gameManager);
        }
    }

    /**
     * method to search for the possible tiles for grab action
     * @param gameManager
     * @param player
     */
    public static void chooseTileGrabAction(GameManager gameManager,Player player){
        List<Tile> targettableTiles=new ArrayList<>();
        for(Tile t:gameManager.getPossibleTiles(0,player.getCurrentTurnAction().getMovementBeforeGrabbing(),player.getCurrentTile())){
            if(((t.isSpawnPoint())&&(!t.getWeapons().isEmpty())&&(!player.getPossibleWeaponToGrab(t).isEmpty()))||(t.getAmmo()!=null))
                targettableTiles.add(t); }
        gameManager.getCurrentTurn().getPossibleChoices().setSelectableTiles(targettableTiles);
        player.notifyOnLog("Choose a tile where you want to grab");
        player.setTurnState(TurnState.CHOOSE_TILE_FOR_GRAB_ACTION);
        player.notifyTiles(gameManager);
    }

    /**
     * method to check if the weapon is available to grab (checking cost and presence in the corresponding tile)
     * @param gameManager
     * @param newWeapon the weapon the player wants to grab
     * @param player
     * @return the corresponding weapon present in possible choices
     * @throws AmmoException
     * @throws WeaponsException
     */
    public static Weapon isAnAvailableWeapon(GameManager gameManager,Weapon newWeapon,Player player) throws AmmoException, WeaponsException {
        for(Weapon w:gameManager.getCurrentTurn().getPossibleChoices().getSelectableWeapons())
            if(w.equals(newWeapon))
                return w;
        if (! player.getCurrentTile().getWeapons().contains(newWeapon)){
            throw new WeaponsException("Weapon not present");
        }
        throw new AmmoException("Not enough ammos to grab this weapon");
    }

    /**
     * in this method the player picks a weapon and puts it in his hand
     * it checks if he needs to drop a weapon or if he wants to discard a powerup
     * @param newWeapon the weapon grabbed
     * @param gameManager the current game
     * @param player the player who's grabbing
     */
    public static void pickWeapon( Weapon newWeapon,GameManager gameManager,Player player) {
        if (player.getWeapons().size() == 3) {
            gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(player.getWeapons());
            gameManager.getCurrentTurn().setCurrentWeapon(newWeapon);
            player.notifyOnLog("Choose a weapon to drop from your hand");
            player.setTurnState(TurnState.CHOOSE_WEAPON_TO_DROP);
            player.notifyWeapons(gameManager);
        }
        else {
            player.getWeapons().add(newWeapon);
            if(!player.getPowerUpToDiscardForCost(newWeapon.getPartiallyLoadedCost(),gameManager).isEmpty()){
                gameManager.getCurrentTurn().setCurrentWeapon(newWeapon);
                gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getPowerUpToDiscardForCost(newWeapon.getPartiallyLoadedCost(),gameManager));
                player.notifyOnLog("Choose a powerUp to discard to pay the cost of the Weapon");
                player.setTurnState(TurnState.DISCARD_POWERUP_FOR_COST_WEAPON);
                player.notifyPowerUps(gameManager);}
            else {
                player.getCurrentTile().removeWeapon(newWeapon);
                player.loadWeaponGrabbed(newWeapon,gameManager,null);
            }
        }
    }

    /**
     *  method to check if a weapon can be dropped or not
     * @param gameManager
     * @param w the weapon selected
     * @return the weapon that can be dropped
     * @throws WeaponsException if the weapon can't be dropped
     */
    public static Weapon isAValidWeaponToDrop(GameManager gameManager,Weapon w) throws WeaponsException{
        for(Weapon weapon:gameManager.getCurrentTurn().getPossibleChoices().getSelectableWeapons())
            if(w.equals(weapon))
                return weapon;
        throw new WeaponsException("Not owned weapon");
    }

    /**
     *  method used to swap weapons during a grab action.Then it makes the player pay the cost of the new weapon
     * @param removedWeapon the weapon dropped by the player
     * @param gameManager
     * @param player the player that is grabbing
     */
    public static void swapWeapon(Weapon removedWeapon,GameManager gameManager,Player player) {
        player.getWeapons().remove(removedWeapon);
        player.getCurrentTile().addWeapon(removedWeapon);
        removedWeapon.setLoaded(LoadedState.PARTIALLY_LOADED);
        Weapon newWeapon=gameManager.getCurrentTurn().getCurrentWeapon();
        player.getWeapons().add(newWeapon);
        player.getCurrentTile().removeWeapon(newWeapon);
        gameManager.getCurrentTurn().setCurrentWeaponToDrop(removedWeapon);
        if (!player.getPowerUpToDiscardForCost(newWeapon.getPartiallyLoadedCost(),gameManager).isEmpty()){
            player.notifyOnLog("Choose a powerUp to discard to pay the cost of the Weapon");
            player.setTurnState(TurnState.DISCARD_POWERUP_FOR_COST_WEAPON);
            gameManager.getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getPowerUpToDiscardForCost(newWeapon.getPartiallyLoadedCost(),gameManager));
            player.notifyPowerUps(gameManager);}
        else {
            player.loadWeaponGrabbed(newWeapon, gameManager,removedWeapon);
        }
    }

    /**
     * method to end the grab action
     * @param player the current player
     * @param gameManager
     */
    public static void afterWeaponGrabbed(Player player,GameManager gameManager) {
        player.handleActions(gameManager);
    }
}
