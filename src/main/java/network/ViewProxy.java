package network;

import controller.TurnStateController;
import model.board.AmmoTile;
import model.board.GameManager;
import model.board.KillShotTrack;
import model.board.TileView;
import model.enums.*;
import model.player.Player;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.utility.LobbyInfo;
import model.utility.MapInfoView;
import model.weapon.Effect;
import model.weapon.Weapon;

import java.io.Serializable;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class ViewProxy implements Serializable {
    private final NetworkManager networkManager;
    private final ViewListener view;
    private final String token;
    private final TurnStateController turnStateController;

    public ViewProxy(ViewListener view, String token,TurnStateController turnStateController) {
        this.networkManager = NetworkManager.get(false);
        this.view = view;
        this.token = token;
        this.turnStateController=turnStateController;
    }

    public String getToken() {
        return token;
    }

    void onNewPlayer(PlayerView player) {
        try {
            view.onNewPlayer(player);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onUpdateCurrentPlayer(PlayerView player, boolean toDisconnect) {
        try {
            view.onUpdateCurrentPlayer(player);
        } catch (SocketException | RemoteException r) {
            if(toDisconnect) {
                networkManager.disconnectToken(token);
            }
        }
    }

    void onMoreText(List<String> sendString) {
        try {
            view.onMoreText(sendString);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    void onGameStarted(boolean gameStarted) {
        try {
            view.onGameStarted(gameStarted);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }


    void onMapInfo(MapInfoView mapInfo, KillShotTrack killShotTrack) {
        try {
            view.onMapInfo(mapInfo,killShotTrack);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
        catch(ClassCastException c){

        }
    }


    void onActiveTurn(boolean activeTurn) {
        try {
            view.onActiveTurn(activeTurn);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }


    public void onSpawn(PlayerView player, TileView tile) {
        try {
            view.onSpawn(player, tile);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onMarks(PlayerView player, PlayerView marker, int marks, int oldMarks) {
        try {
            view.onMarks(player, marker, marks, oldMarks);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onDamage(PlayerView player, PlayerView target, int damage, int marksDown) {
        try {
            view.onDamage(target,player, damage, marksDown);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onWeaponGrab(PlayerView player, Weapon newWeapon,Weapon removedWeapon) {
        try {
            view.onWeaponGrab(player, newWeapon,removedWeapon);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onPowerUpDrawnByEnemy(PlayerView player) {
        try {
            view.onPowerUpDrawnByEnemy(player);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onPowerUpDrawn(PowerUp powerUp) {
        try {
            view.onPowerUpDrawn(powerUp);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onPowerUpDiscard(PlayerView player, PowerUp powerUp) {
        try {
            view.onPowerUpDiscard(player, powerUp);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onReloadWeapon(PlayerView player, Weapon weapon) {
        try {
            view.onReloadWeapon(player, weapon);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onMovement(PlayerView player, TileView tile) {
        try {
            view.onMovement(player, tile);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onAmmoGrab(PlayerView player, AmmoTile ammoTile) {
        try {
            view.onAmmoGrab(player, ammoTile);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onChangeTurn(PlayerView endPlayer, PlayerView newPlayer,List<TileView> tiles) {
        try {
            view.onChangeTurn(endPlayer, newPlayer,tiles);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onPoints(Map<PlayerView, Integer> points,boolean doubleKill,PlayerView scoredOn) {
        try {
            view.onPoints(points,doubleKill,scoredOn);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onKillUpdate(PlayerView killer,boolean overKill,PlayerView killed){
        try {
            view.onKillUpdate(killer,overKill,killed);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onPowerUps(List<PowerUp> selectablePowerUps) {
        try {
            view.onPowerUps(selectablePowerUps);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onAmmos(List<AmmoColor> selectableAmmo) {
        try {
            view.onAmmos(selectableAmmo);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onWeapons(List<Weapon> selectableWeapons) {
        try {
            view.onWeapons(selectableWeapons);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onTiles(List<TileView> selectableTiles) {
        try {
            view.onTiles(selectableTiles);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onActions(List<TypeOfAction> ret) {
        try {
            view.onActions(ret);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    void onUpdateEnemyPlayer(PlayerView p) {
        try {
            view.onUpdateEnemyPlayer(p);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onPrintHelp(GameManager gameManager, Player player) {
        try {
            if(turnStateController!=null)
                view.onPrintHelp(turnStateController.getValidCommands(gameManager,player));
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    void onQuit(boolean close, String name, boolean toDisconnect, boolean causeOfDisconnection) {
        try {
            view.onQuit(close, name, causeOfDisconnection);
        } catch (SocketException | RemoteException r) {
            if(toDisconnect) {
                networkManager.disconnectToken(token);
            }
        }
    }

    void onValidGame(int validGame) {
        try {
            view.onValidGame(validGame);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    void onValidJoin(boolean valid, List<MapInfoView> results, int numberOfMap) {
        try {
            view.onValidJoin(valid, results, numberOfMap);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onText(String s) {
        try {
            view.onText(s);
        }  catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    void onLobbyStatus(List<LobbyInfo> lobbyInfo) {
        try {
            view.onLobbyStatus(lobbyInfo);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    void onToken(String token) {
        try {
            view.onToken(token);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    void onAlreadLoggedUser(String alreadyExistingToken, boolean exist, boolean anotherActive) {
        try {
            view.onAlreadLoggedUser(alreadyExistingToken, exist, anotherActive);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }


    public void onTargets(List<PlayerView> playersToPlayerViews) {
        try {
            view.onTargets(playersToPlayerViews);
        } catch (SocketException | RemoteException r) {

            networkManager.disconnectToken(token);
        }
    }

    public void onRooms(List<RoomColor> targettableRoomColors) {
        try {
            view.onRooms(targettableRoomColors);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onDirections(List<Direction> targettableDirections) {
        try {
            view.onDirections(targettableDirections);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onEffects(List<Effect> selectableOptionalEffects) {
        try {
            view.onEffects(selectableOptionalEffects);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onTypeEffects(List<TypeOfEffect> typeOfEffects) {
        try {
            view.onTypeEffects(typeOfEffects);
        } catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void requestPossibleCommands() {
        try {
            view.requestPossibleCommands();
        }
        catch (SocketException | RemoteException r) {
            networkManager.disconnectToken(token);
        }
    }

    public void onEndGame(List<PlayerView> playerViews,List<Integer> points){
        try{
            view.onEndGame(playerViews,points);
        }
        catch (SocketException|RemoteException r){
            networkManager.disconnectToken(token);
        }
    }

    public void onWeaponUsed(PlayerView playerView,Weapon weapon){
        try{
            view.onWeaponUsed(playerView,weapon);
        }
        catch(RemoteException|SocketException e){
            networkManager.disconnectToken(token);
        }
    }

    public void onPowerUpUsed(PlayerView playerView,PowerUp powerUp){
        try{
            view.onPowerUpUsed(playerView,powerUp);
        }
        catch(RemoteException|SocketException e){
            networkManager.disconnectToken(token);
        }
    }

    public void onFinalFrenzyStart(List<PlayerView> playerViews) {
        try{
            view.onFinalFrenzyStart(playerViews);
        }
        catch (SocketException|RemoteException r){
            networkManager.disconnectToken(token);
        }
    }

    void onUpdateWeaponsPowPoints(List<Weapon> weapons, List<PowerUp> powerUps, int score) {
        try{
            view.onUpdateWeaponsPowPoints(weapons, powerUps, score);
        }
        catch (SocketException|RemoteException r){
            networkManager.disconnectToken(token);
        }
    }

    void onSyn() {
        try{
            view.onSyn();
        }
        catch (SocketException|RemoteException r){
            System.out.println("Error in network syn");
        }
    }

    void onInactivity(boolean inactive) {
        try{
            view.onInactivity(inactive);
        }
        catch (SocketException|RemoteException r){
            System.out.println("Error in network ack");
        }
    }
}
