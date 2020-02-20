package network.response;

import model.enums.Direction;

import java.rmi.RemoteException;
import java.util.List;

public class ChooseDirectionResponse implements Response {
    private final List<Direction> directionsList;

    public ChooseDirectionResponse(List <Direction> directionsList) {
        this.directionsList = directionsList;
    }@Override

    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);

    }

    public List<Direction> getDirectionsList() {
        return directionsList;
    }
}
