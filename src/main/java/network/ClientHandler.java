package network;

import network.notify.*;
import network.requests.Request;
import network.response.*;
import model.board.AmmoTile;
import model.board.KillShotTrack;
import model.board.TileView;
import model.enums.*;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.utility.LobbyInfo;
import model.utility.MapInfoView;
import model.weapon.Effect;
import model.weapon.Weapon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * This is the Visitor (Visitor Pattern) used to decode the messages received by a client (only for Socket purpose)
 */
public class ClientHandler implements ViewListener, Runnable{
    private transient Socket socket;
    private transient final ObjectInputStream ois;
    private transient final ObjectOutputStream oos;
    private transient boolean stop;

    private transient final ServerController serverController;

    public ClientHandler(Socket s, ServerController serverController) throws IOException {
        this.socket = s;
        this.oos = new ObjectOutputStream(s.getOutputStream());
        this.ois = new ObjectInputStream(s.getInputStream());

        this.serverController = serverController;
    }

    private synchronized void respond(Response response) {
        try {
            oos.writeObject(response);
            oos.reset();
        }
        catch (IOException s) {
            if(!stop) {
                close();
            }
        }
    }

    public void close() {
        stop = true;
        if(ois != null) {
            try {
                ois.close();
            } catch (IOException e) {
                System.out.println("Input stream closed");
            }
        }
        if(oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                System.out.println("Output stream closed");
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Socket closed");
        }
    }

    /**
     * thread that stays constantly active (unless it's closed for disconnection or at the end of a game)
     * listening for message from the Client
     */
    @Override
    public void run() {
        do {
            try {
                Request request = (Request) ois.readObject();
                request.handleRequest(this,serverController);
            }
            catch (ClassNotFoundException | IOException s) {
                String token = serverController.getTokenFromHandler(this);
                serverController.disconnectToken(token);
                close();
            }
        } while (! stop);
    }


    @Override
    public void onText(String textResponse) {
        respond(new TextResponse(textResponse));
    }


    @Override
    public void onToken(String token) {
        respond(new TokenGeneratedResponse(token));
    }

    @Override
    public void onAlreadLoggedUser(String alreadyExistingToken, boolean exist, boolean anotherActive)  {
        respond(new AlreadyExistingTokenResponse(alreadyExistingToken, exist, anotherActive));
    }

    @Override
    public void onMoreText(List<String> content)  {
        respond(new MultiTextResponse(content));
    }

    @Override
    public void onNewPlayer(PlayerView player)  {
        respond(new PlayerCreatedResponse(player));
    }

    @Override
    public void onUpdateCurrentPlayer(PlayerView player)  {
        respond(new UpdateCurrentPlayerResponse(player));
    }

    @Override
    public void onUpdateEnemyPlayer(PlayerView enemyPlayer)  {
        respond(new UpdateEnemyPlayerResponse(enemyPlayer));
    }

    @Override
    public void onGameStarted(boolean started) {
        respond(new GameStartedResponse(started));
    }

    @Override
    public void onActiveTurn(boolean active) {
        respond(new ActiveTurnResponse(active));
    }

    @Override
    public void onValidGame(int validGame)  {
        respond(new ValidGameResponse(validGame));
    }

    @Override
    public void onValidJoin(boolean validJoin, List<MapInfoView> toPrint, int mapSize){
        respond(new GamesStatusResponse(validJoin, toPrint, mapSize));
    }

    @Override
    public void onPoints(Map<PlayerView, Integer> points,boolean doubleKill,PlayerView scoredOn){
        respond(new NotifyOnPoints(points,doubleKill,scoredOn));
    }

    @Override
    public void onMovement(PlayerView player, TileView tile) {
        respond(new NotifyOnMovement(player,tile));
    }

    @Override
    public void onWeaponGrab(PlayerView player, Weapon weaponGrabbed, Weapon weaponDropped) {
        respond(new NotifyOnWeaponGrab(weaponGrabbed,player,weaponDropped));
    }

    @Override
    public void onAmmoGrab(PlayerView player, AmmoTile ammoTile) {
        respond(new NotifyOnAmmoTileGrab(player,ammoTile));
    }

    @Override
    public void onSpawn(PlayerView spawned, TileView tile) {
        respond(new NotifyOnSpawn(spawned,tile));
    }

    @Override
    public void onChangeTurn(PlayerView endOfTurn,PlayerView newTurn,List<TileView> tiles)  {
        respond(new NotifyOnEndTurn(endOfTurn,newTurn,tiles));
    }

    @Override
    public void onTargets(List<PlayerView> players) {
        respond(new ChooseTargetResponse(players));
    }

    @Override
    public void onRooms(List<RoomColor> roomColors)  {
        respond(new ChooseRoomResponse(roomColors));
    }

    @Override
    public void onTiles(List<TileView> tiles) {
        respond(new ChooseTileResponse(tiles));
    }

    @Override
    public void onDirections(List<Direction> directions)  {
        respond(new ChooseDirectionResponse(directions));
    }

    @Override
    public void onPowerUps(List<PowerUp> powerUps) {
        respond(new NotifyOnPowerUp(powerUps));
    }

    @Override
    public void onWeapons(List<Weapon> weapons)  {
        respond(new ChooseWeaponResponse(weapons));
    }

    @Override
    public void onAmmos(List<AmmoColor> ammoColors) {
        respond(new ChooseAmmoResponse(ammoColors));
    }

    @Override
    public void onEffects(List<Effect> effects) {
        respond(new ChooseEffectResponse(effects));

    }

    @Override
    public void onTypeEffects(List<TypeOfEffect> typeOfEffects)  {
        respond(new ChooseTypeOfEffectResponse(typeOfEffects));
    }

    @Override
    public void onPowerUpDrawnByEnemy(PlayerView player) {
        respond(new NotifyOnPowerUpDrawByEnemy(player));
    }

    @Override
    public void onPowerUpDrawn(PowerUp powerUp)  {
        respond(new NotifyOnPowerUpDrawn(powerUp));
    }

    @Override
    public void onPowerUpDiscard(PlayerView player, PowerUp powerUp){
        respond(new NotifyOnPowerUpDiscard(powerUp,player));
    }

    @Override
    public void onReloadWeapon(PlayerView player, Weapon weapon){
        respond(new NotifyOnWeaponReload(weapon,player));
    }

    @Override
    public void onPrintHelp(List<String> printHelp)  {
        respond(new PrintHelpResponse(printHelp));
    }


    @Override
    public void onDamage(PlayerView targetPlayer,PlayerView shooter,int newDmg, int marksDown)  {
        respond(new NotifyOnDamage(newDmg,marksDown,shooter,targetPlayer));
    }

    @Override
    public void onLobbyStatus(List<LobbyInfo> lobbyInfo)  {
        respond(new LobbyStatusResponse(lobbyInfo));
    }

    @Override
    public void onMapInfo(MapInfoView mapInfo, KillShotTrack killShotTrack)  {
        respond(new MapInfoResponse(mapInfo,killShotTrack));
    }

    @Override
    public void onMarks(PlayerView marked, PlayerView marker, int newMarks, int oldMarks)  {
        respond(new NotifyOnMarks(oldMarks,newMarks,marker,marked));
    }

    @Override
    public void onStatus(PlayerView player)  {
        //not used
    }

    @Override
    public void onOtherStatus(List<PlayerView> playerViews)  {
        //not used
    }

    @Override
    public void onTileInfo(MapInfoView map, int x, int y)  {
        //not used
    }
	    
    public void onActions(List<TypeOfAction> actionList)  {
        respond(new NotifyOnActions(actionList));
    }

    @Override
    public void onQuit(boolean close, String name, boolean causeOfDisconnection)  {
        respond(new QuitResponse(close, name, causeOfDisconnection));
    }

    @Override
    public void onEndGame(List<PlayerView> endScore,List<Integer> points){
        respond(new NotifyOnEndGame(endScore,points));
    }

    @Override
    public void requestPossibleCommands() {
            respond(new SetNewPossibileCommandsResponse());
    }

    @Override
    public void onPrintCommands(List<String> possibleCommands) throws RemoteException, SocketException {
    }

    @Override
    public void onWeaponUsed(PlayerView playerView, Weapon weapon) throws RemoteException, SocketException {
        respond(new NotifyOnWeaponUsed(playerView,weapon));
    }

    @Override
    public void onPowerUpUsed(PlayerView playerView, PowerUp powerUp) throws RemoteException, SocketException {
        respond(new NotifyOnPowerUpUsed(playerView,powerUp));
    }

    @Override
    public void onKillUpdate(PlayerView killer, boolean overKill, PlayerView killed) throws RemoteException, SocketException {
        respond(new NotifyOnKillUpdate(killer,overKill,killed));
    }

    @Override
    public void onUpdateWeaponsPowPoints(List<Weapon> weapons, List<PowerUp> powerUps, int score) throws RemoteException, SocketException {
        respond(new UpdateWeaponsPowPointsResponse(weapons, powerUps, score));
    }

    @Override
    public void onFinalFrenzyStart(List<PlayerView> playerViews) throws RemoteException, SocketException {
        respond(new NotifyOnFinalFrenzyStart(playerViews));
    }

    @Override
    public void onSyn() throws RemoteException, SocketException {
        respond(new SynPing());
    }

    @Override
    public void onInactivity(boolean inactive) throws RemoteException, SocketException {
        respond(new InactivityResponse(inactive));
    }

    @Override
    public void onLostConnection() throws RemoteException, SocketException {
        respond(new LostConnectionResponse());
    }
}
