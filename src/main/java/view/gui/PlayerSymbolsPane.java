package view.gui;

import javafx.scene.layout.GridPane;
import model.player.PlayerView;
import network.ClientContext;

import java.util.ArrayList;
import java.util.List;

public class PlayerSymbolsPane extends GridPane {
    private final GUIViewJavaFX view;
    private List<BtnEnemySymbol> btnEnemySymbols;

    /**
     * Constructor for PlayerSymbolsPane class, receiving his width and obtaining enemy players from client
     * model.
     * Set dimensions to width received and calculated height to preserve proportions.
     * @param width the width of the pane
     */
    public PlayerSymbolsPane(double width, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        double height = width;
        this.setPrefSize(width, height);
        btnEnemySymbols = new ArrayList<BtnEnemySymbol>();
        BtnEnemySymbol btnEnemySymbol;
        int i=0;
        List<PlayerView> supp = new ArrayList<>(ClientContext.get().getPlayerViews());
        for(PlayerView playerView1: supp){
            btnEnemySymbol = new BtnEnemySymbol(playerView1, width/2, height/2, viewJavaFX);
            btnEnemySymbols.add(btnEnemySymbol);
            add(btnEnemySymbol, i%2, i/2);
            i++;
        }

    }




}
