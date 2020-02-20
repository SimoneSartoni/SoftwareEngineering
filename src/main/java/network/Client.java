package network;

import model.board.TileView;
import model.enums.*;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.weapon.Effect;
import model.weapon.Weapon;
import view.ViewClient;

import java.io.IOException;
import java.rmi.RemoteException;

public interface Client {
    void init() throws IOException;
    void close() throws IOException;
    void run() throws RemoteException;
    String getToken();
    void setToken(String token);
    void createPlayer(int gameId, String username, PlayerColor playerColor);
    void generateToken();
    void setReceivedResponse(boolean receivedResponse);
    void createGame(int map, String endMode);
    void giveGamesStatus();
    void requestPossibleCommands();
    void chooseTarget(PlayerView player);
    void chooseRoom(RoomColor roomColor);
    void chooseDirection(Direction direction);
    void chooseAmmo(AmmoColor ammoColor);
    void chooseTile(TileView tile);
    void chooseTypeOfEffect(TypeOfEffect typeOfEffect);
    void chooseWeapon(Weapon weapon);
    void choosePowerUp(PowerUp powerUp);
    void chooseNothing();
    void chooseEffect(Effect effect);
    void chooseAction(TypeOfAction action);
    void getLobbiesStatus();
    void getMapInfo(int gameId, int map);
    void quit();
    void wake();
    boolean isSyn();
    boolean isAck();
    void setSynCheckTimer(boolean toStart);
    void sendAck();
    void setAck(boolean ack);
    void setSyn(boolean syn);
    void setLostConnection(boolean lostConnection);
    boolean isLostConnection();
    ViewClient getViewClient();
}
