package edu.curtin.gameplugins;

import api.PuzzleGameAPI;
import api.PuzzleGamePlugin;
public class Penalty implements PuzzleGamePlugin {

    //Instance of Game API to communicate with game
    private PuzzleGameAPI gameAPI;
    private long timeOfLastMove; //Time of last move

    //Constructor
    public Penalty (PuzzleGameAPI gameAPI){
        this.gameAPI =gameAPI;
        this.timeOfLastMove = System.currentTimeMillis();

    }

    @Override
    public void onButtonPressed() {
    }

    @Override
    public void onPlayerMove() {
        long currentMoveTime = System.currentTimeMillis();
        boolean obstacleLocFound = false;

        //If move was 5 seconds later than previous move
        if(currentMoveTime-timeOfLastMove>5000){
            System.out.println("pentaly difference"+(currentMoveTime-timeOfLastMove));
            int playerRow = gameAPI.getPlayerRow();
            int playerCol = gameAPI.getPlayerColumn();
            int gridRow = gameAPI.getGridRow();
            int gridCol = gameAPI.getGridCol();

            //Getting possible positions for the obstacle
            int[][] possiblePositions = {
                    {playerRow + 1, playerCol},    // Down
                    {playerRow - 1, playerCol},    // Up
                    {playerRow, playerCol + 1},    // Right
                    {playerRow, playerCol - 1}     // Left
            };

            // Check each position to find a valid one within grid boundaries
            for (int[] position : possiblePositions) {
                int row = position[0];
                int col = position[1];

                // Ensure the position is within the grid boundaries
                if (row >= 0 && row < gridRow && col >= 0 && col < gridCol) {

                    //If obstacle doesn't exist there create one
                    if(!gameAPI.doesObstacleExist(row, col)){
                        gameAPI.addText("Penalty obstacle created at (" + row + ", " + col + ")");

                        //Obstacle removes last added item in inventory
                        gameAPI.addObstacle(row,col,"Remove Last");
                        obstacleLocFound =true;
                        break;
                    }
                }
            }
            //Display this if no position is available for the penalty obstacle
            if(!obstacleLocFound){
                gameAPI.addText("Unable to add penalty obstacle as adjacent grid squares are full");

            }
        }
        timeOfLastMove= System.currentTimeMillis();
    }

    @Override
    public void onResourceAcquired(String resourceAcquired) {
    }
}