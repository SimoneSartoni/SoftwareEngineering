package model.enums;

import java.io.Serializable;

/**
 * enum to describe the link of the tile
 * HOLE if a tile is not valid (out of map)
 */
public enum TileLinks implements Serializable {
    NEAR, DOOR, WALL, ENDOFMAP, HOLE;
    TileLinks() { }
}
