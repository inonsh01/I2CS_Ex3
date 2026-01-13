
import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * This is the major algorithmic class for Ex3 - the PacMan game:
 *
 * This code is a very simple example (random-walk algorithm).
 * Your task is to implement (here) your PacMan algorithm.
 */
public class Ex3Algo implements PacManAlgo {
    private int _count;

    public Ex3Algo() {
        _count = 0;
    }

    @Override
    /*
       This function returns explanation of the move algorithm
     */
    public String getInfo() {
        return "This algorithm controls Pac-Man's movement to eat all pink dots while avoiding ghosts and strategically using green power dots." +
                "Algorithm Flow:" +
                "1. Assess Threat Level: Calculate distance to nearest ghost" +
                "2. Priority Decision:" +
                "\t If ghost too close → Escape or seek green dot" +
                "\t If safe → Target nearest pink dot" +
                "3. Path Planning: Use BFS to find optimal route" +
                "4. Movement Execution: Move one step toward target" +
                "Good luck trying to beat me! (:";
    }

    @Override
    /**
     * This is the main function that calculate every move of the PacMan
     *
     * @param game
     *
     * @return The next move direction
     */
    public int move(PacmanGame game) {
        int code = 0, pink, black, green;
        int[][] board = game.getGame(0);
        String pos = game.getPos(code).toString();
        int blue = Game.getIntColor(Color.BLUE, code);
        GhostCL[] ghosts = game.getGhosts(code);

        if (_count == 0 || _count == 300) {
            printBoard(board);
            pink = Game.getIntColor(Color.PINK, code);
            black = Game.getIntColor(Color.BLACK, code);
            green = Game.getIntColor(Color.GREEN, code);
            System.out.println("Blue=" + blue + ", Pink=" + pink + ", Black=" + black + ", Green=" + green);
            System.out.println("Pacman coordinate: " + pos);
            printGhosts(ghosts);
            int up = Game.UP, left = Game.LEFT, down = Game.DOWN, right = Game.RIGHT;
        }
        _count++;
        Map _map = new Map(board);
        String[] posArr = pos.split(",");
        int pacX = Integer.parseInt(posArr[0]);
        int pacY = Integer.parseInt(posArr[1]);
        Pixel2D pacPos = new Index2D(pacX, pacY);

        Map2D distanceMap = _map.allDistance(pacPos, blue, GameInfo.CYCLIC_MODE);

        String goal = "pink";

        GhostCL closestGhost = getClosestGhost(pacPos, ghosts, code);

        assert closestGhost != null;
        String[] currGhPosArr = closestGhost.getPos(code).split(",");

        int currGhX = Integer.parseInt(currGhPosArr[0]);
        int currGhY = Integer.parseInt(currGhPosArr[1]);

        Pixel2D closestGhostPixel = new Index2D(currGhX, currGhY);

        if (pacPos.distance2D(closestGhostPixel) < GameInfo.SAFETY_RANGE) {
            if(closestGhost.remainTimeAsEatable(code) > 1){
                goal = "pink";
                if(pacPos.distance2D(closestGhostPixel) < GameInfo.TOO_CLOSE) {
                    goal = "hunt";
                }
            }
            else if (isGreenClose(_map, distanceMap, pacPos, closestGhostPixel, code, blue))
                goal = "green";
            else
                goal = "run";
        }
        return getDirection(new Map(board), distanceMap, closestGhostPixel, pacPos, goal, code, blue);
    }


    private static void printBoard(int[][] b) {
        for (int y = 0; y < b[0].length; y++) {
            for (int x = 0; x < b.length; x++) {
                int v = b[x][y];
                System.out.print(v + "\t");
            }
            System.out.println();
        }
    }

    private static void printGhosts(GhostCL[] gs) {
        for (int i = 0; i < gs.length; i++) {
            GhostCL g = gs[i];
            System.out.println(i + ") status: " + g.getStatus() + ",  type: " + g.getType() + ",  pos: " + g.getPos(0) + ",  time: " + g.remainTimeAsEatable(0));
        }
    }

    private static int randomDir() {
        int[] dirs = {Game.UP, Game.LEFT, Game.DOWN, Game.RIGHT};
        int ind = (int) (Math.random() * dirs.length);
        return dirs[ind];
    }

    ////////////////////// Private Methods ///////////////////////

    /**
     * The Main function that runs the all algorithm, it calculates the best next move
     *
     * @param board The game board
     * @param distanceMap The current all distance map
     * @param closestGhost The current closest ghost
     * @param pacman The current position of the PacMan
     * @param goal The current goal of the PacMan
     * @param code The colors code
     * @param obsColor The obstacle color
     *
     * @return direction of the next move
     */
    private int getDirection(Map2D board, Map2D distanceMap, Pixel2D closestGhost, Pixel2D pacman, String goal, int code, int obsColor) {
        int color = 1;
        Pixel2D[] path;

        if(Objects.equals(goal, "run")){
            if (closestGhost == null) return 1;
            path = findEscapePath(board, distanceMap,pacman, closestGhost, code, obsColor);
        }
        else if(Objects.equals(goal, "hunt")){
            path = board.shortestPath(pacman, closestGhost, obsColor, GameInfo.CYCLIC_MODE);
        }
        else{
            color = switch (goal) {
                case "pink" -> Game.getIntColor(Color.PINK, code);
                case "green" -> Game.getIntColor(Color.GREEN, code);
                default -> color;
            };

            // get the shortest path to goal
            path = board.shortestPath(pacman, getClosest(board, distanceMap,color), obsColor, GameInfo.CYCLIC_MODE);
        }

        // if still no path -> explode :)
        if(path == null || path.length < 1){
            return randomDir();
        }

        // pacman reached the goal
        if(path.length == 1){
            return Game.UP; // no reason
        }

        Pixel2D nextMove = path[1];

        int dx = nextMove.getX() - pacman.getX();
        int dy = nextMove.getY() - pacman.getY();
        if (dx == 1 || (GameInfo.CYCLIC_MODE && dx < -1)) return Game.RIGHT;
        else if (dx == -1 || (GameInfo.CYCLIC_MODE && dx > 1)) return Game.LEFT;
        else if (dy == 1 || (GameInfo.CYCLIC_MODE && dy < -1)) return Game.UP;
        return Game.DOWN;
    }

    /**
     * This function's goal is to find the best escape path in case of 'running'
     * while this case the PacMan will try to find the shortest path to a pink dot,
     * using the shortest path algorithm by defining the ghost as an obstacle
     *
     * @param board The game board
     * @param distanceMap The current all distance map
     * @param pacman The current position of the PacMan
     * @param ghost The closest ghost
     * @param code The colors code
     * @param obsColor The obstacle color
     *
     * @return A path to run away
     */
    private Pixel2D[] findEscapePath(Map2D board, Map2D distanceMap, Pixel2D pacman, Pixel2D ghost, int code, int obsColor) {

        int originalValue = board.getPixel(ghost.getX(),ghost.getY());
        board.setPixel(ghost.getX(),ghost.getY(), obsColor);
        Pixel2D[] path = board.shortestPath(pacman, getClosest(board, distanceMap,Game.getIntColor(Color.PINK, code)), obsColor, GameInfo.CYCLIC_MODE);

        // restore board
        board.setPixel(ghost.getX(),ghost.getY(), originalValue);
        return path;
    }

    /**
     * The goal of the function is to find the distance of the closest ghost
     *
     * @param pacman The Pacman current position.
     * @param ghosts Array of GhostCL
     * @param code Array of GhostCL
     * @return A pixel of the closest ghost
     */
    private GhostCL getClosestGhost(Pixel2D pacman, GhostCL[] ghosts, int code) {
        if (ghosts.length == 0 || pacman == null) return null;
        double minDist = Double.MAX_VALUE;
        GhostCL ghost = null;

        for (int i = 0; i < ghosts.length; i++) {
            String[] currGhPosArr = ghosts[i].getPos(code).split(",");
            int currGhX = Integer.parseInt(currGhPosArr[0]);
            int currGhY = Integer.parseInt(currGhPosArr[1]);

            double currDist = pacman.distance2D(new Index2D(currGhX, currGhY));

            if (currDist < minDist) {
                minDist = currDist;
                ghost =  ghosts[i];
            }
        }
        return ghost;
    }

    /**
     * Checks if there is green dot close to Pacman
     *
     * @param board The board map
     * @param distanceMap A all distance Map of current board
     * @param pacman The pacman position
     * @param ghost The closest ghost pixel
     * @param code The code of colors
     * @param obsColor Obstacle color of the game
     * @return A number of the distance of the closest ghost
     */
    private boolean isGreenClose( Map2D board, Map2D distanceMap,Pixel2D pacman, Pixel2D ghost, int code, int obsColor ) {
        if(board == null || distanceMap == null || pacman == null || ghost == null) return false;

        Pixel2D green = getClosest(board, distanceMap, Game.getIntColor(Color.GREEN, code));

        if(green == null)
            return false;

        int closetsGhostDistance = distanceMap.getPixel(ghost.getX(),ghost.getY());
        int closetsGreenDistance = distanceMap.getPixel(green.getX(),green.getY());

        if(sameDirection(board, pacman, green, ghost, obsColor)) {
            return (closetsGreenDistance < (closetsGhostDistance / 2));
        }
        else {
            return (closetsGhostDistance <= GameInfo.MAX_GREEN_DISTANCE);
        }
    }

    /**
     * Returns the closest pixel of a given color
     *
     * @param board The board map
     * @param distanceMap A all distance Map of current board
     * @param color required color to search the closest
     * @return the closest asking element
     */
    private Pixel2D getClosest(Map2D board, Map2D distanceMap, int color) {
        if(board == null || distanceMap == null) return null;

        Pixel2D closest = null;
        for(int i = 0; i < board.getWidth(); i++) {
            for(int j = 0; j < board.getHeight(); j++) {
                if(board.getPixel(i,j) == color)
                  if(closest == null || distanceMap.getPixel(i,j) < distanceMap.getPixel(closest)){
                    closest = new Index2D(i,j);
                }
            }
        }
        return closest;
    }

    /**
     * Returns true if two of ways are the same direction or not
     * by checking the first pixel of each way, calculate by Shortest Path algorithm
     *
     * @param board The game board
     * @param src The source pixel
     * @param dist1 The first destination
     * @param dist2 The second destination
     * @param obsColor Obstacle color of the game
     * @return boolean sameDirection value
     */
    private boolean sameDirection(Map2D board, Pixel2D src, Pixel2D dist1, Pixel2D dist2, int obsColor) {
        return (board.shortestPath(src,dist1, obsColor,GameInfo.CYCLIC_MODE)[0].equals(board.shortestPath(src,dist2, obsColor, GameInfo.CYCLIC_MODE)[0]));
    }
}