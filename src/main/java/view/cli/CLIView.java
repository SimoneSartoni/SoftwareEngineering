package view.cli;

import model.enums.PlayerColor;
import model.utility.MapInfoView;
import network.*;
import view.ViewClient;

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CLIView implements ViewClient {

    private ClientController clientController;
    private Scanner scanner;
    private CLIListener cliListener;
    private CommandParsing commandParsing;
    private String command;
    private String token;
    private boolean finalFrenzy;

    private static final String ERROR_COMMAND = "ERROR";


    public CLIView(){
        this.scanner = new Scanner(System.in);
        this.command = "";
        this.token = "";
    }

    @Override
    public ViewListener getListener() throws RemoteException{
        return cliListener;
    }

    /***
     * This methods start the command line interface, asking if connecting with SOCKET or RMI calling the respective functions.
     * @throws IOException
     * @throws NotBoundException
     */
    public void begin() throws IOException, NotBoundException {
        ack("I see you are a CLI user! Here are some tips you can read freely before having fun:");
        ack("-All the choices you will have are going to be written in the form 'type_of_command'(empty space)'number_selected");
        ack("-You can type status to watch your current status");
        ack("-You can type other_status to see the the other players status");
        ack("-You can type status_tile to see what's in a tile in a certain moment of the game");
        ack("-Type 'help' to find what commands you can write in a specific part of the game");
        ack("-------------------------------------------------------------------------------------------------------------------------");
        ack("First of all you must choose what type of connection you want to use to connect and communicate with server");
        ack("Socket or rmi?");
        String answerConnection = scanner.nextLine();

        while (!answerConnection.equalsIgnoreCase("socket") && !answerConnection.equalsIgnoreCase("rmi")) {
            ack("Socket or rmi?");
            answerConnection = scanner.nextLine();
        }

        if (answerConnection.equalsIgnoreCase("rmi")) {
            startRmi();
        }
        else {
            startSocket();
        }
    }

    /***
     * Start rmi methods ask for the ip of the server to which connect and try to locate the remote registry with
     * the rmi controller. Also initialize the client controller and the listener of the model.
     * The registry is bounded at port 1200.
     * @throws IOException if server is not reachable or cannot locate the registry.
     * @throws NotBoundException
     */
    private void startRmi() throws IOException, NotBoundException {
        try {
            String ip;
            ack("ip: ");
            ip = scanner.nextLine();
            Registry registry = LocateRegistry.getRegistry(ip,1200);
            RemoteController remoteContoller = (RemoteController) registry.lookup("controller");
            ClientRmi clientRmi = new ClientRmi(remoteContoller);
            clientRmi.setViewClient(this);

            this.clientController = new ClientController(clientRmi, this);
            this.cliListener = new CLIListener(this, clientController);
            this.commandParsing = new CommandParsing(clientController);
            clientController.run();
        }
        catch (ConnectException | UnknownHostException e) {
            System.out.println("Server with this address is not reachable");
            System.exit(0);
        }
    }

    /***
     * Start socket method create a new Socket connection between the client and the server. Initialize the socket
     * and input / output streams for comunication as well as the client controller, the listener of the model and the responses decoder.
     * @throws IOException
     */
    private void startSocket() throws IOException {
        String ip;
        int port = 7200;
        ack("ip: ");
        ip = scanner.nextLine();
        ack("port socket: ");
        boolean okPort = false;
        while (!okPort){
            try {
                port = Integer.parseInt(scanner.nextLine());
                okPort = true;
            } catch (NumberFormatException e) {
                ack("port socket: ");
            }
        }

        ClientSocket clientSocket = new ClientSocket(ip, port);
        clientSocket.setViewClient(this);
        clientSocket.init();

        this.clientController = new ClientController(clientSocket, this);
        this.cliListener = new CLIListener(this, clientController);
        this.commandParsing = new CommandParsing(clientController);
        clientSocket.setClientController(this.clientController);
        ResponseDecoder responseDecoder = new ResponseDecoder(clientSocket, cliListener);
        clientSocket.setResponseDecoder(responseDecoder);

        clientController.run();

        clientSocket.close();
    }

    /***
     * First phase to start a game. The user is asked to choose if he wants to start a new game or join an existing one.
     * In the first case, he also has to choose the map to play with and the end mode (between sudden death and final frenzy).
     * Next the choose username phase is running and finally the playing phase.
     * @throws RemoteException
     */
    public void chooseGame() throws RemoteException {
        do {
            clientController.giveGamesStatus();
            clientController.getLobbiesStatus();

            String [] possibleAnswers = {"new", "join"};
            String answer = loopStringAsk("Create a new game or join one? [new | join]: ", Arrays.asList(possibleAnswers));
            if(answer.equalsIgnoreCase("new")) {
                int map = loopIntegerAsk("Which map? [1 to " + ClientContext.get().getNumberOfMaps() + "]: ", ClientContext.get().getNumberOfMaps());
                clientController.getMapInfo(-1, map);
                MapInfoView selectedMap = ClientContext.get().getMap();
                String endMode = loopStringAsk("End mode? " + selectedMap.getAllowedEndModes() + ": ", selectedMap.getAllowedEndModes());
                clientController.createGame(map, endMode.toLowerCase());
                chooseUsername(ClientContext.get().getValidGame());
            }
            else {
                if(ClientContext.get().isValidJoin()) {
                    chooseUsername(-1);
                }
            }
        } while (ClientContext.get().getCurrentPlayer() == null);
        playing();
    }

    /***
     * Method to ask the user the game id to connect / to retrieve information about the map selected or chosen,
     * and to select a name and a color of the player. Success only if the name and the color are not already in use.
     * @param givenId if -1 is a request to join a game, so the game id to which join is asked.
     *                Otherwise this represent the id of the map selected for the new game.
     * @throws RemoteException
     */
    private void chooseUsername(int givenId) throws RemoteException{
        int gameId = -1;
        do {
            if (givenId == -1) {
                do {
                    ack("Game id to connect: ");
                    try {
                        gameId = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        gameId = -1;
                    }
                } while (gameId < 0);
            } else {
                gameId = givenId;
            }

            clientController.getMapInfo(gameId, -1);
        } while (ClientContext.get().getMap() == null || ClientContext.get().getMap().getActualGameId() < 0);

        ack("Username: ");
        String username = scanner.nextLine();
        MapInfoView selectedMap = ClientContext.get().getMap();
        List<String> colors = new ArrayList<>();
        for(PlayerColor pC : selectedMap.getAllowedPlayerColors()) {
            colors.add(pC.name());
        }

        String color = loopStringAsk("Color " + colors + ": " , colors);
        PlayerColor playerColor = PlayerColor.valueOf(color);

        clientController.createPlayer(gameId, username, playerColor);
    }

    /***
     * Playing loop in whic a commad is asked and executed. If the command is not valid (validated server side)
     * a message is shown. Loop ends if a closing response is send by server or if the client as lost connection.
     * In this last case the client is permitted to connect again, otherwise he will be closed.
     * @throws RemoteException
     */
    public void playing() throws RemoteException {
        do {
            command = nextCommand();
            if (!command.equals(ERROR_COMMAND)) {
                commandParsing.initExecutionCommand(command);
            }
        } while (!clientController.isToClose() && !clientController.getClient().isLostConnection());
        if(clientController.getClient().isLostConnection() && !clientController.isToClose()) {
            try {
                begin();
            }
            catch (NotBoundException |IOException e){

            }
        }
        else {
            clientController.close();
        }

    }


    /***
     * Method to get the next command from the user. If the game is not started, a message is shown.
     * If he has to be closed or he has lost connection or he has been set as inactive, the command is not executed.
     * If he is inactive he will send a request of wake.
     * @return the string representing the command
     */
    private String nextCommand() {
        command = scanner.nextLine();
        if(!clientController.isToClose() && clientController.getClient().isLostConnection()){
           return ERROR_COMMAND;
        }
        if(!clientController.isToClose() && ClientContext.get().isDisconnected()) {
            clientController.wake();
        }
        if (!ClientContext.get().isGameStarted()) {
            ack("The game is not started yet");
            command = ERROR_COMMAND;
        }

        return command;
    }

    /***
     * Method to ask user a question for which e can respond only with an integer going from 0 to max.
     * @param question the message shown to user
     * @param max the max value of the valid range of the answer
     * @return the integer representing the answer
     */
    private int loopIntegerAsk(String question, int max) {
        int answer = -1;
        boolean okAnswer = false;
        do {
            ack(question);
            try {
                answer = Integer.parseInt(scanner.nextLine());
                for (int i = 1; i <= max; i++) {
                    if (answer == i) {
                        okAnswer = true;
                        break;
                    }
                }
            } catch (NumberFormatException e) {

            }
        } while (!okAnswer);

        return answer;
    }

    /***
     * Method to ask user a question for which e can respond only with an already established string
     * @param question the message shown to user
     * @param values the possible answers
     * @return the string representing the answer
     */
    private String loopStringAsk(String question, List<String> values) {
        String answer = "";
        boolean okAnswer = false;
        do {
            ack(question);
            answer = scanner.nextLine();
            for (String s : values) {
                if (answer.equalsIgnoreCase(s)) {
                    okAnswer = true;
                    break;
                }
            }
        } while (!okAnswer);

        return answer.toUpperCase();
    }

    @Override
    public void ack(String content) {
        System.out.println(content);
    }

    @Override
    public void errorAck(String content) {
        System.err.println(content);
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }


    @Override
    public void refresh() {
        //not used
    }


    @Override
    public void setFinalFrenzy(boolean finalFrenzy) {
        this.finalFrenzy = finalFrenzy;
    }

    @Override
    public boolean isFinalFrenzy() {
        return finalFrenzy;
    }

    @Override
    public void denyMove() {
        //not used
    }
}
