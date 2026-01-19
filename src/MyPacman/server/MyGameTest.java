package MyPacman.server;

import common.GameInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyGameTest {

    /**
     * checks that init resets the game correctly
     * verifies status is INIT and map is created
     */
    @Test
    void testInit() {
        MyGame game = new MyGame();
        game.init();

        assertEquals(MyGame.INIT, game.getStatus());
        assertNotNull(game.getGame(0));
    }

    /**
     * checks if play() changes status to RUNNING
     */
    @Test
    void testPlay() {
        MyGame game = new MyGame();
        game.init();
        game.play();

        assertEquals(MyGame.RUNNING, game.getStatus());
    }

    /**
     * checks getter and setter for pinks counter
     */
    @Test
    void testSetGetPinksCounter() {
        MyGame game = new MyGame();
        game.init();

        // set a random number
        game.setPinksCounter(50);
        assertEquals(50, game.getPinksCounter());

        // set another number
        game.setPinksCounter(10);
        assertEquals(10, game.getPinksCounter());
    }

    /**
     * checks getter and setter for predator mode
     */
    @Test
    void testPredatorMode() {
        MyGame game = new MyGame();
        // default should be false
        assertFalse(MyGame.getIsPredator());

        // change to true
        game.setIsPredator(true);
        assertTrue(MyGame.getIsPredator());

        // change back
        game.setIsPredator(false);
        assertFalse(MyGame.getIsPredator());
    }

    /**
     * checks that we can retrieve the game board array
     * and that it has the correct size
     */
    @Test
    void testGetGame() {
        MyGame game = new MyGame();
        game.init();

        int[][] board = game.getGame(0);
        assertNotNull(board);
        // map size should be what is defined in GameInfo
        assertEquals(GameInfo.MAP_SIZE, board.length);
        assertEquals(GameInfo.MAP_SIZE, board[0].length);
    }

    /**
     * checks if getPos returns a string in "x,y" format
     */
    @Test
    void testGetPos() {
        MyGame game = new MyGame();
        game.init();

        String pos = game.getPos(0);
        assertNotNull(pos);
        // check if it contains a comma
        assertTrue(pos.contains(","));
    }

    /**
     * checks if getGhosts returns the correct array
     */
    @Test
    void testGetGhosts() {
        MyGame game = new MyGame();
        game.init();

        MyGhost[] ghosts = game.getGhosts(0);
        assertNotNull(ghosts);
        assertEquals(GameInfo.GHOST_NUMBER, ghosts.length);
    }

    /**
     * checks if getGhostIndex finds a ghost correctly
     * we know ghosts start at the center
     */
    @Test
    void testGetGhostIndex() {
        MyGame game = new MyGame();
        game.init();

        int mid = GameInfo.MAP_SIZE / 2;
        // check index at the center (ghost house)
        int idx = MyGame.getGhostIndex(mid, mid);

        // should be a valid index (0 or greater)
        assertTrue(idx >= 0);

        // check index at 0,0 (should be empty/wall)
        int idxEmpty = MyGame.getGhostIndex(0, 0);
        assertEquals(-1, idxEmpty);
    }

    /**
     * checks if getPacmanDir returns the last direction set
     * (note: might require waiting for delay in real run,
     * here just checking the getter exists)
     */
    @Test
    void testGetPacmanDir() {
        // default direction is RIGHT (4)
        assertEquals(4, MyGame.getPacmanDir());
    }

    /**
     * checks initial state of static win/lose flags
     */
    @Test
    void testWinLoseFlags() {
        // purely checking they start as false
        assertFalse(MyGame.getWin());
        assertFalse(MyGame.getLose());
    }

    /**
     * checks the constants values (sanity check)
     */
    @Test
    void testConstants() {
        assertEquals(0, MyGame.INIT);
        assertEquals(1, MyGame.RUNNING);
        assertEquals(2, MyGame.DONE);
    }
}