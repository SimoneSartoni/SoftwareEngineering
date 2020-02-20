package view.cli;

import model.board.KillShotTrack;
import model.board.TileView;
import model.enums.TileLinks;
import model.player.PlayerView;
import model.utility.Ammo;
import model.utility.MapInfoView;
import model.weapon.Effect;
import model.weapon.Weapon;

import java.util.List;

public class MapDrawing {

    private static final String UPPER_LEFT_CORNER = "╔";
    private static final String UPPER_RIGHT_CORNER = "╗";
    private static final String LOWER_RIGHT_CORNER = "╝";
    private static final String LOWER_LEFT_CORNER = "╚";
    private static final String VERTICAL_WALL = "║";
    private static final String HORIZONTAL_WALL = "═════════";
    private static final String HORIZONTAL_SPACE = "       ";
    private static final String SMALL_VERTICAL_SPACE = "  ";
    private static final String VERTICAL_SPACE = "        ";
    private static final String SPACE = " ";
    private static final String SPAWN = "S ";
    private static final String AMMO = "A ";
    private static final String PLAYER_SPACE = "     ";
    private static final String PLAYER_PLACE = "0";
    private static final String PLAYER_DEAD_PLACE="D";
    private static final String PLAYER_OVERKILLED_PLACE="X";
    private static final String COLOR_SQUARE ="■";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static final int HEIGHT = 5;
    private static String toPrint = "";

    private MapDrawing() {

    }

    /***
     * Method to draw the map in cli, it elaborates all the tiles of the map to print the correct ascii string
     * @param map the map to print
     * @param killShotTrack the killshot track related to the map
     */
    public static void drawMap(MapInfoView map, KillShotTrack killShotTrack) {

        for (List<TileView> tiles : map.getMap()) {
            for (int i = 0; i < HEIGHT; i++) {
                toPrint = "";
                for (TileView t : tiles) {
                    elaborateTile(t, i);
                }
                ack(toPrint);
            }
        }
            if(killShotTrack!=null) {
                ack("");
                printKS(killShotTrack);
                ack("");
            }
    }

    /***
     * Method to print the right number of skulls in relation of a killshot track. Ascii coded are used.
     * @param killShotTrack the killshot track to print
     */
    private static void printKS(KillShotTrack killShotTrack) {
        StringBuilder toPrint = new StringBuilder();
        toPrint.append("KillShotTrack :");
        for(int i=0;i<killShotTrack.getMaxKills();i++) {
            if(i<killShotTrack.getDeathOrder().size()) {
                toPrint.append(getAnsiColor(killShotTrack.getDeathOrder().get(i).name()));
                toPrint.append(PLAYER_PLACE);
                if(killShotTrack.getIsDoubleKillDeathOrder().get(i))
                    toPrint.append(PLAYER_PLACE);
                toPrint.append(" ");
                toPrint.append(ANSI_RESET);
            }
            else {
                toPrint.append(getAnsiColor("red"));
                toPrint.append("S");
                toPrint.append(" ");
                toPrint.append(ANSI_RESET);
            }
        }
        if(killShotTrack.getMaxKills()<killShotTrack.getDeathOrder().size()) {
            for (int i = killShotTrack.getMaxKills(); i < killShotTrack.getDeathOrder().size(); i++) {
                toPrint.append(getAnsiColor(killShotTrack.getDeathOrder().get(i).name()));
                toPrint.append(PLAYER_PLACE);
                toPrint.append(" ");
                toPrint.append(ANSI_RESET);
            }
        }
        ack(toPrint.toString());
    }

    /***
     * This method take a tile and the row in which it is placed to draw it. It elaborates every direction to
     * get if a door, a wall, another room or the same room is present in that direction and draw the related ascii symbol
     * with the correct color
     * @param t the tile to print
     * @param i the row of the tile
     */
    private static void elaborateTile(TileView t, int i) {
        String canUp = t.getCanUp().name();
        String canDown = t.getCanDown().name();
        String canRight = t.getCanRight().name();
        String canLeft = t.getCanLeft().name();
        String up = "";
        String down = "";
        String left = "";
        String right = "";

        if(!canDown.equals(TileLinks.HOLE.name()) && !canUp.equals(TileLinks.HOLE.name()) &&
                !canRight.equals(TileLinks.HOLE.name()) && !canLeft.equals(TileLinks.HOLE.name())) {

            up = elaborateUp(canUp);
            down = elaborateDown(canDown);
            left = elaborateLeft(canLeft);
            right = elaborateRight(canRight);

            //print the upper bord
            if(i == 0) {
                toPrint += getAnsiColor(t.getRoom().name()) + UPPER_LEFT_CORNER + up + UPPER_RIGHT_CORNER + ANSI_RESET;
            }
            //print the lower bord
            else if(i == HEIGHT - 1) {
                toPrint += getAnsiColor(t.getRoom().name()) + LOWER_LEFT_CORNER + down + LOWER_RIGHT_CORNER + ANSI_RESET;
            }
            else {
                String label = " " + VERTICAL_SPACE;
                if(t.isSpawnPoint() && i == 1) {
                    label = SPAWN + elaboratePlayers(t.getPlayerViews()) + SMALL_VERTICAL_SPACE;
                }
                else if(t.getAmmo() != null && i == 1) {
                    label = AMMO + elaboratePlayers(t.getPlayerViews()) + SMALL_VERTICAL_SPACE;
                }
                else if(i == 1){
                    label = SMALL_VERTICAL_SPACE + elaboratePlayers(t.getPlayerViews()) + SMALL_VERTICAL_SPACE;
                }
                String color = getAnsiColor(t.getRoom().name());
                toPrint += color + left + label + ANSI_RESET + color + right + ANSI_RESET;
            }
        }

        else {
            toPrint += "           ";
        }
    }

    /***
     * Method to print the players in the map. Represented by circle in the color of the player.
     * @param players the players to draw
     * @return the string to print to draw players
     */
    private static String elaboratePlayers(List<PlayerView> players) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PLAYER_SPACE);
        if(!players.isEmpty()) {
            stringBuilder = new StringBuilder();
            int count = 0;
            for(PlayerView p : players) {
                count ++;
                String color = getAnsiColor(p.getPlayerColor().name());
                if(p.getDamageTakenView().size()<11) {
                    stringBuilder.append(color);
                    stringBuilder.append(PLAYER_PLACE);
                    stringBuilder.append(ANSI_RESET);
                }
                if(p.getDamageTakenView().size()==11) {
                    stringBuilder.append(color);
                    stringBuilder.append(PLAYER_DEAD_PLACE);
                    stringBuilder.append(ANSI_RESET);
                }
                if(p.getDamageTakenView().size()==12) {
                    stringBuilder.append(color);
                    stringBuilder.append(PLAYER_OVERKILLED_PLACE);
                    stringBuilder.append(ANSI_RESET);
                }

            }
            for(int i = count; i < 5; i++) {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    private static String elaborateUp(String canUp) {
        if (canUp.equals(TileLinks.DOOR.name())) {
            return LOWER_RIGHT_CORNER + HORIZONTAL_SPACE + LOWER_LEFT_CORNER;
        } else if (canUp.equals(TileLinks.WALL.name()) || canUp.equals(TileLinks.ENDOFMAP.name())) {
            return HORIZONTAL_WALL;
        } else if (canUp.equals(TileLinks.NEAR.name())) {
            return LOWER_RIGHT_CORNER + HORIZONTAL_SPACE + LOWER_LEFT_CORNER;
        }
        return "";
    }

    private static String elaborateDown(String canDown) {
        if (canDown.equals(TileLinks.DOOR.name())) {
            return UPPER_RIGHT_CORNER + HORIZONTAL_SPACE + UPPER_LEFT_CORNER;
        } else if (canDown.equals(TileLinks.WALL.name()) || canDown.equals(TileLinks.ENDOFMAP.name())) {
            return HORIZONTAL_WALL;
        } else if (canDown.equals(TileLinks.NEAR.name())) {
            return UPPER_RIGHT_CORNER + HORIZONTAL_SPACE + UPPER_LEFT_CORNER;
        }
        return "";
    }

    private static String elaborateRight(String canRight) {
        if (canRight.equals(TileLinks.DOOR.name())) {
            return SPACE;
        } else if (canRight.equals(TileLinks.WALL.name()) || canRight.equals(TileLinks.ENDOFMAP.name())) {
            return VERTICAL_WALL;
        } else if (canRight.equals(TileLinks.NEAR.name())) {
            return SPACE;
        }
        return "";
    }

    private static String elaborateLeft(String canLeft) {
        if (canLeft.equals(TileLinks.DOOR.name())) {
            return SPACE;
        } else if (canLeft.equals(TileLinks.WALL.name()) || canLeft.equals(TileLinks.ENDOFMAP.name())) {
            return VERTICAL_WALL;
        } else if (canLeft.equals(TileLinks.NEAR.name())) {
            return SPACE;
        }
        return "";
    }

    private static String getAnsiColor(String color) {
        String ret = "";
        switch (color.toLowerCase()) {
            case "red":
                ret = ANSI_RED;
                break;
            case "purple":
                ret = ANSI_PURPLE;
                break;
            case "yellow":
                ret = ANSI_YELLOW;
                break;
            case "blue":
                ret = ANSI_BLUE;
                break;
            case "grey":
                ret = ANSI_WHITE;
                break;
            case "green":
                ret = ANSI_GREEN;
                break;
            default:
                ret = ANSI_BLACK;
        }
        return ret;
    }

    private static void ack(String content) {
        System.out.println(content);
    }

    static String drawColor(String toString) {
        String color=getAnsiColor(toString);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(color);
        stringBuilder.append(toString);
        stringBuilder.append(ANSI_RESET);
        return stringBuilder.toString();
    }

    static String drawColorPoint(String toString) {
        String color=getAnsiColor(toString);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(color);
        stringBuilder.append(COLOR_SQUARE);
        stringBuilder.append(ANSI_RESET);
        return stringBuilder.toString();
    }

    static String printCost(Weapon w) {
        StringBuilder stringBuilder = new StringBuilder();
        String color;
        stringBuilder.append("Cost to Grab:");
        if(w.getPartiallyLoadedCost().getYellowValue()==0 && w.getPartiallyLoadedCost().getRedValue()==0 && w.getPartiallyLoadedCost().getBlueValue()==0)
            stringBuilder.append("free ");
        else {
            for (int i = 0; i < w.getPartiallyLoadedCost().getYellowValue(); i++) {
                color = getAnsiColor("yellow");
                stringBuilder.append(color);
                stringBuilder.append(COLOR_SQUARE);
                stringBuilder.append(ANSI_RESET);
                stringBuilder.append(" ");
            }
            for (int i = 0; i < w.getPartiallyLoadedCost().getRedValue(); i++) {
                color = getAnsiColor("red");
                stringBuilder.append(color);
                stringBuilder.append(COLOR_SQUARE);
                stringBuilder.append(ANSI_RESET);
                stringBuilder.append(" ");
            }
            for (int i = 0; i < w.getPartiallyLoadedCost().getBlueValue(); i++) {
                color = getAnsiColor("blue");
                stringBuilder.append(color);
                stringBuilder.append(COLOR_SQUARE);
                stringBuilder.append(ANSI_RESET);
                stringBuilder.append(" ");
            }
        }
        stringBuilder.append("Cost to Reload:");
        for (int i = 0; i < w.getReloadCost().getYellowValue(); i++) {
            color = getAnsiColor("yellow");
            stringBuilder.append(color);
            stringBuilder.append(COLOR_SQUARE);
            stringBuilder.append(ANSI_RESET);
            stringBuilder.append(" ");
        }
        for (int i = 0; i < w.getReloadCost().getRedValue(); i++) {
            color = getAnsiColor("red");
            stringBuilder.append(color);
            stringBuilder.append(COLOR_SQUARE);
            stringBuilder.append(ANSI_RESET);
            stringBuilder.append(" ");
        }
        for (int i = 0; i < w.getReloadCost().getBlueValue(); i++) {
            color = getAnsiColor("blue");
            stringBuilder.append(color);
            stringBuilder.append(COLOR_SQUARE);
            stringBuilder.append(ANSI_RESET);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    static String printCostEffect(Effect e) {
        StringBuilder stringBuilder = new StringBuilder();
        String color;
        stringBuilder.append("Cost of the Effect:");
        if (e.getCost().getYellowValue() == 0 && e.getCost().getRedValue() == 0 && e.getCost().getBlueValue() == 0)
            stringBuilder.append("free ");
        else {
            for (int i = 0; i < e.getCost().getYellowValue(); i++) {
                color = getAnsiColor("yellow");
                stringBuilder.append(color);
                stringBuilder.append(COLOR_SQUARE);
                stringBuilder.append(ANSI_RESET);
                stringBuilder.append(" ");
            }
            for (int i = 0; i < e.getCost().getRedValue(); i++) {
                color = getAnsiColor("red");
                stringBuilder.append(color);
                stringBuilder.append(COLOR_SQUARE);
                stringBuilder.append(ANSI_RESET);
                stringBuilder.append(" ");
            }
            for (int i = 0; i < e.getCost().getBlueValue(); i++) {
                color = getAnsiColor("blue");
                stringBuilder.append(color);
                stringBuilder.append(COLOR_SQUARE);
                stringBuilder.append(ANSI_RESET);
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    static String drawAmmo(Ammo ammo) {
        StringBuilder stringBuilder = new StringBuilder();
        String color;
        for (int i = 0; i <ammo.getYellowValue(); i++) {
            color = getAnsiColor("yellow");
            stringBuilder.append(color);
            stringBuilder.append(COLOR_SQUARE);
            stringBuilder.append(ANSI_RESET);
            stringBuilder.append(" ");
        }
        for (int i = 0; i <ammo.getRedValue(); i++) {
            color = getAnsiColor("red");
            stringBuilder.append(color);
            stringBuilder.append(COLOR_SQUARE);
            stringBuilder.append(ANSI_RESET);
            stringBuilder.append(" ");
        }
        for (int i = 0; i < ammo.getBlueValue(); i++) {
            color = getAnsiColor("blue");
            stringBuilder.append(color);
            stringBuilder.append(COLOR_SQUARE);
            stringBuilder.append(ANSI_RESET);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

}
