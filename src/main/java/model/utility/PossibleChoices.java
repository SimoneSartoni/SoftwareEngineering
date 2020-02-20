package model.utility;

import model.board.Tile;
import model.enums.*;
import model.player.Player;
import model.powerup.PowerUp;
import model.weapon.Effect;
import model.weapon.Weapon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * this class is used to store in a specific moment what are the possible choices that a client of a game can target or use
 * (SERVER_SIDE)
 */
public class PossibleChoices implements Serializable {
    private List<TypeOfAction> selectableActions;
    private List<Tile> selectableTiles;
    private List<Player> selectableTargets;
    private List<RoomColor> selectableRooms;
    private List<Direction> selectableDirections;
    private List<PowerUp> selectablePowerUps;
    private List<Weapon> selectableWeapons;
    private List<TypeOfEffect> selectableEffects;
    private List<Effect> selectableOptionalEffects;
    private List<AmmoColor> selectableAmmo;


    public PossibleChoices () {
        selectableActions =new ArrayList<>();
        selectableTiles = new ArrayList<>();
        selectableTiles = new ArrayList<>();
        selectableTargets = new ArrayList<>();
        selectableRooms = new ArrayList<>();
        selectableDirections = new ArrayList<>();
        selectablePowerUps = new ArrayList<>();
        selectableWeapons = new ArrayList<>();
        selectableEffects = new ArrayList<>();
        selectableOptionalEffects = new ArrayList<>();
        selectableAmmo = new ArrayList<>();
    }

    public List<TypeOfAction> getSelectableActions() { return selectableActions; }

    public List<Direction> getSelectableDirections() {
        return selectableDirections;
    }

    public List<Player> getSelectableTargets() {
        return selectableTargets;
    }

    public List<Weapon> getSelectableWeapons() {
        return selectableWeapons;
    }

    public List<PowerUp> getSelectablePowerUps() {
        return selectablePowerUps;
    }

    public List<RoomColor> getSelectableRooms() {
        return selectableRooms;
    }

    public List<Tile> getSelectableTiles() {
        return selectableTiles;
    }


    public List<AmmoColor> getSelectableAmmo() {
        return selectableAmmo;
    }


    public List<Effect> getSelectableOptionalEffects() {
        return selectableOptionalEffects;
    }

    public List<TypeOfEffect> getSelectableEffects() {
        return selectableEffects;
    }

    public void setSelectablePowerUps(List<PowerUp> selectablePowerUps) {
        this.selectablePowerUps.clear();
        this.selectablePowerUps.addAll(selectablePowerUps);
    }

    public void setSelectableDirections(List<Direction> selectableDirections) {
        this.selectableDirections.clear();
        this.selectableDirections.addAll(selectableDirections);
    }

    public void setSelectableWeapons(List<Weapon> targettableWeapons){
        this.selectableWeapons.clear();
        this.selectableWeapons.addAll(targettableWeapons);
    }

    public void setSelectableRooms(List<RoomColor> selectableRooms) {
        this.selectableRooms.clear();
        this.selectableRooms.addAll(selectableRooms);
    }

    public void setSelectableTargets(List<Player> selectableTargets) {
        this.selectableTargets.clear();
        this.selectableTargets.addAll(selectableTargets);
    }

    public void setSelectableTiles(List<Tile> selectableTiles) {
        this.selectableTiles.clear();
        this.selectableTiles.addAll(selectableTiles);
    }

    public void setSelectableEffects(List<TypeOfEffect> selectableEffects) {
        this.selectableEffects.clear();
        this.selectableEffects.addAll(selectableEffects);
    }

    public void setSelectableAmmo(List<AmmoColor> selectableAmmo) {

        this.selectableAmmo.clear();
        this.selectableAmmo.addAll(selectableAmmo);
    }

    public void setSelectableOptionalEffects(List<Effect> selectableOptionalEffects) {
        this.selectableOptionalEffects.clear();
        this.selectableOptionalEffects.addAll(selectableOptionalEffects);
    }

    public void setSelectableActions(List<TypeOfAction> selectableActions) {
        this.selectableActions.clear();
        this.selectableActions.addAll(selectableActions); }

    public void clear(){
        this.selectablePowerUps.clear();
        this.selectableDirections.clear();
        this.selectableRooms.clear();
        this.selectableTargets.clear();
        this.selectableTiles.clear();
        this.selectableEffects.clear();
        this.selectableOptionalEffects.clear();
        this.selectableAmmo.clear();
        this.selectableWeapons.clear();
    }
}
