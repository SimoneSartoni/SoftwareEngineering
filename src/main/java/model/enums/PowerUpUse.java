package model.enums;

import java.io.Serializable;

/**
 * type of usage for powerUps
 * AFTER_DAMAGE
 * AFTER_TAKEN_DAMAGE
 * SEPARATED
 */
public enum PowerUpUse implements Serializable {
    AFTER_DAMAGE, AFTER_TAKEN_DAMAGE, SEPARATED;
    PowerUpUse() { }
}
