package model.exceptions;

public class WrongRoomException extends  GameException{
    /**
     * Wrong Room Exception empty constructor
     * */
    public WrongRoomException() {
        //empty constructor
    }

    /**
     * Override toString to print the error causing the exception
     */
    @Override
    public String toString() {
        return "Error caused by the choice of a wrong Room";
    }
}
