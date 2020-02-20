package network;

import model.board.KillShotTrack;
import model.board.TileView;
import model.enums.Direction;
import model.enums.LoadedState;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.utility.MapInfoView;
import model.utility.PossibleChoices_Client;
import model.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

public class ClientContext {
    private static ClientContext instance;
    private PlayerView currentPlayer;
    private PlayerView enemyShownBoard;
    private List<PlayerView> playerViews = new ArrayList<>();
    private List<MapInfoView> possibleGames = new ArrayList<>();
    private List<MapInfoView> possibleMaps = new ArrayList<>();
    private List<Weapon> weapons = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private KillShotTrack killShotTrack;
    private int points;
    private boolean gameStarted;
    private String currentToken;
    private boolean activeTurn;
    private int validGame;
    private boolean validJoin;
    private int numberOfMaps;
    private PossibleChoices_Client possibleChoices = new PossibleChoices_Client();
    private List<String> possibleCommands = new ArrayList<>();
    private MapInfoView map;
    private String alreadyExistingToken;
    private boolean askForCommand;
    private boolean disconnected;

    private ClientContext() {

    }


    public static synchronized ClientContext get() {
        if(instance == null) {
            instance = new ClientContext();
        }
        return instance;
    }

    public PlayerView getCurrentPlayer () {
        return currentPlayer;
    }

    public PlayerView getEnemyShownBoard() {
        return enemyShownBoard;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public String getCurrentToken() {
        return currentToken;
    }

    public boolean isActiveTurn() {
        return activeTurn;
    }

    public int getValidGame() {
        return validGame;
    }

    public boolean isValidJoin() {
        return validJoin;
    }

    public void setCurrentPlayer(PlayerView player) {
        this.currentPlayer = player;
    }

    public void setEnemyShownBoard(PlayerView enemyShownBoard) {
        this.enemyShownBoard = enemyShownBoard;
    }

    public void setGameStarted(boolean started) {
        this.gameStarted = started;
    }

    public void setCurrentToken(String token) {
        this.currentToken = token;
    }

    public void setActiveTurn(boolean active) {
        this.activeTurn = active;
    }

    public void setValidGame(int valid) {
        this.validGame = valid;
    }

    public void setValidJoin(boolean validJoin) {
        this.validJoin = validJoin;
    }

    public List<PlayerView> getPlayerViews() { return playerViews; }

    public void setPlayerViews(List<PlayerView> playerViews) {
        this.playerViews = new ArrayList<>();
        this.playerViews.addAll(playerViews);
    }

    public PossibleChoices_Client getPossibleChoices() { return possibleChoices; }

    public void setPossibleChoices(PossibleChoices_Client possibleChoices) { this.possibleChoices = possibleChoices; }

    public List<String> getPossibleCommands() { return possibleCommands; }

    public void setPossibleCommands(List<String> possibleCommands) { this.possibleCommands = possibleCommands; }

    public void setNumberOfMaps(int numberOfMaps) {
        this.numberOfMaps = numberOfMaps;
    }

    public int getNumberOfMaps() {
        return numberOfMaps;
    }

    public MapInfoView getMap() {
        return map;
    }

    public void setMap(MapInfoView map) {
        this.map = map;
    }

    public void setAlreadyExisting(String alreadyExistingToken) {
        this.alreadyExistingToken = alreadyExistingToken;
    }

    public String getAlreadyExistingToken() {
        return alreadyExistingToken;
    }

    /***
     * Method to remove an enemy from the list of all enemies
     * @param username the username of the player to remove
     */
    public void removeEnemyPlayer(String username){
        List<PlayerView> list = new ArrayList<>(playerViews);
        for(PlayerView p: list){
            if(p.getPlayerID().equals(username))
                playerViews.remove(p);
        }
    }

    /***
     * Method to replace an enemy from the list of all enemies
     * @param updatePlayer the username of the player to replace
     */
    public void replaceEnemyPlayer(PlayerView updatePlayer){
        for(PlayerView p : playerViews) {
            if(p.getPlayerID().equals(updatePlayer.getPlayerID())) {
                playerViews.remove(p);
                break;
            }
        }
        playerViews.add(updatePlayer);
    }

    public boolean isAskForCommand() {
        return askForCommand;
    }

    public void setAskForCommand(boolean askForCommand) {
        this.askForCommand = askForCommand;
    }

    public List<MapInfoView> getPossibleGames() {
        return possibleGames;
    }

    public void setPossibleGames(List<MapInfoView> possibleGames) {
        this.possibleGames = possibleGames;
    }

    public List<MapInfoView> getPossibleMaps() {
        return possibleMaps;
    }

    public void setPossibleMaps(List<MapInfoView> possibleMaps) {
        this.possibleMaps = possibleMaps;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    /***
     * Method to replace a weapon from the list of all weapons
     * @param weapon the weapon to replace
     */
    public void addWeapon(Weapon weapon) {
        for(Weapon w : weapons) {
            if(w.getIdName().equals(weapon.getIdName())) {
                weapons.remove(w);
                break;
            }
        }
        weapons.add(weapon);
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    /***
     * Method to remove a weapon from the list of all weapons
     * @param weapon the weapon to remove
     */
    public void removeWeapon(Weapon weapon){
        for(Weapon w : weapons) {
            if(w.getIdName().equals(weapon.getIdName())) {
                weapons.remove(w);
                break;
            }
        }
    }

    /***
     * Method to replace a powerup from the list of all powerups
     * @param powerUp the powerup to replace
     */
    public void addPowerup(PowerUp powerUp) {
        for(PowerUp p : powerUps) {
            if(p.getId() == powerUp.getId()) {
                powerUps.remove(powerUp);
                break;
            }
        }
        powerUps.add(powerUp);
    }

    public KillShotTrack getKillShotTrack() {
        return killShotTrack;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int points) {
        this.points+=points;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    public void removePowerup(PowerUp p) {
        powerUps.remove(p);
    }

    public void setKillShotTrack(KillShotTrack killShotTrack) {
        this.killShotTrack=killShotTrack;
    }

    /***
     * Method to set a weapon as used
     * @param weapon the weapon used
     */
    public void setUsedWeapon(Weapon weapon) {
        for(Weapon w : weapons) {
            if(w.getIdName().equals(weapon.getIdName())) {
                w.setLoaded(LoadedState.UNLOADED);
                break;
            }
        }
    }

    /***
     * Method to return the index of a tile that can be chosen by the user in the list of possible choices
     * @param row the row of the tile
     * @param column the column of the tile
     * @return the index int the possible choices
     */
    public int indexOfSelectableTile(int row, int column) {
        int count = 0;
        for(TileView t : getPossibleChoices().getSelectableTiles()) {
            if(t.getX() == row && t.getY() == column) {
                return count;
            }
            count++;
        }
        return -1;
    }

    /***
     * Method to return the index of a target player in the list of possible targets
     * @param playerView1 the targetted player
     * @return the index in the possible choices
     */
    public int indexOfSelectableTargets(PlayerView playerView1) {
        int count = 0;
        for(PlayerView playerView : getPossibleChoices().getSelectableTargets()) {
            if(playerView.getPlayerID().equals(playerView1.getPlayerID())) {
                return count;
            }
            count++;
        }
        return -1;
    }

    /***
     * Given two tiles it returns the direction in which you have to move starting from tile 1 to reach tile 2
     * @param originTile the starting point
     * @param targetTile the point to which arrive
     * @return the direction
     */
    public Direction getDirectionBetweenTiles(TileView originTile, TileView targetTile){
        if(originTile!=null) {
            if (originTile.getY() == targetTile.getY()) {
                if (originTile.getX() >= targetTile.getX()) {
                    return Direction.NORTH;
                } else {
                    if (originTile.getX() <= targetTile.getX())
                        return Direction.SOUTH;
                    else
                        return null;
                }
            }
            if (originTile.getX() == targetTile.getX()) {
                if (originTile.getY() >= targetTile.getY()) {
                    return Direction.WEST;
                } else {
                    if (originTile.getY() <= targetTile.getY())
                        return Direction.EAST;
                    else
                        return null;
                }
            }
        }
        else {
            return null;
        }
        return null;
    }
}
