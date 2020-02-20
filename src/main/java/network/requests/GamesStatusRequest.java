package network.requests;

import network.ClientHandler;
import network.ServerController;

public class GamesStatusRequest extends Request {
    public GamesStatusRequest(String token) {
        super(token);
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.giveGamesStatus(getToken());
    }
}
