package model.player;

import model.board.PointsBoard;
import model.board.TileView;
import model.board.TurnAction;
import model.enums.PlayerColor;
import model.enums.PlayerState;
import model.utility.Ammo;
import model.weapon.Weapon;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * class for CLIENT_SIDE to represent the player in the view. It has only the necessary fields that the client can see.
 * All the sensitive information are not in this class. To see full characterization see Player class
 */
public class PlayerView implements Serializable {
    private final String playerID;
    private final PlayerColor playerColor;
    private final PlayerState playerState;
    private final Ammo ammo;
    private final int nOfKills;
    private final int nOfPowerUps;
    private final Map<PlayerColor, Integer> marksView;
    private final List<PlayerColor> damageTakenView;
    private final int nOfDeaths;
    private TileView currentTile;
    private final List<Weapon> unloadedWeapon;
    private final int nOfLoadedWeapons;
    private final TurnAction currentTurnAction;
    private final PointsBoard board;


    public PlayerView(String playerID, PlayerColor playerColor, PlayerState playerState, Ammo ammo, int nOfKills, int nOfPowerUps, Map<PlayerColor, Integer> marksView, List<PlayerColor> damageTakenView, int nOfDeaths, TileView currentTile, List<Weapon> unloadedWeapon,int nOfLoadedWeapons, TurnAction currentTurnAction, PointsBoard board) {
        this.playerID = playerID;
        this.playerColor = playerColor;
        this.playerState = playerState;
        this.ammo = ammo;
        this.nOfKills = nOfKills;
        this.nOfPowerUps = nOfPowerUps;
        this.marksView = marksView;
        this.damageTakenView = damageTakenView;
        this.nOfDeaths = nOfDeaths;
        this.currentTile = currentTile;
        this.unloadedWeapon = unloadedWeapon;
        this.nOfLoadedWeapons=nOfLoadedWeapons;
        this.currentTurnAction = currentTurnAction;
        this.board = board;
    }

    public int getnOfLoadedWeapons() {
        return nOfLoadedWeapons;
    }

    public String getPlayerID() {
        return playerID;
    }

    public PlayerColor getPlayerColor() {
        return playerColor;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public Ammo getAmmo() {
        return ammo;
    }

    public int getNOfKills() {
        return nOfKills;
    }

    public int getNOfPowerUps() {
        return nOfPowerUps;
    }

    public Map<PlayerColor, Integer> getMarksView() {
        return marksView;
    }

    public List<PlayerColor> getDamageTakenView() {
        return damageTakenView;
    }

    public int getNOfDeaths() {
        return nOfDeaths;
    }

    public TileView getCurrentTile() {
        return currentTile;
    }

    public List<Weapon> getUnloadedWeapons() {
        return unloadedWeapon;
    }

    public TurnAction getCurrentTurnAction() {
        return currentTurnAction;
    }

    public PointsBoard getBoard() {
        return board;
    }

    public void setTileView(TileView tileView) {
        if(tileView != null) {
            this.currentTile = tileView;
        }
    }
}
