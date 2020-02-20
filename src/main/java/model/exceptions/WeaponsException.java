package model.exceptions;

public class WeaponsException extends GameException{
    private String errorString;

    /**
     * PowerUp Exception empty constructor
     * */
    public WeaponsException(String errorString) {
        this.errorString = errorString;
        //empty constructor
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error linked to weapons: "+errorString;
    }
}
