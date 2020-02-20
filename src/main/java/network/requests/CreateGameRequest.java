package network.requests;

import network.ClientHandler;
import network.ServerController;

public class CreateGameRequest extends Request {

    public final int map;
    public final String endMode;

    public CreateGameRequest(String token, int map, String endMode) {
        super(token);
        this.map = map;
        this.endMode = endMode;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.createGame(getToken(), map, endMode);
    }
}
