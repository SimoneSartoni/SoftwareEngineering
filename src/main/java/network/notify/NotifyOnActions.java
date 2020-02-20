package network.notify;

import network.response.Response;
import network.response.ResponseHandler;
import model.enums.TypeOfAction;

import java.rmi.RemoteException;
import java.util.List;

public class NotifyOnActions implements Response {


    public final List<TypeOfAction> actions;

    public NotifyOnActions(List<TypeOfAction> actions) {
        this.actions = actions;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }
}
