package MyPacman.server;

import common.*;

import java.awt.*;

public class MyGame implements MyPacmanGame {
    public static final int INIT = 0;
    public static final int RUNNING = 1;
    public static final int DONE = 2;
    private long _lastMoveTime = System.currentTimeMillis();
    private int _moveDelay = 200;
    
    private Map2D _map;
    private Pixel2D _pacman;
    private MyGhost[] _ghosts;
    private Pixel2D[] _pinks;
    private int _status;
    private Character _keyChar;
    private final int mid = GameInfo.MAP_SIZE / 2;

    public void init() {
        _status = INIT;
        this._map = new Map(GameInfo.MAP_SIZE);
        this._pacman = new Index2D(this.mid, this.mid + 2 );
        this._ghosts = new MyGhost[4];
        this.generateMap();
        PacManGui.initGraphics();
        this.drawBoard();

    }

    @Override
    public int getStatus() {
        return _status;
    }
    
    @Override
    public Character getKeyChar() {
        _keyChar = PacManGui.getKeyPressed(_keyChar);
        return _keyChar;
    }
    
    @Override
    public void play() {
        if (_status == INIT) {
            _status = RUNNING;
        }
    }

    @Override
    public void generateMap() {
        this.setObs();
        this.setPinks();

        this._map.setPixel(_pacman,GameInfo.PACMAN);

        for (int i = 0; i < this._ghosts.length; i++) {
            this._ghosts[i] = new MyGhost(this.mid, this.mid);
            Pixel2D currGhost = new Index2D(this.mid, this.mid);
            this._map.setPixel(currGhost, GameInfo.GHOST);
        }
    }

    public void drawBoard() {
        PacManGui.drawBoard(this);
    }

    public void move(int dir) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - _lastMoveTime < _moveDelay) {
            return;
        }

        _lastMoveTime = currentTime;
        if(_status != RUNNING) return;

        int nextX = this._pacman.getX();
        int nextY = this._pacman.getY();

        switch (dir) {
            case UP:
                nextY++;
                break;
            case LEFT:
                nextX--;
                break;
            case DOWN:
                nextY--;
                break;
            case RIGHT:
                nextX++;
                break;
        }

        if (isLegalMove(nextX, nextY)) {
            this._map.setPixel(_pacman, GameInfo.EMPTY);
            int x = (nextX + GameInfo.MAP_SIZE) % GameInfo.MAP_SIZE;
            int y = (nextY + GameInfo.MAP_SIZE) % GameInfo.MAP_SIZE;
            _pacman = new Index2D(x, y);
            this._map.setPixel(_pacman, GameInfo.PACMAN);
            this.drawBoard();
        }
    }

    public String getPos(int code) {
        return _pacman.getX() + "," + _pacman.getY();
    }
    
    public MyGhost[] getGhosts(int code) {
        return _ghosts;
    }

    public int[][] getGame(int code) {
        return this._map.getMap();
    }

    ////////////////////// My Methods ///////////////////////

    private void setObs(){
        int size = GameInfo.MAP_SIZE;
        int mid = size / 2;
        for (int i = 0; i < mid + 1; i++) {
            for (int j = 0; j <= size; j++) {
                int val = 0;

                // set the borders
                if (i == 0 || j == 0 || j == size - 1) {
                    if (j != mid) {
                        val = -1;
                    }
                }

                // set shapes (L etc)
                else if ((j == 2 && i >= 2 && i <= 6) || (i == 2 && j >= 2 && j <= 5)) {
                    val = -1;
                }
                else if ((j == size - 3 && i >= 2 && i <= 6) || (i == 2 && j >= size - 6 && j <= size - 3)) {
                    val = -1;
                }

                // set line
                else if (j == mid && i > 1 && i < 5) {
                    val = -1;
                }

                if(this.isMiddleRect(i, j)){
                    val = -1;
                }

                // set the map symmetric
                this._map.setPixel(i, j, val);
                if (val == -1) {
                    this._map.setPixel(size - 1 - i, j, -1);
                }

            }
        }
    }

    private void setPinks(){
        int start = (GameInfo.MAP_SIZE - 3) / 2;
        int end = start + 3;
        for(int i = 0; i < GameInfo.MAP_SIZE; i++) {
            for(int j = 0; j < GameInfo.MAP_SIZE; j++) {
                // pink everywhere except walls and ghost's square
                if (this._map.getPixel(i, j) != GameInfo.WALL) {
                    if (!(i >= start && i < end && j >= start && j < end)) {
                        _map.setPixel(i, j, GameInfo.PINK);
                    }
                }
            }
        }
    }

    private boolean isMiddleRect(int i, int j) {
        // build rect in the middle for ghosts
        int startRow = (GameInfo.MAP_SIZE - 3) / 2;
        int startCol = (GameInfo.MAP_SIZE - 4) / 2;
        return ((i == startRow && j > startCol && j < startCol + 4) || (i == GameInfo.MAP_SIZE / 2 && j == (GameInfo.MAP_SIZE / 2 - 1)));
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

        if (_map.getPixel(actualX, actualY) == -1) {
            return false;
        }

        return true;
    }
}
