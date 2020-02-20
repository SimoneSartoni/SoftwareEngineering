package model.board;

import model.enums.RoomColor;
import model.enums.TileLinks;
import model.player.PlayerView;
import model.weapon.Weapon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/***
 * Class to send to the client instead of the real Tile class. It doesn't contain a reference to the player but a player view,
 * that can be safely sent to the client
 */
public class TileView implements Serializable {
    private final int x;
    private final int y;
    private final RoomColor room;
    private AmmoTile ammo;
    private final List<Weapon> weapons;
    private final boolean spawnPoint;
    private final TileLinks canUp;
    private final TileLinks canDown;
    private final TileLinks canRight;
    private final TileLinks canLeft;
    private final boolean hole;
    private final List<PlayerView> playerViews;

    public TileView(int x, int y, RoomColor room, AmmoTile ammo, List<Weapon> weapons, boolean spawnPoint, TileLinks canUp, TileLinks canDown, TileLinks canRight, TileLinks canLeft, boolean hole, List<PlayerView> playerViews) {
        this.x = x;
        this.y = y;
        this.room = room;
        this.ammo = ammo;
        this.weapons = weapons;
        this.spawnPoint = spawnPoint;
        this.canUp = canUp;
        this.canDown = canDown;
        this.canRight = canRight;
        this.canLeft = canLeft;
        this.hole = hole;
        this.playerViews = new ArrayList<>(playerViews);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public RoomColor getRoom() {
        return room;
    }

    public AmmoTile getAmmo() {
        return ammo;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public boolean isSpawnPoint() {
        return spawnPoint;
    }

    public TileLinks getCanUp() {
        return canUp;
    }

    public TileLinks getCanDown() {
        return canDown;
    }

    public TileLinks getCanRight() {
        return canRight;
    }

    public TileLinks getCanLeft() {
        return canLeft;
    }

    public boolean isHole() {
        return hole;
    }

    public void removePlayer(PlayerView playerView) {
        playerViews.remove(playerView);
    }

    public List<PlayerView> getPlayerViews() {
        return playerViews;
    }

    public AmmoTile removeAmmo(){
        AmmoTile temp= ammo;
        ammo = null;
        return temp;
    }

}
