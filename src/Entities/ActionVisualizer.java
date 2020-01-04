package Entities;

import Math.Vector2D;

public class ActionVisualizer extends Shapes {

    private boolean _state = false;

    public ActionVisualizer(Vector2D origin, double offset_angle, float[] xCoord, float[] yCoord) {
        super(origin, offset_angle, xCoord, yCoord);
    }

    public boolean getState(){ return _state; }
    
    public void setState(boolean new_state){ _state = new_state; }
}
