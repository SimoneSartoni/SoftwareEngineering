package run;

import view.cli.CLIView;
import view.gui.GUIViewJavaFX;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.Scanner;

public class LaunchClient {

    /***
     * main method the run the client. He can choose if playing in CLI or in GUI.
     */
    public static void main(String[] args) throws IOException, NotBoundException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Adrenaline!");
        System.out.println("Before starting the game you need to choose what User Interface you want to use");
        System.out.println("Cli or gui?");
        String answerView = scanner.nextLine();
        //String answerView = "gui";

        while(!answerView.equalsIgnoreCase("cli") && !answerView.equalsIgnoreCase("gui")) {
            System.out.println("Cli or gui?");
            answerView = scanner.nextLine();
        }
        if(answerView.equalsIgnoreCase("gui")) {
            GUIViewJavaFX guiViewJavaFX = new GUIViewJavaFX();
            guiViewJavaFX.runGUIView(null);
        }
        else {
            CLIView tempCliView = new CLIView();
            tempCliView.begin();
        }
    }

    public void hello() {

    }
}
