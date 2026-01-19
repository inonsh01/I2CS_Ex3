package MyPacman.server;

import Algorithms.MyAlgo;
import common.*;

/**
 * The main engine for the Pacman game.
 * This class handles the game loop, map generation, entity movement (Pacman and Ghosts),
 * and win/loss conditions.
 */
public class MyGame implements MyPacmanGame {

    // --- Constants ---
    public static final int INIT = 0;
    public static final int RUNNING = 1;
    public static final int DONE = 2;
    private static final int _moveDelay = 100;
    private static final int PREDATOR_TIME_LIMIT = 5000;

    // --- Static Variables ---
    private static boolean win = false;
    private static boolean lose = false;
    private static long _lastMoveTime = System.currentTimeMillis();
    private static long startPredatorTime;
    private static MyGhost[] _ghosts;
    private static boolean isPredator = false;

    // stores Pacman's current direction for GUI rotation
    private static int _currentDir = 4; // Default RIGHT

    // --- Instance Variables ---
    private Map2D _map;
    private Pixel2D _pacman;
    private int _pinksCounter;
    private int _status;
    private Character _keyChar;
    private final int mid = GameInfo.MAP_SIZE / 2;

    /**
     * Initializes the game state.
     * creates the map, places Pacman and ghosts, and initializes graphics.
     */
    @Override
    public void init() {
        _status = INIT;
        this._map = new Map(GameInfo.MAP_SIZE);
        this.setPinksCounter(0);

        this._pacman = new Index2D(this.mid, this.mid + 2);

        _ghosts = new MyGhost[GameInfo.GHOST_NUMBER];
        this.generateMap();

        PacManGui.initGraphics();
        this.drawBoard();
    }

    /**
     * starts the game loop by changing status from INIT to RUNNING.
     */
    @Override
    public void play() {
        if (_status == INIT) {
            _status = RUNNING;
        }
    }

    /**
     * The main game tick method.
     * Moves ghosts, calculates Pacman's next move, and updates the board.
     *
     * @param dir The direction Pacman intends to move (UP, DOWN, LEFT, RIGHT).
     */
    @Override
    public void move(int dir) {
        if (this.getStatus() != RUNNING) return;

        // Time Management (Speed Control)
        long currentTime = System.currentTimeMillis();
        if (currentTime - _lastMoveTime < _moveDelay) {
            return;
        }
        _lastMoveTime = currentTime;

        // update direction for GUI rotation
        _currentDir = dir;

        // predator mode timer check
        if (isPredator && currentTime - startPredatorTime > PREDATOR_TIME_LIMIT) {
            setIsPredator(false);
        }

        // move ghosts first
        this.ghostsMove();

        // calculate pacman next position
        int nextX = this._pacman.getX();
        int nextY = this._pacman.getY();

        switch (dir) {
            case UP:    nextY++; break;
            case LEFT:  nextX--; break;
            case DOWN:  nextY--; break;
            case RIGHT: nextX++; break;
        }

        // move pacman if the move is legal
        if (isLegalMove(nextX, nextY)) {
            // remove Pacman from old position
            this._map.setPixel(_pacman, GameInfo.EMPTY);

            // cyclic calculation for infinite map borders
            int x = (nextX + GameInfo.MAP_SIZE) % GameInfo.MAP_SIZE;
            int y = (nextY + GameInfo.MAP_SIZE) % GameInfo.MAP_SIZE;

            this.updateMap(x, y);

            // set new position
            _pacman = new Index2D(x, y);
            this._map.setPixel(_pacman, GameInfo.PACMAN);

            this.drawBoard();
        }
    }

    /**
     * Handles the interaction between Pacman and the content of the target pixel.
     * This includes eating dots, picking up power-ups, and colliding with ghosts.
     *
     * @param x The x-coordinate of the target pixel.
     * @param y The y-coordinate of the target pixel.
     */
    private void updateMap(int x, int y) {
        int pixelContent = this._map.getPixel(x, y);

        // clear the pixel (pacman eats the content)
        this._map.setPixel(x, y, GameInfo.EMPTY);

        switch (pixelContent) {
            case GameInfo.PINK:
                // regular point collected
                this.setPinksCounter(this.getPinksCounter() - 1);

                if (this.getPinksCounter() == 0) {
                    System.out.println("WINNER! All dots collected.");
                    win = true;
                    this._status = DONE;
                }
                break;

            case GameInfo.GREEN:
                // power-up collected: Enter Predator Mode
                this.setIsPredator(true);
                startPredatorTime = System.currentTimeMillis();
                break;

            case GameInfo.GHOST:
                // collision with a Ghost
                int ghostIdx = getGhostIndex(x, y);
                if (ghostIdx != -1) {
                    if (getIsPredator()) {
                        // Predator Mode: Eat the ghost

                        // restore the value the ghost was hiding
                        this._map.setPixel(x, y, _ghosts[ghostIdx].getPrevValue());

                        // reset the ghost to the "Ghost House" (center)
                        _ghosts[ghostIdx].setPx(this.mid, this.mid);
                        _ghosts[ghostIdx].setPrevValue(GameInfo.EMPTY);
                    } else {
                        // normal Mode: Pacman dies
                        System.out.println("PACMAN DIED! (Ran into ghost)");
                        lose = true;
                        this._status = DONE;
                    }
                }
                break;
        }
    }

    /**
     * Iterates through all ghosts and updates their positions.
     * Handles ghost AI (random movement) and collisions if a ghost moves onto Pacman.
     */
    private void ghostsMove() {
        for (int i = 0; i < _ghosts.length; i++) {
            Pixel2D currentPos = _ghosts[i].getPx();
            int nextX = currentPos.getX();
            int nextY = currentPos.getY();

            // select random direction for the ghost
            int dir = MyAlgo.randomDir();

            switch (dir) {
                case UP:    nextY++; break;
                case LEFT:  nextX--; break;
                case DOWN:  nextY--; break;
                case RIGHT: nextX++; break;
            }

            // calculate cyclic coordinates
            int targetX = (nextX + GameInfo.MAP_SIZE) % GameInfo.MAP_SIZE;
            int targetY = (nextY + GameInfo.MAP_SIZE) % GameInfo.MAP_SIZE;
            Pixel2D targetPos = new Index2D(targetX, targetY);

            if (isLegalMove(targetX, targetY)) {
                // restore the map content at the ghost's previous position
                this._map.setPixel(currentPos, _ghosts[i].getPrevValue());

                // check what is in the target square BEFORE moving there
                int contentAtTarget = this._map.getPixel(targetPos);

                // --- Scenario A: Ghost runs into Pacman ---
                if (contentAtTarget == GameInfo.PACMAN) {
                    if (isPredator) {
                        // Ghost dies and resets to center
                        _ghosts[i].setPx(this.mid, this.mid);
                        _ghosts[i].setPrevValue(GameInfo.EMPTY);
                        this._map.setPixel(new Index2D(this.mid, this.mid), GameInfo.GHOST);
                        continue;
                    } else {
                        // Pacman dies
                        System.out.println("PACMAN DIED (Ghost caught him)");
                        lose = true;
                        this._status = DONE;
                        _ghosts[i].setPrevValue(GameInfo.EMPTY);
                    }
                }
                // --- Scenario B: Ghost runs into another Ghost ---
                else if (contentAtTarget == GameInfo.GHOST) {
                    int otherGhostIdx = getGhostIndex(targetX, targetY);
                    if (otherGhostIdx != -1 && otherGhostIdx != i) {
                        // swap hidden values to prevent erasing dots
                        _ghosts[i].setPrevValue(_ghosts[otherGhostIdx].getPrevValue());
                    } else {
                        _ghosts[i].setPrevValue(GameInfo.EMPTY);
                    }
                }
                // --- Scenario C: Normal movement ---
                else {
                    _ghosts[i].setPrevValue(contentAtTarget);
                }

                // move the ghost to the new position
                _ghosts[i].setPx(targetX, targetY);
                this._map.setPixel(targetPos, GameInfo.GHOST);
            }
        }
    }

    // --- Getters & Setters ---

    @Override
    public int getStatus() { return _status; }
    public static boolean getWin() { return win; }
    public static boolean getLose() { return lose; }
    public static long getMoveDelay() { return _moveDelay; }
    public static long getLastMoveTime() { return _lastMoveTime; }
    public int getPinksCounter() { return _pinksCounter; }
    public void setPinksCounter(int pinksCounter) { this._pinksCounter = pinksCounter; }
    public static boolean getIsPredator() { return isPredator; }
    public void setIsPredator(boolean _isPredator) { isPredator = _isPredator; }

    /**
     * @return The current direction of Pacman (used for GUI rotation).
     */
    public static int getPacmanDir() { return _currentDir; }

    @Override
    public Character getKeyChar() {
        _keyChar = PacManGui.getKeyPressed();
        return _keyChar;
    }

    /**
     * Finds the index of a ghost located at specific coordinates.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     *
     * @return The index of the ghost in the array, or -1 if no ghost is found there.
     */
    public static int getGhostIndex(int x, int y) {
        if (_ghosts == null) return -1;
        for (int i = 0; i < _ghosts.length; i++) {
            if (_ghosts[i] != null && _ghosts[i].getPx().getX() == x && _ghosts[i].getPx().getY() == y) {
                return i;
            }
        }
        return -1;
    }

    public String getPos(int code) { return _pacman.getX() + "," + _pacman.getY(); }
    public MyGhost[] getGhosts(int code) { return _ghosts; }
    public int[][] getGame(int code) { return this._map.getMap(); }

    /**
     * Triggers the GUI to repaint the board.
     */
    public void drawBoard() { PacManGui.drawBoard(this); }

    // --- Map Generation Methods ---

    /**
     * Generates the game map, including walls, dots, and ghost placement.
     */
    @Override
    public void generateMap() {
        this.setObs();
        this.setDots();

        this._map.setPixel(_pacman, GameInfo.PACMAN);

        for (int i = 0; i < _ghosts.length; i++) {
            _ghosts[i] = new MyGhost(this.mid, this.mid);
            Pixel2D currGhost = new Index2D(this.mid, this.mid);
            this._map.setPixel(currGhost, GameInfo.GHOST);
        }
    }

    /**
     * Sets the obstacles (walls) on the map to create the maze layout.
     * Uses symmetry to create a balanced map.
     */
    private void setObs() {
        int size = GameInfo.MAP_SIZE;
        int mid = size / 2;
        for (int i = 0; i < mid + 1; i++) {
            for (int j = 0; j <= size; j++) {
                int val = 0;

                // borders
                if (i == 0 || j == 0 || j == size - 1) {
                    if (j != mid) val = -1;
                }
                // internal Shapes
                else if ((j == 2 && i >= 2 && i <= 6) || (i == 2 && j >= 2 && j <= 5)) {
                    val = -1;
                } else if ((j == size - 3 && i >= 2 && i <= 6) || (i == 2 && j >= size - 6 && j <= size - 3)) {
                    val = -1;
                }
                // middle Line
                else if (j == mid && i > 1 && i < 5) {
                    val = -1;
                }
                // ghost house
                if (this.isMiddleRect(i, j)) {
                    val = -1;
                }

                // mirror the map
                this._map.setPixel(i, j, val);
                if (val == -1) {
                    this._map.setPixel(size - 1 - i, j, -1);
                }
            }
        }
    }

    /**
     * Places dots (points) and power-ups on the map.
     * Skips walls, the ghost house, and Pacman's starting position.
     */
    private void setDots() {
        int size = GameInfo.MAP_SIZE;
        int start = (size - 3) / 2;
        int end = start + 3;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // ensure no dot is placed on Pacman's start position
                if (i == _pacman.getX() && j == _pacman.getY()) {
                    continue;
                }

                // place power-ups (green dots) in corners
                if ((i == 1 && (j == 1 || j == size - 2)) || (i == size - 2 && (j == 1 || j == size - 2))) {
                    _map.setPixel(i, j, GameInfo.GREEN);
                }

                // place regular dots (pink) in empty spaces
                else if (this._map.getPixel(i, j) != GameInfo.WALL) {
                    // do not put dots inside the ghost house
                    if (!(i >= start && i < end && j >= start && j < end)) {
                        _map.setPixel(i, j, GameInfo.PINK);
                        this._pinksCounter++;
                    }
                }
            }
        }
    }

    /**
     * Checks if a coordinate is within the central "Ghost House" rectangle.
     *
     * @param i Row index.
     * @param j Column index.
     *
     * @return true if inside the ghost house, false otherwise.
     */
    private boolean isMiddleRect(int i, int j) {
        int startRow = (GameInfo.MAP_SIZE - 3) / 2;
        int startCol = (GameInfo.MAP_SIZE - 4) / 2;
        return ((i == startRow && j > startCol && j < startCol + 4) ||
                (i == GameInfo.MAP_SIZE / 2 && j == (GameInfo.MAP_SIZE / 2 - 1)));
    }

    /**
     * Verifies if a move to the target coordinates is legal (not a wall and within bounds).
     * Handles cyclic logic if enabled.
     *
     * @param x Target X coordinate.
     * @param y Target Y coordinate.
     *
     * @return true if the move is allowed, false if it's a wall or out of bounds.
     */
    private boolean isLegalMove(int x, int y) {
        int size = GameInfo.MAP_SIZE;

        if (!GameInfo.CYCLIC_MODE) {
            if (x < 0 || x >= size || y < 0 || y >= size) {
                return false;
            }
        }
        int actualX = (x + size) % size;
        int actualY = (y + size) % size;

        return _map.getPixel(actualX, actualY) != -1;
    }
}