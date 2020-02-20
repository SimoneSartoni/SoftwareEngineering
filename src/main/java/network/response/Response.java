package network.response;

import java.io.Serializable;
import java.rmi.RemoteException;

public interface Response extends Serializable {

    void handleResponse(ResponseHandler handler) throws RemoteException;
}
