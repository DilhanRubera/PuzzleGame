package edu.curtin.gameplugins;

import api.PuzzleGameAPI;
import api.PuzzleGamePlugin;

import java.util.Random;

public class Teleport implements PuzzleGamePlugin {

    //Instance of Game API to communicate with game
    private PuzzleGameAPI gameAPI;

    private boolean teleported = false;

    public Teleport(PuzzleGameAPI gameAPI){
        this.gameAPI = gameAPI;
    }

    @Override
    public void onButtonPressed() {
        gameAPI.addText("Teleport Method called");

        //Check if already teleported
        if(!teleported){
            Random random = new Random();
            //Teleport player to random location within the grid
            int newCol = random.nextInt(gameAPI.getGridCol());
            int newRow = random.nextInt(gameAPI.getGridRow());
            //Move player
            gameAPI.movePlayer(newRow,newCol);
            gameAPI.addText("Teleported to "+newRow+ newCol);

            teleported =true;
        }else{
            gameAPI.addText("Teleport option already used");

        }
    }

    @Override
    public void onPlayerMove() {
    }

    @Override
    public void onResourceAcquired(String resourceAcquired) {
    }
}
