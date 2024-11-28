package edu.curtin.gameplugins;

import api.PuzzleGameAPI;
import api.PuzzleGamePlugin;

import edu.curtin.models.Coordinate;


public class Reveal implements PuzzleGamePlugin {

    //Instance of Game API to communicate with game
    private PuzzleGameAPI gameAPI;

    //Constructor
    public Reveal (PuzzleGameAPI gameAPI)
    {
        this.gameAPI =gameAPI;
    }
    private boolean revealed =false;

    @Override
    public void onButtonPressed() {
    }

    @Override
    public void onPlayerMove() {
    }

    @Override
    public void onResourceAcquired(String resourceAcquired) {
        //If resource acquired is the map
        if(resourceAcquired.equals("map")){
            //If map is already used
            if(!revealed){
                //Get all map coordinates and reveal them
                for (Coordinate coord : gameAPI.getItemMap().keySet()) {
                    gameAPI.revealLocation(coord.getRow(), coord.getCol());
                }

                // Get coordinates from the obstacle map and reveal them
                for (Coordinate coord : gameAPI.getObstacleMap().keySet()) {
                    gameAPI.revealLocation(coord.getRow(), coord.getCol());
                }

                //Reveal goal
                gameAPI.revealLocation(gameAPI.getGoalRow(), gameAPI.getGoalCol());
                revealed =true; //Set revealed to true
                gameAPI.addText("All locations revealed");
            }{
                gameAPI.addText("All locations already revealed");
            }
        }
    }
}