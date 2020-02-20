package network.requests;

import model.enums.Direction;
import network.ClientHandler;
import network.ServerController;

public class ChooseDirectionRequest extends Request{

    private final Direction direction;

    public ChooseDirectionRequest(String token,Direction direction) {
        super(token);
        this.direction=direction;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseDirection(getToken(),direction);
    }
}
