package model.board;

import model.enums.RoomColor;
import model.enums.TileLinks;
import model.player.Player;
import model.weapon.Weapon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tile implements Serializable {
    private RoomColor room;
    private int x;
    private int y;
    private boolean spawnPoint;
    private TileLinks canUp;
    private TileLinks canDown;
    private TileLinks canRight;
    private TileLinks canLeft;
    private List<Player> players;
    private AmmoTile ammo;
    private List<Weapon> spawnPointWeapons;

    public Tile(){
        players = new ArrayList<>();
        spawnPointWeapons = new ArrayList<>();
    }

    public Tile(RoomColor room,int x, int y, boolean spawnPoint, TileLinks canUp,
                TileLinks canDown, TileLinks canLeft, TileLinks canRight, AmmoTile ammo){
        this.x=x;
        this.y=y;
        this.room=room;
        this.spawnPoint = spawnPoint;
        this.canDown=canDown;
        this.canLeft=canLeft;
        this.canRight=canRight;
        this.canUp=canUp;
        this.ammo =ammo;
        players =new ArrayList<>();
        spawnPointWeapons=new ArrayList<>();

    }


    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     *
     * @return a list of all the players in the tile
     */
    public List<Player> getPlayers() {
        return players;
    }

    public void addWeapons(List<Weapon> weapons) {
        if (!spawnPoint)
                return;
        else
            spawnPointWeapons.addAll(weapons);
    }

    public void addWeapon(Weapon weapon){
        spawnPointWeapons.add(weapon);
    }

    public List<Weapon> getWeapons(){
            if(!spawnPoint)
                return new ArrayList<>();
            else
                return spawnPointWeapons;
    }

    public void removeWeapon(Weapon weapon){
            if (!spawnPoint)
                return;
            else
                spawnPointWeapons.remove(weapon);
    }
    /**
     * adds a player to Tile
     * @param p: player to add
     */
    public void addPlayer(Player p){
        players.add(p);
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

    /**
     *
     * @return the AmmoTile removed (grabbed by the player)
     */
    public AmmoTile removeAmmo(){
         AmmoTile temp= ammo;
         ammo =null;
         return temp;
    }
    public void setAmmo(AmmoTile a){
        ammo =a;
    }

    public AmmoTile getAmmo() {
        return ammo;
    }

    public RoomColor getRoom() {
        return room;
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

    public void setRoom(RoomColor room) {
        this.room = room;
    }

    public void setSpawnPoint(boolean spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public void setCanUp(TileLinks canUp) {
        this.canUp = canUp;
    }

    public void setCanDown(TileLinks canDown) {
        this.canDown = canDown;
    }

    public void setCanRight(TileLinks canRight) {
        this.canRight = canRight;
    }

    public void setCanLeft(TileLinks canLeft) {
        this.canLeft = canLeft;
    }

    public boolean isSpawnPoint() {
        return spawnPoint;
    }

    public boolean isHole () {
        return (canUp == TileLinks.HOLE && canDown == TileLinks.HOLE && canLeft == TileLinks.HOLE && canRight == TileLinks.HOLE);
    }

    @Override
    public boolean equals(Object obj) {
        Tile t;
        if(obj instanceof Tile){
            t=(Tile)obj;
        return (t.getX()==getX())&& (t.getY()==getY());}
    return false;
    }


    public int hashCode() {
        return super.hashCode();
    }
}
