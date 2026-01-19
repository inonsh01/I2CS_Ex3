package MyPacman.server;

import common.Pixel2D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyGhostTest {

    /**
     * checks if the constructor initializes the ghost at the right coordinates
     * and if the string representation is correct
     */
    @Test
    void testInitialization() {
        MyGhost ghost = new MyGhost(5, 10);

        // check pixel object
        assertNotNull(ghost.getPx());
        assertEquals(5, ghost.getPx().getX());
        assertEquals(10, ghost.getPx().getY());

        // check string position "x,y"
        assertEquals("5,10", ghost.getPos(0));
    }

    /**
     * checks if updating the pixel position also updates the string position
     */
    @Test
    void testSetPx() {
        MyGhost ghost = new MyGhost(0, 0);

        // move ghost to 20,20
        ghost.setPx(20, 20);

        assertEquals(20, ghost.getPx().getX());
        // verify string updated automatically
        assertEquals("20,20", ghost.getPos(0));
    }

    /**
     * checks the logic for remembering the previous map value
     */
    @Test
    void testPrevValue() {
        MyGhost ghost = new MyGhost(1, 1);

        // simulate ghost standing on a pink dot (value 2 usually)
        ghost.setPrevValue(2);
        assertEquals(2, ghost.getPrevValue());

        // simulate ghost moving to empty space (value 0)
        ghost.setPrevValue(0);
        assertEquals(0, ghost.getPrevValue());
    }

    /**
     * checks the eatable timer logic
     */
    @Test
    void testEatableTime() {
        MyGhost ghost = new MyGhost(1, 1);

        // default should be 0
        assertEquals(0, ghost.remainTimeAsEatable(0));

        // trigger eatable mode
        ghost.setTimeAsEatable(1);

        // logic sets it to 3
        assertEquals(3.0, ghost.remainTimeAsEatable(0));
    }

    /**
     * checks the helper method that parses string pos to pixel object
     */
    @Test
    void testGetGhostPixelHelper() {
        MyGhost ghost = new MyGhost(15, 5);

        // use the helper function
        Pixel2D result = ghost.getGhostPixel(ghost);

        assertNotNull(result);
        assertEquals(15, result.getX());
        assertEquals(5, result.getY());
    }
}