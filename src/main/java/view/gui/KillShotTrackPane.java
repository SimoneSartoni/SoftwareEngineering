package view.gui;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.board.KillShotTrack;
import model.enums.PlayerColor;
import network.ClientContext;

import java.util.ArrayList;
import java.util.List;

class KillShotTrackPane extends HBox {
    /**
     * Constructor for KillShotTrackPane class, receiving his height and calculating width to preserve proportions,
     * obtaining current game KillShotTrack from parameter and showing as root child.
     * model.
     * Set dimensions to width and height.
     * @param height the height of the pane
     * @param killShotTrack the killShotTrack to show.
     */
    KillShotTrackPane(KillShotTrack killShotTrack, double height, GUIViewJavaFX viewJavaFX) {
        super();
        double ratio = 1309.0/400;
        double width = height * ratio;
        setSize(width, height);

        int maxKills = ClientContext.get().getKillShotTrack().getMaxKills();
        List<ImageView> skullsImageViews = new ArrayList<ImageView>();
        double skullWidth = width*107.0/1309;
        double skullHeight = height/2.5;
        double firstSpaceWidthRatio =  170.0/1309.0;

        VoidSpaceButton voidSpaceButton = new VoidSpaceButton("", width*firstSpaceWidthRatio, height, viewJavaFX);
        getChildren().add(0, voidSpaceButton);
        int i=0;
        for(i = 1; i<9-maxKills; i++){
                voidSpaceButton = new VoidSpaceButton("", skullWidth, skullHeight, viewJavaFX );
                getChildren().add(i, voidSpaceButton);
        }
        DamageImagePane damageImagePane;
        int k=0;
        if(!killShotTrack.getDeathOrder().isEmpty()){
            for(PlayerColor playerColor: killShotTrack.getDeathOrder()){
                if(killShotTrack.getIsDoubleKillDeathOrder().get(k))
                    damageImagePane = new DamageImagePane(playerColor, skullWidth, skullHeight,2, viewJavaFX);
                else
                    damageImagePane = new DamageImagePane(playerColor, skullWidth, skullHeight,1, viewJavaFX);
                getChildren().add(i, damageImagePane);
                damageImagePane.setAlignment(Pos.TOP_CENTER);
                damageImagePane.setTranslateY(91.0/400.0*height);
                k++;
                i++;
            }
        }
        k=0;
        for( ; i < 9; i++) {
            skullsImageViews.add(k, new ImageView(new Image("/images/red_skull.png", skullWidth, skullHeight, false, true)));
            getChildren().add(i, skullsImageViews.get(k));
            skullsImageViews.get(k).setTranslateY(91.0/400.0*height);
            k++;
        }
    }


    private void setSize(double width, double height){
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
    }

}

