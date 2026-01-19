package Algorithms;

import MyPacman.server.MyGhost;
import common.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyAlgoTest {

    MyAlgo algo = new MyAlgo();

    /**
     * simple check to see if random works
     */
    @Test
    void testRandomDir() {
        for (int i = 0; i < 20; i++) {
            int dir = MyAlgo.randomDir();
            // direction must be between 1 and 4 (up, left, down, right)
            assertTrue(dir >= 1 && dir <= 4);
        }
    }

    /**
     * checks if the algo finds the closest ghost correctly
     */
    @Test
    void testGetClosestGhost() {
        // create two ghosts
        MyGhost g1 = new MyGhost(2, 2);
        MyGhost g2 = new MyGhost(10, 10);
        MyGhost[] ghosts = {g1, g2};

        // pacman is at (3,3), so g1 is closer
        Pixel2D pacman = new Index2D(3, 3);

        MyGhost result = algo.getClosestGhost(pacman, ghosts, 0);

        // should return g1
        assertEquals(g1, result);
    }

    /**
     * another check where the second ghost is closer
     */
    @Test
    void testGetClosestGhost2() {
        MyGhost g1 = new MyGhost(0, 0);
        MyGhost g2 = new MyGhost(5, 5);
        MyGhost[] ghosts = {g1, g2};

        // pacman is at (5,4), closer to g2
        Pixel2D pacman = new Index2D(5, 4);

        MyGhost result = algo.getClosestGhost(pacman, ghosts, 0);

        assertEquals(g2, result);
    }

    /**
     * checks if the logic for green dot priority works
     * verifies that if green is close, it returns true
     */
    @Test
    void testIsGreenClose() {
        // setup a dummy map (20x20)
        int[][] mapData = new int[20][20];
        // 0 = empty, 1 = wall
        for(int i=0; i<20; i++) {
            for(int j=0; j<20; j++) {
                mapData[i][j] = 0; // empty
            }
        }

        // set green dot at (2,2)
        mapData[2][2] = GameInfo.GREEN;

        Map2D board = new Map(mapData);
        Pixel2D pacman = new Index2D(2, 3); // very close to green
        Pixel2D ghost = new Index2D(10, 10); // ghost is far

        // calculate distances
        Map2D distMap = board.allDistance(pacman, GameInfo.WALL, false);

        // checking logic
        boolean result = algo.isGreenClose(board, distMap, pacman, ghost, GameInfo.WALL);

        assertTrue(result);
    }
}