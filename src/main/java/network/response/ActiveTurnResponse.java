package network.response;

import java.rmi.RemoteException;

public class ActiveTurnResponse implements Response {

    private final boolean active;

    public ActiveTurnResponse(boolean active) {
        this.active = active;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public boolean isActive() {
        return active;
    }
}