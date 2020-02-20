package model.exceptions;

public class PowerUpException extends GameException{
    private String errorString;

    /**
     * PowerUp Exception empty constructor
     * */
    public PowerUpException(String string) {
        this.errorString = string;
        //empty constructor
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error caused by no available PowerUps: "+errorString;
    }
}
