package network.response;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class MultiTextResponse implements Response {
    private List<String> content = new ArrayList<>();

    public MultiTextResponse(List<String> content) {
        this.content.addAll(content);
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<String> getContent() {
        return content;
    }
}
