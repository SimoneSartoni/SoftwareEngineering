package model.exceptions;

public class WrongDirectionException extends GameException {
    /**
     * Wrong Direction Exception empty constructor
     * */
    public WrongDirectionException() {
        //empty constructor
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error caused by the choice of a wrong Direction";
    }
}
