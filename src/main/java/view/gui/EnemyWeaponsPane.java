package view.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.player.PlayerView;
import model.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

class EnemyWeaponsPane extends HBox {

    /**
     * Create an Hbox pane that represent the enemy weapons, only the unloaed weapons will be displayed
     * @param playerView the selected enemy
     * @param height the height of the pane
     * @param viewJavaFX the GUI instance
     */
    EnemyWeaponsPane(PlayerView playerView, double height, GUIViewJavaFX viewJavaFX) {
        super();
        double width = height *720/406;
        setPrefSize(width, height);
        BtnWeapon btnWeapon;
        List<Weapon> weaponList = playerView.getUnloadedWeapons();
        List<String> weaponsImages = viewJavaFX.getWeaponsListImages(weaponList);
        for(int i = 0; ((i<3)&&(i<playerView.getUnloadedWeapons().size())); i++){
            String path = "/images/points.png";
            if(weaponsImages.size() > i) {
                path = weaponsImages.get(i);
            }
            btnWeapon = new BtnWeapon(playerView.getUnloadedWeapons().get(i), path, width/3, height, 0, true, false,  viewJavaFX);
            getChildren().add(btnWeapon);
        }
        ImageView imageViewWeapon;
        for(int i=0; i<playerView.getnOfLoadedWeapons(); i++){
            String path = "/images/cards/WEAPONS_BACK.png";
            imageViewWeapon = new ImageView(new Image(path, width/3, height, false, true));
            getChildren().add(imageViewWeapon);
        }
    }
}
