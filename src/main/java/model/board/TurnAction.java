package model.board;

import java.io.Serializable;

public class TurnAction implements Serializable {
    /**
     * indicates the number of action that a player can do in one turn
     */
    private int maxNOfActions;
    /**
     * indicates the number of movement that a player can do before a shoot action
     */
    private int movementBeforeShooting;
    /**
     * indicates whether a player can reload before the shoot action or not
     */
    private boolean reloadBeforeShooting;
    /**
     * indicates how far a player can move with a run action
     */
    private int onlyMovement;
    /**
     * indicates how many movement a player can do to grab something
     */
    private int movementBeforeGrabbing;

    public TurnAction(int maxNOfActions, int movementBeforeShooting, boolean reloadBeforeShooting, int onlyMovement, int movementBeforeGrabbing) {
        this.maxNOfActions = maxNOfActions;
        this.movementBeforeShooting = movementBeforeShooting;
        this.reloadBeforeShooting = reloadBeforeShooting;
        this.onlyMovement = onlyMovement;
        this.movementBeforeGrabbing = movementBeforeGrabbing;
    }

    public int getMaxNOfActions() {
        return maxNOfActions;
    }


    public int getMovementBeforeShooting() {
        return movementBeforeShooting;
    }


    public boolean isReloadBeforeShooting() {
        return reloadBeforeShooting;
    }


    public int getOnlyMovement() {
        return onlyMovement;
    }


    public int getMovementBeforeGrabbing() {
        return movementBeforeGrabbing;
    }

    @Override
    public String toString() {
        return "[ max number of action = " + maxNOfActions +
                ", run movement = " + onlyMovement +
                ", movement before grabbing=" + movementBeforeGrabbing +
                ", movement before shooting = " + movementBeforeShooting +
                ", reload before shooting = " + reloadBeforeShooting +
                " ]";
    }
}
