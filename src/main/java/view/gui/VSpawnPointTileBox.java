package view.gui;

import javafx.scene.layout.VBox;
import model.enums.AmmoColor;
import model.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

class VSpawnPointTileBox extends VBox {
    private final GUIViewJavaFX view;
    private List<BtnWeapon> weaponButtons;

    /**
     * Constructor for VSpawnPointTileBox class, receiving his width and calculating height to preserve proportions.
     * Set dimensions to width and height.
     * @param width the width of the pane
     * @param weapons weapons in SpawnPoint to show
     * @param imagesString weapons's paths
     * @param ammoColor the color of the weapon
     * @param rotation the rotation, can be a multiple of 90°
     */
    VSpawnPointTileBox(List<Weapon> weapons, List<String> imagesString, AmmoColor ammoColor, double width, double rotation, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        double height = width *860/380;
        setSize(width, height);
        weaponButtons = new ArrayList<>();
        BtnWeapon btnWeapon;
        for(int i = 0; ((i<3)&&(i<weapons.size())); i++){
            String path = "";
            if(imagesString.size() > i) {
                path = imagesString.get(i);
            }
            btnWeapon = new BtnWeapon(weapons.get(i), path, height/2.3, width/1.4, rotation, false, false, view);
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
