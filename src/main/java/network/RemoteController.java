package network;


import model.board.TileView;
import model.enums.*;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.weapon.Effect;
import model.weapon.Weapon;

import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteController extends Remote {

    void chooseTypeOfEffect(String token, TypeOfEffect typeOfEffect) throws RemoteException, SocketException;

    void chooseNothing(String token) throws RemoteException, SocketException;

    void chooseTile(String token, TileView tile) throws RemoteException, SocketException;

    void chooseTarget(String token, PlayerView player)throws RemoteException, SocketException;

    void chooseDirection(String token, Direction direction)throws RemoteException, SocketException;

    void chooseRoom(String token, RoomColor roomColor)throws RemoteException, SocketException;

    void chooseAmmo(String token,AmmoColor ammoColor)throws RemoteException, SocketException;

    void chooseWeapon(String token,Weapon weapon)throws RemoteException, SocketException;

    void createPlayer(String token, int gameId, String username, PlayerColor color) throws RemoteException, SocketException;

    void chooseEffect(String token, Effect effect) throws RemoteException, SocketException;

    void choosePowerup(String token, PowerUp powerUp) throws RemoteException, SocketException;

    void generateToken(ViewListener viewListener) throws RemoteException, SocketException;

    void chooseAction(String token, TypeOfAction typeOfAction) throws  RemoteException, SocketException;

    void createGame(String token, int map, String endMode) throws RemoteException, SocketException;

    void giveGamesStatus(String token) throws RemoteException, SocketException;

    void getLobbiesStatus(String token) throws RemoteException, SocketException;

    void getMapInfo(String token, int gameId, int map) throws RemoteException, SocketException;

    void getAlreadyLoggedUser(ViewListener viewListener, String alreadyExistingToken) throws RemoteException, SocketException;

    void quit(String token) throws RemoteException, SocketException;

    void wake(String token) throws RemoteException, SocketException;

    void requestPossibleCommands(String token) throws RemoteException,SocketException;

    void sendAck(String token) throws  RemoteException,SocketException;
}
