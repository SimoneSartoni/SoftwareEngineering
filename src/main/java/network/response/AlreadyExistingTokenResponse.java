package network.response;

import java.rmi.RemoteException;

public class AlreadyExistingTokenResponse implements Response {
    private String alreadyExistingToken;
    private boolean exist;
    private boolean anotherActive;

    public AlreadyExistingTokenResponse(String alreadyExistingToken, boolean exist, boolean anotherActive) {
        this.alreadyExistingToken = alreadyExistingToken;
        this.exist = exist;
        this.anotherActive = anotherActive;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public String getAlreadyExistingToken() {
        return alreadyExistingToken;
    }

    public boolean isExist() {
        return exist;
    }

    public boolean isAnotherActive() {
        return anotherActive;
    }
}
