package Algorithms;

import MyPacman.server.MyGame;
import MyPacman.server.MyGhost;
import MyPacman.server.MyPacmanGame;
import common.*;
import exe.ex3.game.Game;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

/**
 * The core AI logic for the Pacman client.
 * This class analyzes the game state and determines the optimal move direction
 * based on ghost proximity, remaining dots, and power-ups.
 */
public class MyAlgo {

    private int _count;

    public MyAlgo() {
        _count = 0;
    }

    /**
     * Returns a description of the algorithm's logic.
     *
     * @return A string explanation of the strategy.
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
     * The main execution function called every game tick.
     * Calculates the next move for Pacman.
     *
     * @param game The game interface providing current state.
     *
     * @return The direction code (UP, DOWN, LEFT, RIGHT) for the next move, or -1 if waiting.
     */
    public int move(MyPacmanGame game) {

        // data
        int code = 0;
        int[][] boardData = game.getGame(code);
        MyGhost[] ghosts = game.getGhosts(code);
        String pos = game.getPos(code);

        if (_count == 0 || _count == 300) {
            int blue = Game.getIntColor(Color.BLUE, code);
            int pink = Game.getIntColor(Color.PINK, code);
            int black = Game.getIntColor(Color.BLACK, code);
            int green = Game.getIntColor(Color.GREEN, code);

            printBoard(boardData);
            System.out.println("Blue=" + blue + ", Pink=" + pink + ", Black=" + black + ", Green=" + green);
            System.out.println("Pacman coordinate: " + pos);
        }
        _count = 1;
        if (game.getStatus() != MyPacmanGame.RUNNING) return -1;

        _count++;

        // time management (avoid spamming moves faster than server allows)
        long currentTime = System.currentTimeMillis();
        if (currentTime - MyGame.getLastMoveTime() < MyGame.getMoveDelay()) {
            return -1;
        }

        // map analysis
        Map2D map = new Map(boardData);
        Pixel2D pacPos = parsePosition(pos);

        // calculate distances from Pacman to every other cell (BFS flood fill)
        Map2D distanceMap = map.allDistance(pacPos, GameInfo.WALL, GameInfo.CYCLIC_MODE);

        // decision making
        String goal = "pink"; // Default behavior: Eat dots
        MyGhost closestGhost = getClosestGhost(pacPos, ghosts, code);

        if (closestGhost != null) {
            Pixel2D ghostPixel = closestGhost.getPx();
            double distToGhost = pacPos.distance2D(ghostPixel);

            // THREAT DETECTED
            if (distToGhost < GameInfo.SAFETY_RANGE) {
                if (closestGhost.remainTimeAsEatable(code) >= GameInfo.MIN_TIME_EATABLE && distToGhost < GameInfo.HUNT_RANGE) {
                    goal = "hunt";
                }
                // if ghost is dangerous
                else if (isGreenClose(map, distanceMap, pacPos, ghostPixel, GameInfo.WALL)) {
                    goal = "green";
                } else {
                    goal = "run";
                }
            }
            return getDirection(map, distanceMap, ghostPixel, pacPos, goal, GameInfo.WALL);
        }

        // FIX: handle null if no ghost exists
        return getDirection(map, distanceMap, null, pacPos, goal, GameInfo.WALL);
    }

    // --- Core Navigation Logic ---

    /**
     * Calculates the specific direction Pacman should move based on the decided goal.
     *
     * @param board The game board map
     * @param distanceMap The distance map from Pacman's current position
     * @param closestGhost The pixel position of the nearest ghost
     * @param pacman Pacman's current position
     * @param goal The strategy string ("run", "hunt", "green", "pink")
     * @param obsColor The integer value representing walls
     *
     * @return The direction constant (UP, DOWN, LEFT, RIGHT).
     */
    public int getDirection(Map2D board, Map2D distanceMap, Pixel2D closestGhost, Pixel2D pacman, String goal, int obsColor) {
        Pixel2D[] path = null;

        if (Objects.equals(goal, "run")) {
            if (closestGhost == null) return randomDir();
            path = findSmartPath(board, distanceMap, pacman, closestGhost, goal, obsColor);
        }
        else if (Objects.equals(goal, "hunt")) {
            path = board.shortestPath(pacman, closestGhost, obsColor, GameInfo.CYCLIC_MODE);
        }
        else {
            path = findSmartPath(board, distanceMap, pacman, closestGhost, goal, obsColor);
        }

        // fail-safe: if no path found (trapped or map empty), move randomly
        if (path == null || path.length < 2) {
            return randomDir();
        }

        // calculate direction from current(path[0]) to next(path[1])
        Pixel2D nextMove = path[1];
        int dx = nextMove.getX() - pacman.getX();
        int dy = nextMove.getY() - pacman.getY();

        if (dx == 1 || (GameInfo.CYCLIC_MODE && dx < -1)) return MyGame.RIGHT;
        if (dx == -1 || (GameInfo.CYCLIC_MODE && dx > 1)) return MyGame.LEFT;
        if (dy == 1 || (GameInfo.CYCLIC_MODE && dy < -1)) return MyGame.UP;
        return MyGame.DOWN;
    }

    /**
     * Smart Path: Tries to find a path to a dot while treating the ghost's
     * immediate surroundings as "Walls". If that fails, switches to Panic Mode.
     *
     * @param board The game board map
     * @param distanceMap The distance map from Pacman's current position
     * @param pacman Pacman's current position
     * @param ghost The closest ghost coordianets
     * @param goal The strategy string ("run", "hunt", "green", "pink")
     * @param obsColor The integer value representing walls
     *
     * @return Pixel2D objects array of the path to target
     */
    public Pixel2D[] findSmartPath(Map2D board, Map2D distanceMap, Pixel2D pacman, Pixel2D ghost, String goal, int obsColor) {
        int targetColor = Objects.equals(goal, "green") ? GameInfo.GREEN : GameInfo.PINK;

        java.util.Map<Pixel2D, Integer> originalValues = new HashMap<>();
        int width = board.getWidth();
        int height = board.getHeight();

        // temporarily mark the ghost and its neighbors as obstacles
        if (ghost != null) {
            int[][] directions = {{0, 0}, {0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : directions) {
                int nx = ghost.getX() + dir[0];
                int ny = ghost.getY() + dir[1];

                if (GameInfo.CYCLIC_MODE) {
                    nx = (nx + width) % width;
                    ny = (ny + height) % height;
                } else if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }

                Pixel2D p = new Index2D(nx, ny);
                originalValues.put(p, board.getPixel(nx, ny));
                board.setPixel(nx, ny, obsColor); // make it a wall
            }
        }

        // try to find path to nearest targetColor dot with ghost blocked off
        Pixel2D target = getClosestTarget(board, distanceMap, targetColor);
        Pixel2D[] path = null;

        if (target != null) {
            path = board.shortestPath(pacman, target, obsColor, GameInfo.CYCLIC_MODE);
        }

        // restore map to original state
        for (java.util.Map.Entry<Pixel2D, Integer> entry : originalValues.entrySet()) {
            board.setPixel(entry.getKey().getX(), entry.getKey().getY(), entry.getValue());
        }

        // if 'smart path' failed (trapped), use 'Panic Mode'
        if (path == null && ghost != null) {
            path = panicMode(board, pacman, ghost, obsColor);
        }
        return path;
    }

    /**
     * Panic Mode: Finds the single adjacent cell that maximizes the distance
     * from the ghost. Used when no valid path to a target exists.
     *
     * @param board The game board map
     * @param pacman Pacman's current position
     * @param ghost The closest ghost coordianets
     * @param obsColor The integer value representing walls
     *
     * @return Pixel2D objects array of the path to target
     */
    public Pixel2D[] panicMode(Map2D board, Pixel2D pacman, Pixel2D ghost, int obsColor) {
        if (ghost == null) return null;

        int width = board.getWidth();
        int height = board.getHeight();
        int[][] moves = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}}; // UP, DOWN, RIGHT, LEFT

        Pixel2D bestMove = null;
        double maxDist = -1;

        for (int[] m : moves) {
            int nx = pacman.getX() + m[0];
            int ny = pacman.getY() + m[1];

            if (GameInfo.CYCLIC_MODE) {
                nx = (nx + width) % width;
                ny = (ny + height) % height;
            } else if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                continue;
            }

            // if move is legal (not a wall)
            if (board.getPixel(nx, ny) != obsColor) {
                Pixel2D candidate = new Index2D(nx, ny);
                double dist = candidate.distance2D(ghost);

                if (dist > maxDist) {
                    maxDist = dist;
                    bestMove = candidate;
                }
            }
        }

        if (bestMove != null) {
            return new Pixel2D[]{pacman, bestMove};
        }
        return null; // totally trapped
    }

    // --- Helper Methods ---

    /**
     * Parses a string coordinate "x,y" into a Pixel2D object.
     *
     * @param pos A string coordinates "x,y"
     *
     * @return Pixel2D object with coordinates value
     */
    private Pixel2D parsePosition(String pos) {
        String[] parts = pos.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        return new Index2D(x, y);
    }

    /**
     * Finds the ghost with the minimum distance to Pacman (by allDistance map).
     *
     * @param pacman Pacman's current position
     * @param ghosts The ghosts coordianets
     * @param code
     *
     * @return MyGhost value of the closest ghost
     */
    public MyGhost getClosestGhost(Pixel2D pacman, MyGhost[] ghosts, int code) {
        if (ghosts == null || ghosts.length == 0 || pacman == null) return null;

        double minDist = Double.MAX_VALUE;
        MyGhost closest = null;

        for (MyGhost g : ghosts) {
            Pixel2D gPos = parsePosition(g.getPos(code));
            double dist = pacman.distance2D(gPos);

            if (dist < minDist) {
                minDist = dist;
                closest = g;
            }
        }
        return closest;
    }

    /**
     * Checks if a Green Dot (Power-up) is closer than the ghost,
     * and if it's safe to run towards it.
     *
     * @param board The game board map
     * @param distanceMap The distance map from Pacman's current position
     * @param pacman Pacman's current position
     * @param ghost The closest ghost coordianets
     * @param obsColor The integer value representing walls
     *
     * @return boolean value if there is green close -> by definition
     */
    public boolean isGreenClose(Map2D board, Map2D distanceMap, Pixel2D pacman, Pixel2D ghost, int obsColor) {
        if (board == null || distanceMap == null) return false;

        Pixel2D green = getClosestTarget(board, distanceMap, GameInfo.GREEN);
        if (green == null) return false;

        int distGhost = distanceMap.getPixel(ghost.getX(), ghost.getY());
        int distGreen = distanceMap.getPixel(green.getX(), green.getY());

        // ignore if green is too far away
        if (distGreen > GameInfo.MAX_GREEN_DISTANCE) return false;

        if(!sameDirection(board, pacman, green, ghost, obsColor))
            return true;

        // if same direction return who gets first
        return (distGreen < (distGhost / 2));
    }

    /**
     * Scans the map to find the closest pixel of a specific type (PINK or GREEN)
     * using distance map.
     *
     * @param board The game board map
     * @param distanceMap The distance map from Pacman's current position
     * @param color The target color
     *
     * @return pixel of the closest target
     */
    public Pixel2D getClosestTarget(Map2D board, Map2D distanceMap, int color) {
        if (board == null || distanceMap == null) return null;

        Pixel2D closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                if (board.getPixel(x, y) == color) {
                    int dist = distanceMap.getPixel(x, y);
                    // dist > 0 check ensures reachable (0 is usually the source itself)
                    if (dist > 0 && dist < minDistance) {
                        minDistance = dist;
                        closest = new Index2D(x, y);
                    }
                }
            }
        }
        return closest;
    }

    /**
     * Checks if the first step towards dest1 is the same as the first step towards dest2.
     * Used to avoid running into a ghost while trying to get an item.
     *
     * @param board The game board map
     * @param src The source pixel coordiantes
     * @param dest1 The first destination pixel coordiantes
     * @param dest2 The second destination pixel coordiantes
     * @param obsColor The integer value representing walls
     *
     * @return boolean value isSameDirecation
     */
    public boolean sameDirection(Map2D board, Pixel2D src, Pixel2D dest1, Pixel2D dest2, int obsColor) {
        Pixel2D[] path1 = board.shortestPath(src, dest1, obsColor, GameInfo.CYCLIC_MODE);
        Pixel2D[] path2 = board.shortestPath(src, dest2, obsColor, GameInfo.CYCLIC_MODE);

        if (path1 == null || path1.length < 2 || path2 == null || path2.length < 2) return false;

        return path1[1].equals(path2[1]);
    }

    /**
     * Prints the map to the user
     *
     * @param b 2D array of the game board map
     */
    private static void printBoard(int[][] b) {
        for (int y = 0; y < b[0].length; y++) {
            for (int x = 0; x < b.length; x++) {
                int v = b[x][y];
                System.out.print(v + "\t");
            }
            System.out.println();
        }
    }

    /**
     * Returns random direction
     *
     * @return int direction
     */
    public static int randomDir() {
        int[] dirs = {MyGame.UP, MyGame.LEFT, MyGame.DOWN, MyGame.RIGHT};
        int ind = (int) (Math.random() * dirs.length);
        return dirs[ind];
    }
}