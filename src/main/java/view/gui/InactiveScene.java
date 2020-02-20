package view.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import network.ClientContext;


class InactiveScene extends Scene {
    /***
     * Constructor for InactiveScene class, receiving a StackPane root, his width and height.
     * Set scene root to root and set its dimensions to width and height
     * Set each characteristics and function for reconnecting scene.
     * @param root root of the Scene
     * @param width the wFidth of the pane
     * @param height the height of the pane
     */
    InactiveScene(StackPane root, double width, double height, GUIViewJavaFX guiViewJavaFX) {
        super(root, width, height);

        Button reconnectButton = new Button("Reconnect");
        reconnectButton.setOnAction(e -> {
            if(!guiViewJavaFX.getClientController().isToClose() && ClientContext.get().isDisconnected()) {
                guiViewJavaFX.getClientController().wake();
            }
        });

        root.getChildren().add(reconnectButton);
    }
}
