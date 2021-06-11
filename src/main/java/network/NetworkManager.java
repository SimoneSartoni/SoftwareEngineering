package network;

import controller.TurnStateController;
import model.board.*;
import model.enums.PlayerColor;
import model.enums.PlayerState;
import model.enums.TurnState;
import model.exceptions.InvalidColorException;
import model.exceptions.InvalidGameIdException;
import model.exceptions.InvalidUsernameException;
import model.gamemodes.BoardStructure;
import model.gamemodes.Gamemodes;
import model.player.Player;
import model.player.PlayerView;
import model.utility.Ammo;
import model.utility.MapInfo;
import model.weapon.InitWeapons;
import run.LaunchClient;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class NetworkManager implements Serializable {

    //path and other constants
    private static final String POINTS_FILE = "points.txt";
    private static final String MAP_FILE = "map.txt";
    private static final String WEAPON_FILE = "weaponsToParse.txt";
    private static final String POW_FILE = "powerUpToParse.txt";
    private static final String AMMO_FILE = "ammoTilesToParse.txt";
    private static final int COUNTDOWN_TIME = InitMap.initCountdown(MAP_FILE);
    private static final int PLAYER_COUNTDOWN_TIME = InitMap.initPlayerCountdown(MAP_FILE);
    private static NetworkManager instance;
    private final List<MapInfo> maps = new ArrayList<>(InitMap.initMap(MAP_FILE));
    private TurnStateController turnStateController;

    //timer stuffs
    private List<MyTimer> gameTimer = new ArrayList<>();
    private final List<Boolean> timerStarted = new ArrayList<>();
    private final List<NetworkCountdown> networkCountdowns = new ArrayList<>();
    private List<MyTimer> playerTimer = new ArrayList<>();
    private final List<PlayerCountdown> playerCountdowns = new ArrayList<>();
    private final Map<String,MyTimer> synTimer=new HashMap<>();
    private final Map<String,MyTimer> ackCheckTimer= new HashMap<>();
    private Map<String, Boolean> ackMap= new HashMap<>();
    private final int ackTime=200000;
    private final int synTime=100000;

    //tokens and player management stuffs
    private List<GameManager> gameManagers = new ArrayList<>();
    private Map<String, Player> tokenPlayers = new HashMap<>();
    Map<String, ViewListener> viewListeners = new HashMap<>();
    Map<String, ViewProxy> viewsProxy = new HashMap<>();
    Map<Integer, List<ViewProxy>> viewListenersById = new HashMap<>();
    Map<Integer, MapInfo> mapChosenById = new HashMap<>();
    Map<String, Integer> disconnectedTokens = new HashMap<>();
    private Map<String, ViewListener> disconnectedViewListener = new HashMap<>();
    Map<String, ViewProxy> disconnectedProxyListener = new HashMap<>();

    //Persistence
    private transient MyTimer persistenceTimer = new MyTimer();
    private transient final int persistenceTime = 500000;

    private NetworkManager() {
        persistenceTimer.scheduleAtFixedRate(new PersistenceCountdown(), persistenceTime, persistenceTime);
    }


    /**
     * Method to return the instance (Singleton pattern)
     * @return the instance of Network Manager
     */
    public static synchronized NetworkManager get(boolean load) {
        if(instance == null) {
            instance = new NetworkManager();
        }
        if(load) {
            loadPreviousState();
        }
        return instance;
    }

    /**
     * Method to load from file a previous saved network manager with all related games. (PERSISTENCY feature)
     */
    private static void loadPreviousState() {
        String path = LaunchClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int lastSlashIndex = path.lastIndexOf("/");
        path = path.substring(0, lastSlashIndex + 1);
        path = path + "saves.txt";
        File file = new File(path);
        if(file.exists()) {
            NetworkManager read = (NetworkManager) (new SaveGameUtility().loadFile(path));
            if(read == null) {
                System.err.println("Error while loading Network Manager from file");
            }
            else {
                instance = read;
            }
        }

        else {
            System.out.println("Cannot find any previous state to load. Starting normally..");
        }
    }

    void setTurnStateController(TurnStateController turnStateController) {
        this.turnStateController = turnStateController;
    }

    synchronized List<GameManager> getGameManagers() {
        return gameManagers;
    }

    /***
     * Function to create a new player and make him join the selected game. If the selected game is full or if it is
     * already started he will be notified.
     * When the minimum number of player has been reached a gameTimer, at the end of which the game can be played, is started
     * When the maximum number of player has been reached the gameTimer is stopped and the game automatically started
     * @param token of the player that has done the request
     * @param gameID of the game to join
     * @param name requested by the client for his player
     * @param color requested by the client for his player
     * @throws InvalidUsernameException in case of username requested already exists
     * @throws InvalidGameIdException in case of invalid game id. So if the game is full, or already started, or there's not a game in the index given
     * @throws InvalidColorException in case of color requested already exists
     */
    synchronized Player createPlayer(String token, int gameID, String name, PlayerColor color)
            throws InvalidUsernameException, InvalidGameIdException, InvalidColorException {

        if(gameID < 0 || gameID >= gameManagers.size() || gameManagers.get(gameID).isStarted()) {
            throw new InvalidGameIdException("The game id selected is not valid. Please provide a valid game id. " + getValidGameId());
        }

        else {
            Player player = new Player(name, color, PlayerState.NORMAL, null, gameManagers.get(gameID).getCurrentGameMode().getBaseTurnAction());
            player.setBoard(gameManagers.get(gameID).getCurrentGameMode().getPointsBoard());

            List<Player> players = gameManagers.get(gameID).getPlayerOrderTurn();
            for (Player p : players) {
                if (name.equals(p.getPlayerID())) {
                    String alreadyUsedNames = "[";
                    for (Player pl : players) {
                        alreadyUsedNames = alreadyUsedNames.concat(" " + pl.getPlayerID());
                    }
                    alreadyUsedNames = alreadyUsedNames.concat(" ]");
                    throw new InvalidUsernameException("The username " + name + " is already in use. Already used usernames: " + alreadyUsedNames);
                }
                if (color.equals(p.getPlayerColor())) {
                    String alreadyUsedColors = "[";
                    for (Player pl : players) {
                        alreadyUsedColors = alreadyUsedColors.concat(" " + pl.getPlayerColor().name());
                    }
                    alreadyUsedColors = alreadyUsedColors.concat(" ]");
                    throw new InvalidColorException("The color is already in use. Already used colors: " + alreadyUsedColors);
                }
            }

            player.setGameID(gameID);
            player.setViewPlayer(viewsProxy.get(token));
            player.setToken(token);
            gameManagers.get(gameID).addPlayer(player);
            tokenPlayers.put(token, player);

            //add view listener to the list of view listener of that game
            List<ViewProxy> viewListenersToAdd = new ArrayList<>();
            List<ViewProxy> alreadyPresent = viewListenersById.get(gameID);
            if (alreadyPresent != null) {
                viewListenersToAdd.addAll(viewListenersById.get(gameID));
            }
            viewListenersToAdd.add(viewsProxy.get(token));
            viewListenersById.put(gameID, viewListenersToAdd);

            //notify of the creation
            PlayerView pV = player.createPlayerView();
            pV.setTileView(GameManager.createTileView(player.getCurrentTile()));
            viewsProxy.get(token).onNewPlayer(pV);
            for(ViewProxy  v :viewListenersById.get(gameID)){
                if(!(v.equals(viewsProxy.get(token)))) {
                    v.onNewPlayer(pV);
                }
            }
            for(Player p : gameManagers.get(gameID).getPlayerOrderTurn()) {
                if(!p.equals(player)) {
                    PlayerView pView = p.createPlayerView();
                    pView.setTileView(GameManager.createTileView(p.getCurrentTile()));
                    viewsProxy.get(token).onNewPlayer(pView);
                }
            }
            return player;
        }
    }

    private synchronized String getValidGameId() {
        boolean almostOne = false;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ Valid game id: ");
        for(GameManager g : gameManagers) {
            if(!g.isStarted() && !g.isGameEnd()) {
                stringBuilder.append(gameManagers.indexOf(g));
                stringBuilder.append(" ");
                almostOne = true;
            }
        }
        if(almostOne) {
            stringBuilder.append(" ]");
            return stringBuilder.toString();
        }
        return "No valid games found. Create a new one";
    }


    /**
     * creates the new game
     * @param gameId: id of the Game to create
     * @param map
     * @param endMode
     */
    private synchronized void createNewGame(int gameId, int map, String endMode) {
        boolean end = false;
        if(endMode.equals("final frenzy")) {
            end = true;
        }
        MapInfo chosenMap = createNewMap(map - 1, gameId, endMode);
        chosenMap.setNumberOfSkulls(maps.get(map-1).getNumberOfSkulls());
        mapChosenById.put(gameId, chosenMap);
        Gamemodes gamemodes = new Gamemodes(new BoardStructure(POINTS_FILE));
        KillShotTrack killShotTrack = new KillShotTrack(gamemodes.getNormalMode().getPointsBoard().getPoints(), chosenMap.getNumberOfSkulls(), new ArrayList<>(), 1);
        GameManager gameManager = new GameManager(new Deck<>(InitWeapons.initAllWeapons(WEAPON_FILE)), new Deck<>(InitMap.initPowerUps(POW_FILE)),
                new Deck<>(InitMap.initAmmoTiles(AMMO_FILE)), new ArrayList<>(), chosenMap.getMap(), gamemodes.getNormalMode(), killShotTrack,
                end, gamemodes, chosenMap.getMapWidth(), chosenMap.getMapHeight());
        gameManager.getCurrentGameMode().getEndObserver().setGameManager(gameManager);
        gameManager.setManagerObserver(new ManagerObserver());
        gamemodes.getFinalFrenzyMode().getEndObserver().setGameManager(gameManager);
        gameManagers.add(gameManager);
        gameTimer.add(new MyTimer());
        playerTimer.add(new MyTimer());
        networkCountdowns.add(new NetworkCountdown(turnStateController, gameId, gameManager));
        playerCountdowns.add(new PlayerCountdown(gameManager, gameId));
        timerStarted.add(false);
    }

    /**
     * creates the Board for a new Game
     * @param index the number of the map chosen by user
     * @param gameId the number of the Game that it's starting
     * @param endMode the type of ending chosen by user: final frenzy or sudden death
     * @return the map created
     */
    private MapInfo createNewMap(int index, int gameId, String endMode) {
        MapInfo mapToCopy = maps.get(index);
        int widht = mapToCopy.getMapWidth();
        int height = mapToCopy.getMapHeight();
        int maxN = mapToCopy.getMaxNumberOfPlayer();
        int minN = mapToCopy.getMinNumberOfPlayer();
        List<PlayerColor> playerColors = new ArrayList<>(mapToCopy.getAllowedPlayerColors());
        List<String> endModes = new ArrayList<>(mapToCopy.getAllowedEndModes());
        List<PlayerView> playerViews = new ArrayList<>(mapToCopy.getPlayerViews());


        List<Tile> rowToAdd = new ArrayList<>();
        List<List<Tile>> tilesToAdd = new ArrayList<>();

        for(List<Tile> row : mapToCopy.getMap()) {
            for(Tile t : row) {
                int red = 0;
                int yellow = 0;
                int blue = 0;
                int nOfPow = 0;
                AmmoTile toAddAmmoTile = null;
                if(t.getAmmo() != null) {
                    if(t.getAmmo().getAmmoGained() != null) {
                        red = t.getAmmo().getAmmoGained().getRedValue();
                        yellow = t.getAmmo().getAmmoGained().getYellowValue();
                        blue = t.getAmmo().getAmmoGained().getBlueValue();
                    }
                    nOfPow = t.getAmmo().getNOfPowerUp();
                    toAddAmmoTile = new AmmoTile(new Ammo(red, blue, yellow), nOfPow);
                }

                rowToAdd.add(new Tile(t.getRoom(), t.getX(), t.getY(), t.isSpawnPoint(), t.getCanUp(), t.getCanDown(), t.getCanLeft(), t.getCanRight(), toAddAmmoTile));
            }
            tilesToAdd.add(new ArrayList<>(rowToAdd));
            rowToAdd.clear();
        }

        return new MapInfo(index + 1, gameId, endMode, widht, height, tilesToAdd, playerColors, endModes, maxN, minN, playerViews);
    }

    synchronized int createGame(int map, String endMode) {
        createNewGame(gameManagers.size(), map, endMode);
        gameManagers.get(gameManagers.size() - 1).setGameId(gameManagers.size()-1);
        return gameManagers.size() - 1;
    }

    /***
     * Function to give to the client all the possible games he can join
     * @return a list with the games status
     */
    synchronized List<MapInfo> giveGamesStatus() {
        List<MapInfo> mapInfos = new ArrayList<>();
        int gameCount = 0;
        for(GameManager g : gameManagers) {
            if(!g.isStarted() && !g.isGameEnd()) {
                MapInfo currentMap = mapChosenById.get(gameCount);
                mapInfos.add(currentMap);
            }
            gameCount ++;
        }
        return mapInfos;
    }

    /**
     * generates the unique identifier for a user
     * @return the identifier string
     */
    synchronized String generateToken() {
       return UUID.randomUUID().toString();
    }

    /**
     * timer used to ping the client to see if it's still reachable
     * @param token the client to ping
     */
    synchronized  void startSynTimer(String token){
        synTimer.put(token,new MyTimer());
        synTimer.get(token).scheduleAtFixedRate(new ConnectionServerTimer(viewsProxy.get(token)),synTime,synTime);
    }

    /***
     * Method to get the virtual view of a player to comunicate with him
     * @param token the token of the player to which comunicate
     * @return the virtual view of the player
     */
    synchronized ViewProxy getTokenProxy(String token) {
        if(token != null) {
            return viewsProxy.get(token);
        }
        return null;
    }

    /**
     * Method to get the Game Manager in which a given player, by his token, is playing
     * @param token the player of which is wanted to know the game
     * @return the GameManager he's in
     */
    synchronized GameManager getPlayerGame(String token){
        for(GameManager g : gameManagers) {
            for(Player p : g.getPlayerOrderTurn()) {
                if (p.getToken().equals(token)) {
                    return g;
                }
            }
        }
        return null;
    }

    synchronized Player getPlayer(String token){
        if(token!=null)
           return tokenPlayers.get(token);
        else
            return null;
    }

    /**
     * this method returns the corresponding Player of a token, taking him both from active and from disconnected players
     * @param token the token of the player of which we want to know the game
     * @param game the number of the game he's in
     * @return the corresponding Player
     */
    private synchronized Player getPlayerFromGame(String token, int game) {
        if(token == null) {
            return null;
        }
        GameManager gameManager = gameManagers.get(game);
        Player player = null;
        for(Player p : gameManager.getPlayerOrderTurn()) {
            if(p.getToken().equals(token)) {
                player = p;
                break;
            }
        }

        return player;
    }

    synchronized void addTokenSocket(String token,ClientHandler clientHandler){
        viewListeners.put(token,clientHandler);
        viewsProxy.put(token, new ViewProxy(clientHandler, token,turnStateController));
    }

    synchronized void addTokenView(String token, ViewListener viewListener) {
        viewListeners.put(token,viewListener);
        viewsProxy.put(token, new ViewProxy(viewListener, token,turnStateController));
    }

    /***
     * Method to print the list of players at start of a game
     * @param gameID the game that is starting
     * @return the string to print
     */
    synchronized List<String> printStarting(int gameID) {
        List<String> res = new ArrayList<>();
        for(Player p : gameManagers.get(gameID).getPlayerOrderTurn()) {
            res.add("-> " + p.getPlayerID());
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("End game mode: ");
        if(gameManagers.get(gameID).isFinalFrenzy()){
            stringBuilder.append("final frenzy");
        }
        else {
            stringBuilder.append("sudden death");
        }
        res.add(stringBuilder.toString());
        return res;
    }

    /***
     * Method to notify all the clients of a game that it is starting
     * @param gameID the game that is starting
     */
    synchronized void notifyAllRelatedViews(int gameID) {
        List<ViewProxy> viewListenersToNotify = new ArrayList<>(viewListenersById.get(gameID));
        for(ViewProxy v : viewListenersToNotify) {
            List<String> sendString = new ArrayList<>();
            sendString.add(System.getProperty("line.separator") + ">>> Game ready. Starting with players: ");
            sendString.addAll(printStarting(gameID));
            sendString.add("");
            v.onMoreText(sendString);
        }
    }


    synchronized String getTokenFromPlayer(Player player) {
        for(String s : tokenPlayers.keySet()) {
            if(tokenPlayers.get(s).equals(player)) {
                return s;
            }
        }
        return "";
    }

    /**
     * this method sets all the Virtual Views related to each Player in a game
     * @param gameId the Game in which
     */
    synchronized  void setGameManagerListener(int gameId) {
        GameManager gameManager = gameManagers.get(gameId);
        gameManager.setNotifyObservers(viewListenersById.get(gameId));
        for(Player p : gameManager.getPlayerOrderTurn()) {
            p.setViewPlayer(viewsProxy.get(getTokenFromPlayer(p)));
        }
    }

    synchronized List<MapInfo> getMaps() {
        return maps;
    }

    /***
     * Methods to check if a game has to be started. It checks if the minimum number of player has been reached.
     * In this case a timer is start at the end of which the game is started and the active player is set. If the maximum
     * number of players is reached the game is started automatically.
     * @param gameID the id of the game to check
     */
    synchronized void checkStartingGame(int gameID) {
        GameManager gm = gameManagers.get(gameID);
        MapInfo mapInfo = mapChosenById.get(gameID);
        mapInfo.clearPlayerViews();
        List<PlayerView> viewsToAdd = new ArrayList<>();
        for(Player p : gm.getPlayerOrderTurn()) {
            PlayerView pv = p.createPlayerView();
            pv.setTileView(GameManager.createTileView(p.getCurrentTile()));
            viewsToAdd.add(pv);
        }
        mapInfo.getPlayerViews().addAll(viewsToAdd);
        if (gm.getPlayerOrderTurn().size() == mapChosenById.get(gameID).getMaxNumberOfPlayer()) {
            networkCountdowns.get(gameID).cancel();
            setGameManagerListener(gameID);
            gm.setStarted(true);
            notifyAllRelatedViews(gameID);
            gm.startGame();
            List<ViewProxy> viewListenersToNotify = viewListenersById.get(gameID);
            String currentToken = getTokenFromPlayer(gm.getCurrentPlayerTurn());
            ViewProxy currentView = viewsProxy.get(currentToken);
            for(ViewProxy v : viewListenersToNotify) {
                v.onGameStarted(true);
                v.onMapInfo(MapInfo.createMapView(mapChosenById.get(gameID)),gm.getKillShotTrack());
                if (v.equals(currentView)) {
                    v.onActiveTurn(true);
                }
                else {
                    v.onActiveTurn(false);
                }
            }
            for (String s : printStarting(gameID)) {
                System.out.println(s);
            }
            startPlayerTimer(gameID);
        }
        else if (gm.getPlayerOrderTurn().size() >= mapChosenById.get(gameID).getMinNumberOfPlayer()
                && !timerStarted.get(gameID)) {
            timerStarted.set(gameID, true);
            gameTimer.get(gameID).schedule(networkCountdowns.get(gameID), COUNTDOWN_TIME);
        }
    }

    synchronized boolean getAlreadyLoggedUser(String alreadyExistingToken) {
        return (disconnectedTokens.get(alreadyExistingToken) != null);
    }

    /***
     * Method to quit a player. It removes it from the gaemManager and from all of the structure in this class.
     * @param token the player to quit
     * @return the integer representing the game from which the player has been quit
     */
    synchronized int playerQuit(String token) {
        ViewProxy toQuit = viewsProxy.get(token);
        Player player = getPlayer(token);
        if(player == null) {
            return -1;
        }
        Tile tile = player.getCurrentTile();

        int game = player.getGameID();

        viewListenersById.get(game).remove(toQuit);

        MapInfo map = mapChosenById.get(game);
        if(tile != null) {
            map.getMap().get(tile.getX()).get(tile.getY()).removePlayer(player);
        }

        gameManagers.get(game).quitPlayer(player);
        gameManagers.get(game).removeNotifier(toQuit);

        viewListeners.remove(token);
        viewsProxy.remove(token);
        disconnectedTokens.remove(token);
        tokenPlayers.remove(token);
        return game;
    }

    /***
     * Method to return the game to which a given view is related
     * @param viewProxy the view to which is wanted to know the game
     * @param proxyViews the list from which to check among
     * @return the integer representing the game id
     */
    private synchronized int getGameIdFromListener(ViewProxy viewProxy, Collection<List<ViewProxy>> proxyViews) {
        int gameCount = 0;
        boolean found = false;
        for(List<ViewProxy> views : proxyViews) {
            for(ViewProxy v : views) {
                if(v.getToken().equals(viewProxy.getToken())) {
                    found = true;
                    break;
                }
            }
            if(found) {
                break;
            }
            gameCount ++;
        }
        if(!found) {
            gameCount = -1;
        }
        return gameCount;
    }

    /***
     * Method to disconnect a player from the game. The player is inserted in some maps to retrieve information later on reconnecion.
     * All the other players are warned about the disconnection. If the game cannot be played anymore because the remaining players
     * are less than the minimum number of players the game is quit.
     * @param token the token of the player to disconnect
     */
    synchronized void disconnectToken(String token) {
        if(disconnectedTokens.get(token) != null) {
            return;
        }
        ViewProxy proxyToDisconnect = viewsProxy.get(token);
        int game = getGameIdFromListener(proxyToDisconnect, viewListenersById.values());

        //disconnect player
        Player playerToDisconnect = null;
        String name = "";
        if(game != -1) {
            for (Player p : gameManagers.get(game).getPlayerOrderTurn()) {
                if (p.getToken().equals(token)) {
                    playerToDisconnect = p;
                    p.setDisconnected(true);
                    name = p.getPlayerID();
                    break;
                }
            }

            disconnectedTokens.put(token, game);
            disconnectedViewListener.put(token, viewListeners.get(token));
            disconnectedProxyListener.put(token, proxyToDisconnect);
            gameManagers.get(game).removeNotifier(proxyToDisconnect);

            List<ViewProxy> replaceList = new ArrayList<>();
            if (viewListenersById.get(game) != null) {
                replaceList.addAll(viewListenersById.get(game));
            }
            replaceList.remove(proxyToDisconnect);
            viewListenersById.replace(game, replaceList);
            tokenPlayers.remove(token);
            viewsProxy.remove(token);
            viewListeners.remove(token);

            System.out.println("Player " + name + " with token " + token + " is now disconnected");

            GameManager gameManager = gameManagers.get(game);
            boolean started = gameManagers.get(game).isStarted();
            boolean allQuit = gameManagers.get(game).getRemainingPlayers() < mapChosenById.get(game).getMinNumberOfPlayer();
            if (started && allQuit) {
                System.out.println("The game " + game + " has not enough players. Quit.");
                gameManager.endOfGame();
                stopPlayerTimer(game);
                endGame(game, name);
            }
            else if(started){
                stopPlayerTimer(game);
                restartPlayerTimer(game);
                notifyOfDisconnection(allQuit, name, game);
                if(gameManager.getCurrentPlayerTurn().equals(playerToDisconnect)) {
                    gameManager.changeTurn();
                }
            }
            else if(allQuit){
                //the game is not started yet
                notifyOfDisconnection(false, name, game);
                stopTimer(game);
                gameTimer.set(game, new MyTimer());
                networkCountdowns.set(game, new NetworkCountdown(turnStateController, game, gameManagers.get(game)));
                timerStarted.set(game, false);
            }
        }
    }

    /***
     * Function to disconnect a token during the phase of loading if server crash.
     * Contrary on what happen during normal disconnection, this function will not notify players
     * and will not end the game
     * @param token the player to disconnect
     */
    synchronized void disconnectWhileLoading(String token) {
        if(disconnectedTokens.get(token) != null) {
            return;
        }
        ViewProxy proxyToDisconnect = viewsProxy.get(token);
        int game = getGameIdFromListener(proxyToDisconnect, viewListenersById.values());

        //disconnect player
        Player playerToDisconnect = null;
        if(game != -1) {
            for (Player p : gameManagers.get(game).getPlayerOrderTurn()) {
                if (p.getToken().equals(token)) {
                    playerToDisconnect = p;
                    p.setDisconnected(true);
                    break;
                }
            }

            disconnectedTokens.put(token, game);
            disconnectedViewListener.put(token, viewListeners.get(token));
            disconnectedProxyListener.put(token, proxyToDisconnect);
            gameManagers.get(game).removeNotifier(proxyToDisconnect);

            List<ViewProxy> replaceList = new ArrayList<>();
            if (viewListenersById.get(game) != null) {
                replaceList.addAll(viewListenersById.get(game));
            }
            replaceList.remove(proxyToDisconnect);
            viewListenersById.replace(game, replaceList);
            tokenPlayers.remove(token);
            viewsProxy.remove(token);
            viewListeners.remove(token);
        }
    }

    /**
     * notify views of a game about a player that has disconnected
     * @param all boolean to indicate if each player of the game has to quit
     * @param name the name of the player that has disconnected
     * @param game the game in which the player that has disconnected was playing
     */
    private synchronized void notifyOfDisconnection(boolean all, String name,  int game) {
        for(ViewProxy v : viewListenersById.get(game)) {
            v.onQuit(all, name, false, true);
        }
    }

    /**
     * this method restart the turn timer for the player who's choosing now.
     * @param game the game in which the new timer has to be initialised and started
     */
    synchronized void restartPlayerTimer(int game) {
        playerTimer.set(game, new MyTimer());
        playerCountdowns.set(game, new PlayerCountdown(gameManagers.get(game), game));
        startPlayerTimer(game);
    }

    /**
     * This methods clears all the timers, views, proxys and lists referring to a specific game that has just ended.
     * @param game the game to close
     * @param causePlayer
     */
    synchronized void endGame(int game, String causePlayer) {
        List<String> tokenToDisconnect = new ArrayList<>();
        for(String s : disconnectedTokens.keySet()) {
            if(disconnectedTokens.get(s) == game) {
                tokenToDisconnect.add(s);
            }
        }
        List<ViewProxy> proxyOfThegame = new ArrayList<>(viewListenersById.get(game));
        for(String s : viewsProxy.keySet()) {
            if(proxyOfThegame.contains(viewsProxy.get(s))) {
                tokenToDisconnect.add(s);
            }
        }

        for(String token : tokenToDisconnect) {
            if(viewsProxy.get(token) != null) {
                viewsProxy.get(token).onQuit(true, causePlayer, false, false);
            }
            if(disconnectedProxyListener.get(token) != null) {
                disconnectedProxyListener.get(token).onQuit(true, causePlayer, false, false);
            }
            disconnectedTokens.remove(token);
            disconnectedViewListener.remove(token);
            disconnectedProxyListener.remove(token);
            viewListeners.remove(token);
            ViewProxy proxyToRemove = viewsProxy.get(token);
            gameManagers.get(game).removeNotifier(proxyToRemove);
            viewsProxy.remove(token);
            tokenPlayers.remove(token);
            viewListenersById.get(game).remove(proxyToRemove);
            if(synTimer.get(token) != null) {
                synTimer.get(token).cancel();
            }
            if(ackCheckTimer.get(token) != null) {
                ackCheckTimer.get(token).cancel();
            }
        }
        gameManagers.get(game).setStarted(false);
        gameManagers.get(game).setGameEnd(true);
    }

    /**
     * This method reconnects the disconnected client, removing his token from the disconnected ones.
     *
     * @param alreadyExistingToken the client to reconnect
     * @param viewListener the  new viewListener associated to the client
     */
    synchronized void reconnectUserCauseConnection(String alreadyExistingToken, ViewListener viewListener) {
        int game = disconnectedTokens.get(alreadyExistingToken);
        viewListeners.put(alreadyExistingToken, viewListener);
        viewsProxy.put(alreadyExistingToken, new ViewProxy(viewListener, alreadyExistingToken,turnStateController));
        viewListenersById.computeIfAbsent(game, ArrayList::new);
        viewListenersById.get(game).add(viewsProxy.get(alreadyExistingToken));

        for(Player p : gameManagers.get(game).getPlayerOrderTurn()) {
            if(p.getToken().equals(alreadyExistingToken)) {
                p.setViewPlayer(getTokenProxy(alreadyExistingToken));
                tokenPlayers.put(alreadyExistingToken, p);
                p.setDisconnected(false);
                if(p.getCurrentTile() == null) {
                    if(p.getPowerUps().isEmpty()) {
                        p.setTurnState(TurnState.READY_TO_SPAWN);
                    }
                    else {
                        p.setTurnState(TurnState.READY_TO_SPAWN_DISCONNECTION);
                    }
                }
                else {
                    p.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
                }
                break;
            }
        }

        gameManagers.get(game).insertNotifier(getTokenProxy(alreadyExistingToken));


        disconnectedTokens.remove(alreadyExistingToken);
        disconnectedProxyListener.remove(alreadyExistingToken);
        disconnectedViewListener.remove(alreadyExistingToken);

        startSynTimer(alreadyExistingToken);
        startAckTimer(alreadyExistingToken);
        viewsProxy.get(alreadyExistingToken).onInactivity(false);
        System.out.println("Player " + tokenPlayers.get(alreadyExistingToken).getPlayerID() + " with token " + alreadyExistingToken + " is now connected again");
        if(!gameManagers.get(game).isStarted() && !gameManagers.get(game).isGameEnd()) {
            checkStartingGame(game);
            GameManager g = getGameManagers().get(game);
            if(g.isStarted()) {
                for(Player p : g.getPlayerOrderTurn()){
                    p.getViewPlayer().onPrintHelp(g,p);
                }
            }
        }
    }

    /**
     * In this method the disconnected client (for inactivity) is reconnected to the game he was in before.
     * All the related proxys and virtual Views are set the same way they were before disconnection.
     * The client (token) is removed from the disconnected ones. The ping Timer restarts and the client is notified of his reconnection
     * In case the client has disconnected before a spawn action, then he's going to do an exceptional spawn action during his first turn after being reconnected.
     * @param token the client that needs to be reconnected
     */
    synchronized void reconnectUserCauseInactive(String token) {
        int game = disconnectedTokens.get(token);
        ViewListener disconnectedView = disconnectedViewListener.get(token);
        ViewProxy disconnectedProxy = disconnectedProxyListener.get(token);
        viewListenersById.computeIfAbsent(game, ArrayList::new);
        viewListenersById.get(game).add(disconnectedProxy);
        viewListeners.put(token, disconnectedView);
        viewsProxy.put(token, disconnectedProxy);

        for(Player p : gameManagers.get(game).getPlayerOrderTurn()) {
            if(p.getToken().equals(token)) {
                p.setViewPlayer(getTokenProxy(token));
                tokenPlayers.put(token, p);
                p.setDisconnected(false);
                if(p.getCurrentTile() == null) {
                    if(p.getPowerUps().isEmpty()) {
                        p.setTurnState(TurnState.READY_TO_SPAWN);
                    }
                    else {
                        p.setTurnState(TurnState.READY_TO_SPAWN_DISCONNECTION);
                    }
                }
                else {
                    p.setTurnState(TurnState.WAIT_FOR_OTHER_PLAYERS_TURN);
                }
                break;
            }
        }

        disconnectedViewListener.remove(token);
        disconnectedProxyListener.remove(token);
        disconnectedTokens.remove(token);

        gameManagers.get(game).insertNotifier(getTokenProxy(token));

        startSynTimer(token);
        startAckTimer(token);
        viewsProxy.get(token).onInactivity(false);
        System.out.println("Player " + tokenPlayers.get(token).getPlayerID() + " with token " + token + " is now connected again");
    }

    synchronized boolean isActiveToken(String alreadyExistingToken) {
        return viewListeners.get(alreadyExistingToken) != null;
    }

    synchronized void stopTimer(int gameId) {
        gameTimer.get(gameId).purge();
        gameTimer.get(gameId).cancel();
    }

    synchronized void stopPlayerTimer(int gameId) {
        playerTimer.get(gameId).purge();
        playerTimer.get(gameId).cancel();
    }

    synchronized void startPlayerTimer(int gameID) {
        try {
            playerTimer.get(gameID).schedule(playerCountdowns.get(gameID), PLAYER_COUNTDOWN_TIME);
        } catch (IllegalStateException e) {
            System.out.println("Cannot start player timer");
        }
    }

    /***
     * Given a token this methos says if he is the current player
     * @param game the game of which is asked the current player
     * @param token the token of which is asked if he is the current player
     * @return true if "token" player is the current player, false otherwise
     */
    synchronized boolean isCurrentPlayerForGame(int game, String token) {
        GameManager gameManager = gameManagers.get(game);
        if(gameManager == null) {
            return false;
        }
        Player current = gameManager.getCurrentPlayerTurn();
        if(current == null) {
            return false;
        }
        return current.getToken().equals(token);
    }

    /**
     * This methods changes the Player in a PlayerView that can be used by the View in the Client
     * @param player the Object we want to copy for the View
     * @return the PlayerView associated to the player
     */
    PlayerView createPlayerView(Player player) {
        PlayerView playerView = player.createPlayerView();
        playerView.setTileView(GameManager.createTileView(player.getCurrentTile()));
        return playerView;
    }

    void setAck(String token,boolean received) {
        if(ackMap.containsKey(token))
            ackMap.replace(token,received);
        else
            ackMap.put(token,received);
    }

    /**
     * this methods starts a timer to receive client answer to the ping previously sent
     * @param token the client associated with the timer
     */
    void startAckTimer(String token) {
        ackCheckTimer.put(token,new MyTimer());
        ackCheckTimer.get(token).scheduleAtFixedRate(new ConnectionAckServerTimer(viewsProxy.get(token),this),ackTime,ackTime);
    }

    Map<String, Boolean> getAckMap() {
        return ackMap;
    }

    /**
     * Clears the SynTimer
     * @param token the client related to the timer
     */
    void stopSynTimer(String token) {
        if(synTimer.get(token) != null) {
            synTimer.get(token).purge();
            synTimer.get(token).cancel();
        }
    }

    /**
     * Clears the AckTimer
     * @param token the client related to the timer
     */
    void stopAckTimer(String token) {
        if(ackCheckTimer.get(token) != null) {
            ackCheckTimer.get(token).purge();
            ackCheckTimer.get(token).cancel();
        }
    }
}

