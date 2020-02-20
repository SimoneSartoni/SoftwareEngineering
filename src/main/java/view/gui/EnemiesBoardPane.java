package view.gui;

import javafx.scene.layout.HBox;
import model.player.PlayerView;
import network.ClientContext;

class EnemiesBoardPane extends HBox {

    private final GUIViewJavaFX view;
    private PlayerBoardPane playerBoardPane;
    private PlayerSymbolsPane playerSymbolsPane;

    /**
     * Create a new HBox to represent the board with all the informations about the enemies
     * @param playerView the enemy to show in the pane
     * @param height the height of the pane
     * @param viewJavaFX the GUI instance
     */
    EnemiesBoardPane(PlayerView playerView, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;

        double width = (height/270)*(1120+270);
        this.setPrefSize(width, height);

        double playerBoardPaneRatio = 1120.0/(1120+270);
        double playersSymbolsRatio = 270.0/(1120+270);
        if(ClientContext.get().getEnemyShownBoard()== null){
            if(!ClientContext.get().getPlayerViews().isEmpty())
                ClientContext.get().setEnemyShownBoard(playerView);
        }
        playerBoardPane = new PlayerBoardPane(playerView, width*playerBoardPaneRatio, viewJavaFX);
        playerBoardPane.deleteButtons();
        playerSymbolsPane = new PlayerSymbolsPane(width*playersSymbolsRatio, viewJavaFX);

        getChildren().add(playerBoardPane);
        getChildren().add(playerSymbolsPane);
    }

}
