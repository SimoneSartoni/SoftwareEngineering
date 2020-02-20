package model.utility;

import model.enums.PlayerColor;

import java.io.Serializable;
import java.util.List;

/**
 * class used to store the possible features that can be personalized in a new lobby
 */
public class LobbyInfo implements Serializable {
    /**
     * allowed maps and characteristics
     */
    private MapInfoView map;
    /**
     * allowed player colors
     */
    private List<PlayerColor> playerColors;
    /**
     * total number of maps available
     */
    private int numberOfMaps;
    /**
     * different gamemodes available
     */
    private List<String> mods;

    public LobbyInfo(MapInfoView map, List<PlayerColor> playerColors, int numberOfMaps, List<String> mods) {
        this.map = map;
        this.playerColors = playerColors;
        this.numberOfMaps = numberOfMaps;
        this.mods = mods;
    }

    public MapInfoView getMap() {
        return map;
    }

    public List<PlayerColor> getPlayerColors() {
        return playerColors;
    }

    public int getNumberOfMaps() {
        return numberOfMaps;
    }

    public List<String> getMods() {
        return mods;
    }
}
