package run;

import network.AdrenalineServer;
import network.ServerController;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class LaunchServer {

    /***
     * main method to run the server. It first asks the user if he wants to reload a previous state of the game
     * (PERSISTENCE FEATURE)
     */
    public static void main(String[] args) throws RemoteException, IOException {
        Scanner scanner = new Scanner(System.in);
        String answer = "";
        while(!answer.equalsIgnoreCase("yes") && !answer.equalsIgnoreCase("no")) {
            System.out.println("Do you want to load a previous saved state? (yes / no): ");
            answer = scanner.nextLine();
        }

        boolean load = false;
        if(answer.equalsIgnoreCase("yes")) {
            load = true;
        }

        ServerController serverController = new ServerController(load);
        Registry registry = LocateRegistry.createRegistry(1200);
        registry.rebind("controller", serverController);
        AdrenalineServer server = new AdrenalineServer(7200, serverController);
        try {
            server.run();
        } finally {
            server.close();
        }


    }
}
