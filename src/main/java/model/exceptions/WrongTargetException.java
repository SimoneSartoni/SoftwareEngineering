package model.exceptions;

public class WrongTargetException extends GameException{
    /**
     * Wrong Target Exception empty constructor
     * */
    public WrongTargetException() {
        //empty constructor
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error caused by the choice of a wrong Target";
    }
}
