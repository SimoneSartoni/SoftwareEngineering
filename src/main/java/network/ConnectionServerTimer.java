package network;

import java.io.Serializable;
import java.util.TimerTask;

public class ConnectionServerTimer extends TimerTask implements Serializable {
    private ViewProxy viewProxy;

    ConnectionServerTimer(ViewProxy viewProxy){
        this.viewProxy=viewProxy;
    }

    /***
     * Send the syn to the client
     */
    @Override
    public void run() {
        viewProxy.onSyn();
    }
}

