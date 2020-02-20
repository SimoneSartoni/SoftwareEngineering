package model.enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * enum to state every turn state of the player
 * is used for every player to state what type of commands are available in different parts of the turn
 * states are constantly updated in model methods
 */
public enum TurnState implements Serializable {
    READY_TO_SPAWN("READY_TO_SPAWN"),
    READY_TO_RESPAWN("READY_TO_RESPAWN"),
    WAIT_FOR_OTHER_PLAYERS_TURN("WAIT_FOR_OTHER_PLAYERS_TURN"),
    DISCARD_POWERUP_FOR_SPAWN("DISCARD_POWERUP_FOR_SPAWN"),
    DISCARD_POWERUP_FOR_COST_EFFECT("DISCARD_POWERUP_FOR_COST_EFFECT"),
    DISCARD_POWERUP_FOR_COST_POWERUP("DISCARD_POWERUP_FOR_COST_POWERUP"),
    READY_FOR_ACTION("READY_FOR_ACTION"),
    CHOOSE_WEAPON_TO_TAKE("CHOOSE_WEAPON_TO_TAKE"),
    CHOOSE_WEAPON_HAND("CHOOSE_WEAPON_HAND"),
    CHOOSE_WEAPON_TO_RELOAD_MANDATORY("CHOOSE_WEAPON_TO_RELOAD_MANDATORY"),
    DISCARD_POWERUP_FOR_COST_WEAPON("DISCARD_POWERUP_FOR_COST_WEAPON"),
    DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION("DISCARD_POWERUP_FOR_COST_WEAPON_IN_ACTION"),
    CHOOSE_TYPE_OF_EFFECT("CHOOSE_TYPE_OF_EFFECT"),
    CHOOSE_EFFECT("CHOOSE_EFFECT"),
    CHOOSE_TARGET("CHOOSE_TARGET"),
    CAN_BE_COUNTER_ATTACKED("CAN_BE_COUNTER_ATTACKED"),
    CHOOSE_COUNTER_ATTACK("CHOOSE_COUNTER_ATTACK"),
    CHOOSE_POWERUP("CHOOSE_POWERUP"),
    CHOOSE_AMMOS("CHOOSE_AMMOS"),
    WAIT_FOR_OTHER_PLAYERS_TO_SPAWN("WAIT_FOR_OTHER_PLAYERS_TO_SPAWN"),
    CHOOSE_TILE_FOR_WEAPON_ACTION("CHOOSE_TILE_FOR_WEAPON_ACTION"),
    CHOOSE_TILE_FOR_GRAB_ACTION("CHOOSE_TILE_FOR_GRAB_ACTION"),
    CHOOSE_TILE_FOR_RUN_ACTION("CHOOSE_TILE_FOR_RUN_ACTION"),
    CHOOSE_TILE_FOR_SHOOT_ACTION("CHOOSE_TILE_FOR_SHOOT_ACTION"),
    CHOOSE_WEAPON_TO_DROP("CHOOSE_WEAPON_TO_DROP"),
    CHOOSE_ROOM("CHOOSE_ROOM"),
    CHOOSE_DIRECTION("CHOOSE_DIRECTION"),
    END_OF_TURN("END_OF_TURN"),
    CHOOSE_AFTER_DAMAGE_POWERUP("CHOOSE_AFTER_DAMAGE_POWERUP"),
    CHOOSE_POWERUP_TARGET("CHOOSE_POWERUP_TARGET"),
    CHOOSE_POWERUP_TILE("CHOOSE_POWERUP_TILE"),
    CHOOSE_WEAPON_TO_RELOAD("CHOOSE_WEAPON_TO_RELOAD"),
    CHOOSE_WEAPON_TO_RELOAD_IN_ACTION("CHOOSE_WEAPON_TO_RELOAD_IN_ACTION"),
    CHOOSE_LINKED_EFFECT("CHOOSE_LINKED_EFFECT"),
    READY_TO_SPAWN_DISCONNECTION("READY_TO_SPAWN_DISCONNECTION");

    private String name;
    private static List<TurnState> turnStates;

    TurnState(String name) {
        this.name = name;
        addToList();
    }

    private void addToList() {
        if(turnStates == null) {
            turnStates = new ArrayList<>();
        }
        turnStates.add(this);
    }

    public String getName() {
        return name;
    }

    public static TurnState ToTurnState(String s) {
        for(TurnState t : turnStates) {
            if(t.getName().equals(s)) {
                return t;
            }
        }
        //else throw exception
        return null;
    }
}
