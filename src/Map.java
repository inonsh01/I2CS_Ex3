import java.util.LinkedList;
import java.util.Queue;

/**
 * This class represents a 2D map as a "screen" or a raster matrix or maze over integers.
 * @author boaz.benmoshe
 *
 */
public class Map implements Map2D {
	private int[][] _map;
	private boolean _cyclicFlag = true;
	
	/**
	 * Constructs a w*h 2D raster map with an init value v.
	 * @param w
	 * @param h
	 * @param v
	 */
	public Map(int w, int h, int v) {
        init(w,h, v);
    }
	/**
	 * Constructs a square map (size*size).
	 * @param size
	 */
	public Map(int size) {
        this(size,size, 0);
    }

	/**
	 * Constructs a map from a given 2D array.
	 * @param data
	 */
	public Map(int[][] data) {
		init(data);
	}

	@Override
	public void init(int w, int h, int v) {
		/////// add your code below ///////

        // edge cases
        if (w <= 0 || h <= 0)
            throw new RuntimeException("Map2D init error: wrong dimensions");

        // create new map
        this._map = new int[w][h];

        // insert v to every 'cell'
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                this._map[i][j] = v;
            }
        }

		///////////////////////////////////
	}

	@Override
	public void init(int[][] arr) {
		/////// add your code below ///////

        // edge cases
        if (arr == null || arr.length == 0 || arr[0] == null)
            throw new RuntimeException("Map2D init error: null or empty array");

        // create new map -> width = array length and height = one of the elements in
        // array length
        this._map = new int[arr.length][arr[0].length];

        // deep copy array to map
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                this._map[i][j] = arr[i][j];
            }
        }

		///////////////////////////////////
	}

	@Override
	public int[][] getMap() {
		int[][] ans = null;
		/////// add your code below ///////

        // edge cases
        if (this._map == null)
            return null;

        // get length
        int w = this.getWidth();
        int h = this.getHeight();

        // create new 2D array
        ans = new int[w][h];

        // deep copy map
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                ans[i][j] = this._map[i][j];
            }
        }

		///////////////////////////////////
		return ans;
	}

	@Override
	/////// add your code below ///////
	public int getWidth() {
        return (_map == null || _map.length == 0) ? 0:_map.length ;
    }

	@Override
	/////// add your code below ///////
	public int getHeight() {
        return (this._map == null || this._map.length == 0 || this._map[0] == null) ? 0 : this._map[0].length;
    }

	@Override
	/////// add your code below ///////
	public int getPixel(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
            throw new RuntimeException("Map2D getPixel error: out of bounds");
        return _map[x][y];
    }

	@Override
	/////// add your code below ///////
	public int getPixel(Pixel2D p) {
        return this.getPixel(p.getX(),p.getY());
	}

	@Override
	/////// add your code below ///////
	public void setPixel(int x, int y, int v) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight())
            this._map[x][y] = v;
    }

	@Override
	/////// add your code below ///////
	public void setPixel(Pixel2D p, int v) {
        if (p == null)
            throw new RuntimeException("Map2D setPixel error: null pixel");
        this.setPixel(p.getX(), p.getY(), v);
	}

	@Override
	/** 
	 * Fills this map with the new color (new_v) starting from p.
	 * https://en.wikipedia.org/wiki/Flood_fill
	 */
	public int fill(Pixel2D xy, int new_v) {
		int ans=0;
		/////// add your code below ///////

        // get details
        int width = getWidth();
        int height = getHeight();
        int oldColor = getPixel(xy.getX(), xy.getY());

        // if the color is already the new color, no need to fill
        if (oldColor == new_v)
            return ans;

        Queue<Pixel2D> q = new LinkedList<>();

        // for tracking visited pixels
        boolean[][] visited = new boolean[width][height];

        // add first pixel to queue and to visited list
        q.add(xy);
        visited[xy.getX()][xy.getY()] = true;

        // right, left, down, up
        int[] dx = { 1, -1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };

        while (!q.isEmpty()) {
            // return and remove the head of the queue
            Pixel2D curr = q.poll();

            // paint with color the current pixel and increment count
            setPixel(curr.getX(), curr.getY(), new_v);
            ans++;

            // check the four near neighbors
            for (int i = 0; i < 4; i++) {
                Pixel2D next = getNextPixel(curr, dx[i], dy[i], width, height, this._cyclicFlag);

                if (next != null) {
                    int nx = next.getX();
                    int ny = next.getY();

                    // check if the neighbor has the old color and hasn't been visited
                    if (!visited[nx][ny] && getPixel(nx, ny) == oldColor) {

                        // add pixel to queue and turn visited to true
                        visited[nx][ny] = true;
                        q.add(next);
                    }
                }
            }
        }
		///////////////////////////////////
		return ans;
	}

	@Override
	/**
	 * BFS like shortest the computation based on iterative raster implementation of BFS, see:
	 * https://en.wikipedia.org/wiki/Breadth-first_search
	 */
	public Pixel2D[] shortestPath(Pixel2D p1, Pixel2D p2, int obsColor, boolean cyclic) {
		Pixel2D[] ans = null;  // the result.
		/////// add your code below ///////

        if(p1 == null || p2 == null) return null;

        int width = getWidth();
        int height = getHeight();

        // check if start or end are valid (not obstacles)
        if (getPixel(p1.getX(), p1.getY()) == obsColor || getPixel(p2.getX(), p2.getY()) == obsColor) {
            return null;
        }

        // Special case: start is same as end
        if (p1.equals(p2)) {
            return new Pixel2D[] { p1 };
        }

        Queue<Pixel2D> q = new LinkedList<>();
        q.add(p1);

        // parent[x][y] stores the pixel we came from to reach (x, y)
        Pixel2D[][] parent = new Pixel2D[width][height];

        // mark start as visited
        parent[p1.getX()][p1.getY()] = p1;

        int[] dx = { 1, -1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };

        while (!q.isEmpty()) {
            Pixel2D curr = q.poll();

            // if we reached the target, stop and reconstruct the path
            if (curr.equals(p2)) {
                ans = reconstructPath(p1, p2, parent);
                break;
            }

            for (int i = 0; i < 4; i++) {
                Pixel2D next = getNextPixel(curr, dx[i], dy[i], width, height, cyclic);

                if (next != null) {
                    int nx = next.getX();
                    int ny = next.getY();

                    // check if the neighbor is not an obstacle and hasn't been visited
                    if (parent[nx][ny] == null && getPixel(nx, ny) != obsColor) {
                        parent[nx][ny] = curr;
                        q.add(next);
                    }
                }
            }
        }
		///////////////////////////////////
		return ans;
	}

	@Override
	/////// add your code below ///////
	public boolean isInside(Pixel2D p) {
        return !(p == null || p.getX() < 0 || p.getX() >= this.getWidth() || p.getY() < 0 || p.getY() >= this.getHeight());
    }

	@Override
	/////// add your code below ///////
	public boolean isCyclic() {
		return this._cyclicFlag;
	}

	@Override
	/////// add your code below ///////
	public void setCyclic(boolean cy) {
        this._cyclicFlag = cy;
    }

	@Override
	/////// add your code below ///////
	public Map2D allDistance(Pixel2D start, int obsColor,  boolean cyclic) {
		Map2D ans = null;  // the result.
		/////// add your code below ///////

        // get map dimensions
        int width = getWidth();
        int height = getHeight();

        // create result map with same dimensions, initialize with -1
        ans = new Map(width, height, -1);

        // check if start point is an obstacle
        if (getPixel(start.getX(), start.getY()) == obsColor) {
            return ans;
        }

        // BFS queue and visited tracking
        Queue<Pixel2D> q = new LinkedList<>();
        boolean[][] visited = new boolean[width][height];

        // start point has distance 0
        q.add(start);
        ans.setPixel(start, 0);
        visited[start.getX()][start.getY()] = true;

        // directions: right, left, down, up
        int[] dx = { 1, -1, 0, 0 };
        int[] dy = { 0, 0, 1, -1 };

        while (!q.isEmpty()) {
            // get current pixel and its distance
            Pixel2D curr = q.poll();
            int currDistance = ans.getPixel(curr);

            // check all four neighbors
            for (int i = 0; i < 4; i++) {
                Pixel2D next = getNextPixel(curr, dx[i], dy[i], width, height, cyclic);

                if (next != null) {
                    int nx = next.getX();
                    int ny = next.getY();

                    // if neighbor is not visited and not an obstacle
                    if (!visited[nx][ny] && getPixel(nx, ny) != obsColor) {
                        // set distance and mark as visited
                        ans.setPixel(nx, ny, currDistance + 1);
                        visited[nx][ny] = true;
                        q.add(next);
                    }
                }
            }
        }
		///////////////////////////////////
		return ans;
	}

    ////////////////////// Private Methods ///////////////////////

    /**
     * Computes the next neighbor pixel based on the given direction (dx, dy). If
     * the map is cyclic, it wraps around the boundaries using modulo. If not
     * cyclic, it returns null if the neighbor is outside the map boundaries. *
     *
     * @param curr   The current pixel.
     * @param dx     The change in x direction.
     * @param dy     The change in y direction.
     * @param width  The map width.
     * @param height The map height.
     * @param cyclic Whether the map is cyclic.
     * @return A new Pixel2D representing the neighbor, or null if out of bounds.
     */
    private Pixel2D getNextPixel(Pixel2D curr, int dx, int dy, int width, int height, boolean cyclic) {

        // get the next pixel
        int nx = curr.getX() + dx;
        int ny = curr.getY() + dy;

        if (cyclic) {
            nx = (nx + width) % width;
            ny = (ny + height) % height;
        }
        else {
            // boundary check
            if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                return null;
            }
        }
        return new Index2D(nx, ny);
    }

    /**
     * Reconstructs the path from p2 back to p1 using the parent map.
     *
     * @param p1     The starting pixel.
     * @param p2     The ending pixel.
     * @param parent The parent map.
     *
     * @return An array of Pixel2D representing the path from start to end.
     */
    private Pixel2D[] reconstructPath(Pixel2D p1, Pixel2D p2, Pixel2D[][] parent) {
        LinkedList<Pixel2D> pathList = new LinkedList<>();
        Pixel2D curr = p2;

        // backtrack from end to start
        while (curr != null && !curr.equals(p1)) {

            // add to the front to keep start-to-end order
            pathList.addFirst(curr);

            // update curr to be his parent
            curr = parent[curr.getX()][curr.getY()];
        }

        // add the start pixel
        pathList.addFirst(p1);

        // convert the list to a fixed-size array
        return pathList.toArray(new Pixel2D[0]);
    }
}
