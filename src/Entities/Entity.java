package Entities;

import java.awt.*;
import Math.Vector2D;
import java.util.ArrayList;

public abstract class Entity {

    protected Vector2D _pos = new Vector2D();
    // position after before we check collision
    protected Vector2D _new_pos = new Vector2D();
    protected int _diameter;
    protected Color _color;
    // keeps a trace of the visited coordinates
    protected ArrayList<Vector2D> _trace = new ArrayList<Vector2D>();
    protected boolean _show_trace = false;
    protected int _trace_length;

    public Entity(){
    }

    public Entity(int posX, int posY, int diameter, Color color) {
        _pos.setX(posX);
        _pos.setY(posY);
        _diameter = diameter;
        _color = color;
        _trace.add((new Vector2D(posX, posY)).add(this._diameter/2, this._diameter/2));
    }

    public Entity(Vector2D pos, int diameter, Color color) {
        _pos = pos;
        _diameter = diameter;
        _color = color;
        _trace.add(pos.add(this._diameter/2, this._diameter/2));
    }

    public Entity(int posX, int posY, int diameter, Color color, boolean show_trace, int trace_length){
        _pos.setX(posX);
        _pos.setY(posY);
        _diameter = diameter;
        _color = color;
        _trace.add((new Vector2D(posX, posY)).add(this._diameter/2, this._diameter/2));
        _show_trace = show_trace;
        _trace_length = trace_length;
    }

    public Entity(Vector2D pos, int diameter, Color color, boolean show_trace, int trace_length){
        _pos = pos;
        _diameter = diameter;
        _color = color;
        _trace.add(pos.add(this._diameter/2, this._diameter/2));
        _show_trace = show_trace;
        _trace_length = trace_length;
    }

    public int getPosX(){ return _pos.getX(); }
    public int getPosY(){ return _pos.getY(); }
    public Vector2D getPos(){ return _pos; }
    public int getDiameter(){ return _diameter; }
    public Vector2D getLastTrace(){ return _trace.get(_trace.size()-1); }
    public ArrayList<Vector2D> getTrace(){ return _trace; }

    public void setPosX(int new_posX){ _pos.setX(new_posX); }
    public void setPosY(int new_posY){ _pos.setY(new_posY); }
    public void setDiameter(int new_diameter){ _diameter = new_diameter; }
    public void setPos(Vector2D new_pos){
        _pos = new_pos;
        if(_show_trace){ _trace.add(new_pos.add(this._diameter/2, this._diameter/2)); }
        if(_trace.size() > _trace_length){ _trace.remove(0); }
    }

    public abstract String getID();
    public abstract Color getColor();
    public abstract void setColor(Color new_col);
    public abstract void draw(Graphics g);
}
