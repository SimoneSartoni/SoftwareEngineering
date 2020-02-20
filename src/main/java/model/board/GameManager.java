package model.board;

import model.enums.*;
import model.gamemodes.GameMode;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.utility.CurrentTurn;
import model.utility.TurnStateHandler;
import model.weapon.Weapon;
import network.ManagerObserver;
import network.ViewProxy;

import java.io.Serializable;
import java.util.*;

public class GameManager implements Serializable {

    private Deck<Weapon> weaponsDeck;
    private Deck<PowerUp> powerUpDeck;
    private Deck<AmmoTile> ammoTileDeck;
    private List<List<Tile>> tiles;
    private GameMode currentGameMode;
    private final List<Player> playerOrderTurn;
    private KillShotTrack killShotTrack;
    private boolean finalFrenzy;
    private Player currentPlayer;
    private final int boardLength;
    private final int boardHeight;
    private CurrentTurn currentTurn;
    private Gamemodes gamemodes;
    private boolean started;
    private boolean gameEnd;
    private List<ViewProxy> notifyObservers;
    private ManagerObserver managerObserver;
    private int gameId = -1;

    public GameManager(Deck<Weapon> weaponsDeck, Deck<PowerUp> powerUpDeck, Deck<AmmoTile> ammoTileDeck,
                       List<Player> players, List<List<Tile>> tiles, GameMode currentGameMode, KillShotTrack killshotTrack,
                       boolean finalFrenzy, Gamemodes gamemodes, int boardLength, int boardHeight) {
        this.weaponsDeck = weaponsDeck;
        this.powerUpDeck = powerUpDeck;
        this.ammoTileDeck = ammoTileDeck;
        List<Tile> temp = new ArrayList<>();
        this.playerOrderTurn = new ArrayList<>();
        this.playerOrderTurn.addAll(players);
        this.tiles = new ArrayList<>();
        if (tiles != null)
            for (int i = 0; i < tiles.size(); i++) {
                this.tiles.add(new ArrayList<>());
                temp.addAll(tiles.get(i));
                this.tiles.get(i).addAll(temp);
                temp.clear();
            }
        this.currentGameMode = currentGameMode;
        this.gamemodes = gamemodes;
        if (killshotTrack != null)
            this.killShotTrack = killshotTrack;
        this.finalFrenzy = finalFrenzy;
        this.boardHeight = boardHeight;
        this.boardLength = boardLength;
        this.currentTurn = new CurrentTurn();
        started = false;
        notifyObservers=new ArrayList<>();
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getGameId() {
        return gameId;
    }

    /***
     * Create a list of tile views to send to the client, limiting the possible data visible from the client.
     * It use the static funcion createTileView
     * @param targettableTiles the tiles from which create the list of tile views
     * @return the new list of the views
     */
    public static List<TileView> tilesToTileViews(List<Tile> targettableTiles) {
        List<TileView> tileViews = new ArrayList<>();
        for(Tile t : targettableTiles) {
            tileViews.add(GameManager.createTileView(t));
        }
        return tileViews;
    }

    /***
     * Given a map composed of tile it checks if a tile view is in that list simply by checking his coordinates
     * @param tilesToCheck the map from which to check if a tile exist
     * @param tView the tile that is asked if exist
     * @return the tile or null
     */
    public static Tile getTileFromMap(List<List<Tile>> tilesToCheck, TileView tView) {
        for(List<Tile> row : tilesToCheck) {
            for(Tile t : row) {
                if(t.getX() == tView.getX() && t.getY() == tView.getY()) {
                    return t;
                }
            }
        }
        return null;
    }

    /***
     * Given a list of tile it checks if a tile view is in that list simply by checking his coordinates
     * @param tilesToCheck the map from which to check if a tile exist
     * @param tView the tile that is asked if exist
     * @return the tile or null
     */
    public static Tile getTileFromList(List<Tile> tilesToCheck, TileView tView) {
        for(Tile t : tilesToCheck) {
            if(t.getX() == tView.getX() && t.getY() == tView.getY()) {
                return t;
            }
        }
        return null;
    }

    public void setNotifyObservers(List<ViewProxy> notifyObservers) {
        this.notifyObservers = notifyObservers;
    }

    public List<ViewProxy> getNotifyObservers() {
        return notifyObservers;
    }

    /**
     * Set up the board by filling every spawn point with weapons and every other tile with ammo tiles.
     * Shuffle the players to give them a random order, set up clients' board and start the game
     */
    public void startGame() {
        Collections.shuffle(playerOrderTurn);
        currentPlayer = playerOrderTurn.get(0);
        weaponsDeck.reShuffleAll();
        powerUpDeck.reShuffleAll();
        ammoTileDeck.reShuffleAll();
        for(Player p:playerOrderTurn){
            p.setBoard(gamemodes.getNormalMode().getPointsBoard());
            p.setTurnState(TurnState.READY_TO_SPAWN);
        }
        fillAllAmmoTiles();
        fillWeaponSpawnPoint();
        currentPlayer.notifyPossibleCommands();
    }

    /**
     * this method changes the turn of the game, passing to the next gamemode or ending the game if necessary
     */
    public void changeTurn() {
        restartPlayerTimer();
        int i=currentGameMode.changeTurn(killShotTrack,currentTurn,playerOrderTurn,currentPlayer);
        if (i==0){
            currentTurn.reset();
            setNewCurrentPlayer();
        }
    }

    /**
     * this method is called when all the player that needed to respawn effectively respawned. The turn can finally change and players can be notified.
     * @param oldPlayer the old current player that has to be replaced
     */
    private void onEffectiveChangeTurn(Player oldPlayer){
        List<Tile> tilesToRefill= new ArrayList<>(getToFillTiles());
        fillAllAmmoTiles();
        fillWeaponSpawnPoint();
        currentTurn.reset();
        notifyOnChangeTurn(oldPlayer,currentPlayer,tilesToRefill);
    }


    /**
     * This method uses the NetworkManager observer to notify the network manager to restart the timer of the turn
     */
    private void restartPlayerTimer() {
        if(managerObserver == null) {
            System.out.println("ERROR MANAGER OBSERVER IS NULL");
            return;
        }
        if(playerOrderTurn.isEmpty()) {
            System.out.println("ERROR PLAYER LIST IS EMPTY");
            return;
        }
        if(gameId != -1) {
            managerObserver.restartPlayerTimer(gameId);
        }
        else {
            System.out.println("ERROR WHILE RESTARTING THE TIMER");
        }
    }

    /**
     * this method sets the new current player if no one has to respawn.
     * If someone has to respawn then the setting of the new current player is delayed after the respawn.
     */
    private void setNewCurrentPlayer(){
        currentPlayer.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
        if(getPlayersInAState(TurnState.READY_TO_RESPAWN).isEmpty()) {
            nextPlayer();
            if (currentPlayer.getTurnState() == TurnState.READY_TO_SPAWN) {
                spawnDrawPhase(currentPlayer, 2);
                return;
            } else if (currentPlayer.getTurnState() == TurnState.READY_TO_RESPAWN) {
                spawnDrawPhase(currentPlayer, 1);
                return;
            } else if (currentPlayer.getTurnState() == TurnState.READY_TO_SPAWN_DISCONNECTION) {
                spawnDrawPhase(currentPlayer, 0);
                return;
            }
            currentPlayer.setTurnState(TurnState.READY_FOR_ACTION);
            currentPlayer.notifyPossibleCommands();
            currentPlayer.notifyPrintHelp(this);
            getCurrentTurn().getPossibleChoices().setSelectableActions(currentPlayer.getPossibleActions(this));
            currentPlayer.notifyOnActions(this);
        }
        else {
            Player playerToRespawn = getPlayersInAState(TurnState.READY_TO_RESPAWN).get(0);
            if(playerToRespawn.isDisconnected()) {
                spawnDisconnectedPlayer(playerToRespawn);
            }
            else {
                spawnDrawPhase(playerToRespawn, 1);
            }
        }
    }

    /**
     * This method is used in the situation in which a disconnected player has to spawn after his death.
     * It gives him a random powerup and immediately discard it makin him spawn.
     * @param player the disconnected player to spawn
     */
    private void spawnDisconnectedPlayer(Player player) {
        if(powerUpDeck.isEmpty()) {
            powerUpDeck.reShuffleAll();
        }
        player.addPowerUp(this,powerUpDeck.draw());

        PowerUp powerUp = player.getPowerUps().get(0);
        player.discardPowerUp(this, powerUp);
        for(Tile tile : getSpawnTiles()){
            String roomColorName = tile.getRoom().name();
            String powColorName = powerUp.getColor().name();
            if(roomColorName.equalsIgnoreCase(powColorName)) {
                player.setCurrentTile(tile);
                tile.addPlayer(player);
                for(ViewProxy v:notifyObservers) {
                    if(!v.getToken().equals(player.getToken())) {
                        PlayerView pv = player.createPlayerView();
                        TileView tv = createTileView(tile);
                        pv.setTileView(tv);
                        v.onSpawn(pv, tv);
                    }
                }
                break;
            }
        }

        player.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
        TurnStateHandler.handleSpawnQueue(player, this);
    }

    /**
     * sets the new current Player that has to play the turn
     */
    public void nextPlayer() {
        int index = playerOrderTurn.indexOf(currentPlayer);
        int size = playerOrderTurn.size();
        Player oldPlayer;
        if (index == size - 1) {
            for(int i = 0; i < size; i++) {
                if(!playerOrderTurn.get(i).isDisconnected()) {
                    oldPlayer=currentPlayer;
                    oldPlayer.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
                    currentPlayer = playerOrderTurn.get(i);
                    getCurrentGameMode().setTurnAction(currentPlayer,getPlayerOrderTurn());
                    onEffectiveChangeTurn(oldPlayer);
                    return;
                }
            }
        }
        else {
            for(int i = index + 1; i < size; i++) {
                if(!playerOrderTurn.get(i).isDisconnected()) {
                    oldPlayer=currentPlayer;
                    oldPlayer.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
                    currentPlayer = playerOrderTurn.get(i);
                    getCurrentGameMode().setTurnAction(currentPlayer,getPlayerOrderTurn());
                    onEffectiveChangeTurn(oldPlayer);
                    return;
                }
            }
            currentPlayer = playerOrderTurn.get(size - 1);
            nextPlayer();
        }
    }

    /**
     *
     * @return the list of players in the game order
     */
    public List<Player> getPlayerOrderTurn() {
        return new ArrayList<>(playerOrderTurn);
    }

    public int getNOfPlayers() {
        return playerOrderTurn.size();
    }

    /**
     * @return a copy of the tiles of the map for this game
     */
    public List<List<Tile>> getTiles() {
        List<List<Tile>> temp = new ArrayList<>();
        List<Tile> temp2 = new ArrayList<>();
        if (tiles != null) {
            for (int i = 0; i < tiles.size(); i++) {
                temp.add(new ArrayList<>());
                temp2.addAll(tiles.get(i));
                temp.get(i).addAll(temp2);
                temp2.clear();
            }
        }
        return temp;
    }

    /**
     * Method to get the player corresponding to a give color
     * @param playerColor the color from which to know which player is associated
     * @return the associated player
     */
    private Player getPlayerFromColor(PlayerColor playerColor){
        Player toRet=new Player();
        for(Player p:playerOrderTurn)
            if(p.getPlayerColor().equals(playerColor))
                return p;
        return toRet;
    }

    private void setCurrentGameMode(GameMode currentGameMode) {
        this.currentGameMode = currentGameMode;
    }

    public void manageEndOfFrenzy(){
        endOfGame();
    }

    /**
     * this methods manages the end of sudden death mode. It will end the game if the selected mode is sudden death. If not he will start the final frenzy mode.
     */
    public void manageEndOfNormal(){
        if(finalFrenzy) {
            setCurrentGameMode(gamemodes.getFinalFrenzyMode());
            currentGameMode.setBoards(playerOrderTurn,currentPlayer);
            notifyOnFinalFrenzyStart();
        }
        else{
            endOfGame();
        }
    }

    public GameMode getCurrentGameMode() { return currentGameMode; }

    public Player getFirstPlayerToPlay() {
        if (playerOrderTurn != null)
            return playerOrderTurn.get(0);
        return null;
    }

    /**
     * Makes the player that has to spawn draw the powerups he needs
     * @param player the player that has to spawn
     * @param numberOfCards  the powerUps the player has to draw: 1 to respawn, 2 to spawn
     */
    public void spawnDrawPhase(Player player, int numberOfCards) {
        if (powerUpDeck.isEmpty()) {
            powerUpDeck.reShuffleAll();
        }
        for (int i = 0; i < numberOfCards; i++) {
            if(powerUpDeck.isEmpty())
                powerUpDeck.reShuffleAll();
            player.addPowerUp(this,powerUpDeck.draw());
        }
        getCurrentTurn().getPossibleChoices().setSelectablePowerUps(player.getPowerUps());
        player.setTurnState(TurnState.DISCARD_POWERUP_FOR_SPAWN);
        player.notifyPrintHelp(this);
        player.notifyOnLog("You are going to Spawn! Discard a powerUp to complete the operation");
        player.notifyPowerUps(this);
    }

    /**
     *  Method to make a player spawn after the user select a powerup
     * @param player the player that has spawned/respawn
     * @param powerUp the discarded powerUp to spawn
     */
    public void spawnSetPosition(Player player, PowerUp powerUp) {
        player.discardPowerUp(this,powerUp);
        for(Tile tile : getSpawnTiles()){
            String roomColorName = tile.getRoom().name();
            String powColorName = powerUp.getColor().name();
            if(roomColorName.equalsIgnoreCase(powColorName)) {
                player.setCurrentTile(tile);
                tile.addPlayer(player);
                for(ViewProxy v:notifyObservers) {
                    PlayerView pv = player.createPlayerView();
                    TileView tv = createTileView(tile);
                    pv.setTileView(tv);
                    v.onSpawn(pv, tv);
                }
                break;
            }
        }
        if(!currentPlayer.equals(player)) {
            player.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
            TurnStateHandler.handleSpawnQueue(player, this);
        }
        else {
            player.setTurnState(TurnState.READY_FOR_ACTION);
            player.getViewPlayer().requestPossibleCommands();
            player.notifyOnActions(this);
        }

    }


    /**
     * Method to manage the end of the game. It calculates the points to assign to each player by sorting them,
     * notifing every one both of clients' points and of the end of the game
     */
    public void endOfGame() {
        Map<PlayerColor, Integer> deaths = killShotTrack.getDeathCount();
        List<Integer> integers = new ArrayList<>(deaths.values());
        List<PlayerColor> players = new ArrayList<>(deaths.keySet());
        List<PlayerColor> deathOrder = new ArrayList<>(killShotTrack.getDeathOrder());

        //dummy sorting algorithm
        for(int i = 0; i < integers.size() - 1; i++) {
            for(int j = i + 1; j < integers.size(); j++) {
                if(integers.get(j) > integers.get(i)) {
                    //swap players and integers
                    PlayerColor playerToSwap = players.get(j);
                    Integer integerToSwap = integers.get(j);
                    players.set(j, players.get(i));
                    integers.set(j, integers.get(i));
                    players.set(i, playerToSwap);
                    integers.set(i, integerToSwap);
                }

                else if(integers.get(j).equals(integers.get(i)) &&
                        deathOrder.indexOf(players.get(j)) < deathOrder.indexOf(players.get(i))) {
                    //swap players
                    PlayerColor playerToSwap = players.get(j);
                    players.set(j, players.get(i));
                    players.set(i, playerToSwap);
                }
            }
        }
        Map <PlayerView,Integer> scores= new HashMap<>();
        int counter = 0;
        for(PlayerColor player : players) {
            getPlayerFromColor(player).addScore(killShotTrack.getKillShotTrackScore().get(counter));
            scores.put(getPlayerFromColor(player).createPlayerView(),killShotTrack.getKillShotTrackScore().get(counter));
            if(counter < killShotTrack.getDeathCount().size() - 1) {
                counter++;
            }
        }
        notifyOnPoints(scores,false,null);
        for(Player player:playerOrderTurn)
            currentGameMode.countPlayerPoints(player,player.getDamageTaken(),player.getBoard(),player.getNOfDeaths());
        notifyOnEndGame(createFinalRank());
    }

    /**
     *
     * @return the list of players of the game sorted by the highest points
     */
    private List<Player> createFinalRank() {
        int max=0;
        int maxKills=0;
        List<Player> finalRank= new ArrayList<>();
        List<Player> tiebreak=new ArrayList<>();
        List<Player> support2= new ArrayList<>();
        List<Player> support=new ArrayList<>(playerOrderTurn);
        while (!support.isEmpty()) {
            for(Player p:support)
                if(p.getScore()>max)
                    max=p.getScore();
            for(Player p:playerOrderTurn)
                if (p.getScore()==max)
                    tiebreak.add(p);
                max=0;
            if(tiebreak.size()>1) {
                while(!tiebreak.isEmpty()){
                    support2=new ArrayList<>(tiebreak);
                    for (Player p : support2)
                        if(killShotTrack.getNOfKillsForPlayer(p.getPlayerColor())>maxKills)
                            maxKills=killShotTrack.getNOfKillsForPlayer(p.getPlayerColor());
                    for (Player player : support2)
                        if (killShotTrack.getNOfKillsForPlayer(player.getPlayerColor()) == maxKills) {
                            finalRank.add(finalRank.size(),player);
                            tiebreak.remove(player);
                            support.remove(player);
                        }
                }
            }
            else{
                finalRank.add(finalRank.size(),tiebreak.get(0));
               support.remove(tiebreak.get(0));
               tiebreak.clear();
            }
            maxKills=0;
        }
        support=new ArrayList<>(finalRank);
        for(Player p:support)
            if(p.isDisconnected()) {
                p.setScore(0);
                finalRank.remove(p);
                finalRank.add(finalRank.size(),p);
            }
        return finalRank;
    }

    public KillShotTrack getKillShotTrack() {
        return this.killShotTrack;
    }


    /**
     *
     * @return all the tiles of the map that need to be filled with weapons or ammo tiles
     */
    private List<Tile> getToFillTiles(){
        List<Tile> ret=new ArrayList<>();
        for(List<Tile> tileRow: tiles)
            for(Tile t:tileRow)
                if(t.isSpawnPoint()){
                    if(t.getWeapons().size()<3)
                        ret.add(t);
                }
                else {
                    if(t.getAmmo()==null)
                        ret.add(t);
                }
        return ret;
    }

    /**
     * Method to know which player is in a certain state
     * @param turnState the state to analyze
     * @return the list of players in that state
     */
    public List<Player> getPlayersInAState(TurnState turnState){
        List<Player> ret=new ArrayList<>();
        for(Player p: playerOrderTurn){
            if(p.getTurnState()==turnState){
                ret.add(p);
            }
        }
        return ret;
    }

    /**
     *
     * @return the list of all the tiles that are spawn points
     */
    public List<Tile> getSpawnTiles(){
        ArrayList<Tile> spawnTiles = new ArrayList<>();

        for(List<Tile> outer : tiles) {
            for(Tile inner : outer) {
                if(inner.isSpawnPoint()) {
                    spawnTiles.add(inner);
                }
            }
        }
        return spawnTiles;
    }

    public boolean isFinalFrenzy() {
        return finalFrenzy;
    }

    public CurrentTurn getCurrentTurn() {
        return currentTurn;
    }


    public void setPlayerState(Player p){
        currentGameMode.setTurnAction(p,playerOrderTurn);
        if((p.getPlayerState()==PlayerState.DEAD)&&(!currentTurn.getDeadPlayers().contains(p)))
            currentTurn.addDeadPlayer(p);
    }


    public void setBoardForPlayers(){
        if (finalFrenzy) {
            for (Player p : playerOrderTurn) {
                if ((p.getPlayerState() == PlayerState.DEAD)||(p.getDamageTaken().isEmpty()))
                    p.setBoard(gamemodes.getFinalFrenzyMode().getPointsBoard());
            }
        }
    }

    public void setBoardForPlayer(Player p){
        if((finalFrenzy)&&(p.getPlayerState()==PlayerState.DEAD)){
            p.setBoard(gamemodes.getFinalFrenzyMode().getPointsBoard());
    }}

    /**
     * Method to know only the player that have position different from null
     * @return the list of players on the board
     */
    public List<Player> getOnBoardPlayers() {
        List<Player> onBoardPlayers = new ArrayList<>(playerOrderTurn);
        for(Player p : playerOrderTurn) {
            if(p.getCurrentTile() == null) {
                onBoardPlayers.remove(p);
            }
        }
        return onBoardPlayers;
    }

    public void setCurrentTurn(CurrentTurn currentTurn) {
        this.currentTurn = currentTurn;
    }

    public void setFinalFrenzy(boolean newMode) {
        finalFrenzy = newMode;
    }

    private boolean matchRoom(Tile tile, RoomColor roomColor) {
        return tile.getRoom() == roomColor;
    }

    public List<Tile> getTileOfColor(RoomColor roomColor) {
        List<Tile> returnTile = new ArrayList<>();
        for(List<Tile> t : tiles) {
            for(Tile t1 : t) {
                if(t1!=null && matchRoom(t1, roomColor))
                    returnTile.add(t1);
	        }
        }
        return returnTile;
    }

    /**
     * Method to return alla of the tile of a room
     * @param tile identify the tile of which we want to analyze the room
     * @return a list of all tiles of the same room of the given tile
     * */
    public List<Tile> getTileOfRoom(Tile tile) {
        RoomColor roomColor=tile.getRoom();
        List<Tile> returnTile = new ArrayList<>();
        for(List<Tile> t : tiles) {
            for(Tile t1 : t) {
                if((t1.getCanRight()!=TileLinks.HOLE || t1.getCanUp()!=TileLinks.HOLE || t1.getCanLeft()!=TileLinks.HOLE || t1.getCanDown()!=TileLinks.HOLE) && matchRoom(t1, roomColor))
                    returnTile.add(t1);
            }
        }
        return returnTile;
    }

    public Player getCurrentPlayerTurn() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Deck<Weapon> getWeaponsDeck() {
        return weaponsDeck;
    }

    public Deck<PowerUp> getPowerUpDeck() {
        return powerUpDeck;
    }

    public Deck<AmmoTile> getAmmoTileDeck() {
        return ammoTileDeck;
    }

    /**
     * @param player : it's the starting point for the research for visible players
     * @return the list of the visible players for a given player
     */
    public List<Player> getVisiblePlayers(Player player) {
        ArrayList<Player> returnList = new ArrayList<>();
        for (Tile t: getVisibleTiles(player))
           returnList.addAll(t.getPlayers());
        returnList.remove(player);
        return returnList;
    }

    /***
     * Method to know which tiles a player can see. It cannot see through the walls but only in his room and adjacent ones throuh doors
     * @param player the player for which we need to know the tiles he can see
     * @return the list of the tile the player can see
     */
    private List<Tile>getVisibleTiles(Player player){
        List<Tile> ret= new ArrayList<>(getTileOfRoom(player.getCurrentTile()));
        if (player.getCurrentTile().getCanUp().equals(TileLinks.DOOR))
          ret.addAll(getTileOfRoom(tiles.get(player.getCurrentTile().getX()-1).get(player.getCurrentTile().getY())));
        if (player.getCurrentTile().getCanDown().equals(TileLinks.DOOR))
          ret.addAll(getTileOfRoom(tiles.get(player.getCurrentTile().getX()+1).get(player.getCurrentTile().getY())));
        if (player.getCurrentTile().getCanLeft().equals(TileLinks.DOOR))
          ret.addAll(getTileOfRoom(tiles.get(player.getCurrentTile().getX()).get(player.getCurrentTile().getY()-1)));
        if (player.getCurrentTile().getCanRight().equals(TileLinks.DOOR))
          ret.addAll(getTileOfRoom(tiles.get(player.getCurrentTile().getX()).get(player.getCurrentTile().getY()+1)));
        return ret;
    }

    /**
     *
     * @param minDist the in distance of the range
     * @param maxDist the max distance of the range
     * @param t1 the starting point tile
     * @return the list of tiles in the selected range having the starting point in the specified tile
     */
    public List<Tile> getPossibleTiles(int minDist, int maxDist, Tile t1) {
        int tempDist;
        ArrayList<Tile> ret=new ArrayList<>();
           for(List<Tile> t: tiles)
               for(Tile t2 : t){
                  tempDist= getDistanceBetweenTiles(t1,t2);
            if ((tempDist>= minDist) && (tempDist <= maxDist)&&(!t2.isHole())) {
                 ret.add(t2);
            }}
        return ret;
    }


    /***
     * Method to calculate the distance between two tiles not considering walls or holes.
     * It's the distance that a player can freely walk
     * @param t1 the starting point
     * @param t2 the end point
     * @return the distance as an integer between two tiles
     */
    public int getDistanceBetweenTiles(Tile t1,Tile t2) {
        ArrayList<Tile> alreadyVisited = new ArrayList<>();
        ArrayList<Tile> toVisitQueue = new ArrayList<>();
        ArrayList<Integer> distQueue = new ArrayList<>();
        toVisitQueue.add(t1);
        Tile temp;
        int tempDist=0;
        distQueue.add(0, 0);
        if ((t1 == null) || (t2 == null)) {
            return -1;
        }
        while (!toVisitQueue.isEmpty()) {
            temp = toVisitQueue.remove(0);
            tempDist = distQueue.remove(0);
            if(!alreadyVisited.contains(temp)){
                if(temp.equals(t2))
                    break;
                if((temp.getCanUp()==TileLinks.DOOR)||(temp.getCanUp()==TileLinks.NEAR) &&
                                !alreadyVisited.contains(tiles.get(temp.getX()-1).get(temp.getY()))) {
                    toVisitQueue.add(tiles.get(temp.getX() - 1).get(temp.getY()));
                    distQueue.add(tempDist + 1);
                }
                if((temp.getCanDown()==TileLinks.DOOR)||(temp.getCanDown()==TileLinks.NEAR) &&
                                !alreadyVisited.contains(tiles.get(temp.getX()+1).get(temp.getY()))) {
                    toVisitQueue.add(tiles.get(temp.getX()+1).get(temp.getY()));
                    distQueue.add(tempDist+1);
                }
                if((temp.getCanLeft()==TileLinks.DOOR)||(temp.getCanLeft()==TileLinks.NEAR) &&
                                !alreadyVisited.contains(tiles.get(temp.getX()).get(temp.getY()-1))) {
                    toVisitQueue.add(tiles.get(temp.getX()).get(temp.getY()-1));
                    distQueue.add(tempDist+1);
                }
                if((temp.getCanRight()==TileLinks.DOOR)||(temp.getCanRight()==TileLinks.NEAR) &&
                                !alreadyVisited.contains(tiles.get(temp.getX()).get(temp.getY()+1))) {
                    toVisitQueue.add(tiles.get(temp.getX()).get(temp.getY()+1));
                    distQueue.add(tempDist+1);
                }
                alreadyVisited.add(temp);
            }
        }
        return tempDist;
    }

    /**
     * Refill all the empty tiles that are not spawn point, with ammo tiles
     */
    public void fillAllAmmoTiles() {
        for(List<Tile> tileRow : tiles) {
            for(Tile tile : tileRow) {
                if(!tile.isSpawnPoint() && !tile.isHole() && tile.getAmmo() == null) {
                    if(ammoTileDeck.isEmpty())
                        ammoTileDeck.reShuffleAll();
                    tile.setAmmo(ammoTileDeck.draw());
                }
            }
        }
    }

    /**
     * Refill all the empty tiles that are spawn point, with weapons
     */
    public void fillWeaponSpawnPoint(){
        for(List<Tile> tileRow:tiles){
            for(Tile tile : tileRow){
                if(tile.isSpawnPoint()&&tile.getWeapons().size()<3){
                    for(int i=tile.getWeapons().size();i<3;i++)
                        if(weaponsDeck.pileSize()>0)
                            tile.addWeapon(weaponsDeck.draw());
                }
            }
        }
    }

    /**
     * refill a single tile that is not a spawn point with an ammo tile
     * @param tile
     */
    public void fillAmmoTile(Tile tile) {
        if(!tile.isSpawnPoint() && tile.getAmmo() == null) {
            tile.setAmmo(ammoTileDeck.draw());
        }
    }

    public Gamemodes getGamemodes() {
        return gamemodes;
    }

    public int getBoardLength() {
        return boardLength;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public void addPlayer(Player player) {
        playerOrderTurn.add(player);
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }


    /**
     * Send notify of someone giving a mark to another one to update clients' local context
     * @param player the player who received the marks
     * @param marker the player who give the marks
     * @param marks the amount of marks
     * @param oldMarks the amount of marks he holds before this
     */
    public void notifyOnMark(Player player,Player marker,int marks,int oldMarks){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for(ViewProxy v:toNotify) {
            PlayerView pv1 = player.createPlayerView();
            TileView tv1 = createTileView(player.getCurrentTile());
            pv1.setTileView(tv1);
            PlayerView pv2 = marker.createPlayerView();
            TileView tv2 = createTileView(marker.getCurrentTile());
            pv2.setTileView(tv2);
            v.onMarks(pv1, pv2, marks, oldMarks);
        }
    }

    /**
     * Send notify of someone giving a damage to another one to update clients' local context
     * @param player the player who give the damage
     * @param target the player who received the damage
     * @param damage the amount of damage
     * @param marksDown the amount of marks that have to became damage
     */
    public void notifyOnDamage(Player player,Player target,int damage,int marksDown){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for (ViewProxy v :toNotify) {
            PlayerView pView = player.createPlayerView();
            pView.setTileView(GameManager.createTileView(player.getCurrentTile()));
            PlayerView tView = target.createPlayerView();
            tView.setTileView(GameManager.createTileView(target.getCurrentTile()));
            v.onDamage(pView, tView, damage, marksDown);
        }
    }

    /**
     * Send notify of someone grabbing a weapon to update clients' local context
     * @param player the player who grabbed the weapon
     * @param newWeapon the weapon grabbed
     * @param removedWeapon the weapon dropped (if exist)
     */
    public void notifyOnWeaponGrab(Player player,Weapon newWeapon,Weapon removedWeapon){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for(ViewProxy v:toNotify) {
            PlayerView pV = player.createPlayerView();
            pV.setTileView(GameManager.createTileView(player.getCurrentTile()));
            v.onWeaponGrab(pV, newWeapon,removedWeapon);
        }
    }

    /**
     * Send notify of someone drawing a powerup to update clients' local context
     * @param player the player grabbing the powerup
     * @param powerUp the powerup grabbed
     */
    public void notifyOnDrawnPowerUp(Player player, PowerUp powerUp) {
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for (ViewProxy v : toNotify) {
            if (player.getViewPlayer().equals(v))
                v.onPowerUpDrawn(powerUp);
            else {
                PlayerView pV = player.createPlayerView();
                pV.setTileView(GameManager.createTileView(player.getCurrentTile()));
                v.onPowerUpDrawnByEnemy(pV);
            }
        }
    }

    /**
     * Send notify of someone discarding a powerup to update clients' local context
     * @param player the player who is discarding a powerup
     * @param powerUp the powerup discarded
     */
    public void notifyOnDiscardPowerUp(Player player,PowerUp powerUp){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for(ViewProxy v:toNotify) {
            PlayerView pV = player.createPlayerView();
            pV.setTileView(GameManager.createTileView(player.getCurrentTile()));
            v.onPowerUpDiscard(pV, powerUp);
        }
    }

    /**
     * Send notify of someone reloading a weapon to update clients' local context
     * @param player the player who is reloading
     * @param weapon the weapon reloaded
     */
    public void notifyOnReloadWeapon(Player player,Weapon weapon){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for(ViewProxy v: toNotify) {
            PlayerView pV = player.createPlayerView();
            pV.setTileView(GameManager.createTileView(player.getCurrentTile()));
            v.onReloadWeapon(pV, weapon);
        }
    }

    /**
     * Send notify of someone moving to a tile to update clients' local context
     * @param player the player that is moving
     * @param tile the destination tile
     */
    public void notifyOnMovement(Player player,Tile tile){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for(ViewProxy v:toNotify) {
            PlayerView pV = player.createPlayerView();
            pV.setTileView(GameManager.createTileView(player.getCurrentTile()));
            v.onMovement(pV, createTileView(tile));
        }
    }

    /***
     * Send notify of someone grabbing an ammo tile to update clients' local context
     * @param ammoTile the grabbed ammo tile
     * @param player the player who grabbed
     */
    public void notifyOnAmmoTileGrab(AmmoTile ammoTile, Player player) {
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for (ViewProxy v : toNotify) {
            PlayerView pV = player.createPlayerView();
            pV.setTileView(GameManager.createTileView(player.getCurrentTile()));
            v.onAmmoGrab(pV, ammoTile);
        }
    }

    /**
     * Send notify of changing turn to update clients' local context
     * @param endPlayer the player who ends his turn
     * @param newPlayer the player who begins his turn
     * @param tiles the tiles to update
     */
    private void notifyOnChangeTurn(Player endPlayer,Player newPlayer,List<Tile> tiles){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        List<TileView> tileViews = new ArrayList<>();
        for(Tile t : tiles) {
            tileViews.add(createTileView(t));
        }
        for(ViewProxy v:toNotify) {
            PlayerView endV = endPlayer.createPlayerView();
            endV.setTileView(GameManager.createTileView(endPlayer.getCurrentTile()));
            PlayerView newV = newPlayer.createPlayerView();
            newV.setTileView(GameManager.createTileView(newPlayer.getCurrentTile()));
            v.onChangeTurn(endV, newV, tileViews);
        }
    }

    /**
     * Send notify of someone killing another player to update clients' local context
     * @param killer the player who made the kill
     * @param overKill the boolean indicating if there's a overkill
     * @param killed the player that has been killed
     */
    public void notifyOnKillUpdate(Player killer,boolean overKill,Player killed){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for(ViewProxy v:toNotify) {
            PlayerView killerV = killer.createPlayerView();
            killerV.setTileView(GameManager.createTileView(killer.getCurrentTile()));
            PlayerView killedV = killed.createPlayerView();
            killedV.setTileView(GameManager.createTileView(killed.getCurrentTile()));
            v.onKillUpdate(killerV, overKill, killedV);
        }
    }

    /**
     * Send notify of someone scoring points to update clients' local context
     * @param points the point made
     * @param doubleKill the boolean indicating if there was overkill
     * @param scoredOn the player killed
     */
    public void notifyOnPoints(Map<PlayerView,Integer> points,boolean doubleKill,Player scoredOn){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);
        PlayerView playerView;
        for(ViewProxy v:toNotify)
            if(scoredOn==null)
                v.onPoints(points,doubleKill,null);
            else {
                playerView=scoredOn.createPlayerView();
                playerView.setTileView(GameManager.createTileView(scoredOn.getCurrentTile()));
                v.onPoints(points, doubleKill, playerView);
            }
    }

    /**
     * Send notify of ending game to update clients' local context. It also end every timer and call the end game method from network manager
     * @param rank the final leaderboard in which player are sorted by their points
     */
    private void notifyOnEndGame(List<Player> rank){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);
        List<PlayerView> playerViews = new ArrayList<>();
        List<Integer> points= new ArrayList<>();
        PlayerView newP;
        for(Player player:rank) {
            newP=player.createPlayerView();
            newP.setTileView(GameManager.createTileView(player.getCurrentTile()));
            playerViews.add(newP);
            points.add(player.getScore());
        }

        for(ViewProxy v:toNotify) {
            v.onEndGame(playerViews, points);
        }
        try {
            managerObserver.stopPlayerTimer(gameId);
            managerObserver.endGame(gameId);
        }
        catch (NullPointerException e){
            System.out.println("Manager Observer Not found");
        }
    }

    /***
     * Send notify of someone using a weapon to update clients' local context
     * @param player the player who is using a weapon
     * @param weapon the weapon used
     */
    public void notifyOnWeaponUsed(Player player,Weapon weapon){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);
        PlayerView playerView;
        for(ViewProxy v:toNotify) {
            playerView=player.createPlayerView();
            playerView.setTileView(GameManager.createTileView(player.getCurrentTile()));
            v.onWeaponUsed(playerView, weapon);
        }
    }

    /**
     * Send notify of someone using a powerup to update clients' local context
     * @param player the player who is using the powerup
     * @param powerUp the powerup used
     */
    public void notifyOnPowerUpUsed(Player player,PowerUp powerUp){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);
        PlayerView playerView;
        for(ViewProxy v:toNotify) {
            playerView=player.createPlayerView();
            playerView.setTileView(GameManager.createTileView(player.getCurrentTile()));
            v.onPowerUpUsed(playerView, powerUp);

        }
    }

    public void simpleLogNotify(String newMsg){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);

        for(ViewProxy v:toNotify)
            v.onText(newMsg);
    }

    /**
     * Send notify of starting final frenzy
     */
    private void notifyOnFinalFrenzyStart(){
        List<ViewProxy> toNotify = new ArrayList<>(notifyObservers);
        List<PlayerView> playerViews= new ArrayList<>();
        PlayerView playerView;
        for(Player p:playerOrderTurn) {
            playerView = p.createPlayerView();
            playerView.setTileView(GameManager.createTileView(p.getCurrentTile()));
            playerViews.add(p.createPlayerView());
        }

        for(ViewProxy v:toNotify)
            v.onFinalFrenzyStart(playerViews);
    }

    /**
     * Method to quit a player. If he was the current player the turn is changed
     * @param player the player who is quitting
     */
    public void quitPlayer(Player player) {
        for(List<Tile> row : tiles) {
            for(Tile t : row) {
                if(t.getPlayers().contains(player)) {
                    t.removePlayer(player);
                }
            }
        }

        if(isStarted() && currentPlayer.equals(player) && playerOrderTurn.size() > 3) {
            changeTurn();
        }

        else if(playerOrderTurn.size() <= 3) {
            if(managerObserver == null) {
                System.out.println("ERROR MANAGER OBSERVER IS NULL");
                return;
            }

            if(gameId != -1) {
                managerObserver.stopPlayerTimer(gameId);
            }
            else {
                System.out.println("ERROR WHILE CLOSING THE TIMER");
            }
        }

        playerOrderTurn.remove(player);
    }

    public void insertNotifier (ViewProxy v) {
        if(v != null && !notifyObservers.contains(v)) {
            notifyObservers.add(v);
        }
    }

    public void removeNotifier (ViewProxy v) {
        if(v != null){
            notifyObservers.remove(v);
        }
    }

    public int getRemainingPlayers() {
        int count = 0;
        for(Player p : playerOrderTurn) {
            if(!p.isDisconnected()) {
                count ++;
            }
        }
        return count;
    }

    public void setGameEnd(boolean end) {
        this.gameEnd = end;
    }

    public boolean isGameEnd() {
        return gameEnd;
    }

    public void setManagerObserver(ManagerObserver managerObserver) {
        this.managerObserver = managerObserver;
    }


    /**
     * Methos to create a tile view to send to the client to not send any content a client could'nt have
     * @param tile the tile to which create the tile view
     * @return the tile vire created
     */
    public static TileView createTileView(Tile tile) {
        if(tile == null) {
            return null;
        }
        List<PlayerView> playerViews = new ArrayList<>();
        for(Player p : tile.getPlayers()) {
            PlayerView pV = p.createPlayerView();
            playerViews.add(pV);
        }
        List<Weapon> weaponsToCopy = new ArrayList<>(tile.getWeapons());
        List<PlayerView> playerViewToCopy = new ArrayList<>(playerViews);
        return new TileView(tile.getX(), tile.getY(), tile.getRoom(), tile.getAmmo(), weaponsToCopy, tile.isSpawnPoint(), tile.getCanUp(), tile.getCanDown(), tile.getCanRight(), tile.getCanLeft(), tile.isHole(), playerViewToCopy);
    }


}


