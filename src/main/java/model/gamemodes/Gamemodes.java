package model.gamemodes;

import java.io.Serializable;

/**
 * this class contains all the possible gamemodes in a game
 */
public class Gamemodes implements Serializable {
    /**
     * final frenzy rules are applied: see game rules for more details
     */
    private GameMode finalFrenzyMode;
    /**
     * normal rules in a game: see game rules for more details
     */
    private GameMode normalMode;

    public Gamemodes(BoardStructure b){
        finalFrenzyMode=new FinalFrenzyGameMode(b);
        normalMode= new NormalGameMode(b);
    }

    public GameMode getFinalFrenzyMode() {
        return finalFrenzyMode;
    }



    public GameMode getNormalMode() {
        return normalMode;
    }

}
