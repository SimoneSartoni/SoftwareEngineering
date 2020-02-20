package network.response;

import java.rmi.RemoteException;

public class TextResponse implements Response {

    private final String content;

    public TextResponse (String content) {
        this.content = content;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    @Override
    public String toString() {
        return ">>> " + content;
    }
}
