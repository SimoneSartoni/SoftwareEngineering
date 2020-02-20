package network.response;

import java.rmi.RemoteException;

public class GameStartedResponse implements Response {
    private final boolean started;

    public GameStartedResponse(boolean started) {
        this.started = started;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public boolean isStarted() {
        return started;
    }
}
