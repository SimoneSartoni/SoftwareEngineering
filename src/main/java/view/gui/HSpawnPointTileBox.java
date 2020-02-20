package view.gui;

import javafx.scene.layout.HBox;
import model.enums.AmmoColor;
import model.player.PlayerView;
import model.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

class HSpawnPointTileBox extends HBox {
    private final GUIViewJavaFX view;
    private PlayerView playerView;
    private List<BtnWeapon> weaponButtons;

    /**
     * Constructor for HSpawnPointTileBox class, receiving his height and calculating width to preserve proportions.
     * Set dimensions to width and height.
     * @param height the height of the pane
     * @param weapons weapons in SpawnPoint to show
     * @param imagesPath weapons's paths
     * @param ammoColor the color of the weapon
     */
    HSpawnPointTileBox(List<Weapon> weapons, List<String> imagesPath, AmmoColor ammoColor,  double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        double width = height *860.0/380;
        setSize(width, height);
        weaponButtons = new ArrayList<>();
        BtnWeapon btnWeapon;
        for(int i = 0; ((i<3)&&(i<weapons.size())); i++){
            String path = "";
            if(imagesPath.size() > i) {
                path = imagesPath.get(i);
            }
            btnWeapon = new BtnWeapon(weapons.get(i), path, width/3.3, height,  0,false, false, view);
            weaponButtons.add(btnWeapon);
            getChildren().add(btnWeapon);
        }
    }


    private void setSize(double width, double height){
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
    }
}
