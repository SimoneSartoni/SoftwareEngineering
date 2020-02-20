package network.requests;

import network.ClientHandler;
import network.ServerController;

import java.io.Serializable;

public abstract class Request implements Serializable {

    private final String token;

    public Request() {token = null;}

    public Request(String token) {
        this.token = token;
    }

    public abstract void handleRequest(ClientHandler clientHandler, ServerController remoteController);

    public String getToken() {
        return token;
    }
}
