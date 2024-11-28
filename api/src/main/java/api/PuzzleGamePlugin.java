package api;

//Interface for plugins
public interface PuzzleGamePlugin {

    //Method to trigger when button pressed
    void onButtonPressed();

    //Method to trigger when player moves
    void onPlayerMove();

    //Method to trigger when resource acquired
    void onResourceAcquired(String resourceAcquired);
}