package network;

import run.LaunchClient;

import java.io.Serializable;
import java.util.TimerTask;

public class PersistenceCountdown extends TimerTask implements Serializable {


    /***
     * "Save the game" saving on file "saves.txt" all the network manager
     */
    @Override
    public synchronized void run() {
        //save games
        NetworkManager networkManager = NetworkManager.get(false);

        if(networkManager.getGameManagers().isEmpty()) {
            return;
        }


        String path = LaunchClient.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int lastSlashIndex = path.lastIndexOf("/");
        path = path.substring(0, lastSlashIndex + 1);
        path = path + "saves.txt";
        //save data
        synchronized (NetworkManager.get(false)) {
            new SaveGameUtility().saveToFile(networkManager, path);
        }    }
}
