package network;

import model.board.TileView;
import model.enums.*;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.weapon.Effect;
import model.weapon.Weapon;
import run.LaunchClient;
import view.ViewClient;

import java.io.*;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Timer;

public class ClientRmi implements Client, Remote, Serializable {

    private final RemoteController controller;
    private String token;
    private ViewClient viewClient;
    private boolean syn;
    private boolean ack;
    private final int synCheckTime = 1000;
    private Timer synCheckTimer;
    private boolean lostConnection;
    private Timer retryingToConnectTimer;
    private boolean reconnectTimerStarted;

    public ClientRmi(RemoteController controller) {
        this.controller = controller;
        synCheckTimer = new Timer();
        retryingToConnectTimer = new Timer();
    }

    public void setViewClient(ViewClient viewClient) {
        this.viewClient = viewClient;
    }

    @Override
    public void init() throws IOException {
        //empty
    }

    @Override
    public void setSynCheckTimer(boolean toStart) {
        if(toStart) {
            synCheckTimer = new Timer();
            synCheckTimer.scheduleAtFixedRate(new ConnectionClientTimer(this),(synCheckTime/2)+synCheckTime, synCheckTime);
        }
        else {
            synCheckTimer.purge();
            synCheckTimer.cancel();
        }
    }

    @Override
    public void sendAck() {
        try {
            controller.sendAck(token);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void setAck(boolean ack) {
        this.ack=ack;
    }

    @Override
    public void setSyn(boolean syn) {
       this.syn=syn;
    }

    @Override
    public void close() throws IOException {
        System.out.println(System.getProperty("line.separator") + "Quit.");
        System.exit(0);
    }
    
    @Override
    public void createPlayer(int gameId, String username, PlayerColor playerColor) {
        try {
            controller.createPlayer(token, gameId, username, playerColor);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void quit() {
        try {
            controller.quit(token);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void createGame(int map, String endMode) {
        try {
            controller.createGame(token, map, endMode);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void giveGamesStatus() {
        try {
            controller.giveGamesStatus(token);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }


    @Override
    public void requestPossibleCommands() {
        try {
            controller.requestPossibleCommands(token);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void generateToken() {
        try {
            controller.generateToken(viewClient.getListener());
            setSynCheckTimer(true);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }


    /***
     * Method to run the client. It first check if an already existing token can be used to reconncet to a prevoius game.
     * In this case the game is joined automatically. Otherwise the phase of choosing a game and playing is done. In case of
     * lost connection this method is called.
     * @throws RemoteException
     */
    @Override
    public void run() throws RemoteException {

        String path = LaunchClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int lastSlashIndex = path.lastIndexOf("/");
        path = path.substring(0, lastSlashIndex + 1);
        File tokenFile = new File(path + "token.txt");
        try {
            if (tokenFile.createNewFile()) {
                //nothing
            } else {
                //nothing
            }
        }
        catch (IOException e) {
            System.err.println("Error while creating token file");
            System.err.println(e.getMessage());
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tokenFile))){
            lostConnection = false;
            reconnectTimerStarted = false;
            retryingToConnectTimer.cancel();
            retryingToConnectTimer.purge();
            String line = bufferedReader.readLine();
            while(line != null && ClientContext.get().getAlreadyExistingToken() == null) {
                if(!line.isEmpty()) {
                    alreadyLoggedUser(line);
                }
                line = bufferedReader.readLine();
            }
            if(ClientContext.get().getAlreadyExistingToken() != null) {
                viewClient.ack(">>> You already have a token.. Connecting to the game you were in before");
                setSynCheckTimer(true);
                viewClient.playing();
            }
            else {
                viewClient.ack(">>> You don't have a token, generating it.. ");
                generateToken();
                viewClient.chooseGame();
            }
        } catch (IOException e) {
            if(!reconnectTimerStarted) {
                lostConnection = true;
                retryingToConnectTimer = new Timer();
                retryingToConnectTimer.scheduleAtFixedRate(new RetryConnectionTask(this), 0, 10000);
                reconnectTimerStarted = true;
            }
        }
    }

    private void alreadyLoggedUser(String alreadyExistingToken) {
        try {
            controller.getAlreadyLoggedUser(viewClient.getListener(), alreadyExistingToken);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void setReceivedResponse(boolean receivedResponse) {
        //only used for socket
    }

    @Override
    public boolean isSyn() {
        return syn;
    }

    @Override
    public boolean isAck() {
        return ack;
    }

    @Override
    public void chooseTarget(PlayerView player) {
        try {
            controller.chooseTarget(token, player);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void chooseRoom(RoomColor roomColor) {
        try {
            controller.chooseRoom(token, roomColor);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void chooseDirection(Direction direction) {
        try {
            controller.chooseDirection(token, direction);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void chooseAmmo(AmmoColor ammoColor) {
        try {
            controller.chooseAmmo(token, ammoColor);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void chooseTile(TileView tile) {
        try {
            controller.chooseTile(token, tile);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void chooseTypeOfEffect(TypeOfEffect typeOfEffect) {
        try {
            controller.chooseTypeOfEffect(token, typeOfEffect);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void chooseWeapon(Weapon weapon) {
        try {
            controller.chooseWeapon(token, weapon);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void choosePowerUp(PowerUp powerUp) {
        try {
            controller.choosePowerup(token, powerUp);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void chooseNothing() {
        try {
            controller.chooseNothing(token);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void chooseEffect(Effect effect) {
        try {
            controller.chooseEffect(token, effect);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void chooseAction(TypeOfAction action) {
        try {
            controller.chooseAction(token, action);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void getLobbiesStatus() {
        try {
            controller.getLobbiesStatus(token);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void getMapInfo(int gameId, int map) {
        try {
            controller.getMapInfo(token, gameId, map);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public void wake() {
        try {
            controller.wake(token);
        } catch (RemoteException | SocketException e) {
            viewClient.errorAck(e.getMessage());
        }
    }

    @Override
    public boolean isLostConnection() {
        return lostConnection;
    }

    @Override
    public void setLostConnection(boolean lostConnection) {
        this.lostConnection = lostConnection;
    }

    @Override
    public ViewClient getViewClient() {
        return viewClient;
    }
}
