package MyPacman.server;

import common.Index2D;
import common.Pixel2D;
import exe.ex3.game.GhostCL;

public class MyGhost implements GhostCL {

    private String _ghostPos;
    private Pixel2D _ghostPx;
    private int _prevValue;
    private double _remainTimeAsEatable = 0;

    public MyGhost(int x, int y) {
        setPx(x, y);
    }

    @Override
    public int getType() {
        return 0;
    }

    public Pixel2D getPx() {
        return this._ghostPx;
    }

    public void setPx(int x, int y) {
        this._ghostPx = new Index2D(x, y);
        setPos(x, y);
    }

    public int getPrevValue() {
        return this._prevValue;
    }

    public void setPrevValue(int x) {
        this._prevValue = x;
    }

    @Override
    public String getPos(int i) {
        return this._ghostPos;
    }

    public void setPos(int x, int y) {
        this._ghostPos = x + "," + y;
    }

    @Override
    public String getInfo() {
        return "";
    }

    @Override
    public double remainTimeAsEatable(int i) {
        return _remainTimeAsEatable;
    }

    public void setTimeAsEatable(int i) {
       this._remainTimeAsEatable = 3;
    }

    @Override
    public int getStatus() {
        return 0;
    }

    public Pixel2D getGhostPixel(GhostCL g) {
        if(g == null) return null;

        String[] gArr =  g.getPos(0).split(",");
        int gX = Integer.parseInt(gArr[0]);
        int gY = Integer.parseInt(gArr[1]);
        return new Index2D(gX, gY);
    }
}
