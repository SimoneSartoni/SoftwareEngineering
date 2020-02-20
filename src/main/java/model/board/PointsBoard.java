package model.board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class  PointsBoard implements Serializable {
    private List<Integer> points;
    private int pointsFirstBlood;

    public PointsBoard(List <Integer> points, int pointsFirstBlood){
        this.points=new ArrayList<>();
        this.points.addAll(points);
        this.pointsFirstBlood=pointsFirstBlood;
    }

    public List<Integer> getPoints() {
        return points;
    }

    public int getPointsFirstBlood() {
        return pointsFirstBlood;
    }
}
