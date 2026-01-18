package MyPacman.server;

import java.awt.Color;
import java.awt.Font;
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

        for (int x = 0; x < GameInfo.MAP_SIZE; x++) {
            for (int y = 0; y < GameInfo.MAP_SIZE; y++) {
                int val = map[x][y];
                drawCell(x, y, val);
            }
        }

        // check for game over state to display overlay
        if (MyGame.getWin()) {
            drawOverlayMessage("YOU WIN!", Color.GREEN);
        }
        else if (MyGame.getLose()) {
            drawOverlayMessage("GAME OVER", Color.RED);
        }

        StdDraw.show();
    }

    private static void drawCell(int x, int y, int val) {

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

            case GameInfo.GREEN:
                StdDraw.setPenColor(Color.GREEN);
                StdDraw.filledCircle(x + 0.5, y + 0.5, 0.05);
                break;

            case GameInfo.PACMAN:
                StdDraw.picture(x + 0.5, y + 0.5, "p1.png", 1, 1);
                break;

            case GameInfo.GHOST:
                int ghostIndex = MyGame.getGhostIndex(x, y);
                if (ghostIndex == -1) {break;}
                double widthHeight = MyGame.getIsPredator() ? 0.5 : 1;
                StdDraw.picture(x + 0.5, y + 0.5, "g" + ghostIndex + ".png", widthHeight, widthHeight);
                break;
        }
    }

    private static void drawOverlayMessage(String text, Color color) {
        // semi-transparent background
        StdDraw.setPenColor(new Color(0, 0, 0, 150));
        StdDraw.filledSquare(GameInfo.MAP_SIZE / 2.0, GameInfo.MAP_SIZE / 2.0, GameInfo.MAP_SIZE / 2.0);

        StdDraw.setPenColor(color);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.text(GameInfo.MAP_SIZE / 2.0, GameInfo.MAP_SIZE / 2.0, text);
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

        // reset keys
        if (!StdDraw.isKeyPressed(KeyEvent.VK_SPACE) && prevKeyChar == ' ') prevKeyChar = 0;
        if (!StdDraw.isKeyPressed(KeyEvent.VK_H) && prevKeyChar == 'h') prevKeyChar = 0;

        return null;
    }
}