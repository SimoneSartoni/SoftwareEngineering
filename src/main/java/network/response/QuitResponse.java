package network.response;

import java.rmi.RemoteException;

public class QuitResponse implements Response {
    private final boolean close;
    private final String name;
    private final boolean causeOfDisconnection;
    public QuitResponse(boolean close, String name, boolean causeOfDisconnection) {
        this.close = close;
        this.name = name;
        this.causeOfDisconnection = causeOfDisconnection;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public boolean isClose() {
        return close;
    }

    public String getName() {
        return name;
    }

    public boolean isCauseOfDisconnection() {
        return causeOfDisconnection;
    }
}
