package model.board;

import model.enums.*;
import model.powerup.PowerUp;
import model.powerup.PowerUpEffect;
import model.utility.Ammo;
import model.utility.MapInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * Class that parse all the maps from file
 */
public final class InitMap {

    private static Logger logger = Logger.getAnonymousLogger();
    private static final String ENDCOMMAND = "@ENDALL: ";
    private static final String IOEXMESSAGE = "IO Exception throwed";

    private InitMap() {

    }

    /***
     * Read a string that contains information about the tile to create
     * It sets the color of the tile, the coordinates, tha adjacents tile as one of DOOR, WALL, ENDOFMAP, HOLE and NEAR
     * which meaning is easily understandable
     * @param splitted the string to parse
     * @return the tile created
     */
    private static Tile addTile(String [] splitted) {
        Tile tile = new Tile();
        switch (splitted[0]) {
            case "B":
                tile.setRoom(RoomColor.BLUE);
                break;
            case "R":
                tile.setRoom(RoomColor.RED);
                break;
            case "P":
                tile.setRoom(RoomColor.PURPLE);
                break;
            case "Y":
                tile.setRoom(RoomColor.YELLOW);
                break;
            case "GN":
                tile.setRoom(RoomColor.GREEN);
                break;
            case "GY":
                tile.setRoom(RoomColor.GREY);
                break;

            default:
                break;
        }
        tile.setX(Integer.parseInt(splitted[1]));
        tile.setY(Integer.parseInt(splitted[2]));
        tile.setSpawnPoint(Boolean.parseBoolean(splitted[3]));
        switch (splitted[4]) {
            case "N":
                tile.setCanUp(TileLinks.NEAR);
                break;
            case "D":
                tile.setCanUp(TileLinks.DOOR);
                break;
            case "W":
                tile.setCanUp(TileLinks.WALL);
                break;
            case "E":
                tile.setCanUp(TileLinks.ENDOFMAP);
                break;
            case "H":
                tile.setCanUp(TileLinks.HOLE);
                break;
            default:
                break;
        }
        switch (splitted[5]) {
            case "N":
                tile.setCanDown(TileLinks.NEAR);
                break;
            case "D":
                tile.setCanDown(TileLinks.DOOR);
                break;
            case "W":
                tile.setCanDown(TileLinks.WALL);
                break;
            case "E":
                tile.setCanDown(TileLinks.ENDOFMAP);
                break;
            case "H":
                tile.setCanDown(TileLinks.HOLE);
                break;
            default:
                break;
        }
        switch (splitted[6]) {
            case "N":
                tile.setCanLeft(TileLinks.NEAR);
                break;
            case "D":
                tile.setCanLeft(TileLinks.DOOR);
                break;
            case "W":
                tile.setCanLeft(TileLinks.WALL);
                break;
            case "E":
                tile.setCanLeft(TileLinks.ENDOFMAP);
                break;
            case "H":
                tile.setCanLeft(TileLinks.HOLE);
                break;
            default:
                break;
        }
        switch (splitted[7]) {
            case "N":
                tile.setCanRight(TileLinks.NEAR);
                break;
            case "D":
                tile.setCanRight(TileLinks.DOOR);
                break;
            case "W":
                tile.setCanRight(TileLinks.WALL);
                break;
            case "E":
                tile.setCanRight(TileLinks.ENDOFMAP);
                break;
            case "H":
                tile.setCanRight(TileLinks.HOLE);
                break;
            default:
                break;
        }
        return tile;
    }

    /**
     * Read a string that contains informations about the forbidden color of that map (. if no forbidden color)
     * @param splitted the string to parse
     * @return the list of color that can be used
     */
    private static List<PlayerColor> addColors(String [] splitted) {
        List<PlayerColor> ret = new ArrayList<>((Arrays.asList(PlayerColor.values())));
        List<String> retString = new ArrayList<>();
        for(PlayerColor p : ret) {
            retString.add(p.name());
        }

        for(String s : splitted) {
            if(s.equalsIgnoreCase(".")) {
                break;
            }
            else if(retString.contains(s.toUpperCase())){
                ret.remove(PlayerColor.valueOf(s.toUpperCase()));
            }
        }

        return ret;
    }

    /**
     * Read a string that contains informations about the possible type of "end of game" of that map
     * (for example sudden death or final frenzy)
     * @param splitted the string to parse
     * @return the list of end modes as strings
     */
    private static List<String> addEnd(String[] splitted) {
        List<String> ret = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        String toAdd = "";

        for(String s : splitted) {
            if(s.equalsIgnoreCase("|")) {
                toAdd = toAdd.substring(0, toAdd.length() - 1);
                ret.add(toAdd);
                toAdd = "";
                stringBuilder = new StringBuilder();
            }
            else {
                stringBuilder.append(s);
                stringBuilder.append(" ");
                toAdd = stringBuilder.toString();
            }
        }

        toAdd = toAdd.substring(0, toAdd.length() - 1);
        ret.add(toAdd);
        return ret;
    }

    /**
     * Main method that initialize stuffs of reading and call the sub methods to create the various part of the maps,
     * relating on the "identifiers" read
     * @param fileName the name of the file to which read (only the name without / and with extension)
     * @return the list of maps created
     */
    public static List<MapInfo> initMap(String fileName) {
        List<MapInfo> returnedMap = new ArrayList<>();
        try (
                BufferedReader buf = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)))
        ) {
            int count = 0;
            int minPlayers = 0;
            int maxPlayers = 0;
            int width = 0;
            int height = 0;
            int numberOfSkulls = 0;
            List<PlayerColor> colors = new ArrayList<>();
            List<String> end = new ArrayList<>();
            List<Tile> tiles = new ArrayList<>();
            List<List<Tile>> map = new ArrayList<>();
            Tile tile;
            String line = buf.readLine();
            while(line != null) {
                if(!line.isEmpty() && !line.startsWith("//") && !line.matches(ENDCOMMAND)) {
                    String identifier;
                    identifier = line.substring(0, line.indexOf(':') + 2);
                    line = line.substring(line.indexOf(':') + 2);

                    String[] splitted = line.split("\\s");

                    switch (identifier) {

                        case "@D: ":
                            width = Integer.parseInt(splitted[0]);
                            height = Integer.parseInt(splitted[1]);
                            break;

                        case "@T: ":
                            tile = addTile(splitted);
                            tiles.add(tile);
                            break;

                        case "@SET: ":
                            map.add(new ArrayList<>(tiles));
                            tiles.clear();
                            break;

                        case "@FORBIDDENCOLOR: ":
                            colors = addColors(splitted);
                            break;

                        case "@NUMBEROFPLAYERS: ":
                            minPlayers = Integer.parseInt(splitted[0]);
                            maxPlayers = Integer.parseInt(splitted[1]);
                            break;

                        case "@ALLOWEDEND: ":
                            end = addEnd(splitted);
                            break;

                        case "@NUMBEROFSKULLS: ":
                            numberOfSkulls = Integer.parseInt(splitted[0]);
                            break;

                        case "@END: ":
                            MapInfo toAdd = new MapInfo(count, -1, "", width, height, map, colors, end, maxPlayers, minPlayers, new ArrayList<>());
                            toAdd.setNumberOfSkulls(numberOfSkulls);
                            returnedMap.add(toAdd);
                            tiles.clear();
                            map.clear();
                            colors.clear();
                            end.clear();
                            count ++;
                            break;

                        default:
                    }
                }
                line = buf.readLine();
            }

        }

        catch (IOException e) {
            logger.log(Level.SEVERE, IOEXMESSAGE, e);
        }

        return returnedMap;
    }

    /**
     * Main method that initialize stuffs of reading and call the sub methods to create the ammo tiles,
     * relating on the "identifiers" read
     * @param fileName the name of the file to which read (only the name without / and with extension)
     * @return the list of ammo tiles created
     */
    public static List<AmmoTile> initAmmoTiles(String fileName) {
        List <AmmoTile> returnedTiles = new ArrayList<>();
        try (
                BufferedReader buf = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)))
        ) {

            String line = buf.readLine();

            while(line != null && !line.matches(ENDCOMMAND)) {

                String [] splitted;

                if(!line.startsWith("#")) {
                    splitted = line.split("\\s");
                    for(int i = 0; i < Integer.parseInt(splitted[0]); i++) {
                        returnedTiles.add(new AmmoTile(
                                new Ammo(Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3])),
                                Integer.parseInt(splitted[4])
                        ));
                    }
                }

                line = buf.readLine();
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, IOEXMESSAGE, e);
        }

        return returnedTiles;
    }

    /**
     * Main method that initialize stuffs of reading and call the sub methods to create the powerups,
     * relating on the "identifiers" read
     * @param fileName the name of the file to which read (only the name without / and with extension)
     * @return the list of powerup created
     */
    public static List<PowerUp> initPowerUps(String fileName) {
        List <PowerUp> returnedPowerUps = new ArrayList<>();
        int counter = 0;
        try (
                BufferedReader buf = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)))
        ) {
            String line = buf.readLine();

            while(line != null && !line.matches(ENDCOMMAND)) {

                String[] splitted;

                if (!line.matches("") && !line.startsWith("#")) {

                    splitted = line.split("\\s");

                    int numberoOf = Integer.parseInt(splitted[0]);
                    AmmoColor color;
                    PowerUpUse use;
                    int cost = Integer.parseInt(splitted[4]);
                    Ammo cost2=new Ammo(0,0,0);
                    switch(cost){
                        case 0:
                            cost2=new Ammo(0,0,0);
                            break;
                        case 1:
                            cost2=new Ammo(-1,-1,-1);
                            break;
                        default:break;
                    }
                    switch (splitted[1]) {
                        case "R":
                            color = AmmoColor.RED;
                            break;
                        case "B":
                            color = AmmoColor.BLUE;
                            break;
                        case "Y":
                            color = AmmoColor.YELLOW;
                            break;
                        default:
                            color = AmmoColor.RED;
                            break;
                    }
                    switch (splitted[2]) {
                        case "S":
                            use = PowerUpUse.SEPARATED;
                            break;
                        case "AD":
                            use = PowerUpUse.AFTER_DAMAGE;
                            break;
                        case "ADT":
                            use = PowerUpUse.AFTER_TAKEN_DAMAGE;
                            break;
                        default:
                            use = PowerUpUse.SEPARATED;
                            break;
                    }

                    for(int i = 0; i < numberoOf; i++) {
                        PowerUp powToAdd = new PowerUp(color, use, splitted[3],
                                new PowerUpEffect(cost2, Integer.parseInt(splitted[5]), Integer.parseInt(splitted[6]),
                                        Integer.parseInt(splitted[7]),Integer.parseInt(splitted[8]),
                                        Boolean.parseBoolean(splitted[9]), Boolean.parseBoolean(splitted[10]),Boolean.parseBoolean(splitted[11])));
                        powToAdd.setId(counter);
                        returnedPowerUps.add(powToAdd);
                        counter ++;
                    }
                }

                line = buf.readLine();
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, IOEXMESSAGE, e);
        }
        return returnedPowerUps;
    }

    /**
     * Read the countdown time to start the game from file
     * @param mapFile the file to which read the countdown time
     * @return the countdown time
     */
    public static int initCountdown(String mapFile) {
        int count = 0;
        try (
                BufferedReader buf = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(mapFile)))
        ) {
            String line = buf.readLine();
            while(line != null) {
                if (!line.isEmpty() && !line.startsWith("//") && !line.matches(ENDCOMMAND)) {
                    String identifier;
                    identifier = line.substring(0, line.indexOf(':') + 2);
                    line = line.substring(line.indexOf(':') + 2);

                    if(identifier.equals("@COUNTDOWN: ")){
                        count = Integer.parseInt(line);
                    }
                }
                line = buf.readLine();
            }
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, IOEXMESSAGE, e);
        }
        return count;
    }

    /**
     * Read the countdown time to disconnect a player cause of inactivity from file
     * @param mapFile the file to which read the countdown time
     * @return the countdown time
     */
    public static int initPlayerCountdown(String mapFile) {
        int count = 0;
        try (
                BufferedReader buf = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(mapFile)))
        ) {
            String line = buf.readLine();
            while(line != null) {
                if (!line.isEmpty() && !line.startsWith("//") && !line.matches(ENDCOMMAND)) {
                    String identifier;
                    identifier = line.substring(0, line.indexOf(':') + 2);
                    line = line.substring(line.indexOf(':') + 2);

                    if (identifier.equals("@PLAYERCOUNTDOWN: ")) {
                            count = Integer.parseInt(line);
                    }
                }
                line = buf.readLine();
            }
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, IOEXMESSAGE, e);
        }
        return count;
    }
}
