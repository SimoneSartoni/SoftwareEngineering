package network;

import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.TimerTask;

public class ConnectionClientTimer extends TimerTask {

    private Client client;


    ConnectionClientTimer(Client client){
        this.client=client;
    }

    /***
     * Timer client-side the check if he received the syn by server. If not there's a network error and the client has lost connection
     * Otherwise he responds with an ack
     */
    @Override
    public void run() {
        if(!client.isSyn()) {
            System.out.println("Network error! Impossible to reach the Server...");
            client.setSynCheckTimer(false);
            client.setLostConnection(true);
            try {
                client.getViewClient().getListener().onLostConnection();
            } catch (RemoteException | SocketException e) {
                System.out.println("Remote error while setting lost connection");
            }
        }
        else{
            client.setSyn(false);
            client.sendAck();
            client.setLostConnection(false);
        }

    }


}
