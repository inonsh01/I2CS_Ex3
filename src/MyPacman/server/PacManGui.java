package MyPacman.server;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

import common.GameInfo;
import utils.StdDraw;

/**
 * Handles all graphical rendering for the Pacman game using the StdDraw library.
 * This class draws the map, entities (Pacman, Ghosts, Items).
 */
public class PacManGui {

    private static char prevKeyChar;

    /**
     * Initializes the graphics window settings.
     * sets the canvas size and the coordinate scale to match the map dimensions.
     */
    public static void initGraphics() {
        StdDraw.setCanvasSize(GameInfo.MAP_SIZE_PX, GameInfo.MAP_SIZE_PX);
        StdDraw.setXscale(0, GameInfo.MAP_SIZE);
        StdDraw.setYscale(0, GameInfo.MAP_SIZE);
        StdDraw.enableDoubleBuffering();
    }

    /**
     * Main rendering method. clears the screen, draws the map, and checks for game-over states.
     *
     * @param game The game instance containing the map data.
     */
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

    /**
     * Draws a single cell on the grid based on its content value.
     * Handles rotation for Pacman based on movement direction.
     *
     * @param x   The x-coordinate.
     * @param y   The y-coordinate.
     * @param val The value at this coordinate (Wall, Dot, Pacman, Ghost, etc.).
     */
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
                // calculate rotation angle based on current direction
                double angle = 0;
                int dir = MyGame.getPacmanDir();

                // original image faces RIGHT (0 degrees)
                // 1=UP, 2=LEFT, 3=DOWN, 4=RIGHT
                if (dir == 1) angle = 90;
                else if (dir == 2) angle = 180;
                else if (dir == 3) angle = 270;
                else angle = 0;

                try {
                    StdDraw.picture(x + 0.5, y + 0.5, "p1.png", 1, 1, angle);
                } catch (Exception e) {
                    // if image not found
                    StdDraw.setPenColor(Color.YELLOW);
                    StdDraw.filledCircle(x + 0.5, y + 0.5, 0.4);
                }
                break;

            case GameInfo.GHOST:
                int ghostIndex = MyGame.getGhostIndex(x, y);
                if (ghostIndex == -1) {break;}

                // scale ghost size if eatable
                double widthHeight = MyGame.getIsPredator() ? 0.5 : 1;

                try {
                    StdDraw.picture(x + 0.5, y + 0.5, "g" + ghostIndex + ".png", widthHeight, widthHeight);
                } catch (Exception e) {
                    // if image not found
                    StdDraw.setPenColor(Color.WHITE);
                    StdDraw.filledSquare(x + 0.5, y + 0.5, 0.4);
                }
                break;
        }
    }

    /**
     * Displays a large text overlay on the center of the screen (e.g., Win/Loss).
     *
     * @param text  The message to display.
     * @param color The color of the text.
     */
    private static void drawOverlayMessage(String text, Color color) {
        // Semi-transparent background
        StdDraw.setPenColor(new Color(0, 0, 0, 150));
        StdDraw.filledSquare(GameInfo.MAP_SIZE / 2.0, GameInfo.MAP_SIZE / 2.0, GameInfo.MAP_SIZE / 2.0);

        StdDraw.setPenColor(color);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.text(GameInfo.MAP_SIZE / 2.0, GameInfo.MAP_SIZE / 2.0, text);
    }

    /**
     * Checks for specific key presses (Space for start, 'H' for help/debug).
     *
     * @return The character key pressed, or null if none relevant.
     */
    public static Character getKeyPressed() {
        if(StdDraw.isKeyPressed(KeyEvent.VK_SPACE) && prevKeyChar != ' ') {
            prevKeyChar = ' ';
            return ' ';
        }
        else if(StdDraw.isKeyPressed(KeyEvent.VK_H) && prevKeyChar != 'h') {
            prevKeyChar = 'h';
            return 'h';
        }

        // Reset keys
        if (!StdDraw.isKeyPressed(KeyEvent.VK_SPACE) && prevKeyChar == ' ') prevKeyChar = 0;
        if (!StdDraw.isKeyPressed(KeyEvent.VK_H) && prevKeyChar == 'h') prevKeyChar = 0;

        return null;
    }
}