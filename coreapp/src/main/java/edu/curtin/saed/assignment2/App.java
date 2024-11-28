package edu.curtin.saed.assignment2;

import api.PuzzleGameAPI;
import api.PuzzleGamePlugin;
import edu.curtin.models.Coordinate;
import edu.curtin.models.Item;
import edu.curtin.models.Obstacle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
//import jjparse.GameParser;


public class App extends Application
{
    public GridArea area;
    public TextArea textArea; //Text area for notifications
    public Map<Coordinate, Item> itemMap ; //Map to store items and their coordinates

    public Map<Coordinate, Obstacle> obstacleMap ; //Map to store obstacles and their coordinates

    static private List<String> pluginsToLoad; //List of plugins to load

    static private List<PuzzleGamePlugin> loadedPlugins; //List of loaded plugins
    public List<String> inventory; //Inventory list

    public int gridRow; //Number of rows in the grid
    public int gridCol; //Number of columns in the grid
    public int playerRow; //Players row
    public int playerCol; //Players column
    public int goalCol; //Goal column
    public int goalRow; //Goal row

    private PuzzleGameApiImpl puzzleGameApiImpl ; // Instance of API Implementation
    private TextField localeInputField; //Input field to enter locale
    private Button changeLocaleButton; //Button to change locale
    private List<String> messageKeys; // Stores keys for messages added to TextArea
    private ResourceBundle messages; //Resource bundle for locale
    private Locale currentLocale; //Current locale
    private Button moveUpBtn; //Button to move up
    private Button moveDownBtn ; //Button to move down
    private Button moveLeftBtn ; //Button to move left
    private Button moveRightBtn ; //Button to move right
    private LocalDate gameDate; //Game date
    private Label dateLabel; //Date label
    private TextArea topTextArea; //Text area for inventory
    private int moveCount; //Number of player moves
    private Label moveCountLabel; //Label for player moves
    public static void main(String[] args)
    {
        launch(args);

    }

    @Override
    public void start(Stage stage)
    {

        //Loading default locale
        this.currentLocale = Locale.forLanguageTag("en");
        loadResourceBundle();

        //Initializing the message keys
        messageKeys =new ArrayList<>();

        //Getting file name
        List<String> args = getParameters().getRaw();

        if (args.isEmpty()) {
            System.out.println("No file specified. Please provide the input file as a command-line argument.");
            return;
        }

        // Get the first argument as the file path
        String filename = args.get(0);

        //Parsing file
        parseInputFile(filename);

        //Initializing grid rows and columns
        gridCol = GameParser.gridCols;
        gridRow = GameParser.gridRows;
        area = new GridArea(GameParser.gridCols+1,GameParser.gridRows+1);
        area.setStyle("-fx-background-color: #FFFFFF;");

        //Initializing API implementation instance
        puzzleGameApiImpl = new PuzzleGameApiImpl(this);

        //Initializing plugins to load list and loaded plugins list
        pluginsToLoad = new ArrayList<>();
        loadedPlugins =new ArrayList<>();

        //Get the unique plugins
        addUniquePlugins(GameParser.plugins);

        //Loading plugins
        loadPlugins();

        //Run script
        runScript(GameParser.pythonScript);

        //Initialize inventory list
        inventory= new ArrayList<>();

        //Getting goal row and column
        goalCol =GameParser.goalCol;
        goalRow =GameParser.goalRow;

        //Displaying goal
        area.getIcons().add(new GridAreaIcon(
                goalCol+0.5,goalRow+0.5, 0, 1,
                App.class.getClassLoader().getResourceAsStream("goal.png"),
                "","goal"));

        //Displaying player
        area.getIcons().add(new GridAreaIcon(
                 GameParser.startCol+0.5,GameParser.startRow+0.5, 0, 1,
                App.class.getClassLoader().getResourceAsStream("player.png"),
                "","player"));


        //Initializing items map
        itemMap =new HashMap<>();

        //Initializing obstacles map
        obstacleMap = new HashMap<>();

        //Displaying items
        for (Item item : GameParser.items) {
            for (Coordinate coord : item.getCoordinates()) {
                itemMap.put(coord, item);
                area.getIcons().add(new GridAreaIcon(
                        coord.getCol() + 0.5,
                        coord.getRow() + 0.5,
                        0,
                        1.0,
                        App.class.getClassLoader().getResourceAsStream("resource.png"),
                       "", "resource"
                ));
            }
        }

        //Displaying obstacles
        for (Obstacle obstacle : GameParser.obstacles) {
            for (Coordinate coord : obstacle.getCoordinates()) {
                obstacleMap.put(coord, obstacle);
                area.getIcons().add(new GridAreaIcon(
                        coord.getCol() + 0.5,
                        coord.getRow() + 0.5,
                        0,
                        1.0,
                        App.class.getClassLoader().getResourceAsStream("obstacle.png"),
                        "","obstacle"
                ));
            }
        }

        //Getting players starting location
        int startX = GameParser.startCol;
        int startY = GameParser.startRow;

        for (int row = 0; row < GameParser.gridRows; row++) {
            for (int col = 0; col < GameParser.gridCols; col++) {
                boolean isVisible = false;

                //Keep the players location and adjacent grid cells visible
                if ((row == startY && col == startX) ||
                        (row==startY-1 && col==startX) || (row==startY+1 && col==startX)||
                        (row==startY && col==startX-1)|| (row==startY && col==startX+1)
                )
                {
                    isVisible = true;
                }

                //Cover all grid cells except players location and adjacent grid cells
                if (!isVisible) {
                    area.getIcons().add(new GridAreaIcon(
                            col+0.5, row+0.5, 0, 1.0,
                            App.class.getClassLoader().getResourceAsStream("black.png"),
                            "","blackSquare"));
                }
            }
        }


        //Define buttons
        moveUpBtn = new Button(getMessage("button.up"));
        moveDownBtn = new Button(getMessage("button.down"));
        moveLeftBtn = new Button(getMessage("button.left"));
        moveRightBtn = new Button(getMessage("button.right"));

        //Get players location
        playerRow = GameParser.startRow;
        playerCol = GameParser.startCol;

        //Set on action methods when button is pressed
        //Move the player to target location
        moveUpBtn.setOnAction(event -> movePlayer(playerRow - 1, playerCol));   // Move up
        moveDownBtn.setOnAction(event -> movePlayer(playerRow + 1, playerCol)); // Move down
        moveLeftBtn.setOnAction(event -> movePlayer(playerRow, playerCol - 1)); // Move left
        moveRightBtn.setOnAction(event -> movePlayer(playerRow, playerCol + 1));// Move right

        //Initialize notifications text area
        this.textArea = new TextArea();
        appendMessage(textArea, "notification.title");

        //Initialize text fields and buttons to receive and change locale
        localeInputField = new TextField();
        localeInputField.setPromptText(getMessage("locale.prompt"));
        changeLocaleButton = new Button(getMessage("button.changeLocale"));
        changeLocaleButton.setOnAction(e -> handleChangeLocale());

        //Set date
        gameDate = LocalDate.now();
        dateLabel = new Label(getMessage("date.label") + gameDate.toString());

        //Set move count
        moveCountLabel =new Label(getMessage("move.count.label")+moveCount);

        //Initialize toolbar
        var toolbar = new ToolBar();

        //Set toolbar if plugin is present
        if (pluginsToLoad.contains("edu.curtin.gameplugins.Teleport")) {
            var teleportBtn = new Button("Teleport");
            teleportBtn.setOnAction(event -> triggerPluginsOnButtonPressed());
            toolbar.getItems().addAll(moveUpBtn, moveDownBtn, moveLeftBtn, moveRightBtn ,new Separator(),localeInputField,changeLocaleButton,new Separator(),dateLabel,new Separator(),moveCountLabel,new Separator(),teleportBtn);

        }else{
            //Set toolbar if plugin isn't present
            toolbar.getItems().addAll(moveUpBtn, moveDownBtn, moveLeftBtn, moveRightBtn ,new Separator(),localeInputField,changeLocaleButton,new Separator(),dateLabel,new Separator(),moveCountLabel);
        }

        //Initialize inventory text area
         topTextArea = new TextArea();
         topTextArea.appendText("Inventory\n");

         //Set constraints for text areas
        VBox textAreaContainer = new VBox();
        textAreaContainer.getChildren().addAll( topTextArea,textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        VBox.setVgrow(topTextArea, Priority.ALWAYS);

        //Set split plane
        var splitPane = new SplitPane();
        splitPane.getItems().addAll(area, textAreaContainer);
        splitPane.setDividerPositions(0.75);

        //Set content pane
        stage.setTitle(getMessage("title"));
        var contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);
        var scene = new Scene(contentPane, 1200, 1000);

        //Display stage
        stage.setScene(scene);
        stage.show();
    }

    //Method to move player to target location
    private void movePlayer(int targetRow, int targetCol){
        //Block player if he tries to move outside grid
        if (targetRow < 0 || targetRow >= GameParser.gridRows || targetCol < 0 || targetCol >= GameParser.gridCols) {
            appendMessage(textArea, "grid.boundary.error");
            return;
        }

        //Create coordinate for target location
        Coordinate targetCoordinate = new Coordinate(targetRow, targetCol);

        //Check if obstacle is present at coordinate
        if(obstacleMap.containsKey(targetCoordinate)){
            System.out.println("Obstacle map contains coordinate");

            //Get obstacle and it's requirements
            Obstacle obstacle = obstacleMap.get(targetCoordinate);
            List<String> requiredItems = obstacle.getRequirements();

            //Check if inventory has required items
            if (hasItemsInInventory(requiredItems)) {
                for (String item :requiredItems){
                    if(requiredItems.contains("Remove Last")){
                        if(!inventory.isEmpty()){
                            inventory.removeLast();
                            appendMessage(textArea, "penalty.item.removed");

                        }{
                            appendMessage(textArea, "penalty.item.removed");
                        }

                    }else{
                        //Remove item if inventory has item
                        System.out.println("You have this required item to move here"+item);
                        removeItemFromInventory(item);
                        appendMessage(textArea, "item.has.required");
                    }
                    //Trigger obstacle traversed callback
                    puzzleGameApiImpl.triggerObstacleTraversedCallback();

                }
                //Update inventory
                updateInventoryDisplay();

                //Remove obstacle from map
                removeObstacleIcon(targetRow,targetCol);
                obstacleMap.remove(targetCoordinate);

            }else
            {
                //Display required items needed if inventory doesn't have them
                appendMessage(textArea, "item.required");
                for(String item: requiredItems){
                    textArea.appendText(item + "\n");
                    System.out.println(item);
                }
                //Block player from moving since inventory doesn't have required items
                return;
            }

        }

        //Check if target location contains item
        if (itemMap.containsKey(targetCoordinate)) {
            Item item = itemMap.get(targetCoordinate);
            appendMessage(textArea, "item.found");
            System.out.println("You found and item "+item.getName()+ item.getMessage());

            //Trigger plugins on resource acquired method
            triggerPluginsOnResourceAcquired(item.getName());
            inventory.add(item.getName()); // Add item to player's inventory

            //Trigger scripts on item acquired callback
            puzzleGameApiImpl.triggerItemAcquiredCallback();

            itemMap.remove(targetCoordinate); // Remove the item from the map
            removeItemIcon(targetRow,targetCol);

            updateInventoryDisplay(); //Update inventory
        }

        movePlayerIcon(targetRow, targetCol); //Move player
        Platform.runLater(() -> appendMessage(textArea, "player.moved"));
        triggerPluginsOnPlayerMove(); //Trigger plugins on player move method
        incrementDate(); //Increment date

        //Display alert if player reaches goal
        if(targetRow==goalRow && targetCol ==goalCol){
            showWinAlert();
        }
    }


    //Method to alert player when he reaches the goal
    private void showWinAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations");
        alert.setHeaderText(null);
        alert.setContentText("You won!");
        alert.show();
    }

    //Method to check if inventory has obstacles required items
    private Boolean hasItemsInInventory(List<String> requiredItems) {
        for (String requiredItem : requiredItems) {
           if(!inventory.isEmpty()) {
               boolean hasItem = inventory.stream()
                       .anyMatch(playerItem -> normalizedEquals(playerItem, requiredItem));
               if (!hasItem && (!requiredItems.contains("Remove Last"))) {
                   System.out.println("Required item missing: " + requiredItem);
                   return false;
               }
           }
        }
        return true;
    }

    //Helper method to compare required items via Unicode compatibility normalization
    private boolean normalizedEquals(String str1, String str2) {
        String normalizedStr1 = Normalizer.normalize(str1, Normalizer.Form.NFKC);
        String normalizedStr2 = Normalizer.normalize(str2, Normalizer.Form.NFKC);
        return normalizedStr1.equalsIgnoreCase(normalizedStr2);
    }

    //Method to remove item from inventory
    private void removeItemFromInventory(String item) {
        inventory.remove(item);
    }

    //Method to move player icon
    public void movePlayerIcon(int targetRow, int targetCol) {
        Platform.runLater(() -> {

            int[][] adjacentCells = {
                    {targetRow, targetCol},       // Player's position
                    {targetRow - 1, targetCol},   // Above
                    {targetRow + 1, targetCol},   // Below
                    {targetRow, targetCol - 1},   // Left
                    {targetRow, targetCol + 1}    // Right
            };

            for (int[] cell : adjacentCells) {
                int row = cell[0];
                int col = cell[1];

                // Check if the cell is within the grid bounds
                if (row >= 0 && row < GameParser.gridRows && col >= 0 && col < GameParser.gridCols) {
                    // Find and remove the black square icon at this position
                    GridAreaIcon blackSquare = area.getIcons().stream()
                            .filter(icon -> icon.getX() == col + 0.5 && icon.getY() == row + 0.5 && "blackSquare".equals(icon.getName()))
                            .findFirst()
                            .orElse(null);

                    if (blackSquare != null) {
                        area.getIcons().remove(blackSquare);
                        area.requestLayout();// Remove the black square
                    }
                }
            }

            // Find the player's icon
            GridAreaIcon playerIcon = area.getIcons().stream()
                    .filter(icon -> "player".equals(icon.getName()))
                    .findFirst()
                    .orElse(null);

            //Update players icon
            if (playerIcon != null) {
                playerIcon.setPosition(targetCol+0.5,targetRow+0.5);
                area.requestLayout();
            }
        });

        //Update players new position
        playerRow =targetRow;
        playerCol=targetCol;
    }

    //Helper method to remove obstacle icon
    private void removeObstacleIcon(int targetRow, int targetCol){
        Platform.runLater(()->{
            GridAreaIcon obstacleIcon = area.getIcons().stream()
                    .filter(icon -> icon.getX() == targetCol + 0.5 && icon.getY() == targetRow + 0.5 && "obstacle".equals(icon.getName()))
                    .findFirst()
                    .orElse(null);
            if (obstacleIcon != null) {
                area.getIcons().remove(obstacleIcon);
                area.requestLayout();
            }
        });
    }

    //Helper method to remove item icon
    private void removeItemIcon(int targetRow, int targetCol){
        Platform.runLater(()->{
            GridAreaIcon resourceIcon = area.getIcons().stream()
                    .filter(icon -> icon.getX() == targetCol + 0.5 && icon.getY() == targetRow + 0.5 && "resource".equals(icon.getName()))
                    .findFirst()
                    .orElse(null);
            if (resourceIcon != null) {
                area.getIcons().remove(resourceIcon);
                area.requestLayout();
            }
        });
    }

    //Method to parse input file
    private void parseInputFile(String filename) {
        try {
            // Determine the charset based on the file extension
            Charset charset;
            if (filename.endsWith(".utf8.map")) {
                charset = StandardCharsets.UTF_8;
            } else if (filename.endsWith(".utf16.map")) {
                charset = StandardCharsets.UTF_16;
            } else if (filename.endsWith(".utf32.map")) {
                charset = Charset.forName("UTF-32");
            } else if (filename.endsWith(".txt")) {
                charset = StandardCharsets.UTF_8; // Default to UTF-8 for .txt files
            } else {
                throw new IllegalArgumentException("Unsupported file extension. Expected .utf8.map, .utf16.map, .utf32.map, or .txt");
            }

            // Open the file with the determined charset
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(filename), charset)) {
                GameParser.parse(reader); // Pass reader directly to GameParser
            }
        } catch (IOException e) {
            textArea.appendText("Error parsing file: " + e.getMessage() + "\n");
        } catch (ParseException e) {
            textArea.appendText("Parse error: " + e.getMessage() + "\n");
        }
    }

    //Method to add plugins that are unique to list
    public static void addUniquePlugins(List<String> plugins) {
        Set<String> uniquePlugins = new HashSet<>(plugins);
        pluginsToLoad.addAll(uniquePlugins);
    }

    //Loading plugins
    public void loadPlugins() {
        System.out.println("Loading plugins");
        for (String className : pluginsToLoad) {
            try {
                // Load the class by name
                Class<?> pluginClass = Class.forName(className);

                // Instantiate the plugin
                PuzzleGamePlugin plugin = (PuzzleGamePlugin) pluginClass.getConstructor(PuzzleGameAPI.class).newInstance(puzzleGameApiImpl);
                System.out.println("Loaded plugin: " + className);
                loadedPlugins.add(plugin); //Add plugin to loaded plugin list

            } catch (ClassNotFoundException e) {
                System.out.println("Plugin class not found: " + className);
            } catch (ClassCastException | NoSuchMethodException e) {
                System.out.println("Plugin does not implement PuzzleGamePlugin: " + className);
            }catch (InstantiationException e) {
                System.out.println("Error instantiating plugin: " + className);
            } catch (IllegalAccessException e) {
                System.out.println("Illegal access when loading plugin: " + className);
            } catch (InvocationTargetException e) {
                System.out.println("Error invoking constructor of plugin: " + className);
            }
        }
    }

    //Method to trigger plugins on button press
    private void triggerPluginsOnButtonPressed(){
        for (PuzzleGamePlugin plugin :loadedPlugins){
            plugin.onButtonPressed();
        }
    }

    //Method to trigger plugins on player move
    private void triggerPluginsOnPlayerMove(){
        for (PuzzleGamePlugin plugin :loadedPlugins){
            plugin.onPlayerMove();
        }
    }
    //Method to trigger plugins on resource acquired
    private void triggerPluginsOnResourceAcquired(String resourceAcquired){
        for (PuzzleGamePlugin plugin :loadedPlugins){
            plugin.onResourceAcquired(resourceAcquired);
        }
    }
    //Method to add obstacle icon
    public void addObstacleIcon(int row, int col){
        area.getIcons().add(new GridAreaIcon(
                col + 0.5, // Position the icon in the center of the cell
                row+ 0.5,
                0, // No rotation
                1.0, // Scale (size) of the icon
                App.class.getClassLoader().getResourceAsStream("obstacle.png"), // Load the obstacle's image
                "","obstacle" // Use a generic label for the obstacle
        ));
    }

    //Method to reveal an obstacle or items location
    //Used to remove a black square in any location
    public void revealLocation(int row, int col){
        GridAreaIcon blackSquare = area.getIcons().stream()
                .filter(icon -> icon.getX() == col + 0.5 && icon.getY() == row + 0.5 && "blackSquare".equals(icon.getName()))
                .findFirst()
                .orElse(null);

        if (blackSquare != null) {
            area.getIcons().remove(blackSquare);
            area.requestLayout();
        }
    }

    //Method used to run the script
   public void runScript(String pythonScript) {
       if (puzzleGameApiImpl == null) {
           throw new IllegalArgumentException("API instance cannot be null.");
       }

       try (PythonInterpreter interpreter = new PythonInterpreter()) {
           interpreter.set("api", puzzleGameApiImpl); // Bind the API to the script
           System.out.println("API instance successfully bound to Python interpreter.");

           interpreter.exec(pythonScript); // Execute the Python script

           // Retrieve the Python class instance and set up callbacks
           PyObject counter = interpreter.get("counter");
           PyObject onItemAcquired = counter.__getattr__("onItemAcquired");
           PyObject onObstacleTraversed = counter.__getattr__("onObstacleTraversed");

           // Register the callbacks in the API
           puzzleGameApiImpl.setOnItemAcquiredCallback(onItemAcquired);
           puzzleGameApiImpl.setOnObstacleTraversedCallback(onObstacleTraversed);

       } catch (PyException e) {
           System.err.println("Error running Python script: " + e.getMessage());
       }
   }

   //Method to handle locale change
    private void handleChangeLocale() {
        String languageTag = localeInputField.getText().trim();
        Locale newLocale;

        //Change to French locale if user inputs "fr" or "fr-FR"
        if ("fr".equalsIgnoreCase(languageTag) || "fr-FR".equalsIgnoreCase(languageTag)) {
            newLocale = Locale.forLanguageTag("fr-FR");
        }else{
            //Use default locale for any other entered locale
            newLocale = Locale.forLanguageTag("en");
        }
        //Change locale
        changeLocale(newLocale);
        System.out.println("Locale changed to: " + newLocale.getDisplayName());

    }

    //Method to change locale of all UI components
    private void changeLocale(Locale newLocale) {
       //Loading new locale
        this.currentLocale= newLocale;
        loadResourceBundle();

        //Updating UI elements with new locale
        updateTextAreaContentOnLocaleChange(textArea);
        moveUpBtn.setText(getMessage("button.up"));
        moveDownBtn.setText(getMessage("button.down"));
        moveLeftBtn.setText(getMessage("button.left"));
        moveRightBtn.setText(getMessage("button.right"));
        changeLocaleButton.setText(getMessage("button.changeLocale"));

        dateLabel.setText(getMessage("date.label")+ gameDate.toString());
        moveCountLabel.setText(getMessage("move.count.label")+moveCount);
        localeInputField.setPromptText(getMessage("locale.prompt"));

        updateInventoryDisplay();
    }

    //Method to Update notification area after locale change
    private void updateTextAreaContentOnLocaleChange(TextArea textArea) {
        textArea.clear();
        for (String key : messageKeys) {
            textArea.appendText(getMessage(key) + "\n");
        }
    }

    // Load the ResourceBundle based on the current locale
    private void loadResourceBundle() {
        messages = ResourceBundle.getBundle("MessagesBundle", currentLocale);
    }

    // Retrieve a localized message by key
    public String getMessage(String key) {
        return messages.getString(key);
    }

    // Append a localized message to a TextArea by key
    private void appendMessage(TextArea textArea, String key, String... placeholders) {
        String message = getMessage(key);
        for (int i = 0; i < placeholders.length; i++) {
            message = message.replace("{" + i + "}", placeholders[i]);
        }
        textArea.appendText(message + "\n");
        messageKeys.add(key);
    }

    //Method to increment date after player move
    private void incrementDate() {
        gameDate = gameDate.plusDays(1); // Increment date by one day
        moveCount++; // Increment move count
        dateLabel.setText(getMessage("date.label")+ gameDate.toString()); // Update displayed date
        moveCountLabel.setText(getMessage("move.count.label")+moveCount);
    }

    //Method to update inventory display
    private void updateInventoryDisplay() {
        // Clear existing content
        topTextArea.clear();
        topTextArea.appendText(getMessage("inventory.title")+"\n");
        for (String item : inventory) {
            topTextArea.appendText(item + "\n");
        }
    }
}
