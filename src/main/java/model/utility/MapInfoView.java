package model.utility;

import model.board.TileView;
import model.enums.PlayerColor;
import model.enums.TileLinks;
import model.player.PlayerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * it's used to store the current Map and all characteristics of a Game
 * (CLIENT_SIDE) It contains ViewPlayers and ViewTiles to limit client's visibility about sensitive informations
 */
public class MapInfoView implements Serializable {

    private final int number;
    private final int actualGameId;
    private final String actualEndMode;
    private final int mapWidth;
    private final int mapHeight;
    private final List<List<TileView>> map;
    private final List<PlayerColor> allowedPlayerColors;
    private final List<String> allowedEndModes;
    private final int maxNumberOfPlayer;
    private final int minNumberOfPlayer;
    private final List<PlayerView> playerViews;
    private int turnTimer;
    private int numberOfSkulls;
    public MapInfoView(int number, int actualGameId, String actualEndMode, int mapWidth, int mapHeight, List<List<TileView>> map, List<PlayerColor> allowedPlayerColors, List<String> allowedEndModes, int maxNumberOfPlayer, int minNumberOfPlayer, List<PlayerView> playerViews) {
        this.number = number;
        this.actualGameId = actualGameId;
        this.actualEndMode = actualEndMode;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map = new ArrayList<>(map);
        this.allowedPlayerColors = new ArrayList<>(allowedPlayerColors);
        this.allowedEndModes = new ArrayList<>(allowedEndModes);
        this.maxNumberOfPlayer = maxNumberOfPlayer;
        this.minNumberOfPlayer = minNumberOfPlayer;
        this.playerViews = new ArrayList<>(playerViews);
    }

    public int getNumber() {
        return number;
    }

    public int getActualGameId() {
        return actualGameId;
    }

    public String getActualEndMode() {
        return actualEndMode;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public List<List<TileView>> getMap() {
        return map;
    }

    public List<PlayerColor> getAllowedPlayerColors() {
        return allowedPlayerColors;
    }

    public List<String> getAllowedEndModes() {
        return allowedEndModes;
    }

    public int getMaxNumberOfPlayer() {
        return maxNumberOfPlayer;
    }

    public int getMinNumberOfPlayer() {
        return minNumberOfPlayer;
    }

    public List<PlayerView> getPlayerViews() {
        return playerViews;
    }

    public void clearPlayerViews() {
        playerViews.clear();
    }

    public int getNumberOfSkulls() {
        return numberOfSkulls;
    }

    public int getTurnTimer() {
        return turnTimer;
    }

    public static boolean isPlayable(TileView tile) {
        return !(tile.getCanLeft() == TileLinks.HOLE && tile.getCanRight() == TileLinks.HOLE
                && tile.getCanUp() == TileLinks.HOLE && tile.getCanDown() == TileLinks.HOLE);
    }
}