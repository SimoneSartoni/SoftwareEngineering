package model.utility;

import java.io.Serializable;

public class Ammo implements Serializable{

    /**
     * this is the generic class to indicate Ammos
     * the cost is -(quantity) (for every type of Ammo) if you can choose any type you want (and any quantity)
     * in order to achieve the quantity of Ammos requested
     */
    private int redValue;
    private int blueValue;
    private int yellowValue;

    public Ammo(int redValue, int blueValue, int yellowValue) {
        this.redValue = redValue;
        this.blueValue = blueValue;
        this.yellowValue = yellowValue;
    }

    public void setRedValue(int redValue){
        this.redValue = redValue;
    }

    public void setBlueValue(int blueValue){
        this.blueValue = blueValue;
    }

    public void setYellowValue(int yellowValue) {
        this.yellowValue = yellowValue;
    }

    public int getRedValue() {
        return redValue;
    }

    public int getBlueValue() {
        return blueValue;
    }

    public int getYellowValue() {
        return yellowValue;
    }

    /**
     * return true if the parameter can be paid or not
     * @param ammo the ammo cost we want to check
     * @return true if can be paid, false otherwise
     */
    public boolean hasCorrectCost(Ammo ammo) {
        if((ammo.getRedValue()<0)&&(ammo.getYellowValue()<0)&&(ammo.getBlueValue()<0))
            return redValue+yellowValue+blueValue>=-ammo.getRedValue();
        return redValue >= ammo.getRedValue() && blueValue >= ammo.getBlueValue() && yellowValue >= ammo.getYellowValue();
    }

    @Override
    public String toString() {
        return "Ammo[R: " + redValue + ", B: " + blueValue + ", Y: " + yellowValue + "]";
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(!(o instanceof Ammo)) {
            return false;
        }
        Ammo ammo = (Ammo)o;
        return ammo.getRedValue() == redValue && ammo.getBlueValue() == blueValue && ammo.getYellowValue() == yellowValue;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}