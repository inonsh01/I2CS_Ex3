package common;

import Algorithms.Ex3Algo;
import Algorithms.ManualAlgo;
import exe.ex3.game.PacManAlgo;
/**
 * This class contains all the needed parameters for the Pacman game.
 * Make sure you update your details below!
 */
public class GameInfo {
	public static final String MY_ID = "323071977";
	public static final int CASE_SCENARIO = 4; // [0,4]
	public static final long RANDOM_SEED = 31; // Random seed
	public static final boolean CYCLIC_MODE = true;
	public static final int DT = 100; // [20,200]
	public static final double RESOLUTION_NORM = 1.2; // [0.75,1.2]
	private static PacManAlgo _manualAlgo = new ManualAlgo();
	private static PacManAlgo _myAlgo = new Ex3Algo();
//    public static final PacManAlgo ALGO = _manualAlgo;
	public static final PacManAlgo ALGO = _myAlgo;
    public static final int SAFETY_RANGE = (int) Math.floor(CASE_SCENARIO * 2.5);
    public static final int HUNT_RANGE = 2;
    public static final double MIN_TIME_EATABLE = 1.5;
    public static final int MAX_GREEN_DISTANCE = 7;
    public static final int MAP_SIZE = 15;
    public static final int MAP_SIZE_PX = 800;
    public static final int WALL = -1;
    public static final int EMPTY = 0;
    public static final int PINK = 1;
    public static final int GREEN = 2;
    public static final int BLACK = 3;
    public static final int PACMAN = 4;
    public static final int GHOST = 5;
    public static final int GHOST_NUMBER = 2;
}
