package network.requests;

import network.ClientHandler;
import network.ServerController;

public class PrintHelpRequest extends Request {
    public PrintHelpRequest(String token) {
        super(token);
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.requestPossibleCommands(getToken());
    }
}
