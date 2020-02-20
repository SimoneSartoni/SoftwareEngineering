package network;

import java.io.Serializable;

public class ManagerObserver implements Serializable {
    private final NetworkManager networkManager;

    ManagerObserver() {
        networkManager = NetworkManager.get(false);
    }

    public void restartPlayerTimer(int gameID) {
        networkManager.stopPlayerTimer(gameID);
        networkManager.restartPlayerTimer(gameID);
    }

    public void stopPlayerTimer(int gameID) {
        networkManager.stopPlayerTimer(gameID);
    }

    public void endGame(int gameId) {
        networkManager.endGame(gameId, "");
    }
}
