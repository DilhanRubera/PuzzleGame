package edu.curtin.models;


import java.util.List;

public class Item {

    private String name;
    private List<Coordinate> coordinates;
    private String message;

    public Item(String name, List<Coordinate> coordinates, String message) {
        this.name = name;
        this.coordinates = coordinates;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
