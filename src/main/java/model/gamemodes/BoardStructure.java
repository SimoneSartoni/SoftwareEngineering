package model.gamemodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BoardStructure {
    /**
     * actions allowed in normal mode
     */
    private int baseActions;
    private int baseMovShoot;
    private boolean baseReloadShoot;
    private int baseOnlyMov;
    private int baseGrabMov;

    private int adrenaline1Actions;
    private int adrenaline1MovShoot;
    private boolean adrenaline1ReloadShoot;
    private int adrenaline1OnlyMov;
    private int adrenaline1GrabMov;

    private int adrenaline2Actions;
    private int adrenaline2MovShoot;
    private boolean adrenaline2ReloadShoot;
    private int adrenaline2OnlyMov;
    private int adrenaline2GrabMov;

    private int normalFirstBlood;
    /**
     * points in normal board
     */
    private List<Integer> normalScores = new ArrayList<>();
    /**
     * actions available in final frenzy
     */
    private int frenzyAfterActions;
    private int frenzyAfterMovShoot;
    private boolean frenzyAfterReloadShoot;
    private int frenzyAfterOnlyMov;
    private int frenzyAfterGrabMov;

    private int frenzyBeforeActions;
    private int frenzyBeforeMovShoot;
    private boolean frenzyBeforeReloadShoot;
    private int frenzyBeforeOnlyMov;
    private int frenzyBeforeGrabMov;

    private int frenzyFirstBlood;
    /**
     * points in final frenzy board
     */
    private List<Integer> frenzyScores = new ArrayList<>();

    /**
     * parser for the boards and points boards parameters
     * @param fileName the file to parse
     */
    public BoardStructure(String fileName) {

        try (
             BufferedReader buf = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)))
        ) {

            String line = buf.readLine();
            while(line != null) {
                if (!line.isEmpty() && !line.startsWith("//")) {

                    String identifier;
                    identifier = line.substring(0, line.indexOf(':') + 2);
                    line = line.substring(line.indexOf(':') + 2);

                    String[] splitted = line.split("\\s");

                    switchSet(identifier, splitted);

                }
                line = buf.readLine();
            }

        }
        catch (IOException e) {
            System.out.println("Eccezione nella lettura del file dei punti -> " + e.getMessage());
        }
    }

    private void switchSet(String identifier, String [] splitted) {
        switch (identifier) {
            case "@SETNORMAL: ":
                baseActions = Integer.parseInt(splitted[0]);
                baseMovShoot = Integer.parseInt(splitted[1]);
                baseReloadShoot = Boolean.parseBoolean(splitted[2]);
                baseOnlyMov = Integer.parseInt(splitted[3]);
                baseGrabMov = Integer.parseInt(splitted[4]);
                break;

            case "@SETADRENALINE1: ":
                adrenaline1Actions = Integer.parseInt(splitted[0]);
                adrenaline1MovShoot = Integer.parseInt(splitted[1]);
                adrenaline1ReloadShoot = Boolean.parseBoolean(splitted[2]);
                adrenaline1OnlyMov = Integer.parseInt(splitted[3]);
                adrenaline1GrabMov = Integer.parseInt(splitted[4]);
                break;

            case "@SETADRENALINE2: ":
                adrenaline2Actions = Integer.parseInt(splitted[0]);
                adrenaline2MovShoot = Integer.parseInt(splitted[1]);
                adrenaline2ReloadShoot = Boolean.parseBoolean(splitted[2]);
                adrenaline2OnlyMov = Integer.parseInt(splitted[3]);
                adrenaline2GrabMov = Integer.parseInt(splitted[4]);
                break;

            case "@SETFRENZYBEFORE: ":
                frenzyBeforeActions = Integer.parseInt(splitted[0]);
                frenzyBeforeMovShoot = Integer.parseInt(splitted[1]);
                frenzyBeforeReloadShoot = Boolean.parseBoolean(splitted[2]);
                frenzyBeforeOnlyMov = Integer.parseInt(splitted[3]);
                frenzyBeforeGrabMov = Integer.parseInt(splitted[4]);
                break;

            case "@SETFRENZYAFTER: ":
                frenzyAfterActions = Integer.parseInt(splitted[0]);
                frenzyAfterMovShoot = Integer.parseInt(splitted[1]);
                frenzyAfterReloadShoot = Boolean.parseBoolean(splitted[2]);
                frenzyAfterOnlyMov = Integer.parseInt(splitted[3]);
                frenzyAfterGrabMov = Integer.parseInt(splitted[4]);
                break;

            case "@SETNORMALPOINTS: ":
                for (String s : splitted) {
                    normalScores.add(Integer.parseInt(s));
                }
                break;

            case "@SETFRENZYPOINTS: ":
                for (String s : splitted) {
                    frenzyScores.add(Integer.parseInt(s));
                }
                break;

            case "@SETNORMALFB: " :
                normalFirstBlood = Integer.parseInt(splitted[0]);
                break;

            case "@SETFRENZYFB: " :
                frenzyFirstBlood = Integer.parseInt(splitted[0]);
                break;

            default:
        }
    }

    int getBaseActions() {
        return baseActions;
    }

    int getBaseMovShoot() {
        return baseMovShoot;
    }

    boolean isBaseReloadShoot() {
        return baseReloadShoot;
    }

    int getBaseOnlyMov() {
        return baseOnlyMov;
    }

    int getBaseGrabMov() {
        return baseGrabMov;
    }

    int getAdrenaline1Actions() {
        return adrenaline1Actions;
    }

    int getAdrenaline1MovShoot() {
        return adrenaline1MovShoot;
    }

    boolean isAdrenaline1ReloadShoot() {
        return adrenaline1ReloadShoot;
    }

    int getAdrenaline1OnlyMov() {
        return adrenaline1OnlyMov;
    }

    int getAdrenaline1GrabMov() {
        return adrenaline1GrabMov;
    }

    int getAdrenaline2Actions() {
        return adrenaline2Actions;
    }

    int getAdrenaline2MovShoot() {
        return adrenaline2MovShoot;
    }

    boolean isAdrenaline2ReloadShoot() {
        return adrenaline2ReloadShoot;
    }

    int getAdrenaline2OnlyMov() {
        return adrenaline2OnlyMov;
    }

    int getAdrenaline2GrabMov() {
        return adrenaline2GrabMov;
    }

    int getNormalFirstBlood() {
        return normalFirstBlood;
    }

    List<Integer> getNormalScores() {
        return normalScores;
    }

    int getFrenzyAfterActions() {
        return frenzyAfterActions;
    }

    int getFrenzyAfterMovShoot() {
        return frenzyAfterMovShoot;
    }

    boolean isFrenzyAfterReloadShoot() {
        return frenzyAfterReloadShoot;
    }

    int getFrenzyAfterOnlyMov() {
        return frenzyAfterOnlyMov;
    }

    int getFrenzyAfterGrabMov() {
        return frenzyAfterGrabMov;
    }

    int getFrenzyBeforeActions() {
        return frenzyBeforeActions;
    }

    int getFrenzyBeforeMovShoot() {
        return frenzyBeforeMovShoot;
    }

    boolean isFrenzyBeforeReloadShoot() {
        return frenzyBeforeReloadShoot;
    }

    int getFrenzyBeforeOnlyMov() {
        return frenzyBeforeOnlyMov;
    }

    int getFrenzyBeforeGrabMov() {
        return frenzyBeforeGrabMov;
    }

    int getFrenzyFirstBlood() {
        return frenzyFirstBlood;
    }

    List<Integer> getFrenzyScores() {
        return frenzyScores;
    }
}
