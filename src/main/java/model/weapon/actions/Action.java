package model.weapon.actions;
import model.enums.ForcedMovement;
import model.weapon.Effect;

import java.io.Serializable;

public interface Action extends Visitable, Serializable {
    /**
     * return the minimum amount of damage, marks, movements depending on the type of the actions
     * @return min amount of actions
     * */
    int getMinAmount();

    /**
     * return the maximum amount of damage, marks, movements depending on the type of the actions
     * @return max amount of actions
     * */
    int getMaxAmount();

    /**
     * return what kind of movement have the actions (only for movementAction)
     * @return if it's forced to a particular player, a specific tile or it's not restricted
     * */
    ForcedMovement getToTile();

    /**
     * return if the actions is optional or not
     * */
        
    boolean isActionExecuted();

    void setActionExecuted(boolean actionExecuted);

    Action getLinkedToNext();

    Effect getLinkedEffect();

    Effect getHitInTheEffect();

    void setLinkedToNext(Action linkedTo);

    void setLinkedEffect(Effect effect);

    void setHitInTheEffect(Effect effect);

    boolean isMandatoryChoice();
}
