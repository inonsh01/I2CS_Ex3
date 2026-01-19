package MyPacman.server;

/**
 * The interface defining the core behavior and contract for the Pacman game engine.
 * Any class implementing this interface must handle game initialization,
 * movement logic, map generation, and state management.
 */
public interface MyPacmanGame {

    // --- Direction Constants ---
    int UP = 1;
    int LEFT = 2;
    int DOWN = 3;
    int RIGHT = 4;

    // --- Status Constants ---
    int RUNNING = 1;

    // --- Game Control Methods ---
    /**
     * Initializes the game settings, loads the map, places entities (Pacman and Ghosts),
     * and sets up the graphical interface.
     */
    void init();

    /**
     * Starts the game loop or transitions the game state from initialization to running.
     */
    void play();

    /**
     * Executes a single game step (tick).
     * This moves the player in the specified direction, moves the ghosts,
     * and updates the game state based on collisions or item collection.
     *
     * @param dir The direction code (UP, DOWN, LEFT, RIGHT) for Pacman's next move.
     */
    void move(int dir);

    // --- Data Access Methods ---

    /**
     * Retrieves the current status of the game.
     *
     * @return An integer representing the state (e.g., INIT, RUNNING, DONE).
     */
    int getStatus();

    /**
     * Gets the last valid key pressed by the user from the keyboard input.
     *
     * @return The character of the key pressed.
     */
    Character getKeyChar();

    /**
     * Returns the current state of the game board.
     *
     * @param code A security or access code (legacy use).
     * @return A 2D integer array representing the map and its contents.
     */
    int[][] getGame(int code);

    /**
     * Returns the current position of the Pacman.
     *
     * @param code A security or access code (legacy use).
     * @return A string representation of the coordinates -> "x,y".
     */
    String getPos(int code);

    /**
     * Retrieves the array of ghost entities currently in the game.
     *
     * @param code A security or access code (legacy use).
     *
     * @return An array of MyGhost objects.
     */
    MyGhost[] getGhosts(int code);

    // --- Map & Rendering Methods ---

    /**
     * Generates the map layout, including walls, dots, power-ups, and ghost barriers.
     */
    void generateMap();

    /**
     * Triggers the GUI to repaint the current state of the board.
     * This connects the logic layer to the visual layer.
     */
    void drawBoard();
}