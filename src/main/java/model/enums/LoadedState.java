package model.enums;

import java.io.Serializable;

/**
 * possible loading state for the weapons
 */
public enum  LoadedState implements Serializable {
    UNLOADED, PARTIALLY_LOADED, LOADED;
    LoadedState() {  }
}
