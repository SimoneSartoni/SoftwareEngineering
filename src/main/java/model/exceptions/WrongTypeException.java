package model.exceptions;

public class WrongTypeException extends  GameException{
    String message;
    public WrongTypeException(String message){
        this.message=message;
    }

    public String toString(){
        return "error you can't use "+message;
    }
}

