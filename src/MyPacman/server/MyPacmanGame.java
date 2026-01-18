package MyPacman.server;

public interface MyPacmanGame {
    int UP = 1;
    int LEFT = 2;
    int DOWN = 3;
    int RIGHT = 4;
    void init();
    int getStatus();
    Character getKeyChar();
    void generateMap();
    void drawBoard();
    void play();
    int[][] getGame(int code);
    void move(int dir);
    String getPos(int code);
    MyGhost[] getGhosts(int code);
}
