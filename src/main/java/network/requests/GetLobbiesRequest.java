package network.requests;

import network.ClientHandler;
import network.ServerController;

public class GetLobbiesRequest extends Request {
    public GetLobbiesRequest(String token) {
        super(token);
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.getLobbiesStatus(getToken());
    }
}
