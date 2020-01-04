package Math;

import java.util.Vector;

public class Vector2D {
    private int _x, _y;

    // coordinates of the unit vector
    private double _x_unit, _y_unit;

    public Vector2D(){
        _x = 0;
        _y = 0;
    }

    public Vector2D(int x, int y){
        _x = x;
        _y = y;
    }

    public Vector2D(Vector2D other){
        _x = other.getX();
        _y = other.getY();
    }

    public int getX(){ return _x; }
    public int getY(){ return _y; }
    public double getXUnit(){ return _x_unit; }
    public double getYUnit(){ return _y_unit; }

    public double getNorm(){ return Math.sqrt(_x*_x + _y*_y); }

    public void setX(int new_x){ _x = new_x; }
    public void setY(int new_y){ _y = new_y; }
    public void setCoord(int new_x, int new_y){ setX(new_x); setY(new_y); }

    public Vector2D add(Vector2D rhs){ return new Vector2D(_x+rhs.getX(), _y+rhs.getY()); }
    public Vector2D add(int new_x, int new_y){ return new Vector2D(_x+new_x, _y+new_y); }
    public Vector2D sub(Vector2D rhs){
        return new Vector2D(_x-rhs.getX(), _y-rhs.getY());
    }
    public Vector2D sub(int new_x, int new_y){
        return new Vector2D(_x-new_x, _y-new_y);
    }

    public void normalize(){
        // normalizing factor
        double u = getNorm();
        _x_unit = (double)_x / u;
        _y_unit = (double)_y / u;
    }

    public int dotProd(Vector2D other){
        return _x*other.getX() + _y*other.getY();
    }

    public int crossProd(Vector2D other){
        return _x * other.getY() - _y * other.getX();
    }
}
