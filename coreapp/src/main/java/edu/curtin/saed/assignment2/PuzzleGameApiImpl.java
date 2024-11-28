package edu.curtin.saed.assignment2;

import api.PuzzleGameAPI;
import edu.curtin.models.Coordinate;
import edu.curtin.models.Obstacle;
import edu.curtin.models.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.python.core.PyObject;

public class PuzzleGameApiImpl implements PuzzleGameAPI {

    //Instance of app
    private  App app;
    private PyObject onItemAcquiredCallback;     // Python callback for item acquired
    private PyObject onObstacleTraversedCallback; //Python callback for on obstacle traversed

    //Constructor
    public PuzzleGameApiImpl(App app){
        this.app =app;

    }
    //Get number of grid rows
    @Override
    public int getGridRow() {
        return app.gridRow;
    }

    //Get number of grid columns
    @Override
    public int getGridCol() {
        return app.gridCol;
    }

    //Get the player's current row position
    @Override
    public int getPlayerRow() { return app.playerRow;  }

    //Get the player's current column position
    @Override
    public int getPlayerColumn() {
        return app.playerCol;
    }

    //Move player
    @Override
    public void movePlayer(int newRow, int newColumn) {
        app.movePlayerIcon(newRow,newColumn);
    }

    //Add an item to the player's inventory
    @Override
    public void addItemToInventory(String item) { app.inventory.add(item); }

    //Check if inventory has item
    @Override
    public boolean hasItemInInventory(String item) {
        return false;
    }

    //Add text
    @Override
    public void addText(String text) {
        app.textArea.appendText(text+"\n");
    }

    //Add obstacle
    @Override
    public void addObstacle(int row, int col,String obstacleRequirement) {
        Coordinate coordinate = new Coordinate(row,col);
        List<Coordinate> coordinateList= new ArrayList<>();
        coordinateList.add(coordinate);
        List<String> requirementList= new ArrayList<>();
        requirementList.add(obstacleRequirement);
        Obstacle obstacle = new Obstacle(coordinateList,requirementList);
        app.obstacleMap.put(coordinate,obstacle);
        app.addObstacleIcon(row,col);
    }

    //Check if obstacle exists
    @Override
    public boolean doesObstacleExist(int row, int col) {
        Coordinate coordinate = new Coordinate(row, col);
        if (app.obstacleMap.containsKey(coordinate)){
            return true;
        }
        return false;
    }

    //Get obstacle map
    @Override
    public Map<Coordinate,Obstacle> getObstacleMap(){
        return app.obstacleMap;
    }

    //Get item map
    @Override
    public Map<Coordinate,Item> getItemMap(){ return app.itemMap; }

    //Reveal an obstacle or items location
    @Override
    public void revealLocation(int row, int col){ app.revealLocation(row,col); }

    //set item acquired callback
    @Override
    public void setOnItemAcquiredCallback(PyObject callback) {
        this.onItemAcquiredCallback = callback;
    }

    //set on obstacle traversed callback
    @Override
    public void setOnObstacleTraversedCallback(PyObject callback) {
        this.onObstacleTraversedCallback = callback;
    }

    // Trigger item acquired callback
    @Override
    public void triggerItemAcquiredCallback() {
        if (onItemAcquiredCallback != null) {
            onItemAcquiredCallback.__call__();
        }
    }

    // Trigger obstacle traversed callback
    @Override
    public void triggerObstacleTraversedCallback() {
        if (onObstacleTraversedCallback != null) {
            onObstacleTraversedCallback.__call__();
        }
    }

    //Get goal column position
    @Override
    public int getGoalCol(){
       return app.goalCol;
    }

    //Get goal row position
    @Override
    public int getGoalRow(){
        return app.goalRow;
    }

}
