package network.requests;

import network.ClientHandler;
import network.ServerController;

public class AckPing extends Request {

    public AckPing(String token){
        super(token);
    }
    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.sendAck(getToken());
    }
}
