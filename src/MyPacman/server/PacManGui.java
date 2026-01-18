package MyPacman.server;

import java.awt.Color;
import java.awt.event.KeyEvent;

import common.GameInfo;
import utils.StdDraw;

public class PacManGui {

    private static char prevKeyChar;

    public static void initGraphics() {
        StdDraw.setCanvasSize(GameInfo.MAP_SIZE_PX, GameInfo.MAP_SIZE_PX);
        StdDraw.setXscale(0, GameInfo.MAP_SIZE);
        StdDraw.setYscale(0, GameInfo.MAP_SIZE);

        StdDraw.enableDoubleBuffering();
    }

    public static void drawBoard(MyPacmanGame game) {
        int[][] map = game.getGame(0);
        if (map == null) return;

        StdDraw.clear(Color.BLACK);
        int ghostCounter = 0;

        for (int x = 0; x < GameInfo.MAP_SIZE; x++) {
            for (int y = 0; y < GameInfo.MAP_SIZE; y++) {
                int val = map[x][y];
                ghostCounter = drawCell(x, y, val, ghostCounter);
            }
        }

        StdDraw.show();
    }

    private static int drawCell(int x, int y, int val, int ghostCounter) {

        switch (val) {
            case GameInfo.EMPTY:
                StdDraw.setPenColor(Color.WHITE);
                break;

            case GameInfo.WALL:
                StdDraw.setPenColor(Color.BLUE);
                double padding = 0.1;
                StdDraw.square(x + 0.5, y + 0.5, 0.5 - padding);
                break;

            case GameInfo.PINK:
                StdDraw.setPenColor(Color.PINK);
                StdDraw.filledCircle(x + 0.5, y + 0.5, 0.05);
                break;

            case GameInfo.PACMAN:
                StdDraw.picture(x + 0.5, y + 0.5, "p1.png", 1, 1);
                break;

            case GameInfo.GHOST:
                StdDraw.picture(x + 0.5, y + 0.5, "g" + ghostCounter + ".png", 1, 1);
                ghostCounter++;
                break;
        }
        return ghostCounter;
    }

    public static Character getKeyPressed() {
        if(StdDraw.isKeyPressed(KeyEvent.VK_SPACE) && prevKeyChar != ' ') {
            prevKeyChar = ' ';
            return ' ';
        }
        else if(StdDraw.isKeyPressed(KeyEvent.VK_H) && prevKeyChar != 'h') {
            prevKeyChar = 'h';
            return 'h';
        }
        return null;
    }
}

