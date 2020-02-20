package model.board;

import model.utility.Ammo;

import java.io.Serializable;

/**
 * Ammo Tile class representing the card a player can grab to get ammos or power ups
 * */
public class AmmoTile implements Serializable{
    /**
     * {@link Ammo} representing how many ammos the player receive grabbing the ammo tile
     */
    private Ammo ammoGained;
    /**
     * {@link Ammo} representing how many power ups the player receive grabbing the ammo tile
     * */
    private int nOfPowerUp;

    /**
     * Ammo tile constructor
     * @param ammoGained represents how many ammos the player receive grabbing the ammo tile
     * @param nOfPowerUp represents how many power ups the player receive grabbing the ammo tile
     */
    public AmmoTile(Ammo ammoGained,int nOfPowerUp) {
        this.ammoGained=ammoGained;
        this.nOfPowerUp=nOfPowerUp;
    }

    public Ammo getAmmoGained() {
        return ammoGained;
    }

    public int getNOfPowerUp() {
        return nOfPowerUp;
    }

    public String toString () {
        return ammoGained.toString() + " | Number of powerups: " + nOfPowerUp;
    }
}
