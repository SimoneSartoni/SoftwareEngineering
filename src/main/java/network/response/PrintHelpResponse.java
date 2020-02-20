package network.response;

import java.rmi.RemoteException;
import java.util.List;

public class PrintHelpResponse implements Response {
    private final List<String> printHelp;

    public PrintHelpResponse(List<String> printHelp) {
        this.printHelp = printHelp;
    }

    @Override
    public void handleResponse(ResponseHandler handler) throws RemoteException {
        handler.handle(this);
    }

    public List<String> getPrintHelp() {
        return printHelp;
    }
}
