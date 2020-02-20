package network.requests;

import network.ClientHandler;
import network.ServerController;

public class QuitRequest extends Request {
    public QuitRequest(String token) {
        super(token);
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.quit(getToken());
    }
}
