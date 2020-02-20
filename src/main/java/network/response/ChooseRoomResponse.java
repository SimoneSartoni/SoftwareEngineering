package network.response;

import model.enums.RoomColor;

import java.rmi.RemoteException;
import java.util.List;

public class ChooseRoomResponse implements Response {
    private final List<RoomColor> roomColors;

    public ChooseRoomResponse(List<RoomColor> roomColors) {
        this.roomColors = roomColors;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<RoomColor> getRoomColors() {
        return roomColors;
    }
}
