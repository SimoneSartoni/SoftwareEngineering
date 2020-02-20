package network.requests;

import network.ClientHandler;
import network.ServerController;

public class ChooseNothingRequest extends Request {


    public ChooseNothingRequest(String token) {
        super(token);
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseNothing(getToken());
    }
}
