package network.requests;

import model.enums.RoomColor;
import network.ClientHandler;
import network.ServerController;

public class ChooseRoomRequest extends Request {

    private RoomColor roomColor;

    public ChooseRoomRequest(String token, RoomColor roomColor) {
        super(token);
        this.roomColor=roomColor;
    }

    @Override
    public void handleRequest(ClientHandler clientHandler, ServerController remoteController) {
        remoteController.chooseRoom(getToken(), roomColor);
    }
}
