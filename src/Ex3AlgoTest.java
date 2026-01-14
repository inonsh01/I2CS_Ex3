import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.util.Arrays;

public class Ex3AlgoTest {
    private Map2D _map;
    private Pixel2D pacman = new Index2D(5,5);;
    private Ex3Algo _algo;
    private int[][] board;
    private int code = 0;
    int blue;

    @BeforeEach
    public void setup() {
        int code = 0;
        blue = Game.getIntColor(Color.BLUE, code);
        int pink = Game.getIntColor(Color.PINK, code);
        int green = Game.getIntColor(Color.GREEN, code);
        int white = Game.getIntColor(Color.WHITE, code);

        board = new int[][] {
            {blue, blue, blue, blue, blue, blue, blue, blue, blue, blue, blue, blue},
            {blue, pink, pink, pink, pink, pink, pink, pink, pink, pink, pink, blue},
            {blue, pink, blue, pink, blue, blue, pink, blue, blue, pink, pink, blue},
            {blue, pink, pink, pink, pink, pink, pink, pink, pink, pink, pink, blue},
            {blue, pink, blue, pink, blue, white, pink, blue, blue, pink, green, blue},
            {blue, pink, pink, pink, pink, pink, pink, pink, pink, pink, pink, blue},
            {blue, pink, blue, pink, blue, blue, pink, blue, blue, pink, pink, blue},
            {blue, pink, pink, pink, pink, pink, pink, pink, pink, pink, pink, blue},
            {blue, green, blue, pink, blue, blue, pink, blue, blue, pink, pink, blue},
            {blue, blue, blue, blue, blue, blue, blue, blue, blue, blue, blue, blue}
        };
        
        _map = new Map(board);
        _algo = new Ex3Algo();
        
        _map = new Map(board);
        _algo = new Ex3Algo();
    }

    @Test
    public void getDirectionTest() {
        System.out.println("Testing getDirection()...");
        
        Pixel2D pacman = new Index2D(5, 5);
        Pixel2D ghost = new Index2D(8, 5);
        int blue = Game.getIntColor(Color.BLUE, code);
        Map2D distanceMap = _map.allDistance(pacman, blue, true);
        
        int direction = _algo.getDirection(_map, distanceMap, ghost, pacman, "pink", code, blue);
        assertTrue(direction >= 1 && direction <= 4);
        
        System.out.println("✓ getDirection() tests passed");
    }

    @Test
    public void findEscapePathTest() {
        System.out.println("Testing findEscapePath()...");
        
        Pixel2D ghost = new Index2D(9, 8);
        Map2D distanceMap = _map.allDistance(pacman, blue, GameInfo.CYCLIC_MODE);
        Pixel2D[] path = _algo.findEscapePath(_map, distanceMap, pacman, ghost, code, blue);
        assertNotNull(path);

        System.out.println("✓ findEscapePath() tests passed");
    }

    @Test
    public void getClosestGhostTest() {
        System.out.println("Testing getClosestGhost()...");

        GhostCL ghost1 = new GhostCL() {
            @Override
            public String getPos(int code) { return "8,5"; }
            @Override
            public int getType() { return 1; }
            @Override
            public double remainTimeAsEatable(int code) { return 2; }
            @Override
            public int getStatus() { return 0; }
            @Override
            public String getInfo() { return ""; }
        };

        GhostCL ghost2 = new GhostCL() {
            @Override
            public String getPos(int code) { return "3,2"; }
            @Override
            public int getType() { return 2; }
            @Override
            public double remainTimeAsEatable(int code) { return 0; }
            @Override
            public int getStatus() { return 0; }
            @Override
            public String getInfo() { return ""; }
        };

        GhostCL[] ghosts = new GhostCL[] { ghost1, ghost2 };

        GhostCL closest = _algo.getClosestGhost(pacman, ghosts, code);

        assertNotNull(closest);
        assertEquals(1, closest.getType());
        
        System.out.println("✓ getClosestGhost() tests passed");
    }

    @Test
    public void isGreenCloseTest() {
        System.out.println("Testing isGreenClose()...");
        
        Pixel2D closeGreen = new Index2D(2, 1);
        Pixel2D farGreen = new Index2D(1, 1);
        Map2D distanceMap = _map.allDistance(pacman, blue, GameInfo.CYCLIC_MODE);

        boolean resultClose = _algo.isGreenClose(_map, distanceMap, pacman, closeGreen, code, blue);
        boolean resultFar = _algo.isGreenClose(_map, distanceMap, pacman, farGreen, code, blue);

        assertTrue(resultClose);
        assertFalse(resultFar);
        
        System.out.println("✓ isGreenClose() tests passed");
    }

    @Test
    public void getClosestTest() {
        System.out.println("Testing getClosest()...");
        
        Map2D distanceMap = _map.allDistance(pacman, blue, true);
        
        int testColor = 1;
        Pixel2D closest = _algo.getClosest(_map, distanceMap, testColor);
        
        if (closest != null) {
            int closestDist = distanceMap.getPixel(closest);
            
            for (int i = 0; i < _map.getWidth(); i++) {
                for (int j = 0; j < _map.getHeight(); j++) {
                    if (_map.getPixel(i, j) == testColor) {
                        int dist = distanceMap.getPixel(i, j);

                        assertTrue(closestDist <= dist);
                    }
                }
            }
        }
        
        System.out.println("✓ getClosest() tests passed");
    }

    @Test
    public void sameDirectionTest() throws Exception {
        System.out.println("Testing sameDirection()...");

        Pixel2D dest1 = new Index2D(3, 2);
        Pixel2D dest2 = new Index2D(1, 2);
        Pixel2D dest3 = new Index2D(7, 3);

        assertTrue(_algo.sameDirection(_map, pacman,dest1, dest2, blue));
        assertFalse(_algo.sameDirection(_map, pacman,dest1, dest3, blue));

        System.out.println("✓ sameDirection() tests passed");

    }
}
