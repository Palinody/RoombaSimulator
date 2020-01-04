package Entities;

import Math.Vector2D;
import Math.Bezier;
import Objects.Walls;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Vector;

public class Bumper extends Shapes {

    private boolean _state = false;

    public Bumper(Vector2D origin, double offset_angle, float[] xCoord, float[] yCoord) {
        super(origin, offset_angle, xCoord, yCoord);
    }

    public boolean getState(){ return _state; }
    public void setState(boolean new_state){ _state = new_state; }
    
    public boolean isColliding(Walls walls, HeatSource heatsource/*Agent agent*/){
        ArrayList<Vector2D> walls_coords = walls.getWallsPos();
        // iterating through segments (walls)
        for (int n = 0; n < walls_coords.size() / 2; ++n) {
            Vector2D point1 = walls_coords.get(2 * n);
            Vector2D point2 = walls_coords.get(2 * n + 1);
            Vector2D[] segment1 = new Vector2D[]{point1, point2};
            Vector2D[] segment2 = new Vector2D[2];
            // iterating over polygon (bumper) {iterating over half -> front part of bumper only}
            for(int j = 0; j < _transformed_matrix.getN() / 4; ++j){
                if(j % 1 == 0){
                    float x_start = _transformed_matrix.get(0, 2*j);
                    float y_start = _transformed_matrix.get(1, 2*j);
                    float x_end = _transformed_matrix.get(0, 2*j+1);
                    float y_end = _transformed_matrix.get(1, 2*j+1);
                    segment2[0] = new Vector2D((int)x_start, (int)y_start);
                    segment2[1] = new Vector2D((int)x_end, (int)y_end);
                    Bezier bezier_curve = new Bezier(segment1, segment2);
                    Vector2D intersection = bezier_curve.getIntersection();
                    if(intersection.getX() != -1 && intersection.getY() != -1){
                        return true;
                    }
                }
            }
        }
        // START: HEATSOURCE COLLISION
        // we add the first point to close the object thus +1
        Vector2D[] points_list = new Vector2D[heatsource.getSize()+1];
        for(int n = 0; n < heatsource.getSize(); ++n){
            points_list[n] = new Vector2D(heatsource.getPoint(n));
        }
        points_list[heatsource.getSize()] = new Vector2D(heatsource.getPoint(0));
        for(int n = 0; n < points_list.length-1; ++n) {
            // heatsource points
            Vector2D point1 = points_list[n];
            Vector2D point2 = points_list[n + 1];
            Vector2D[] segment1 = new Vector2D[]{point1, point2};
            Vector2D[] segment2 = new Vector2D[2];
            // iterating over polygon (bumper) {iterating over half -> front part of bumper only}
            for(int j = 0; j < _transformed_matrix.getN() / 4; ++j){
                if(j % 1 == 0){
                    float x_start = _transformed_matrix.get(0, 2*j);
                    float y_start = _transformed_matrix.get(1, 2*j);
                    float x_end = _transformed_matrix.get(0, 2*j+1);
                    float y_end = _transformed_matrix.get(1, 2*j+1);
                    segment2[0] = new Vector2D((int)x_start, (int)y_start);
                    segment2[1] = new Vector2D((int)x_end, (int)y_end);
                    Bezier bezier_curve = new Bezier(segment1, segment2);
                    Vector2D intersection = bezier_curve.getIntersection();
                    if(intersection.getX() != -1 && intersection.getY() != -1){
                        return true;
                    }
                }
            }
        }
        // END: HEATSOURCE COLLISION
        return false;
        }
}
