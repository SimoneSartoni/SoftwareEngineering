package model.enums;

import java.io.Serializable;

/**
 * types of state of the player depending on the gameMode and the damage taken
 */
public enum PlayerState implements Serializable{
    NORMAL, ADRENALINE_1, ADRENALINE_2, FRENZY_BEFORE, FRENZY_AFTER, DEAD;
    PlayerState() { }
}
