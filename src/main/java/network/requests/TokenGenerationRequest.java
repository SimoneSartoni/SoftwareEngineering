package network.requests;

import network.ClientHandler;
import network.ServerController;

public class TokenGenerationRequest extends Request {


    @Override
    public void handleRequest(ClientHandler clientHandler ,ServerController remoteController) {
        remoteController.createToken(clientHandler);
    }
}
