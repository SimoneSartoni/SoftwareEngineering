package model.weapon;

import model.enums.ForcedMovement;
import model.enums.LoadedState;
import model.utility.Ammo;
import model.utility.Visibility;
import model.weapon.actions.Action;
import model.weapon.actions.DamageAction;
import model.weapon.actions.MarkAction;
import model.weapon.actions.MovementAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class InitWeapons {
    private static Action action;
    private static List<Action> actions = new ArrayList<>();
    private static String[] splitted;

    private InitWeapons() {

    }

    /**
     * this methods loads all the Weapon used in game from the corresponding txt file
     * @param fileName the file to parse
     * @return the List of weapon parsed
     */
    public static List<Weapon> initAllWeapons(String fileName) {
        splitted = new String[15];
        List<Weapon> weapons = new ArrayList<>();
        List<Effect> effects = new ArrayList<>();
        Weapon weapon = new Weapon();
        Visibility visibility = new Visibility();
        Effect effect = new Effect();
        actions = new ArrayList<>();
        Logger logger = Logger.getAnonymousLogger();

        try (
                BufferedReader buf = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)))
            ) {
            String line = buf.readLine();
            while (line != null) {
                if (!line.matches("")) {
                    String identifier;
                    identifier = line.substring(0, line.indexOf(':') + 2);
                    line = line.substring(line.indexOf(':') + 2);
                    splitted = line.split("\\s");

                    switch (identifier) {
                        case "@W: ":
                            weapon = new Weapon(Integer.parseInt(splitted[0]),
                                    new Ammo(Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3])),
                                    new Ammo(Integer.parseInt(splitted[4]), Integer.parseInt(splitted[5]), Integer.parseInt(splitted[6])),
                                    splitted[7], LoadedState.PARTIALLY_LOADED);
                            break;
                        case "@A: ":
                            addAction();
                            break;
                        case "@V: ":
                            visibility = addVisibility();
                            break;
                        case "@EB: ":
                            effect = addBaseEffect(weapon, visibility);
                            weapon.setBaseEffect(effect);
                            break;
                        case "@EO: ":
                            effect = addOptionalEffect(visibility);
                            effects.add(effect);
                            break;
                        case "@EA: ":
                            effect = addAlternativeEffect(visibility);
                            weapon.setAlternativeEffect(effect);
                            break;
                        case "@SETOPTIONAL: ":
                            weapon.setOptionalEffect(effects);
                            break;
                        case "@SETCHAIN: ":
                            addChain(weapon);
                            break;
                        case "@SETCHAINACTION: ":
                            effect.setChainedToAction(Integer.parseInt(splitted[0]));
                            break;
                        case "@SETLINKEDTONEXT: ":
                            addLinkedToNext(weapon);
                            break;
                        case "@SETLINKEDEFFECT: ":
                            addLinkedEffect(weapon, effect);
                            break;
                        case "@SETHITINTHEEFFECT: ":
                            addHitInTheEffect(weapon, effect);
                            break;
                        case "@SETWEAPON: ":
                            weapons.add(weapon);
                            break;
                        case "@CLEARACTIONS: ":
                            actions.clear();
                            break;
                        case "@CLEAREFFECTS: ":
                            effects.clear();
                            break;
                        default:
                            break;
                    }
                }
                line = buf.readLine();
            }
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "IO exception throwed", e);
        }

        return weapons;
    }

    private static void addHitInTheEffect(Weapon weapon, Effect effect) {
        switch (splitted[0]) {
            case "EB":
                effect.getActions().get(Integer.parseInt(splitted[1])).setHitInTheEffect(weapon.getBaseEffect());
                break;
            case "EA":
                effect.getActions().get(Integer.parseInt(splitted[1])).setHitInTheEffect(weapon.getAlternativeEffect());
                break;
            case "EO":
                effect.getActions().get(Integer.parseInt(splitted[2])).setHitInTheEffect(weapon.getOptionalEffect().get(Integer.parseInt(splitted[1])));
                break;
            default:
                break;
        }
    }

    private static void addLinkedEffect(Weapon weapon, Effect effect) {
        switch (splitted[0]) {
            case "EB":
                weapon.getBaseEffect().getActions().get(Integer.parseInt(splitted[1])).setLinkedEffect(weapon.getOptionalEffect().get(Integer.parseInt(splitted[2])));
                break;
            case "EA":
                weapon.getAlternativeEffect().getActions().get(Integer.parseInt(splitted[1])).setLinkedEffect(weapon.getOptionalEffect().get(Integer.parseInt(splitted[2])));
                break;
            case "EO":
                effect.getActions().get(Integer.parseInt(splitted[1])).setLinkedEffect(weapon.getOptionalEffect().get(Integer.parseInt(splitted[2])));
                break;
            default:
                break;
        }
    }

    private static void addLinkedToNext(Weapon weapon) {
        switch (splitted[0]) {
            case "EB":
                weapon.getBaseEffect().getActions().get(Integer.parseInt(splitted[1])).setLinkedToNext(
                        weapon.getBaseEffect().getActions().get(Integer.parseInt(splitted[2])));
                break;
            case "EA":
                weapon.getAlternativeEffect().getActions().get(Integer.parseInt(splitted[1])).setLinkedToNext(
                        weapon.getAlternativeEffect().getActions().get(Integer.parseInt(splitted[2])));
                break;
            case "EO":
                weapon.getOptionalEffect().get(Integer.parseInt(splitted[1])).getActions().get(Integer.parseInt(splitted[2])).setLinkedToNext(
                        weapon.getOptionalEffect().get(Integer.parseInt(splitted[1])).getActions().get(Integer.parseInt(splitted[3]))
                );
                break;
            default:
                break;
        }
    }

    private static void addChain(Weapon weapon) {
        int chained = Integer.parseInt(splitted[0]);
        int chain = Integer.parseInt(splitted[2]);
        if (splitted[1].matches("b"))
            weapon.getOptionalEffect().get(chained).setChainedTo(weapon.getBaseEffect());
        else
            weapon.getOptionalEffect().get(chained).setChainedTo(weapon.getOptionalEffect().get(chain));
    }

    private static Effect addAlternativeEffect(Visibility visibility) {
        Effect effect;
        effect = new Effect(splitted[0], false, actions, Boolean.parseBoolean(splitted[1]),
                Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]), null, visibility,
                new Ammo(Integer.parseInt(splitted[4]), Integer.parseInt(splitted[5]), Integer.parseInt(splitted[6])),
                -1, Boolean.parseBoolean(splitted[7]), Boolean.parseBoolean(splitted[8]),false);
        effect.setCanBeUsed(true);
        return effect;
    }

    private static Effect addOptionalEffect(Visibility visibility) {
        Effect effect;
        effect = new Effect(splitted[0], false, actions, Boolean.parseBoolean(splitted[1]),
                Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]), null, visibility,
                new Ammo(Integer.parseInt(splitted[4]), Integer.parseInt(splitted[5]), Integer.parseInt(splitted[6])),
                -1, Boolean.parseBoolean(splitted[7]), Boolean.parseBoolean(splitted[8]),Boolean.parseBoolean(splitted[9]));
        effect.setCanBeUsed(Boolean.parseBoolean(splitted[7]));
        return effect;
    }

    private static Effect addBaseEffect(Weapon weapon, Visibility visibility) {
        Effect effect;
        String name;
        if (weapon!= null && weapon.getTypeEffect() == 2)
            name = "basic_mode";
        else
            name = "basic_effect";
        effect = new Effect(name, false, actions, Boolean.parseBoolean(splitted[0]),
                Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), null, visibility,
                new Ammo(Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]), Integer.parseInt(splitted[5])),
                -1, Boolean.parseBoolean(splitted[6]), Boolean.parseBoolean(splitted[7]),false);
        effect.setCanBeUsed(true);
        return effect;
    }

    private static Visibility addVisibility() {
        int minEverywhere = 0;
        int maxEverywhere = 0;
        if(splitted.length >= 12) {
            minEverywhere = Integer.parseInt(splitted[11]);
            maxEverywhere = Integer.parseInt(splitted[12]);
        }
        return new Visibility(Boolean.parseBoolean(splitted[0]), Boolean.parseBoolean(splitted[1]),
                Boolean.parseBoolean(splitted[2]), Boolean.parseBoolean(splitted[3]),
                Boolean.parseBoolean(splitted[4]), Boolean.parseBoolean(splitted[5]),
                Boolean.parseBoolean(splitted[6]), Boolean.parseBoolean(splitted[7]),Boolean.parseBoolean(splitted[8]),Integer.parseInt(splitted[9]), Integer.parseInt(splitted[10]),
                maxEverywhere, minEverywhere);
    }

    private static void addAction() {
        switch (Integer.parseInt(splitted[0])) {
            case 0:
                addDamageAction();
                break;
            case 1:
                addMarkAction();
                break;
            case 2:
                if(splitted[4].matches("FTP"))
                    addMovementAction(ForcedMovement.FORCED_TO_PLAYER);
                else
                    addMovementAction(ForcedMovement.NOT_RESTRICTED);
                break;
            default:
                break;
        }
    }

    private static void addDamageAction() {
        action = new DamageAction(Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]),
                Boolean.parseBoolean(splitted[3]),Integer.parseInt(splitted[4]), Integer.parseInt(splitted[5]),
                null, null, null,Boolean.parseBoolean(splitted[6]));
        actions.add(action);
    }

    private static void addMarkAction() {
        action = new MarkAction(Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]),
                Boolean.parseBoolean(splitted[3]), Integer.parseInt(splitted[4]), Integer.parseInt(splitted[5]),
                null, null, null,Boolean.parseBoolean(splitted[6]));
        actions.add(action);
    }

    private static void addMovementAction(ForcedMovement forcedMovement) {
        action = new MovementAction(Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]),
                Boolean.parseBoolean(splitted[3]), forcedMovement, null,
                null,null,Boolean.parseBoolean(splitted[5]) );
        actions.add(action);
    }
}
