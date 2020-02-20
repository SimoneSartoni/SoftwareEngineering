package model.player;

import model.board.GameManager;
import model.board.Tile;
import model.board.TileView;
import model.board.TurnAction;
import model.enums.LoadedState;
import model.enums.TurnState;
import model.exceptions.ShootingException;
import model.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

import static model.enums.TurnState.CHOOSE_WEAPON_TO_RELOAD_MANDATORY;

public class ShootHandler {

    /**
     * method used to calculate if the shooting action is valid or not.
     * It generates all the possible path searching for all the possibilities(in terms of tile selection and weapon selection).
     * If it finds a path in which the player can shoot, then it will return true
     * @param player the current player
     * @param gameManager the current game
     * @return true if it's possible to shoot, false otherwise
     */
    public static boolean isValidShoot(Player player,GameManager gameManager) {
        if (player.getWeapons().isEmpty())
            return false;
        Tile currentTile = player.getCurrentTile();
        currentTile.removePlayer(player);
        for (Tile t : gameManager.getPossibleTiles(0, player.getCurrentTurnAction().getMovementBeforeShooting(), currentTile)) {
            player.setCurrentTile(t);
            t.addPlayer(player);
            for (Weapon w : player.getWeapons()) {
                if (!player.getCurrentTurnAction().isReloadBeforeShooting()) {
                    if (w.isValid(player, gameManager)) {
                        player.setCurrentTile(currentTile);
                        t.removePlayer(player);
                        currentTile.addPlayer(player);
                        return true;
                    }
                } else {
                    if (w.isValid(player, gameManager)) {
                        player.setCurrentTile(currentTile);
                        t.removePlayer(player);
                        currentTile.addPlayer(player);
                        return true;
                    }
                    else {
                        LoadedState loadedState = w.getLoaded();
                        w.setLoaded(LoadedState.LOADED);
                        if ((loadedState == LoadedState.UNLOADED) && (player.getPotentialAmmos().hasCorrectCost(w.getReloadCost())) && (w.isValid(player, gameManager))) {
                            player.setCurrentTile(currentTile);
                            t.removePlayer(player);
                            w.setLoaded(loadedState);
                            currentTile.addPlayer(player);
                            return true;
                        }
                        w.setLoaded(loadedState);
                    }
                }
            }
            t.removePlayer(player);
        }
        player.setCurrentTile(currentTile);
        currentTile.addPlayer(player);
        return false;
    }

    /**
     *  method to check if it's a valid shooting action
     * @param player the current player
     * @param gameManager the current game
     * @param t1 the tileView selected to shoot
     * @return true if it's a valid shooting action
     * @throws ShootingException if the action is not valid. the error message depends on why the action is not possible
     */
    public static Tile isAValidShootingAction(Player player, GameManager gameManager, TileView t1) throws ShootingException{
        LoadedState loadedState;
        for (Tile t : gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles())
            if (t.getX() == t1.getX() && t.getY() == t1.getY())
                return t;
        Tile temp=player.getCurrentTile();
        int cont=0;
        if (t1 == null) {
            throw new ShootingException("Out of bounds");
        }

        Tile dummyTile = new Tile();
        for (Tile t : gameManager.getCurrentTurn().getPossibleChoices().getSelectableTiles())
            if (t.getX() == t1.getX() && t.getY() == t1.getY())
                dummyTile = t;
        if (gameManager.getDistanceBetweenTiles(dummyTile, player.getCurrentTile()) <= player.getCurrentTurnAction().getMovementBeforeShooting()) {
            for (Weapon w : player.getWeapons()) {
                player.setCurrentTile(dummyTile);
                if (!player.getCurrentTurnAction().isReloadBeforeShooting())
                    if (!w.isValid(player, gameManager)) {
                        cont++;
                    } else {
                        loadedState = w.getLoaded();
                        w.setLoaded(LoadedState.LOADED);
                        if ((loadedState == LoadedState.UNLOADED) && (player.getPotentialAmmos().hasCorrectCost(w.getReloadCost())) && (w.isValid(player, gameManager))) {
                            w.setLoaded(loadedState);
                        } else {
                            w.setLoaded(loadedState);
                            cont++;
                        }
                    }
            }
            player.setCurrentTile(temp);
            if (cont==player.getWeapons().size()) {
                throw new ShootingException("no valid weapons in this tile");
            }
        }
        throw new ShootingException("not a reachable tile");
    }

    public static void chooseTileShootingAction(Player player,GameManager gameManager){
        List<Tile> targettableTiles=new ArrayList<>();
        LoadedState loadedState;
        Tile temp=player.getCurrentTile();
        temp.removePlayer(player);
        for(Tile t:gameManager.getPossibleTiles(0,player.getCurrentTurnAction().getMovementBeforeShooting(),temp)) {
            t.addPlayer(player);
            player.setCurrentTile(t);
            for (Weapon w : player.getWeapons()) {
                if (!player.getCurrentTurnAction().isReloadBeforeShooting()) {
                    if (w.isValid(player, gameManager)) {
                        targettableTiles.add(t);
                        break;
                    }
                } else {
                    if (w.isValid(player, gameManager)) {
                        targettableTiles.add(t);
                        break;
                    } else {
                        loadedState = w.getLoaded();
                        w.setLoaded(LoadedState.LOADED);
                        if ((loadedState == LoadedState.UNLOADED) && (player.getPotentialAmmos().hasCorrectCost(w.getReloadCost())) && (w.isValid(player, gameManager))) {
                            targettableTiles.add(t);
                            w.setLoaded(loadedState);
                            break;
                        }
                        w.setLoaded(loadedState);
                    }
                }
            }
            t.removePlayer(player);
        }
        temp.addPlayer(player);
        player.setCurrentTile(temp);
        if((targettableTiles.size()==1)&&(targettableTiles.get(0).equals(temp))){
            player.shootingActionAfterTile(player.getCurrentTile(), gameManager);
        }
        else{
            player.notifyOnLog("Choose a tile where you want to move before shooting");
            player.setTurnState(TurnState.CHOOSE_TILE_FOR_SHOOT_ACTION);
            gameManager.getCurrentTurn().getPossibleChoices().setSelectableTiles(targettableTiles);
            player.notifyTiles(gameManager);
        }
    }

    /**
     * method to do the shooting action after the player has chosen a valid tile where he wants to move before shooting
     * it distinguish before different cases depending on if the game is in final frenzy or not (can reload before shoot)
     * @param gameManager the current game
     * @param player the player's shooting
     * @param t1 the selected tile
     */
    public static void ShootingActionAfterTile(GameManager gameManager,Player player,Tile t1){
        Tile currentTile=player.getCurrentTile();
        TurnAction currentTurnAction=player.getCurrentTurnAction();
        if (!t1.equals(currentTile)) {
            currentTile.removePlayer(player);
            t1.addPlayer(player);
            player.setCurrentTile(t1);
            gameManager.notifyOnMovement(player, t1);
        }
        if (currentTurnAction.isReloadBeforeShooting()) {
            if(!player.getMandatoryWeapon(gameManager).isEmpty()) {
                player.setTurnState(CHOOSE_WEAPON_TO_RELOAD_MANDATORY);
                player.notifyOnLog("Choose one weapon to reload before shooting(You have to choose one of the following weapon, if you wanna shoot)");
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(player.getMandatoryWeapon(gameManager));
            }
            else
            if(!player.getPossibleWeaponsToReload().isEmpty()) {
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(player.getPossibleWeaponsToReload());
                player.notifyOnLog("Choose one weapon to reload before shooting");
                player.setTurnState(TurnState.CHOOSE_WEAPON_TO_RELOAD_IN_ACTION);
            }
            else {
                player.setTurnState(TurnState.CHOOSE_WEAPON_HAND);
                player.notifyOnLog("Choose one weapon to shoot with");
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(player.getValidWeapons(gameManager));
            }
        }
        else {
                player.setTurnState(TurnState.CHOOSE_WEAPON_HAND);
            player.notifyOnLog("Choose one weapon to shoot with");
                gameManager.getCurrentTurn().getPossibleChoices().setSelectableWeapons(player.getValidWeapons(gameManager));
        }
        player.notifyWeapons(gameManager);
    }
}
