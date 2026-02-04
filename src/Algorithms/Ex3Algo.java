package Algorithms;

import common.*;
import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;

import java.awt.*;
import java.util.Objects;

/**
 * This is the major algorithmic class for Ex3 - the PacMan game:
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
        // define vars
        int code = 0, blue, pink, black, green;
        int[][] board = game.getGame(0);

        String goal = "pink";
        String pos = game.getPos(code).toString();

        GhostCL[] ghosts = game.getGhosts(code);

        blue = Game.getIntColor(Color.BLUE, code);
        pink = Game.getIntColor(Color.PINK, code);
        black = Game.getIntColor(Color.BLACK, code);
        green = Game.getIntColor(Color.GREEN, code);

        if (_count == 0 || _count == 300) {
            printBoard(board);
            System.out.println("Blue=" + blue + ", Pink=" + pink + ", Black=" + black + ", Green=" + green);
            System.out.println("Pacman coordinate: " + pos);
            printGhosts(ghosts);
        }
        _count++;

        // get pacman coordinate
        String[] posArr = pos.split(",");
        int pacX = Integer.parseInt(posArr[0]);
        int pacY = Integer.parseInt(posArr[1]);
        Pixel2D pacPos = new Index2D(pacX, pacY);

        // get all distance map
        Map2D _map = new Map(board);
        Map2D distanceMap = _map.allDistance(pacPos, blue, GameInfo.CYCLIC_MODE);

        // get closest ghost
        Pixel2D closestGhostPixel = null;
        GhostCL closestGhost = getClosestGhost(pacPos, ghosts, code);

        if (closestGhost != null) {
            // get distance from closets ghost
            String[] currGhPosArr = closestGhost.getPos(code).split(",");
            closestGhostPixel = new Index2D(Integer.parseInt(currGhPosArr[0]), Integer.parseInt(currGhPosArr[1]));
            double distToGhost = pacPos.distance2D(closestGhostPixel);

            if (distToGhost < GameInfo.SAFETY_RANGE) {
                if (closestGhost.remainTimeAsEatable(code) >= 1.5 && distToGhost < GameInfo.HUNT_RANGE) {
                        goal = "hunt";
                } else if (isGreenClose(_map, distanceMap, pacPos, closestGhostPixel, code, blue)) {
                    goal = "green";
                }
            }
        }

        return getDirection(_map, distanceMap, closestGhostPixel, pacPos, goal, code, blue);
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

        if (Objects.equals(goal, "hunt")) {
            path = board.shortestPath(pacman, closestGhost, obsColor, GameInfo.CYCLIC_MODE);
        } else {
            color = switch (goal) {
                case "pink" -> Game.getIntColor(Color.PINK, code);
                case "green" -> Game.getIntColor(Color.GREEN, code);
                default -> color;
            };

            // get the shortest path to goal
            path = findSmartPath(board, distanceMap, pacman, closestGhost, goal, code, obsColor);
        }

        // if still no path or pacman reached the goal -> explode :)
        if (path == null || path.length <= 1) {
            return randomDir();
        }

        // calculate which direction pacman should go.
        Pixel2D nextMove = path[1];
        int dx = nextMove.getX() - pacman.getX();
        int dy = nextMove.getY() - pacman.getY();
        if (dx == 1 || (GameInfo.CYCLIC_MODE && dx < -1)) return Game.RIGHT;
        else if (dx == -1 || (GameInfo.CYCLIC_MODE && dx > 1)) return Game.LEFT;
        else if (dy == 1 || (GameInfo.CYCLIC_MODE && dy < -1)) return Game.UP;
        return Game.DOWN;
    }

    /**
     * This function's goal is to find the best smart path in case of 'running'
     * while this case the PacMan will try to find the shortest path to a pink dot,
     * using the shortest path algorithm by defining the ghost as an obstacle
     *
     * @param board The game board
     * @param distanceMap The current all distance map
     * @param pacman The current position of the PacMan
     * @param ghost The closest ghost
     * @param goal The strategy string ("run", "hunt", "green", "pink")
     * @param code The colors code
     * @param obsColor The obstacle color
     *
     * @return A path to target avoiding ghosts
     */
    public Pixel2D[] findSmartPath(Map2D board, Map2D distanceMap, Pixel2D pacman, Pixel2D ghost,String goal, int code, int obsColor) {
        java.util.Map<Pixel2D, Integer> originalValues = new java.util.HashMap<>();
        int[][] directions = {{0, 0}, {0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        int targetColor = Objects.equals(goal, "green") ? Game.getIntColor(Color.GREEN, code) : Game.getIntColor(Color.PINK, code);;
        int width = board.getWidth();
        int height = board.getHeight();

        // mark ghost surroundings as obstacles
        for (int[] dir : directions) {
            int nx, ny;
            if (GameInfo.CYCLIC_MODE) {
                nx = (ghost.getX() + dir[0] + width) % width;
                ny = (ghost.getY() + dir[1] + height) % height;
            } else {
                nx = ghost.getX() + dir[0];
                ny = ghost.getY() + dir[1];

                // if next X or next Y is out of bounds
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }
            }

            // store original values and paint next X and Y with obstacle color
            Pixel2D p = new Index2D(nx, ny);
            originalValues.put(p, board.getPixel(nx, ny));
            board.setPixel(nx, ny, obsColor);
        }

        // get shortest path (BFS)
        Pixel2D target = getClosest(board, distanceMap, targetColor);
        Pixel2D[] path = board.shortestPath(pacman, target, obsColor, GameInfo.CYCLIC_MODE);

        // restore original map
        for (java.util.Map.Entry<Pixel2D, Integer> entry : originalValues.entrySet()) {
            board.setPixel(entry.getKey().getX(), entry.getKey().getY(), entry.getValue());
        }

        // if cannot blocked
        if (path == null) {
            path = panicMode(board, pacman, ghost, obsColor);
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
        if (ghost == null) return null;

        int width = board.getWidth();
        int height = board.getHeight();

        // possible moves: up, Down, Right, Left
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
     *
     * @return A pixel of the closest ghost
     */
    public GhostCL getClosestGhost(Pixel2D pacman, GhostCL[] ghosts, int code) {
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
                ghost = ghosts[i];
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
     *
     * @return A number of the distance of the closest ghost
     */
    public boolean isGreenClose(Map2D board, Map2D distanceMap, Pixel2D pacman, Pixel2D ghost, int code, int obsColor) {
        if (board == null || distanceMap == null || pacman == null || ghost == null) return false;

        Pixel2D green = getClosest(board, distanceMap, Game.getIntColor(Color.GREEN, code));
        if (green == null) return false;
        int closetsGhostDistance = distanceMap.getPixel(ghost.getX(), ghost.getY());
        int closetsGreenDistance = distanceMap.getPixel(green.getX(), green.getY());

        if (closetsGreenDistance > GameInfo.MAX_GREEN_DISTANCE)
            return false;

        if (!sameDirection(board, pacman, green, ghost, obsColor))
            return true;

        return (closetsGreenDistance < (closetsGhostDistance / 2));
    }

    /**
     * Returns the closest pixel of a given color
     *
     * @param board The board map
     * @param distanceMap An all distance common.Map of current board
     * @param color required color to search the closest
     *
     * @return the closest asking element
     */
    public Pixel2D getClosest(Map2D board, Map2D distanceMap, int color) {
        if (board == null || distanceMap == null) return null;

        Pixel2D closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {

                if (board.getPixel(i, j) == color) {
                    int dist = distanceMap.getPixel(i, j);
                    if (dist > 0 && dist < minDistance) {
                        minDistance = dist;
                        closest = new Index2D(i, j);
                    }
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
     *
     * @return boolean sameDirection value
     */
    public boolean sameDirection(Map2D board, Pixel2D src, Pixel2D dest1, Pixel2D dest2, int obsColor) {
        return (board.shortestPath(src, dest1, obsColor, GameInfo.CYCLIC_MODE)[1].equals(board.shortestPath(src, dest2, obsColor, GameInfo.CYCLIC_MODE)[1]));
    }
}