package Entities;

import Math.Vector2D;
import Math.Bezier;
import Math.RayToSegIntersection;
import Math.Matrix;

import Objects.Walls;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

public class IRSensor extends Shapes {
    private Vector2D _initial_pos;
    private float _ir_length;
    private float _ir_length_curr;
    private Vector2D _intersection_pos;
    private boolean _intersection_occured = false;
    private boolean _length_modified = false;


    public IRSensor(Vector2D origin, double offset_angle, float[] xCoord, float[] yCoord) {
        super(origin, offset_angle, xCoord, yCoord);
        _initial_pos = new Vector2D((int)xCoord[xCoord.length-1], (int)yCoord[yCoord.length-1]);
        _ir_length = (float)Math.sqrt(Math.pow(xCoord[xCoord.length-1], 2) + Math.pow(yCoord[yCoord.length-1], 2));
        _ir_length_curr = _ir_length;
    }

    private void resetIR(float new_dist){
        _ir_length_curr = new_dist;
        float new_x = (float)((new_dist) * Math.cos(Math.PI/4));
        //System.out.println("Error: " + (new_dist * Math.cos(Math.PI/4) - (float)new_x));
        float new_y = (float)((new_dist) * Math.sin(Math.PI/4));
        _original_matrix.set(0, 1, new_x);
        if(_original_matrix.get(1, 1) >= 0) {
            _original_matrix.set(1, 1, new_y);
        } else {
            _original_matrix.set(1, 1, -new_y);
        }
    }
    public float getIRRange(){ return _ir_length; }
    public float getCurrIR(){ return _ir_length_curr; }

    public GeneralPath getOpenedPolyline(){
        transform2D();
        /*
        if(_length_modified){
            _transformed_matrix.set(0, 1, _intersection_pos.getX());
            _transformed_matrix.set(1, 1, _intersection_pos.getY());
        }
        
         */
        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, _original_matrix.getN());
        polyline.moveTo(_transformed_matrix.get(0, 0), _transformed_matrix.get(1, 0));
        polyline.lineTo(_transformed_matrix.get(0, 1), _transformed_matrix.get(1, 1));
        return polyline;
    }

    private void setIR_tip_pos_from_transformed(Vector2D curr_pos){
        _transformed_matrix.set(0, 1, curr_pos.getX());
        _transformed_matrix.set(1, 1, curr_pos.getY());
    }

    public void updateIR(Walls walls, HeatSource heatsource, Graphics g){
        // walls_coords are the coordinates of each points of a wall stacked one after another
        ArrayList<Vector2D> walls_coords = walls.getWallsPos();
        _intersection_occured = false;
        _length_modified = false;
        float shortest_dist = _ir_length;
        for(int n = 0; n < walls_coords.size() / 2; ++n){
            RayToSegIntersection ray = new RayToSegIntersection(new Vector2D[]{_origin, this.getPoint(1).sub(_origin)},
                    new Vector2D[]{walls_coords.get(2*n), walls_coords.get(2*n+1)});
            Vector2D curr_intersection = ray.getIntersection();
            if((curr_intersection.getX() != -1 && curr_intersection.getY() != -1)){
                float rel_dist = ray.getDist();
                _intersection_occured = true;
                if(rel_dist < shortest_dist){
                    g.setColor(Color.yellow);
                    g.drawOval(curr_intersection.getX()-5, curr_intersection.getY()-5, 10, 10);
                    
                    _length_modified = true;
                    shortest_dist = rel_dist;
                    _ir_length_curr = rel_dist;
                    _intersection_pos = new Vector2D(curr_intersection.getX(), curr_intersection.getY());
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
            RayToSegIntersection ray = new RayToSegIntersection(new Vector2D[]{_origin, this.getPoint(1).sub(_origin)},
                    new Vector2D[]{point1, point2});
            Vector2D curr_intersection = ray.getIntersection();
            if((curr_intersection.getX() != -1 && curr_intersection.getY() != -1)){
                float rel_dist = ray.getDist();
                _intersection_occured = true;
                if(rel_dist < shortest_dist){
                    g.setColor(Color.yellow);
                    g.drawOval(curr_intersection.getX()-5, curr_intersection.getY()-5, 10, 10);

                    _length_modified = true;
                    shortest_dist = rel_dist;
                    _ir_length_curr = rel_dist;
                    _intersection_pos = new Vector2D(curr_intersection.getX(), curr_intersection.getY());
                }
            }
        }

        if(_intersection_occured && (shortest_dist < _ir_length)){ resetIR(_ir_length_curr); }
        else { resetIR(_ir_length); }
    }
}
