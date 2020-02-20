package model.enums;

import java.io.Serializable;

/**
 * type of movement in effect actions in weapons
 */
public enum ForcedMovement implements Serializable {
    FORCED_TO_PLAYER, NOT_RESTRICTED, NO_MOVEMENT;
    ForcedMovement() { }
}
