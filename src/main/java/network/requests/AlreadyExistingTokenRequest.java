package network.requests;

import network.ClientHandler;
import network.ServerController;

public class AlreadyExistingTokenRequest extends Request {

    private String alreadyExistingToken;

    public AlreadyExistingTokenRequest(String alreadyExistingToken) {
        this.alreadyExistingToken = alreadyExistingToken;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.getAlreadyLoggedUser(clientHandler, alreadyExistingToken);
    }
}
