# I2CS_Ex3 - Pacman Game Algorithm Design

## Overview
This algorithm controls Pac-Man's movement to eat all pink dots while avoiding ghosts and strategically using green power dots.

## Algorithm Description
### Explanation:
Objective: The primary goal is to "eat" all pink dots on the map.

Ghost Interaction: In every step, the PacMan checks the distance to the ghost. If the ghost is within SAFETY_RANGE, the PacMan chooses one of two options:

Attack: Eat a green dot to make ghosts "eatable" and then hunt them.

Escape: Move away from the ghost toward a "safe zone."

Movement Logic: The PacMan uses the shortestPath algorithm to reach its goal.

### Main Decision Logic

```
function move(game):
    dist[][] = allDistance()
    goal = "pink"
    
    if(closestGhostDistance < SAFETY_RANGE)
        if(closestGhost.remainTimeAsEatable(code) > MIN_TIME_EATABLE)
            if(pacPos.distance2D(closestGhostPixel) < GameInfo.TOO_CLOSE)
                goal = "hunt";
        else if(isGreenClose(dist))
            goal = "green"
        else
            goal = "run"
    return getDirection(goal,dist)
```

### Calculate Direction 
```
function getDirection(goal,dist):

    path = shortestPath(pacman, getClosest(goal, dist))
    if(goal == "run")
            path = findEscapePath(pacman, nearestGhost, nearestPink)
    else if(goal = "hunt")
            path = board.shortestPath(closestGhost)
    else
        path = board.shortestPath(goal == "pink" ? pink : green);
        
    if(path == null || path.size() <= 1)
        return random // explode :)
        
    nextPosition = path[1]
    moveToPosition(nextPosition)
```
### Target Selection
```
function getClosest(color, dist):
    for(i < board_width)
        for(j < board_height)
            if(pixelFromBoard == color && dist.getPixel < closest
                closest = pixel    
    return closest
```

### Green Dot Strategy
```
function isGreenClose(dist):
    green = getClosest("green", dist)
    if(green == null) 
        return false
    
    if(dist[green.x][green.y] > MAX_GREEN_DISTANCE) return false
    
    // safety: check if ghost is blocking the path
    if(!sameDirection(pacman, green, pacman, ghost)) return true 
    return (dist[green.x][green.y] < (ghostDistance / 2))
    
```

### Escape Algorithm
```
// find path avoiding ghost by define them as obstacle
function findEscapePath(pacman, ghost, dist):
    originalValue = board[ghostPos.x][ghostPos.y]
    
    board[ghostPos.x][ghostPos.y] = obsColor 
    path = shortestPath(pacman, getClosest("pink", dist))
    
    // restore board
    board[ghostPos.x][ghostPos.y] = originalValue
    
    if(path == null)
        path = panikMode()
    return path
   
// find the best safe path as a last resort   
function panikMode(board, pacman, ghost):
    maxDist = -1
    bestMove = null

    for each direction in [UP, DOWN, LEFT, RIGHT]:
        nextPos = getNextPosition(pacman, direction)
        if (isOutsideBoundaries(nextPos)) continue
        
        if (board[nextPos] != obsColor):
            currentDist = distance(nextPos, ghost)
            if (currentDist > maxDist):
                maxDist = currentDist
                bestMove = nextPos

    if (bestMove != null) return [pacman, bestMove]
    return null
```

### Ghost
```
function updateGhosts():
    // Ghost behavior algorithm
```


## Data Structures

### Pixel2D Interface
```
interface Pixel2D {
    int getX()
    int getY()
    double distance2D(Pixel2D p2)
    String toString()
}
```

### Index2D Class
```
class Index2D implements Pixel2D {
    private int _x, _y
    
    Index2D(int x, int y)
    Index2D(Pixel2D t)
    
    int getX()
    int getY()
    double distance2D(Pixel2D t)
    boolean equals(Object t)
}
```

## Key Functions

| Function           | Purpose | Input | Output |
|--------------------|---------|-------|--------|
| `game.getGame(0)`  | Get board matrix | Player code | int[][] board |
| `game.getPos(0)`   | Get Pacman position | Player code | Pixel2D position |
| `game.getGhosts(0)` | Get ghost array | Player code | GhostCL[] ghosts |
| `Game.getIntColor()` | Get color codes | Color, code | int colorValue |
| `allDistances()`   | BFS distance matrix (implement) | Game state | int[][] distances |
| `shortestPath()`   | Find path between points (implement) | start, end | Position[] path |
| `sameDirection()`  | Check path alignment (implement) | 4 positions | boolean |

## Algorithm Flow

1. **Assess Threat Level**: Calculate distance to nearest ghost
2. **Priority Decision**:
    - If ghost too close → Escape or seek green dot
    - - If ghosts are “eatable” and close hunt them
    - - In last resort just run away
    - If safe → Target nearest pink dot
3. **Path Planning**: Use BFS to find optimal route
4. **Movement Execution**: Move one step toward target

## Constants
- `SAFETY_RANGE = 5` - Minimum safe distance from ghosts
- `MAX_GREEN_DISTANCE = 5` - Maximum distance to consider green dots
- `MIN_TIME_EATABLE = 2` - Minimum time for chasing after ghosts
- `TOO_CLOSE = 2` - Distance from ghost for hunting

## Game Screenshots
![Normal Gameplay](images/normal_mode.png)
*Pac-Man collecting pink dots while avoiding ghosts*

![Power Mode](images/power_mode.png)
*Pac-Man after eating green dot with vulnerable ghosts*

## Video Demonstration
[![Pac-Man Algorithm Demo](images/demo_thumbnail.png)](videos/pacman_demo.mp4)
*Click to view complete algorithm demonstration*
