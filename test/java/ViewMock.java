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
import network.ViewListener;

import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class ViewMock implements ViewListener {

    @Override
    public void onMoreText(List<String> content) throws RemoteException, SocketException {

    }

    @Override
    public void onText(String content) throws RemoteException, SocketException {

    }

    @Override
    public void onDamage(PlayerView shooter, PlayerView targetPlayer, int newDmg, int marksDown) throws RemoteException, SocketException {

    }

    @Override
    public void onMarks(PlayerView marked, PlayerView marker, int newMarks, int oldMarks) throws RemoteException, SocketException {

    }

    @Override
    public void onPoints(Map<PlayerView, Integer> points, boolean overKill, PlayerView scoredOn) throws RemoteException, SocketException {

    }

    @Override
    public void onKillUpdate(PlayerView killer, boolean overKill, PlayerView killed) throws RemoteException, SocketException {

    }

    @Override
    public void onMovement(PlayerView player, TileView tile) throws RemoteException, SocketException {

    }

    @Override
    public void onWeaponGrab(PlayerView player, Weapon weaponGrabbed,Weapon weaponDropped) throws RemoteException, SocketException {

    }

    @Override
    public void onAmmoGrab(PlayerView player, AmmoTile ammoTile) throws RemoteException, SocketException {

    }

    @Override
    public void onSpawn(PlayerView spawned, TileView tile) throws RemoteException, SocketException {

    }

    @Override
    public void onChangeTurn(PlayerView endOfTurn, PlayerView newTurn, List<TileView> tiles) throws RemoteException, SocketException {

    }

    @Override
    public void onTargets(List<PlayerView> players) throws RemoteException, SocketException {

    }

    @Override
    public void onRooms(List<RoomColor> roomColors) throws RemoteException, SocketException {

    }

    @Override
    public void onTiles(List<TileView> tiles) throws RemoteException, SocketException {

    }

    @Override
    public void onDirections(List<Direction> directions) throws RemoteException, SocketException {

    }

    @Override
    public void onPowerUps(List<PowerUp> powerUps) throws RemoteException, SocketException {

    }

    @Override
    public void onWeapons(List<Weapon> weapons) throws RemoteException, SocketException {

    }

    @Override
    public void onAmmos(List<AmmoColor> ammoColors) throws RemoteException, SocketException {

    }

    @Override
    public void onEffects(List<Effect> effects) throws RemoteException, SocketException {

    }

    @Override
    public void onTypeEffects(List<TypeOfEffect> typeOfEffects) throws RemoteException, SocketException {

    }

    @Override
    public void onToken(String token) throws RemoteException, SocketException {

    }

    @Override
    public void onNewPlayer(PlayerView player) throws RemoteException, SocketException {

    }

    @Override
    public void onUpdateCurrentPlayer(PlayerView player) throws RemoteException, SocketException {

    }

    @Override
    public void onUpdateEnemyPlayer(PlayerView enemyPlayer) throws RemoteException, SocketException {

    }

    @Override
    public void onGameStarted(boolean started) throws RemoteException, SocketException {

    }

    @Override
    public void onActiveTurn(boolean active) throws RemoteException, SocketException {

    }

    @Override
    public void onValidGame(int validGame) throws RemoteException, SocketException {

    }

    @Override
    public void onValidJoin(boolean validJoin, List<MapInfoView> toPrint, int mapSize) throws RemoteException, SocketException {

    }

    @Override
    public void onStatus(PlayerView player) throws RemoteException, SocketException {

    }

    @Override
    public void onPrintHelp(List<String> printHelp) throws RemoteException, SocketException {

    }

    @Override
    public void onPowerUpDiscard(PlayerView player, PowerUp p) throws RemoteException, SocketException {

    }

    @Override
    public void onPowerUpDrawnByEnemy(PlayerView player) throws RemoteException, SocketException {

    }

    @Override
    public void onPowerUpDrawn(PowerUp powerUp) throws RemoteException, SocketException {

    }

    @Override
    public void onReloadWeapon(PlayerView player, Weapon weapon) throws RemoteException, SocketException {

    }

    @Override
    public void onOtherStatus(List<PlayerView> playerViews) throws RemoteException, SocketException {

    }

    @Override
    public void onLobbyStatus(List<LobbyInfo> lobbyInfo) throws RemoteException, SocketException {

    }


    @Override
    public void onTileInfo(MapInfoView map, int x, int y) throws RemoteException, SocketException {

    }

    @Override
    public void onActions(List<TypeOfAction> ret) throws RemoteException, SocketException {

    }

    @Override
    public void onAlreadLoggedUser(String alreadyExistingToken, boolean exist, boolean anotherActive) throws RemoteException, SocketException {

    }

    @Override
    public void onQuit(boolean close, String name, boolean causeOfDisconnection) throws RemoteException, SocketException {

    }

    @Override
    public void requestPossibleCommands() throws RemoteException, SocketException {

    }

    @Override
    public void onEndGame(List<PlayerView> players, List<Integer> points) throws RemoteException, SocketException {

    }

    @Override
    public void onPrintCommands(List<String> possibleCommands) throws RemoteException, SocketException {

    }

    @Override
    public void onUpdateWeaponsPowPoints(List<Weapon> weapons, List<PowerUp> powerUps, int score) throws RemoteException, SocketException {

    }

    @Override
    public void onWeaponUsed(PlayerView playerView, Weapon weapon) throws RemoteException, SocketException {

    }

    @Override
    public void onPowerUpUsed(PlayerView playerView, PowerUp powerUp) throws RemoteException, SocketException {

    }

    @Override
    public void onMapInfo(MapInfoView mapInfo, KillShotTrack killShotTrack) throws RemoteException, SocketException {

    }

    @Override
    public void onFinalFrenzyStart(List<PlayerView> playerViews) throws RemoteException, SocketException {

    }

    @Override
    public void onSyn() throws RemoteException, SocketException {

    }

    @Override
    public void onInactivity(boolean inactive) throws RemoteException, SocketException {

    }

    @Override
    public void onLostConnection() throws RemoteException, SocketException {

    }
}
