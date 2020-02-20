package network.response;

import java.rmi.RemoteException;

public class ValidGameResponse implements Response {

    private final int valid;

    public ValidGameResponse(int valid) {
        this.valid = valid;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public int getValid() {
        return valid;
    }
}
