package network;

import java.io.Serializable;
import java.util.TimerTask;

public class ConnectionAckServerTimer extends TimerTask implements Serializable {

    private ViewProxy viewProxy;
    private NetworkManager networkManager;

    ConnectionAckServerTimer(ViewProxy viewProxy,NetworkManager networkManager) {
        this.viewProxy=viewProxy;
        this.networkManager=networkManager;
    }

    /***
     * Check if a player has send the ack, if not he is disconnected, otherwise the other ack is waited
     */
    @Override
    public void run() {
        if(!networkManager.getAckMap().get(viewProxy.getToken())) {
            networkManager.disconnectToken(viewProxy.getToken());
            networkManager.stopSynTimer(viewProxy.getToken());
            networkManager.stopAckTimer(viewProxy.getToken());
        }
        else {
            networkManager.setAck(viewProxy.getToken(),false);
        }
    }
}
