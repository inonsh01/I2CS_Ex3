package MyPacman.client;

import MyPacman.server.MyGhost;
import MyPacman.server.MyPacmanGame;
import MyPacman.server.MyGame;
import common.*;
import java.awt.*;
import java.util.Objects;

public class MyAlgo {

    private int _count;

    public MyAlgo() {
        _count = 0;
    }

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

    /**
     * This is the main function that calculate every move of the PacMan
     *
     * @param game The MyPacmanGame object
     *
     * @return The next move direction
     */
    public int move(MyPacmanGame game) {
        int code = 0, pink, black, green;
        int[][] board = game.getGame(0);
        String pos = game.getPos(code).toString();
        int blue = GameInfo.WALL;
        MyGhost[] ghosts = game.getGhosts(code);

        if (_count == 0 || _count == 300) {
            printBoard(board);
            pink = GameInfo.PINK;
            black = GameInfo.BLACK;
            green = GameInfo.GREEN;
            System.out.println("Blue=" + blue + ", Pink=" + pink + ", Black=" + black + ", Green=" + green);
            System.out.println("Pacman coordinate: " + pos);
            printGhosts(ghosts);
            int up = MyGame.UP, left = MyGame.LEFT, down = MyGame.DOWN, right = MyGame.RIGHT;
        }
        _count++;
        Map2D _map = new Map(board);
        String[] posArr = pos.split(",");
        int pacX = Integer.parseInt(posArr[0]);
        int pacY = Integer.parseInt(posArr[1]);
        Pixel2D pacPos = new Index2D(pacX, pacY);

        Map2D distanceMap = _map.allDistance(pacPos, blue, GameInfo.CYCLIC_MODE);

        String goal = "pink";

        MyGhost closestGhost = getClosestGhost(pacPos, ghosts, code);

        assert closestGhost != null;
        String[] currGhPosArr = closestGhost.getPos(code).split(",");

        int currGhX = Integer.parseInt(currGhPosArr[0]);
        int currGhY = Integer.parseInt(currGhPosArr[1]);

        Pixel2D closestGhostPixel = new Index2D(currGhX, currGhY);

        if (pacPos.distance2D(closestGhostPixel) < GameInfo.SAFETY_RANGE) {
            if(closestGhost.remainTimeAsEatable(code) > 2){
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

    private static void printGhosts(MyGhost[] gs) {
        for (int i = 0; i < gs.length; i++) {
            MyGhost g = gs[i];
            System.out.println(i + ") status: " + g.getStatus() + ",  type: " + g.getType() + ",  pos: " + g.getPos(0) + ",  time: " + g.remainTimeAsEatable(0));
        }
    }

    private static int randomDir() {
        int[] dirs = {MyGame.UP, MyGame.LEFT, MyGame.DOWN, MyGame.RIGHT};
        int ind = (int) (Math.random() * dirs.length);
        return dirs[ind];
    }

    ////////////////////// My Methods ///////////////////////

    // I made them public for testing

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
    public int getDirection(Map2D board, Map2D distanceMap, Pixel2D closestGhost, Pixel2D pacman, String goal, int code, int obsColor) {
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
                case "pink" -> GameInfo.PINK;
                case "green" -> GameInfo.GREEN;
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
            return MyGame.UP; // no reason
        }

        Pixel2D nextMove = path[1];

        int dx = nextMove.getX() - pacman.getX();
        int dy = nextMove.getY() - pacman.getY();
        if (dx == 1 || (GameInfo.CYCLIC_MODE && dx < -1)) return MyGame.RIGHT;
        else if (dx == -1 || (GameInfo.CYCLIC_MODE && dx > 1)) return MyGame.LEFT;
        else if (dy == 1 || (GameInfo.CYCLIC_MODE && dy < -1)) return MyGame.UP;
        return MyGame.DOWN;
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
    public Pixel2D[] findEscapePath(Map2D board, Map2D distanceMap, Pixel2D pacman, Pixel2D ghost, int code, int obsColor) {
        java.util.Map<Pixel2D, Integer> originalValues = new java.util.HashMap<>();

        int[][] directions = {{0,0}, {0,1}, {0,-1}, {1,0}, {-1,0}};

        int width = board.getWidth();
        int height = board.getHeight();

        for (int[] dir : directions) {
            int nx, ny;
            if (GameInfo.CYCLIC_MODE) {
                nx = (ghost.getX() + dir[0] + width) % width;
                ny = (ghost.getY() + dir[1] + height) % height;
            } else {
                nx = ghost.getX() + dir[0];
                ny = ghost.getY() + dir[1];

                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }
            }

            Pixel2D p = new Index2D(nx, ny);
            originalValues.put(p, board.getPixel(nx, ny));
            board.setPixel(nx, ny, obsColor);
        }

        Pixel2D target = getClosest(board, distanceMap, GameInfo.PINK);
        Pixel2D[] path = board.shortestPath(pacman, target, obsColor, GameInfo.CYCLIC_MODE);

        // restore original map
        for (java.util.Map.Entry<Pixel2D, Integer> entry : originalValues.entrySet()) {
            board.setPixel(entry.getKey().getX(), entry.getKey().getY(), entry.getValue());
        }

        if(path == null){
            path = panicMode(board, pacman, ghost,obsColor);
        }
        return path;
    }


    /**
     * Finds the single best move to maximize distance from the ghost
     * when no safe path to a target is available.
     *
     * @param board The current board map
     * @param pacman The current position of the PacMan
     * @param ghost The closest ghost
     * @param obsColor The obstacle color
     */
    public Pixel2D[] panicMode(Map2D board, Pixel2D pacman, Pixel2D ghost, int obsColor) {
        int width = board.getWidth();
        int height = board.getHeight();

        // possible moves: Up, Down, Right, Left
        int[][] moves = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        Pixel2D bestMove = null;
        double maxDistance = -1;

        for (int[] m : moves) {
            int nx, ny;

            if (GameInfo.CYCLIC_MODE) {
                nx = (pacman.getX() + m[0] + width) % width;
                ny = (pacman.getY() + m[1] + height) % height;
            } else {
                nx = pacman.getX() + m[0];
                ny = pacman.getY() + m[1];

                // boundary check for non-cyclic mode
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }
            }

            // check if cell is not an obstacle
            if (board.getPixel(nx, ny) != obsColor) {
                Pixel2D candidate = new Index2D(nx, ny);

                // calculate distance to the ghost
                double distFromGhost = candidate.distance2D(ghost);

                // track of the move that provides the maximum distance
                if (distFromGhost > maxDistance) {
                    maxDistance = distFromGhost;
                    bestMove = candidate;
                }
            }
        }

        // return a path (current position -> best escape move)
        if (bestMove != null) {
            return new Pixel2D[]{pacman, bestMove};
        }

        return null; // no possible moves (trapped)
    }

    /**
     * The goal of the function is to find the distance of the closest ghost
     *
     * @param pacman The Pacman current position.
     * @param ghosts Array of GhostCL
     * @param code Array of GhostCL
     * @return A pixel of the closest ghost
     */
    public MyGhost getClosestGhost(Pixel2D pacman, MyGhost[] ghosts, int code) {
        if (ghosts.length == 0 || pacman == null) return null;
        double minDist = Double.MAX_VALUE;
        MyGhost ghost = null;

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
     * @param distanceMap An all distance common.Map of current board
     * @param pacman The pacman position
     * @param ghost The closest ghost pixel
     * @param code The code of colors
     * @param obsColor Obstacle color of the game
     * @return A number of the distance of the closest ghost
     */
    public boolean isGreenClose(Map2D board, Map2D distanceMap, Pixel2D pacman, Pixel2D ghost, int code, int obsColor ) {
        if(board == null || distanceMap == null || pacman == null || ghost == null) return false;

        Pixel2D green = getClosest(board, distanceMap, GameInfo.GREEN);
        if(green == null) return false;
        int closetsGhostDistance = distanceMap.getPixel(ghost.getX(),ghost.getY());
        int closetsGreenDistance = distanceMap.getPixel(green.getX(),green.getY());

        if(closetsGreenDistance > GameInfo.MAX_GREEN_DISTANCE)
            return false;

        if(!sameDirection(board, pacman, green, ghost, obsColor))
            return true;

        return (closetsGreenDistance < (closetsGhostDistance / 2));
    }

    /**
     * Returns the closest pixel of a given color
     *
     * @param board The board map
     * @param distanceMap An all distance common.Map of current board
     * @param color required color to search the closest
     * @return the closest asking element
     */
    public Pixel2D getClosest(Map2D board, Map2D distanceMap, int color) {
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
     * @param dest1 The first destination
     * @param dest2 The second destination
     * @param obsColor Obstacle color of the game
     * @return boolean sameDirection value
     */
    public boolean sameDirection(Map2D board, Pixel2D src, Pixel2D dest1, Pixel2D dest2, int obsColor) {
        return (board.shortestPath(src,dest1, obsColor, GameInfo.CYCLIC_MODE)[1].equals(board.shortestPath(src,dest2, obsColor, GameInfo.CYCLIC_MODE)[1]));
    }
}