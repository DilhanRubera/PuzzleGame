package api;

import edu.curtin.models.Coordinate;
import edu.curtin.models.Item;
import edu.curtin.models.Obstacle;
import org.python.core.PyObject;

import java.util.Map;

//Interface for plugins to communicate with game
public interface PuzzleGameAPI {

    int getGridRow(); //Get number of grid rows
    int getGridCol(); //Get number of grid columns
    int getPlayerRow(); // Get the player's current row position
    int getPlayerColumn(); // Get the player's current column position
    int getGoalRow(); //Get goal row position
    int getGoalCol(); //Get goal column position
    void movePlayer(int newRow, int newColumn); //Move player
    void addItemToInventory(String item); // Add an item to the player's inventory
    boolean hasItemInInventory(String item); //Check if inventory has item
    void addText(String text); //Add text
    void addObstacle (int row,int col,String obstacleRequirement); //Add obstacle
    boolean doesObstacleExist(int row, int col); //Check if obstacle exists
    Map<Coordinate, Obstacle> getObstacleMap(); //Get obstacle map
    Map<Coordinate, Item> getItemMap(); //Get item map
    void revealLocation(int row,int col); //Reveal an obstacle or items location

    void triggerItemAcquiredCallback(); //Trigger item acquired callback
    void triggerObstacleTraversedCallback(); //Trigger on obstacle traversed callback

    void setOnItemAcquiredCallback(PyObject callback); //Set on item acquired call back
    void setOnObstacleTraversedCallback(PyObject callback); //Set on obstacle traversed callback

}