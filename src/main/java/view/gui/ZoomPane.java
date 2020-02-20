package view.gui;

import javafx.scene.layout.StackPane;

class ZoomPane extends StackPane {

    private final GUIViewJavaFX view;
    public BtnPowerUp btnPowerUp;
    public BtnWeapon btnWeapon;
    double widthPane, heightPane, width, height;

    /***
     * Constructor for ZoomPane class, receiving his width and height.
     * Set dimensions to width and height received
     * Create a VoidSpace Button and add it to its children
     * @param width the width of the pane
     * @param height the height of the pane
     */
    ZoomPane(double width,  double height, GUIViewJavaFX viewJavaFX) {
        super();
        this.view = viewJavaFX;
        btnPowerUp = null;
        btnWeapon = null;
        this.width = width;
        this.height = height;
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        getChildren().add(0, new VoidSpaceButton("", width, height, viewJavaFX));
    }

    /***
     * Create a WeaponPane and set it in position 0 of its children, showing the weapon enlarged
     * @param btnNewWeapon BtnWeapon to zoom on
     */
    public void addWeapon(BtnWeapon btnNewWeapon) {
        btnPowerUp = null;
        if(width<height*240.0/406.0){
            heightPane = width*406.0/240.0;
            widthPane = width;
        }
        else {
            widthPane = height*240.0/406.0;
            heightPane = height;
        }
        if(btnNewWeapon!=null){
            btnWeapon = new BtnWeapon(btnNewWeapon.weapon, btnNewWeapon.imagePath, widthPane, heightPane,0, btnNewWeapon.addEffectsButtons, false,  view);
            if(getChildren().isEmpty())
                getChildren().add(0, btnWeapon);
            else
                getChildren().set(0, btnWeapon);
        }
    }


    /***
     * Create a PowerUpPane and set it in position 0 of its children, showing the weapon enlarged
     * @param btnNewPowerUp PowerUp to zoom on
     */
    public void addPowerUp(BtnPowerUp btnNewPowerUp) {
        btnWeapon = null;
        double widthPane, heightPane;
        if(width<height*169.0/264.0){
            heightPane = width*264.0/169.0;
            widthPane = width;
        }
        else {
            widthPane = height*169.0/264.0;
            heightPane = height;
        }
        if(btnNewPowerUp!=null){
            btnPowerUp = new BtnPowerUp(btnNewPowerUp.powerUp, btnNewPowerUp.imagePath, widthPane, heightPane, view);
            if(getChildren().isEmpty())
                getChildren().add(0,btnPowerUp);
            else
                getChildren().set(0, btnPowerUp);
        }
    }

    public void reset() {
        btnWeapon = null;
        btnPowerUp = null;
        getChildren().set(0, new VoidSpaceButton("", width, height, view));
    }
}
