package model.utility;

import model.board.GameManager;
import model.board.Tile;
import model.board.TileView;
import model.enums.PlayerColor;
import model.enums.TileLinks;
import model.player.PlayerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * it contains all the information about the map and the characteristics of a specific game
 */
public class MapInfo implements Serializable {
    private final int number;
    private final int actualGameId;
    private final String actualEndMode;
    private final int mapWidth;
    private final int mapHeight;
    private final List<List<Tile>> map = new ArrayList<>();
    private final List<PlayerColor> allowedPlayerColors = new ArrayList<>();
    private final List<String> allowedEndModes = new ArrayList<>();
    private final int maxNumberOfPlayer;
    private final int minNumberOfPlayer;
    private int numberOfSkulls;
    private final List<PlayerView> playerViews = new ArrayList<>();

    public MapInfo(int number, int actualGameId, String actualEndMode, int mapWidth, int mapHeight, List<List<Tile>> map,
                   List<PlayerColor> allowedPlayerColors, List<String> allowedEndModes, int maxNumberOfPlayer, int minNumberOfPlayer,
                   List<PlayerView> playerViews) {
        this.number = number;
        this.actualGameId = actualGameId;
        this.actualEndMode = actualEndMode;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map.addAll(map);
        this.allowedPlayerColors.addAll(allowedPlayerColors);
        this.allowedEndModes.addAll(allowedEndModes);
        this.maxNumberOfPlayer = maxNumberOfPlayer;
        this.minNumberOfPlayer = minNumberOfPlayer;
        this.playerViews.addAll(playerViews);
    }

    public static boolean isPlayable(TileView tile) {
        return !(tile.getCanLeft() == TileLinks.HOLE && tile.getCanRight() == TileLinks.HOLE
                && tile.getCanUp() == TileLinks.HOLE && tile.getCanDown() == TileLinks.HOLE);
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

    public List<List<Tile>> getMap() {
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
        this.playerViews.clear();
    }

    public static MapInfoView createMapView(MapInfo mapToChange) {
        List<List<TileView>> views = new ArrayList<>();
        List<TileView> rowViews = new ArrayList<>();
        for(List<Tile> row : mapToChange.getMap()) {
            for(Tile t : row) {
                rowViews.add(GameManager.createTileView(t));
            }
            views.add(new ArrayList<>(rowViews));
            rowViews.clear();
        }
        return new MapInfoView(
                mapToChange.number, mapToChange.actualGameId, mapToChange.actualEndMode, mapToChange.mapWidth, mapToChange.mapHeight,
                views, mapToChange.allowedPlayerColors, mapToChange.allowedEndModes, mapToChange.maxNumberOfPlayer, mapToChange.minNumberOfPlayer, mapToChange.playerViews);
    }

    public void setNumberOfSkulls(int numberOfSkulls) {
        this.numberOfSkulls = numberOfSkulls;
    }

    public int getNumberOfSkulls() {
        return numberOfSkulls;
    }
}