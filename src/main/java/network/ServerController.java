package network;

import controller.TurnStateController;
import model.board.GameManager;
import model.board.KillShotTrack;
import model.board.TileView;
import model.enums.*;
import model.exceptions.GameException;
import model.exceptions.InvalidColorException;
import model.exceptions.InvalidGameIdException;
import model.exceptions.InvalidUsernameException;
import model.player.Player;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.utility.LobbyInfo;
import model.utility.MapInfo;
import model.utility.MapInfoView;
import model.weapon.Effect;
import model.weapon.Weapon;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ServerController extends UnicastRemoteObject implements RemoteController {
    private final transient NetworkManager networkManager;
    private static final String ERROR_STRING = "ERROR: ";
    private static final String TURN_STATE_PATH = "state_commands.txt";

    private final transient TurnStateController turnStateController;
    private transient List<Boolean> loadingGames = new ArrayList<>();
    private transient Map<Integer, List<TurnState>> loadedStates = new HashMap<>();

    /***
     * Constructor of the class Server Controller. It receive a parameter that indicate if the game has to be load from file.
     * If not it initialize all as new, otherwise it load the previous network manger and disconnect all the tokens of all the game.
     * Doing so every player that was previously in the game will be reconnected as the log again and the game will be "re started" from
     * the point it was only when every previous player is reconnected
     * @param load boolean indicating if the game has to be load
     * @throws RemoteException
     */
    public ServerController(boolean load) throws RemoteException {
        super();
        turnStateController=new TurnStateController(TURN_STATE_PATH);
        networkManager = NetworkManager.get(load);
        if(load) {
            List<String> tokenToDisconnect = new ArrayList<>(networkManager.viewListeners.keySet());
            for(String s : tokenToDisconnect) {
                networkManager.disconnectWhileLoading(s);
            }
            int i = 0;
            for(GameManager g : networkManager.getGameManagers()) {
                networkManager.stopPlayerTimer(i);
                loadingGames.add(true);
                List<TurnState> states = new ArrayList<>();
                for(Player p : g.getPlayerOrderTurn()) {
                    states.add(p.getTurnState());
                }
                loadedStates.put(i, new ArrayList<>(states));
                states.clear();
                i++;
            }
        }
        else {
            networkManager.setTurnStateController(turnStateController);
        }
    }

    /***
     * Invoke the method create player from network manager and retrieve information about the game sending info to every other player
     * joined to that game
     * @param token the token of the player requesting the creation
     * @param gameId the game to join
     * @param username the username requested
     * @param color the color requested
     * @throws RemoteException
     */
    @Override
    public synchronized void createPlayer(String token, int gameId, String username, PlayerColor color) throws RemoteException{
        try {
            Player player = networkManager.createPlayer(token, gameId, username, color);
            int actualGame = player.getGameID();
            networkManager.checkStartingGame(actualGame);
            GameManager g = networkManager.getGameManagers().get(actualGame);
            if(g.isStarted()) {
                for(Player p : g.getPlayerOrderTurn()){
                    p.getViewPlayer().onPrintHelp(g,p);
                }
            }
            MapInfo map = networkManager.mapChosenById.get(player.getGameID());
            KillShotTrack killShotTrack = networkManager.getGameManagers().get(player.getGameID()).getKillShotTrack();
            networkManager.getTokenProxy(token).onMapInfo(MapInfo.createMapView(map), killShotTrack);
        } catch (InvalidGameIdException | InvalidUsernameException | InvalidColorException e) {
            networkManager.getTokenProxy(token).onText(ERROR_STRING + e.getMessage());
        }
    }

    /***
     * Method to quit a player. It sends messages to every other player joined to that game to notify them of the quit
     * or to quit the game beacuse of lack of players
     * @param token the player to quit
     */
    @Override
    public synchronized void quit(String token) {
        networkManager.stopSynTimer(token);
        networkManager.stopAckTimer(token);

        String name = networkManager.getPlayer(token) != null ? networkManager.getPlayer(token).getPlayerID() : "";
        ViewProxy view = networkManager.getTokenProxy(token);
        int game = networkManager.getPlayer(token).getGameID();

        //check if the game has to be stopped
        if(game != -1) {
            GameManager gameManager = networkManager.getGameManagers().get(game);
            MapInfo mapToSend = networkManager.mapChosenById.get(game);
            boolean allQuit = gameManager.getRemainingPlayers() <= mapToSend.getMinNumberOfPlayer();
            List<ViewProxy> views = networkManager.viewListenersById.get(game);
            if (allQuit) {
                gameManager.endOfGame();
                //networkManager.endGame(game, name);
                System.out.println("The game " + game + " has not enough players. Quit.");
                networkManager.stopPlayerTimer(game);
            } else {
                for (ViewProxy v : views) {
                    v.onQuit(false, name, true, true);
                    v.onMapInfo(MapInfo.createMapView(mapToSend), networkManager.getGameManagers().get(game).getKillShotTrack());
                }
            }
        }
        networkManager.playerQuit(token);
        view.onQuit(true, name, false, true);
    }

    /***
     * Method to reconnect a user that was previously disconnected cause of inactivity. It restores the player in the right data structures of NetworkManager
     * @param token the token of the player to wake
     */
    @Override
    public synchronized void wake(String token) {
        int game = networkManager.disconnectedTokens.get(token) != null ? networkManager.disconnectedTokens.get(token) : -1;
        if(game != -1 && networkManager.getGameManagers().get(game).isStarted() && !networkManager.getGameManagers().get(game).isGameEnd()) {
            networkManager.reconnectUserCauseInactive(token);
            updateContext(networkManager.getTokenProxy(token), token);
            for (ViewProxy v : networkManager.viewListenersById.get(game)) {
                v.onText(networkManager.getPlayer(token).getPlayerID() + " has been reconnected");
            }
        }
        else {
            ViewProxy view = networkManager.disconnectedProxyListener.get(token);
            if(view == null) {
                System.out.println("Error");
                return;
            }
            view.onQuit(true, "You", false, true);
            networkManager.stopSynTimer(token);
            networkManager.stopAckTimer(token);
        }
    }

    /***
     * Create a new game with the given map and the given end mode
     * @param token the player who request the creation of the game
     * @param map the index of the map requested
     * @param endMode the end mode requested
     */
    @Override
    public synchronized void createGame(String token, int map, String endMode) {
        int validGame = networkManager.createGame(map, endMode);
        networkManager.getTokenProxy(token).onValidGame(validGame);
    }

    /**
     * Method to send to a client the list of all possible games to join
     * @param token the player that has done the request
     */
    @Override
    public synchronized void giveGamesStatus(String token) {
        List<MapInfo> results = networkManager.giveGamesStatus();
        List<MapInfoView> ret=new ArrayList<>();
        for(MapInfo mapInfo:results)
            ret.add(MapInfo.createMapView(mapInfo));
        boolean valid = false;
        if(!results.isEmpty()) {
            valid = true;
        }
        int numberOfMap = networkManager.getMaps().size();
        networkManager.getTokenProxy(token).onValidJoin(valid, ret, numberOfMap);
    }

    /**
     * Method to send to a client the list of all possble maps and related information
     * @param token the player that has done the request
     */
    @Override
    public synchronized void getLobbiesStatus(String token) {
        List<LobbyInfo> lobbyInfo = new ArrayList<>();
        int size = networkManager.getMaps().size();
        for(MapInfo map : networkManager.getMaps()) {
            lobbyInfo.add(new LobbyInfo(MapInfo.createMapView(map), map.getAllowedPlayerColors(), size, map.getAllowedEndModes()));
        }
        networkManager.getTokenProxy(token).onLobbyStatus(lobbyInfo);
    }

    /**
     * Method to send to a client the requested map for a join. If that map not exists or the game is already started or ended
     * player is not allowed to join that game
     * @param token the player that has done the request
     * @param gameId the game id to join
     * @param map the map requested
     */
    @Override
    public synchronized void getMapInfo(String token, int gameId, int map) {
        MapInfo mapInfo;
        if(gameId == -1) {
            mapInfo = networkManager.getMaps().get(map - 1);
        } else {
            if(gameId < networkManager.getGameManagers().size() &&
                    !networkManager.getGameManagers().get(gameId).isGameEnd()) {
                mapInfo = networkManager.mapChosenById.get(gameId);
            }
            else {
                mapInfo = new MapInfo(-1, -1, "", -1, -1, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), -1, -1, new ArrayList<>());
            }
        }
        if(gameId>=0 &&gameId<networkManager.getGameManagers().size()) {
            networkManager.getTokenProxy(token).onMapInfo(MapInfo.createMapView(mapInfo), networkManager.getGameManagers().get(gameId).getKillShotTrack());
            if(!networkManager.getGameManagers().get(gameId).isGameEnd()) {
                networkManager.getTokenProxy(token).onValidGame(gameId);
            }
        }
        else {
            networkManager.getTokenProxy(token).onMapInfo(MapInfo.createMapView(mapInfo), null);
        }
    }

    /***
     * Create the token for a client connected with socket
     * @param clientHandler the view to which crete the token
     */
    public synchronized void createToken(ClientHandler clientHandler) {
        String token = networkManager.generateToken();
        networkManager.addTokenSocket(token,clientHandler);
        ViewProxy v = networkManager.getTokenProxy(token);
        v.onToken(token);
        networkManager.getAckMap().put(token,false);
        networkManager.startSynTimer(token);
        networkManager.startAckTimer(token);
    }

    /***
     * Create the token for a client connected with rmi
     * @param viewListener the view to which crete the token
     */
    @Override
    public synchronized void generateToken(ViewListener viewListener) {
        String token = networkManager.generateToken();
        networkManager.addTokenView(token, viewListener);
        networkManager.getTokenProxy(token).onToken(token);
        networkManager.getAckMap().put(token,false);
        networkManager.startSynTimer(token);
        networkManager.startAckTimer(token);
    }

    /***
     * Check if a given token corresponds to a disconnected player. If so reconnect that user to that game sending him all updated information,
     * otherwise give negative answer
     * @param viewListener the view to which respond
     * @param alreadyExistingToken the token to check
     */
    @Override
    public synchronized void getAlreadyLoggedUser(ViewListener viewListener, String alreadyExistingToken) {
        boolean exist = networkManager.getAlreadyLoggedUser(alreadyExistingToken);
        boolean anotherActive = networkManager.isActiveToken(alreadyExistingToken);
        if(exist) {
            int game = networkManager.getPlayerGame(alreadyExistingToken).getGameId();
            if(! networkManager.getGameManagers().get(game).isGameEnd()) {

                networkManager.reconnectUserCauseConnection(alreadyExistingToken, viewListener);
                ViewProxy existingProxy = networkManager.getTokenProxy(alreadyExistingToken);
                existingProxy.onAlreadLoggedUser(alreadyExistingToken, true, anotherActive);
                //update the client context
                updateContext(existingProxy, alreadyExistingToken);
                for (ViewProxy v : networkManager.viewListenersById.get(game)) {
                    v.onText(networkManager.getPlayer(alreadyExistingToken).getPlayerID() + " has been reconnected");
                }
                checkLoadingPhase(game);
            }
            else {
                new ViewProxy(viewListener, alreadyExistingToken,turnStateController).onAlreadLoggedUser(alreadyExistingToken, false, anotherActive);
            }
        }
        else {
            new ViewProxy(viewListener, alreadyExistingToken,turnStateController).onAlreadLoggedUser(alreadyExistingToken, false, anotherActive);
        }
    }

    /***
     * Method to verify if a given game is waiting for other players to join before to be re started. (Persistency)
     * @param game the game to check
     */
    private void checkLoadingPhase(int game) {
        if(game >= loadingGames.size()) {
            return;
        }

        if(loadingGames.get(game)) {
            GameManager g = networkManager.getGameManagers().get(game);
            if(g.getRemainingPlayers() == g.getPlayerOrderTurn().size()) {
                int i = 0;
                for(Player p : g.getPlayerOrderTurn()) {
                    p.setTurnState(loadedStates.get(game).get(i));
                    i++;
                }
                g.changeTurn();
                loadingGames.set(game, false);
            }
        }
    }

    /***
     * Given a client it updates all the client context data sending him the updated information
     * @param view the view to update
     * @param token the token of the playe to update
     */
    private synchronized void updateContext(ViewProxy view, String token) {
        GameManager gameManager = networkManager.getPlayerGame(token);
        Player player = networkManager.getPlayer(token);
        List<Player> enemies = new ArrayList<>(gameManager.getPlayerOrderTurn());
        enemies.remove(player);
        updateEnemies(player, view, enemies);
        view.onGameStarted(gameManager.isStarted());
        view.onToken(token);
        if(gameManager.isStarted()) {
            view.onActiveTurn(player.getToken().equals(gameManager.getCurrentPlayerTurn().getToken()));
        } else {
            view.onActiveTurn(false);
        }
        view.onValidGame(player.getGameID());
        view.onValidJoin(true, Collections.singletonList(MapInfo.createMapView(networkManager.mapChosenById.get(player.getGameID()))), networkManager.getMaps().size());
        view.onPrintHelp(gameManager, player);
        view.onMapInfo(MapInfo.createMapView(networkManager.mapChosenById.get(player.getGameID())),gameManager.getKillShotTrack());
        PlayerView playerView = player.createPlayerView();
        playerView.setTileView(GameManager.createTileView(player.getCurrentTile()));
        view.onNewPlayer(playerView);
        view.onUpdateWeaponsPowPoints(player.getWeapons(), player.getPowerUps(), player.getScore());

        List<Player> allPlayers = new ArrayList<>(gameManager.getPlayerOrderTurn());
        List<PlayerView> allPlayersView = new ArrayList<>();
        for(Player p : allPlayers) {
            PlayerView toCreate = p.createPlayerView();
            playerView.setTileView(GameManager.createTileView(p.getCurrentTile()));
            allPlayersView.add(toCreate);
        }
        if(player.getPlayerState() == PlayerState.FRENZY_AFTER || player.getPlayerState() == PlayerState.FRENZY_BEFORE) {
            view.onFinalFrenzyStart(allPlayersView);
        }
    }

    /**
     * Method called when the user select a tile for an action. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param tile the selected tile
     */
    @Override
    public synchronized void chooseTile(String token, TileView tile) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);

        try {
            if (turnStateController.isAValidCommand(gameManager, "tile", player))
                turnStateController.handleChooseTile(tile, player, gameManager);
            if(!gameManager.isGameEnd()) {
                networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
            }
        }
        catch(GameException e){
            networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select a target for an action. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param playerView the selected target
     */
    @Override
    public synchronized void chooseTarget(String token, PlayerView playerView) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        try {
            if (turnStateController.isAValidCommand(gameManager, "target", player)){
                turnStateController.handleChooseTarget(playerView , player, gameManager);
                if(!gameManager.isGameEnd()) {
                    networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
                }
            }
        } catch(GameException e){
            networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select a direction for an action. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param direction the selected direction
     */
    @Override
    public synchronized void chooseDirection(String token, Direction direction) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        try {
            if (turnStateController.isAValidCommand(gameManager, "direction", player))
                turnStateController.handleDirection(gameManager, player, direction);
            if(!gameManager.isGameEnd()) {
                networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
            }
        } catch(GameException e){
            networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select a room for an action. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param roomColor the selected room
     */
    @Override
    public synchronized void chooseRoom(String token, RoomColor roomColor) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        try {
            if (turnStateController.isAValidCommand(gameManager, "room", player))
                turnStateController.handleRoom(gameManager, player, roomColor);
            if(!gameManager.isGameEnd()) {
                networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
            }
        } catch (GameException e){
            networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select an ammo for an action. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param ammoColor the selected ammo
     */
    @Override
    public synchronized void chooseAmmo(String token, AmmoColor ammoColor) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        try{
            if (turnStateController.isAValidCommand(gameManager,"ammo",player)) {
                turnStateController.handleAmmo(gameManager, player, ammoColor);
            }
            if(!gameManager.isGameEnd()) {
                networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
            }
        }
        catch(GameException e){
        networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select a weapon for an action. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param weapon the selected weapon
     */
    @Override
    public synchronized void chooseWeapon(String token, Weapon weapon) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        try {
            if (turnStateController.isAValidCommand(gameManager, "weapon", player))
                turnStateController.handleWeapon(gameManager, player, weapon);
            if(!gameManager.isGameEnd()) {
                networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
            }
        } catch (GameException e){
            networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select an effect for an action. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param effect the selected effect
     */
    @Override
    public synchronized void chooseEffect(String token, Effect effect) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        try {
            if (turnStateController.isAValidCommand(gameManager, "effect", player))
                turnStateController.handleEffect(gameManager, player, effect);
            if(!gameManager.isGameEnd()) {
                networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
            }
        } catch(GameException e){
            networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select a powerup for an action. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param powerUp the selected powerup
     */
    @Override
    public synchronized void choosePowerup(String token, PowerUp powerUp) {
        Player player=networkManager.getPlayer(token);
        GameManager gameManager=networkManager.getPlayerGame(token);
        try {
            if (turnStateController.isAValidCommand(gameManager, "powerup", player)) {
                turnStateController.handlePowerUp(powerUp, player, gameManager);
                if(!gameManager.isGameEnd()) {
                    networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
                }
            }
        }
        catch(GameException | NullPointerException e){
            networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select an action between use poweru, run, grab, shoot. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param typeOfAction the selected action
     */
    @Override
    public synchronized void chooseAction(String token,TypeOfAction typeOfAction) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        try {
            if (turnStateController.isAValidCommand(gameManager, "action", player))
                turnStateController.handleActions(gameManager, typeOfAction, player);
            if(!gameManager.isGameEnd()) {
                networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
            }
        } catch(GameException e){
             networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select nothing command. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     */
    @Override
    public synchronized void chooseNothing(String token) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        try {
            if (turnStateController.isAValidCommand(gameManager, "nothing", player))
                turnStateController.handleNothing(gameManager, player);
            if(!gameManager.isGameEnd()) {
                networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
            }
        } catch(GameException e){
            networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /**
     * Method called when the user select a type of effect for an action. The playing timer is restarted. The action is validated by turnstate controller
     * @param token the token that has made the action
     * @param typeOfEffect the selected type of effect
     */
    @Override
    public synchronized void chooseTypeOfEffect(String token,TypeOfEffect typeOfEffect) {
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        try{
        if (turnStateController.isAValidCommand(gameManager,"type",player))
            turnStateController.handleTypeOfEffect(gameManager,player,typeOfEffect);
            if(!gameManager.isGameEnd()) {
                networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
            }
        }
        catch(GameException e){
            networkManager.getTokenProxy(token).onText(e.toString());
        }
        restartTimer(player);
    }

    /***
     * Method to get a token given a view
     * @param clientHandler the view to which the token has to be known
     * @return the token
     */
    synchronized String getTokenFromHandler(ClientHandler clientHandler) {
        for(String token : networkManager.viewListeners.keySet()) {
            if(networkManager.viewListeners.get(token).equals(clientHandler)) {
                return token;
            }
        }
        return "error";
    }

    synchronized void disconnectToken(String token) {
        networkManager.disconnectToken(token);
        //wake(token);
    }

    private synchronized void restartTimer(Player player) {
        int game = player.getGameID();
        if(networkManager.getGameManagers().get(game).isGameEnd()) {
            return;
        }
        if(networkManager.isCurrentPlayerForGame(game, player.getToken())) {
            networkManager.stopPlayerTimer(game);
            networkManager.restartPlayerTimer(game);
        }
    }

    @Override
    public synchronized void sendAck(String token) {
        networkManager.setAck(token,true);
    }

    @Override
    public void requestPossibleCommands(String token){
        GameManager gameManager=networkManager.getPlayerGame(token);
        Player player=networkManager.getPlayer(token);
        if(!gameManager.isGameEnd()) {
            networkManager.getTokenProxy(token).onPrintHelp(gameManager, player);
        }
    }

    private void updateEnemies(Player reconnectedPlayer, ViewProxy reconnectedViewProxy, List<Player> enemies) {
        for(Player p : enemies) {
            PlayerView pV = networkManager.createPlayerView(p);
            PlayerView curr = networkManager.createPlayerView(reconnectedPlayer);
            pV.setTileView(GameManager.createTileView(p.getCurrentTile()));
            curr.setTileView(GameManager.createTileView(reconnectedPlayer.getCurrentTile()));

            reconnectedViewProxy.onUpdateEnemyPlayer(pV);
            ViewProxy enemyViewProxy = networkManager.viewsProxy.get(p.getToken());
            if(enemyViewProxy != null) {
                enemyViewProxy.onUpdateEnemyPlayer(curr);
            }
        }
    }
}
