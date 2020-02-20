package network;

import model.board.TileView;
import model.enums.*;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.utility.MapInfoView;
import model.weapon.Effect;
import model.weapon.Weapon;
import view.ViewClient;
import view.cli.MapDrawing;
import view.gui.GUIViewJavaFX;
import view.gui.PaneToUpdate;

import java.io.IOException;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.List;

public class ClientController {
    private final Client client;
    private ViewClient viewClient;
    private boolean toClose;

    public ClientController(Client client, ViewClient view) {
        this.client = client;
        this.viewClient = view;
    }

    public void run() throws IOException {
        client.run();
    }

    public void createPlayer(int gameId, String username, PlayerColor playerColor) {
        client.setReceivedResponse(true);
        client.createPlayer(gameId, username, playerColor);
    }

    public void createGame(int map, String endMode) {
        client.setReceivedResponse(true);
        client.createGame(map, endMode);
    }

    public void giveGamesStatus() {
        client.setReceivedResponse(true);
        client.giveGamesStatus();
    }

    public void requestPossibleCommands() {
        client.setReceivedResponse(true);
        client.requestPossibleCommands();
    }

    public void printCommands(){
        try {
            viewClient.getListener().onPrintCommands(ClientContext.get().getPossibleCommands());
        }
        catch (RemoteException|SocketException e){
            System.err.println(e.getCause().getMessage());
        }
    }

    public void giveStatus() {
        PlayerView player = ClientContext.get().getCurrentPlayer();
        if(player != null) {
            try {
                viewClient.getListener().onStatus(player);
            } catch (RemoteException | SocketException e) {
                System.err.println(e.getCause().getMessage());
            }
        } else {
            System.err.println("Error on reading player");
        }
    }

    public void giveOtherStatus() {
        List<PlayerView> playerView = ClientContext.get().getPlayerViews();
        if(playerView != null) {
            try {
                viewClient.getListener().onOtherStatus(playerView);
            } catch (RemoteException| SocketException e) {
                System.err.println(e.getCause().getMessage());
            }
        } else {
            System.err.println("Error on reading enemy");
        }
    }

    public void giveTileStatus(int x, int y) {
        MapInfoView mapInfo = ClientContext.get().getMap();
        if(mapInfo != null) {
            try {
                viewClient.getListener().onTileInfo(mapInfo, x , y);
            } catch (RemoteException| SocketException e) {
                System.err.println(e.getCause().getMessage());
            } catch (IndexOutOfBoundsException e){
            }
        } else {
            System.err.println("Error on reading tile");
        }
    }

    public void drawMap() {
        MapInfoView mapInfo = ClientContext.get().getMap();
        if(mapInfo != null) {
            MapDrawing.drawMap(mapInfo,ClientContext.get().getKillShotTrack());
        }
    }

    public Client getClient(){
        return client;
    }

    public void chooseTarget(PlayerView player){
        client.setReceivedResponse(true);
        client.chooseTarget(player);
    }

    public void chooseRoom(RoomColor roomColor){
        client.setReceivedResponse(true);
        client.chooseRoom(roomColor);
    }

    public void chooseDirection(Direction direction){
        client.setReceivedResponse(true);
        client.chooseDirection(direction);
    }

    public void chooseAmmo(AmmoColor ammoColor){
        client.setReceivedResponse(true);
        client.chooseAmmo(ammoColor);
    }

    public void chooseTile(TileView tile){
        client.setReceivedResponse(true);
        client.chooseTile(tile);
    }

    public void chooseUsername(String username){
    }

    public void chooseTypeOfEffect(TypeOfEffect typeOfEffect){
        client.setReceivedResponse(true);
        client.chooseTypeOfEffect(typeOfEffect);
    }

    public void chooseWeapon(Weapon weapon){
        client.setReceivedResponse(true);
        client.chooseWeapon(weapon);

    }

    public void choosePowerUp(PowerUp powerUp){
        client.setReceivedResponse(true);
        client.choosePowerUp(powerUp);
    }

    public void chooseNothing() {
        client.setReceivedResponse(true);
        client.chooseNothing();
    }

    public void chooseEffect(Effect effect) {
        client.setReceivedResponse(true);
        client.chooseEffect(effect);
    }

    public void chooseAction(TypeOfAction action) {
        client.setReceivedResponse(true);
        client.chooseAction(action);
    }

    public void getLobbiesStatus() {
        client.setReceivedResponse(true);
        client.getLobbiesStatus();
    }

    public void getMapInfo(int gameId, int map) {
        client.setReceivedResponse(true);
        client.getMapInfo(gameId, map);
    }


    public void quit() {
        client.setReceivedResponse(true);
        client.quit();
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            System.out.println(e.getCause().getMessage());
        }
    }

    public void setToClose(boolean toClose) {
        this.toClose = toClose;
    }

    public boolean isToClose() {
        return toClose;
    }

    public void wake() {
        client.setReceivedResponse(true);
        client.wake();
        client.setSynCheckTimer(true);
    }

    public void setEnembyShownBoard(PlayerView playerView) {
        ClientContext.get().setEnemyShownBoard(playerView);
        ((GUIViewJavaFX)viewClient).onUpdate(PaneToUpdate.ENEMYPLAYER);
    }

    public ViewClient getViewClient() {
        return viewClient;
    }

    public void onSyn() {
        client.setSyn(true);
    }

}
