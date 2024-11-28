package edu.curtin.models;

import java.util.List;

public class Obstacle {

    private List<Coordinate> coordinates;

    private List<String> requirements;

    public Obstacle (List<Coordinate> coordinates, List<String> requirements){
        this.coordinates = coordinates;
        this.requirements =requirements;

    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }
}
