package network;

import java.rmi.RemoteException;
import java.util.TimerTask;

class RetryConnectionTask extends TimerTask {

    private Client client;

    RetryConnectionTask(Client client) {
        this.client = client;
    }

    /***
     * When the connection is lost retries to connect
     */
    @Override
    public void run() {
        try {
            System.out.println("Connection error, retrying");
            client.run();
        } catch (RemoteException e) {
            System.out.println("Error while running client");
        }
    }
}
