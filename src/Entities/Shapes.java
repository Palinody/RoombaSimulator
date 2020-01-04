package Entities;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Vector;

import Math.Vector2D;
import Math.Matrix;
import Math.Bezier;
import Objects.Walls;

public abstract class Shapes {
    /** this._f_matrix contains the coordinates of each key point of shape
     * [[x0, x1, ..., xn],
     *  [y0, y1, ..., yn]]
     * m dim -> always 2
     * n dim -> depends on number of key points
     *
     * we draw the shape centered at origin and store data not shifted
     * by the given origin. We shift it when we get GeneralPath to draw
     *
     * _f_matrix is the original matrix without transformation
     * _transformed_matrix is the result of the transformation applied
     * to it by the transform2D() method with the current origin and angle
     */

    // for a Graphics.Circle, orig is startCoord + diameter/2
    protected Vector2D _origin;
    protected Matrix _rotation_matrix = new Matrix(2, 2);
    protected double _des_angle;
    protected double _offset_angle;
    protected Matrix _original_matrix;
    protected Matrix _transformed_matrix;

    public Shapes(Vector2D origin, double offset_angle, float[] xCoord, float[] yCoord){
        _original_matrix = new Matrix(2, xCoord.length);
        _origin = origin;
        _des_angle = 0;
        _offset_angle = offset_angle;
        _rotation_matrix.set(0, 0, (float)Math.cos(_des_angle+_offset_angle));
        _rotation_matrix.set(0, 1, -(float)Math.sin(_des_angle+_offset_angle));
        _rotation_matrix.set(1, 0, (float)Math.sin(_des_angle+_offset_angle));
        _rotation_matrix.set(1, 1, (float)Math.cos(_des_angle+_offset_angle));
        // we store the non shifted data
        for(int j = 0; j < _original_matrix.getN(); ++j) {
            _original_matrix.set(0, j, xCoord[j]); // j + 0 * _n
            _original_matrix.set(1, j, yCoord[j]); // j + 1 * _n
        }
        _transformed_matrix = new Matrix(2, _original_matrix.getN());
    }

    public Vector2D getOrigin(){ return _origin; }
    public double getCurrAngle(){ return _des_angle; }
    public int getSize(){ return _original_matrix.getN(); }
    public Vector2D getPoint(int index){ return new Vector2D((int)_transformed_matrix.get(0, index), (int)_transformed_matrix.get(1, index)); }

    public void setOrigin(Vector2D new_origin){ _origin = new_origin; }
    public void setAngle(double new_angle){
        _des_angle = (float)new_angle;
        _rotation_matrix.set(0, 0, (float)Math.cos(_des_angle+_offset_angle));
        _rotation_matrix.set(0, 1, -(float)Math.sin(_des_angle+_offset_angle));
        _rotation_matrix.set(1, 0, (float)Math.sin(_des_angle+_offset_angle));
        _rotation_matrix.set(1, 1, (float)Math.cos(_des_angle+_offset_angle));
    }

    public GeneralPath getPolyline(){
        transform2D();
        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, _original_matrix.getN());
        polyline.moveTo(_transformed_matrix.get(0, 0), _transformed_matrix.get(1, 0));
        for(int index = 1; index < _original_matrix.getN(); ++index){
            polyline.lineTo(_transformed_matrix.get(0, index), _transformed_matrix.get(1, index));
        }
        polyline.closePath();
        return polyline;
    }

    public GeneralPath getOpenedPolyline(){
        transform2D();
        GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, _original_matrix.getN());
        polyline.moveTo(_transformed_matrix.get(0, 0), _transformed_matrix.get(1, 0));
        for(int index = 1; index < _original_matrix.getN(); ++index){
            polyline.lineTo(_transformed_matrix.get(0, index), _transformed_matrix.get(1, index));
        }
        return polyline;
    }

    public void transform2D(){
        /**
         * in this method we apply translation and rotation at the same time
         * the original data from _f_matrix is transformed and stored in _transformed_matrix
         * */
        for(int j = 0; j < _original_matrix.getN(); ++j){
            _transformed_matrix.set(0, j, _rotation_matrix.get(0, 0) * _original_matrix.get(0, j) + _rotation_matrix.get(0, 1) * _original_matrix.get(1, j) + _origin.getX());
            _transformed_matrix.set(1, j, _rotation_matrix.get(1, 0) * _original_matrix.get(0, j) + _rotation_matrix.get(1, 1) * _original_matrix.get(1, j) + _origin.getY());
        }
    }
}
