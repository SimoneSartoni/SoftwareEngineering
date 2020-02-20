package view.gui;

import javafx.scene.layout.HBox;
import model.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

class WeaponPane extends HBox {

    private final GUIViewJavaFX view;
    private List<BtnWeapon> weaponButtons;

    /***
     * Constructor for WeaponPane class, receiving his height.
     * Set dimensions to height received and calculated width to preserve proportions.
     * Create from 0 to 3 WeaponsPane to add to its children according to number of weapons in current player hand,
     * obtained from client model
     * @param height the height of the pane
     */
    WeaponPane(List<Weapon> weapons, List<String> imagesPath, double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        double width = height *720/406;
        setPrefSize(width, height);
        weaponButtons = new ArrayList<>();
        BtnWeapon btnWeapon;
        for(int i = 0; ((i<3)&&(i<weapons.size())); i++){
            String path = "";
            if(imagesPath.size() > i) {
                path = imagesPath.get(i);
            }
            btnWeapon = new BtnWeapon(weapons.get(i), path, width/3, height, 0, true, true,  view);
            weaponButtons.add(btnWeapon);
            getChildren().add(btnWeapon);
        }
    }
}
