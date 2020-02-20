package network.requests;

import network.ClientHandler;
import network.ServerController;

public class WakeRequest extends Request {

    public WakeRequest(String token) {
        super(token);
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.wake(getToken());
    }
}
