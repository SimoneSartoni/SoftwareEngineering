package network.response;

import java.rmi.RemoteException;

public class LostConnectionResponse implements Response {

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }
}
