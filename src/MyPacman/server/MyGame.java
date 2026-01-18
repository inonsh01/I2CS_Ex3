package MyPacman.server;

import MyPacman.client.MyAlgo;
import common.*;
import java.awt.*;

public class MyGame implements MyPacmanGame {

    // --- Constants ---
    public static final int INIT = 0;
    public static final int RUNNING = 1;
    public static final int DONE = 2;
    private static final int _moveDelay = 50;

    // --- Static Variables ---
    private static boolean win = false;
    private static boolean lose = false;
    private static long _lastMoveTime = System.currentTimeMillis();
    private static long startPredatorTime;
    private static MyGhost[] _ghosts;
    private static boolean isPredator = false;

    // --- Instance Variables ---
    private Map2D _map;
    private Pixel2D _pacman;
    private int _pinksCounter;
    private int _status;
    private Character _keyChar;
    private final int mid = GameInfo.MAP_SIZE / 2;

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

    @Override
    public void play() {
        if (_status == INIT) {
            _status = RUNNING;
        }
    }

    @Override
    public void move(int dir) {
        if (this.getStatus() != RUNNING) return;

        // 1. Time Management (Speed Control)
        long currentTime = System.currentTimeMillis();
        if (currentTime - _lastMoveTime < _moveDelay) {
            return;
        }
        _lastMoveTime = currentTime;

        // 2. Predator Mode Timer
        if (isPredator && currentTime - startPredatorTime > 5000) {
            setIsPredator(false);
        }

        // 3. Move Ghosts First
        this.ghostsMove();

        // 4. Calculate Pacman Next Position
        int nextX = this._pacman.getX();
        int nextY = this._pacman.getY();

        switch (dir) {
            case UP:    nextY++; break;
            case LEFT:  nextX--; break;
            case DOWN:  nextY--; break;
            case RIGHT: nextX++; break;
        }

        // 5. Move Pacman if Legal
        if (isLegalMove(nextX, nextY)) {
            // Remove Pacman from old position
            this._map.setPixel(_pacman, GameInfo.EMPTY);

            // Cyclic calculation
            int x = (nextX + GameInfo.MAP_SIZE) % GameInfo.MAP_SIZE;
            int y = (nextY + GameInfo.MAP_SIZE) % GameInfo.MAP_SIZE;

            // Update Logic (Eat coins, collisions etc.)
            this.updateMap(x, y);

            // Set new position
            _pacman = new Index2D(x, y);
            this._map.setPixel(_pacman, GameInfo.PACMAN);

            this.drawBoard();
        }
    }

    private void updateMap(int x, int y) {
        int pixelContent = this._map.getPixel(x, y);

        this._map.setPixel(x, y, GameInfo.EMPTY);

        switch (pixelContent) {
            case GameInfo.PINK:
                this.setPinksCounter(this.getPinksCounter() - 1);

                if (this.getPinksCounter() == 0) {
                    System.out.println("WINNER! All dots collected.");
                    win = true;
                    this._status = DONE;
                }
                break;

            case GameInfo.GREEN:
                this.setIsPredator(true);
                startPredatorTime = System.currentTimeMillis();
                break;

            case GameInfo.GHOST:
                int ghostIdx = getGhostIndex(x, y);
                if (ghostIdx != -1) {
                    if (getIsPredator()) {
                        System.out.println("Ate a ghost!");

                        // restore the value the ghost was hiding
                        this._map.setPixel(x, y, _ghosts[ghostIdx].getPrevValue());

                        // reset the ghost to the center
                        _ghosts[ghostIdx].setPx(this.mid, this.mid);
                        _ghosts[ghostIdx].setPrevValue(GameInfo.EMPTY);
                    } else {
                        System.out.println("PACMAN DIED!");
                        lose = true;
                        this._status = DONE;
                    }
                }
                break;
        }
    }
    private void ghostsMove() {
        for (int i = 0; i < _ghosts.length; i++) {
            Pixel2D currentPos = _ghosts[i].getPx();
            int nextX = currentPos.getX();
            int nextY = currentPos.getY();

            // select random direction
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
                // 1. Restore the map content at the ghost's previous position
                this._map.setPixel(currentPos, _ghosts[i].getPrevValue());

                // 2. Check what is in the target square BEFORE moving there
                int contentAtTarget = this._map.getPixel(targetPos);

                // --- Scenario A: Ghost runs into Pacman ---
                if (contentAtTarget == GameInfo.PACMAN) {
                    if (isPredator) {
                        // If Pacman is a predator, this ghost dies.
                        // Instead of moving, reset ghost to the starting box.
                        _ghosts[i].setPx(this.mid, this.mid);
                        _ghosts[i].setPrevValue(GameInfo.EMPTY);

                        // We must re-draw the ghost at home immediately
                        this._map.setPixel(new Index2D(this.mid, this.mid), GameInfo.GHOST);

                        // Pacman stays where he is (we don't overwrite him)
                        continue; // Skip the rest of the loop for this ghost
                    } else {
                        // Normal mode: Ghost caught Pacman -> Game Over
                        System.out.println("PACMAN DIED (Ghost ran into him)");
                        lose = true;
                        this._status = DONE;

                        // Ghost overwrites Pacman, so prevValue is effectively EMPTY
                        _ghosts[i].setPrevValue(GameInfo.EMPTY);
                    }
                }
                // --- Scenario B: Ghost runs into another Ghost ---
                else if (contentAtTarget == GameInfo.GHOST) {
                    // Find the other ghost to steal its 'prevValue'
                    // (This prevents ghosts from erasing dots when they overlap)
                    int otherGhostIdx = getGhostIndex(targetX, targetY);
                    if (otherGhostIdx != -1 && otherGhostIdx != i) {
                        _ghosts[i].setPrevValue(_ghosts[otherGhostIdx].getPrevValue());
                    } else {
                        _ghosts[i].setPrevValue(GameInfo.EMPTY);
                    }
                }
                // --- Scenario C: Normal movement (Empty space, Dot, Power-up) ---
                else {
                    _ghosts[i].setPrevValue(contentAtTarget);
                }

                // 3. Move the ghost to the new position
                _ghosts[i].setPx(targetX, targetY);
                this._map.setPixel(targetPos, GameInfo.GHOST);
            }
        }
    }
    // --- Getters & Setters ---

    @Override
    public int getStatus() { return _status; }
    public static long getLastMoveTime() { return _lastMoveTime; }
    public static long getMoveDelay() { return _moveDelay; }
    public static boolean getWin() { return win; }
    public static boolean getLose() { return lose; }
    public int getPinksCounter() { return _pinksCounter; }
    public void setPinksCounter(int pinksCounter) { this._pinksCounter = pinksCounter; }
    public static boolean getIsPredator() { return isPredator; }
    public void setIsPredator(boolean _isPredator) { isPredator = _isPredator; }

    @Override
    public Character getKeyChar() {
        _keyChar = PacManGui.getKeyPressed();
        return _keyChar;
    }

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

    public void drawBoard() { PacManGui.drawBoard(this); }

    // --- Map Generation Methods ---

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

    private void setObs() {
        int size = GameInfo.MAP_SIZE;
        int mid = size / 2;
        for (int i = 0; i < mid + 1; i++) {
            for (int j = 0; j <= size; j++) {
                int val = 0;

                // Borders
                if (i == 0 || j == 0 || j == size - 1) {
                    if (j != mid) val = -1;
                }
                // Shapes
                else if ((j == 2 && i >= 2 && i <= 6) || (i == 2 && j >= 2 && j <= 5)) {
                    val = -1;
                } else if ((j == size - 3 && i >= 2 && i <= 6) || (i == 2 && j >= size - 6 && j <= size - 3)) {
                    val = -1;
                }
                // Middle Line
                else if (j == mid && i > 1 && i < 5) {
                    val = -1;
                }
                // Ghost House
                if (this.isMiddleRect(i, j)) {
                    val = -1;
                }

                // Symmetric Set
                this._map.setPixel(i, j, val);
                if (val == -1) {
                    this._map.setPixel(size - 1 - i, j, -1);
                }
            }
        }
    }

    private void setDots() {
        int size = GameInfo.MAP_SIZE;
        int start = (size - 3) / 2;
        int end = start + 3;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == _pacman.getX() && j == _pacman.getY()) {
                    continue;
                }

                if ((i == 1 && (j == 1 || j == size - 2)) || (i == size - 2 && (j == 1 || j == size - 2))) {
                    _map.setPixel(i, j, GameInfo.GREEN);
                }
                else if (this._map.getPixel(i, j) != GameInfo.WALL) {

                    // do not put dots inside ghost house
                    if (!(i >= start && i < end && j >= start && j < end)) {
                        _map.setPixel(i, j, GameInfo.PINK);
                        this._pinksCounter++;
                    }
                }
            }
        }
    }

    private boolean isMiddleRect(int i, int j) {
        int startRow = (GameInfo.MAP_SIZE - 3) / 2;
        int startCol = (GameInfo.MAP_SIZE - 4) / 2;
        return ((i == startRow && j > startCol && j < startCol + 4) ||
                (i == GameInfo.MAP_SIZE / 2 && j == (GameInfo.MAP_SIZE / 2 - 1)));
    }

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