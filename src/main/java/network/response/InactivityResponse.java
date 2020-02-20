package network.response;

import java.rmi.RemoteException;

public class InactivityResponse implements Response {
    private boolean inactive;

    public InactivityResponse(boolean inactive) {
        this.inactive = inactive;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public boolean isInactive() {
        return inactive;
    }
}
