package MyPacman.server;

import common.Index2D;
import common.Pixel2D;
import exe.ex3.game.GhostCL;

/**
 * Represents a Ghost entity in the Pacman game.
 * This class handles the ghost's position, state, and interaction with the map
 */
public class MyGhost implements GhostCL {

    private String _ghostPos;
    private Pixel2D _ghostPx;

    private int _prevValue;
    private double _remainTimeAsEatable = 0;

    // --- Constructors ---

    /**
     * Constructs a new Ghost instance at the specified coordinates.
     *
     * @param x The initial X coordinate.
     * @param y The initial Y coordinate.
     */
    public MyGhost(int x, int y) {
        setPx(x, y);
    }

    // --- Interface Methods (GhostCL) ---

    @Override
    public int getType() {
        return 0;
    }

    /**
     * Returns the position of the ghost as a string representation "x,y".
     *
     * @param i Not used in this implementation (legacy parameter).
     *
     * @return A string representing the ghost's position.
     */
    @Override
    public String getPos(int i) {
        return this._ghostPos;
    }

    /**
     * Returns additional info about the ghost.
     *
     * @return Returns an string.
     */
    @Override
    public String getInfo() {
        return "";
    }

    /**
     * Returns the remaining time that this ghost is eatable.
     *
     * @param i Not used in implementation.
     *
     * @return The time in seconds (or ticks) remaining.
     */
    @Override
    public double remainTimeAsEatable(int i) {
        return _remainTimeAsEatable;
    }

    /**
     * Returns the status of the ghost.
     *
     * @return 0
     */
    @Override
    public int getStatus() {
        return 0;
    }

    // --- Getters & Setters ---

    /**
     * Gets the current 2D pixel position of the ghost.
     *
     * @return A Pixel2D object representing the location.
     */
    public Pixel2D getPx() {
        return this._ghostPx;
    }

    /**
     * Sets the position of the ghost to specific X and Y coordinates.
     * Updates both the Pixel2D object and the string representation.
     *
     * @param x The new X coordinate.
     * @param y The new Y coordinate.
     */
    public void setPx(int x, int y) {
        this._ghostPx = new Index2D(x, y);
        setPos(x, y);
    }

    /**
     * Gets the map value that was stored before the ghost moved onto the current cell.
     *
     * @return The integer value of the underlying map cell.
     */
    public int getPrevValue() {
        return this._prevValue;
    }

    /**
     * Stores the value of the map cell the ghost is about to occupy.
     *
     * @param x The value to store (e.g., GameInfo.PINK, GameInfo.EMPTY).
     */
    public void setPrevValue(int x) {
        this._prevValue = x;
    }

    /**
     * Updates the string representation of the ghost's position.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     */
    public void setPos(int x, int y) {
        this._ghostPos = x + "," + y;
    }

    /**
     * Sets the time for which the ghost remains eatable.
     * Currently sets a hardcoded value.
     *
     * @param i The parameter to determine duration.
     */
    public void setTimeAsEatable(int i) {
        this._remainTimeAsEatable = 3;
    }

    // --- Helper Methods ---

    /**
     * Utility method to convert a MyGhost object's string position into a Pixel2D object.
     *
     * @param g The GhostCL object to parse.
     * @return A Pixel2D object representing the ghost's position, or null if input is null.
     */
    public Pixel2D getGhostPixel(MyGhost g) {
        if (g == null) return null;

        String[] gArr = g.getPos(0).split(",");
        int gX = Integer.parseInt(gArr[0]);
        int gY = Integer.parseInt(gArr[1]);
        return new Index2D(gX, gY);
    }
}