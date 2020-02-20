package network.response;

import java.rmi.RemoteException;

public class SetNewPossibileCommandsResponse implements Response {


    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }
}
