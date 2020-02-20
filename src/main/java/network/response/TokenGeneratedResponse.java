package network.response;

import java.rmi.RemoteException;

public class TokenGeneratedResponse implements Response {
    private final String token;

    public TokenGeneratedResponse(String generateToken) {
        this.token = generateToken;
    }

    @Override
    public void handleResponse(ResponseHandler handler)throws RemoteException {
        handler.handle(this);
    }

    public String getToken() {
        return token;
    }
}
