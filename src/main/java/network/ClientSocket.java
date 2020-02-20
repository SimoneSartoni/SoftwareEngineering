package network;

import network.requests.*;
import network.response.Response;
import model.board.TileView;
import model.enums.*;
import model.player.PlayerView;
import model.powerup.PowerUp;
import model.weapon.Effect;
import model.weapon.Weapon;
import run.LaunchClient;
import view.ViewClient;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.Timer;

public class ClientSocket implements Client {
    private Socket connection;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final String ip;
    private final int port;
    private String token;
    private Thread receiver;
    private final int synCheckTime=1000;
    private ClientController clientController;
    private ResponseDecoder responseDecoder;
    private boolean notReceivedResponse;
    private ViewClient viewClient;
    private Timer synCheckTimer;
    private boolean syn;
    private boolean ack;
    private boolean lostConnection;
    private Timer retryingToConnectTimer;
    private boolean reconnectTimerStarted;

    public ClientSocket(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.notReceivedResponse = true;
        synCheckTimer=new Timer();
        retryingToConnectTimer = new Timer();
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public void setViewClient(ViewClient viewClient) {
        this.viewClient = viewClient;
    }

    public void setResponseDecoder(ResponseDecoder responseDecoder) {
        this.responseDecoder = responseDecoder;
    }

    public void init() throws IOException {
        try {
            connection = new Socket(ip, port);
            oos = new ObjectOutputStream(connection.getOutputStream());
            ois = new ObjectInputStream(connection.getInputStream());
            startReceiving();
        } catch (SocketException | UnknownHostException e) {
            System.out.println("Server this address is not reachable");
            System.exit(0);
        }
    }

    public void close() throws IOException {
        oos.close();
        ois.close();
        connection.close();
        viewClient.ack(System.getProperty("line.separator") + "Quit.");
        System.exit(0);
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

    /***
     * Method to send requests to the server
     * @param request the request to send to the server
     */
    public synchronized void request(Request request) {
        try {
            oos.writeObject(request);
            oos.reset();
        } catch (IOException e) {
            viewClient.errorAck("Exception on network: " + e.getMessage());
        }
    }

    /***
     * Method to wait responses of the server
     * @return the response received
     */
    private Response nextResponse() {
        try {
            return ((Response) ois.readObject());
        } catch (IOException e) {
            System.out.println("Bad formatting");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Wrong deserialization: " + e.getMessage());
        }

        return null;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public synchronized void createPlayer(int gameId, String username, PlayerColor playerColor) {
       request(new PlayerCreateRequest(token, gameId, username, playerColor));
       while(notReceivedResponse){
           try {
               this.wait();
           }
           catch(InterruptedException e){
               viewClient.errorAck(e.getMessage());
           }
       }
    }



    @Override
    public synchronized void quit() {
        request(new QuitRequest(token));
        while(notReceivedResponse){
            try {
                this.wait();
            }
            catch(InterruptedException e){
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void createGame(int map, String endMode) {
        request(new CreateGameRequest(token, map, endMode));
        while(notReceivedResponse){
            try {
                this.wait();
            }
            catch(InterruptedException e){
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void giveGamesStatus() {
        request(new GamesStatusRequest(token));
        while(notReceivedResponse){
            try {
                this.wait();
            }
            catch(InterruptedException e){
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void requestPossibleCommands() {
        request(new PrintHelpRequest(token));
        while(notReceivedResponse){
            try {
                this.wait();
            }
            catch(InterruptedException e){
                viewClient.errorAck(e.getMessage());
            }
        }
    }


    @Override
    public synchronized void generateToken() {
        setReceivedResponse(true);
        request(new TokenGenerationRequest());
        setSynCheckTimer(true);
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseTarget(PlayerView player) {
        request(new ChooseTargetRequest(token, player));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseRoom(RoomColor roomColor) {
        request(new ChooseRoomRequest(token, roomColor));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseDirection(Direction direction) {
        request(new ChooseDirectionRequest(token, direction));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseAmmo(AmmoColor ammoColor) {
        request(new ChooseAmmoRequest(token, ammoColor));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseTile(TileView tile) {
        request(new ChooseTileRequest(token, tile));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseTypeOfEffect(TypeOfEffect typeOfEffect) {
        request(new ChooseTypeOfEffectRequest(token, typeOfEffect));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseWeapon(Weapon weapon) {
        request(new ChooseWeaponRequest(token, weapon));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void choosePowerUp(PowerUp powerUp) {
        request(new ChoosePowerUpRequest(token, powerUp));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseNothing() {
        request(new ChooseNothingRequest(token));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseEffect(Effect effect) {
        request(new ChooseEffectRequest(token, effect));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void chooseAction(TypeOfAction action) {
        request(new ChooseActionRequest(token, action));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void getLobbiesStatus() {
        request(new GetLobbiesRequest(token));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void getMapInfo(int gameId, int map) {
        request(new MapInfoRequest(token, gameId, map));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void wake() {
        request(new WakeRequest(token));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
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
            lostConnection = false;
            reconnectTimerStarted = false;
            retryingToConnectTimer.cancel();
            retryingToConnectTimer.purge();
        } catch (IOException e) {
            if(!reconnectTimerStarted) {
                lostConnection = true;
                retryingToConnectTimer = new Timer();
                retryingToConnectTimer.scheduleAtFixedRate(new RetryConnectionTask(this), 0, 10000);
                reconnectTimerStarted = true;
            }
        }
    }

    /**
     * method that sends to the Server a request to check if he owns a token that has been disconnected because of connection or because the client has crashed
     * @param alreadyExistingToken the token to check
     */
    private synchronized void alreadyLoggedUser(String alreadyExistingToken) {
        setReceivedResponse(true);
        request(new AlreadyExistingTokenRequest(alreadyExistingToken));
        while (notReceivedResponse) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                viewClient.errorAck(e.getMessage());
            }
        }
    }


    /**
     * Method that starts the socket's thread to receive messages from the Server.
     * After the message is received it calls the decoder (visitor) to handle with the corresponding type of message
     */
    void startReceiving() {
        receiver = new Thread(
                () -> {
                    Response response = null;
                    do {
                        response = nextResponse();
                        if (response != null && !clientController.isToClose()) {
                            try {
                                response.handleResponse(responseDecoder);
                            } catch (RemoteException e) {
                                viewClient.errorAck(e.getMessage());
                            }
                        }
                    } while (response != null);
                }
        );
        receiver.start();
    }



    @Override
    public void setReceivedResponse(boolean receivedResponse) {
        notReceivedResponse=receivedResponse;
    }

    @Override
    public void sendAck() {
        request(new AckPing(token));
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
