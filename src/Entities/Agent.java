package Entities;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import Math.Vector2D;
import Objects.Walls;
import Utils.ColorPalette;
import Utils.PRNG;
import Math.PointToSegmentDist;
import Math.Matrix;

public class Agent extends Entity {

    // {rightBumper, leftBumper, camera, backVisualizer, rightVisualizer, frontVisualizer, leftVisualizer, leftIR, rightIR, Vision}
    private Shapes[] _shapes_list = new Shapes[10];
    private boolean _lBumperState = false;
    private boolean _rBumperState = false;
    private boolean _bVisualizerState = false;
    private boolean _rVisualizerState = false;
    private boolean _fVisualizerState = false;
    private boolean _lVisualizerState = false;
    // angle of the robot
    private double _theta_curr = 0.0;
    private double _theta_dot_curr = 0.0;
    // angle of the camera relatively to angle of robot
    private double _phi_curr = 0.0;
    private double _phi_dot_curr = 0.0;
    private int _wheels_velocity = 10;
    // IR sensors measured distances
    private int _leftIR = 0;
    private int _rightIR = 0;

    public Agent(){
    }

    public Agent(int posX, int posY, int diameter, Color color) {
        super(posX, posY, diameter, color);
        initBumpers();
        initCamera();
        initActionVisualizers();
        initIRSensors();
        initVision();
    }

    public Agent(Vector2D pos, int diameter, Color color) {
        super(pos, diameter, color);
        initBumpers();
        initCamera();
        initActionVisualizers();
        initIRSensors();
        initVision();
    }

    public Agent(int posX, int posY, int diameter, Color color, boolean show_trace, int trace_length) {
        super(posX, posY, diameter, color, show_trace, trace_length);
        initBumpers();
        initCamera();
        initActionVisualizers();
        initIRSensors();
        initVision();
    }

    public Agent(Vector2D pos, int diameter, Color color, boolean show_trace, int trace_length) {
        super(pos, diameter, color, show_trace, trace_length);
        initBumpers();
        initCamera();
        initActionVisualizers();
        initIRSensors();
        initVision();
    }

    public Agent(int posX, int posY, double theta0, double theta_dot0, int diameter, Color color) {
        super(posX, posY, diameter, color);
        _theta_curr = theta0;
        _theta_dot_curr = theta_dot0;
        initBumpers();
        initCamera();
        initActionVisualizers();
        initIRSensors();
        initVision();
    }

    public Agent(Vector2D pos, double theta0, double theta_dot0, int diameter, Color color) {
        super(pos, diameter, color);
        _theta_curr = theta0;
        _theta_dot_curr = theta_dot0;
        initBumpers();
        initCamera();
        initActionVisualizers();
        initIRSensors();
        initVision();
    }

    public Agent(int posX, int posY, double theta0, double theta_dot0, int diameter, Color color, boolean show_trace, int trace_length) {
        super(posX, posY, diameter, color, show_trace, trace_length);
        _theta_curr = theta0;
        _theta_dot_curr = theta_dot0;
        initBumpers();
        initCamera();
        initActionVisualizers();
        initIRSensors();
        initVision();
    }

    public Agent(Vector2D pos, double theta0, double theta_dot0, int diameter, Color color, boolean show_trace, int trace_length) {
        super(pos, diameter, color, show_trace, trace_length);
        _theta_curr = theta0;
        _theta_dot_curr = theta_dot0;
        initBumpers();
        initCamera();
        initActionVisualizers();
        initIRSensors();
        initVision();
    }

    @Override
    public String getID() { return "agent"; }
    @Override
    public Color getColor() {
        return _color;
    }
    public boolean getLBumperState(){ return _lBumperState; }
    public boolean getRBumperState(){ return _rBumperState; }
    public float getLeftIR(){
        IRSensor IR = (IRSensor)_shapes_list[7];
        return IR.getCurrIR();
    }
    public float getRightIR(){
        IRSensor IR = (IRSensor)_shapes_list[8];
        return IR.getCurrIR();
    }

    /**
     * Current state: sensors infos
     * everything is normalized from -1 to 1
     * [0, range] -> [-1, 1]: i / range * 2 - 1
     * */
    public Matrix getLocalState(){
        // _shapes_list content:
        // {rightBumper, leftBumper, camera, backVisualizer, rightVisualizer, frontVisualizer, leftVisualizer, leftIR, rightIR, Vision}
        IRSensor lIR = (IRSensor)_shapes_list[7];
        IRSensor rIR = (IRSensor)_shapes_list[8];
        Vision vision = (Vision)_shapes_list[9];
        float[] pixel_array = vision.getPixels();
        float ir_range = lIR.getIRRange();
        float[] res_array = new float[]{_lBumperState?1F:-1F, _rBumperState?1F:-1F,
                pixel_array[0]*2F-1F, pixel_array[1]*2F-1F, pixel_array[2]*2F-1F, pixel_array[3]*2F-1F,
                lIR.getCurrIR()/ir_range*2F-1F, rIR.getCurrIR()/ir_range*2F-1F};
        // data has been normalized btw [-1, 1]
        return new Matrix(res_array, 1, res_array.length);
    }

    /**
     * Current fitness
     */
    public float getLocalFitness(){
        if(_lBumperState || _rBumperState){
            return 0;
        }
        Vision vision = (Vision)_shapes_list[9];
        float[] pixel_values = vision.getPixels();
        float avg = 0F;
        for(float pixel : pixel_values) {
            avg += pixel;
        }
        return avg / (float)pixel_values.length;
    }

    @Override
    public void setColor(Color new_col) {
        _color = new_col;
    }

    public void setInitialConditions(double theta, double thetaDot){
        _theta_curr = theta;
        _theta_dot_curr = thetaDot;
    }

    private void initBumpers(){
        /**
         * (personal note)
         * [from -> to(init here)][to(init here) -> from]
         * */
        double bumper_offset = (double)_diameter/10.0;
        // divider goes from 1 to infty -> from PI/1 to PI/infty -> from PI to 0
        double from_divider = 1.0F;
        double to_divider = 100.0F;
        int key_points = 100;
        // START: CONSTRUCTING RIGHT BUMPER POINTS
        {
            float[] xCoords_l = new float[2*key_points];
            float[] yCoords_l = new float[2*key_points];
            // START: WITH DIAMETER OFFSET
            // loop never reaches last element
            xCoords_l[key_points-1] = (float) ((_diameter + bumper_offset) / 2F * Math.cos(Math.PI / to_divider));
            yCoords_l[key_points-1] = (float) ((_diameter + bumper_offset) / 2F * Math.sin(Math.PI / to_divider));
            for (int k = key_points - 1; k >= 1; --k) {
                double curr_divider = from_divider + (to_divider - from_divider) / (double) k;
                double angle = Math.PI / curr_divider;
                xCoords_l[key_points-k-1] = (float) ((_diameter + bumper_offset) / 2F * Math.cos(angle));
                yCoords_l[key_points-k-1] = (float) ((_diameter + bumper_offset) / 2F * Math.sin(angle));
            }
            // END: WITH DIAMETER OFFSET
            // START: WITHOUT DIAMETER OFFSET
            xCoords_l[key_points] = (float) (_diameter / 2F * Math.cos(Math.PI / to_divider));
            yCoords_l[key_points] = (float) (_diameter / 2F * Math.sin(Math.PI / to_divider));
            for (int k = 1; k <= key_points-1; ++k) {
                double curr_divider = from_divider + (to_divider - from_divider) / (double) k;
                double angle = Math.PI / curr_divider;
                xCoords_l[key_points+k] = (float) (_diameter / 2F * Math.cos(angle));
                yCoords_l[key_points+k] = (float) (_diameter / 2F * Math.sin(angle));
            }
            // END: WITHOUT DIAMETER OFFSET
            _shapes_list[1] = new Bumper(_pos.add(_diameter/2, _diameter/2), 0.0, xCoords_l, yCoords_l);
        }
        // END: CONSTRUCTING LEFT BUMPER POINTS
        // START: CONSTRUCTING RIGHT BUMPER POINTS
        {
            float[] xCoords_r = new float[2*key_points];
            float[] yCoords_r = new float[2*key_points];
            // loop never reaches last element
            xCoords_r[key_points-1] = (float) ((_diameter + bumper_offset) / 2F * Math.cos(Math.PI / to_divider));
            yCoords_r[key_points-1] = -(float) ((_diameter + bumper_offset) / 2F * Math.sin(Math.PI / to_divider));
            for (int k = key_points - 1; k >= 1; --k) {
                double curr_divider = from_divider + (to_divider - from_divider) / (double) k;
                double angle = Math.PI / curr_divider;
                xCoords_r[key_points-k-1] = (float) ((_diameter + bumper_offset) / 2F * Math.cos(angle));
                yCoords_r[key_points-k-1] = -(float) ((_diameter + bumper_offset) / 2F * Math.sin(angle));
            }
            // START: WITHOUT DIAMETER OFFSET
            xCoords_r[key_points] = (float) (_diameter / 2F * Math.cos(Math.PI / to_divider));
            yCoords_r[key_points] = -(float) (_diameter / 2F * Math.sin(Math.PI / to_divider));
            for (int k = 1; k <= key_points-1; ++k) {
                double curr_divider = from_divider + (to_divider - from_divider) / (double) k;
                double angle = Math.PI / curr_divider;
                xCoords_r[key_points+k] = (float) (_diameter / 2F * Math.cos(angle));
                yCoords_r[key_points+k] = -(float) (_diameter / 2F * Math.sin(angle));
            }
            // END: WITHOUT DIAMETER OFFSET
            _shapes_list[0] = new Bumper(_pos.add(_diameter/2, _diameter/2), 0.0, xCoords_r, yCoords_r);
        }
        // END: CONSTRUCTING RIGHT BUMPER POINTS
    }

    private void initCamera(){
        /**
         * the camera is positioned at center along x
         * and to the left along y
         * */
        float thickness = _diameter/10F;
        float width = _diameter/3F;
        float width_offset = _diameter/8F;
        float thickness_offset = _diameter/8F;
        // up-left -> up-right -> down-right -> down-left
        float[] xCoords = {0F-thickness/2F, thickness/2F, thickness/2F, 0F-thickness/2F};
        float[] yCoords = {0F-width/2F, 0F-width/2F, width/2F, width/2F};
        _shapes_list[2] = new Camera(_pos.add(_diameter/2, _diameter/2), 0/*Math.PI/4.0*/, xCoords, yCoords);
    }

    private void initActionVisualizers(){
        double visualizer_offset = (double)_diameter/5.0;
        // divider goes from 1 to infty -> from PI/1 to PI/infty -> from PI to 0
        // backward: 3PI/4 -> 5PI/4
        // right: 5PI/4 -> 7PI/4
        // forward: -PI/4 -> PI/4
        // left: PI/4 -> 3PI/4
        double angle_offset = 0.03490658503988659; // 2° = 2 * PI / 180° rad
        double from = 3F/4F + angle_offset;
        double to = 1F - angle_offset;
        int key_points = 20;

        // START: CONSTRUCTING FORWARD VIZUALIZER POINTS (3PI/4 -> 5PI/4PI)
        float[] xCoords_b = new float[4*key_points];
        float[] yCoords_b = new float[4*key_points];
        {
            // START: WITH DIAMETER OFFSET (3PI/4 -> PI)
            // loop never reaches last element
            xCoords_b[key_points-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.cos(to * Math.PI));
            yCoords_b[key_points-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.sin(to * Math.PI));
            for (int k = key_points - 1; k >= 1; --k) {
                double curr_multiplier = from + (to - from) / (double) k;
                double angle = curr_multiplier * Math.PI;
                xCoords_b[key_points-k-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.cos(angle));
                yCoords_b[key_points-k-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.sin(angle));
            }
            // END: WITH DIAMETER OFFSET (3PI/4 -> PI)
            from = 1F + angle_offset;
            to = 5F/4F - angle_offset;
            // START: WITH DIAMETER OFFSET (PI -> 5PI/4)
            xCoords_b[2*key_points-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.cos(to * Math.PI));
            yCoords_b[2*key_points-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.sin(to * Math.PI));
            for (int k = key_points - 1; k >= 1; --k) {
                double curr_multiplier = from + (to - from) / (double) k;
                double angle = curr_multiplier * Math.PI;
                xCoords_b[2*key_points-k-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.cos(angle));
                yCoords_b[2*key_points-k-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.sin(angle));
            }
            // END: WITH DIAMETER OFFSET (PI -> 5PI/4)
            // START: WITHOUT DIAMETER OFFSET (5PI/4 -> PI)
            xCoords_b[2*key_points] = (float) (_diameter / 2F * Math.cos(to * Math.PI));
            yCoords_b[2*key_points] = (float) (_diameter / 2F * Math.sin(to * Math.PI));
            for (int k = 1; k <= key_points-1; ++k) {
                double curr_multiplier = from + (to - from) / (double) k;
                double angle = curr_multiplier * Math.PI;
                xCoords_b[2*key_points+k] = (float) (_diameter / 2F * Math.cos(angle));
                yCoords_b[2*key_points+k] = (float) (_diameter / 2F * Math.sin(angle));
            }
            // END: WITHOUT DIAMETER OFFSET (5PI/4 -> PI)
            // START: WITHOUT DIAMETER OFFSET (PI -> 3PI/4)
            from = 3F/4F + angle_offset;
            to = 1F - angle_offset;
            xCoords_b[3*key_points] = (float) (_diameter / 2F * Math.cos(to * Math.PI));
            yCoords_b[3*key_points] = (float) (_diameter / 2F * Math.sin(to * Math.PI));
            for (int k = 1; k <= key_points-1; ++k) {
                double curr_multiplier = from + (to - from) / (double) k;
                double angle = curr_multiplier * Math.PI;
                xCoords_b[3*key_points+k] = (float) (_diameter / 2F * Math.cos(angle));
                yCoords_b[3*key_points+k] = (float) (_diameter / 2F * Math.sin(angle));
            }
            // END: WITHOUT DIAMETER OFFSET (PI -> 3PI/4)
        }
        _shapes_list[3] = new ActionVisualizer(_pos.add(_diameter/2, _diameter/2), 0.0, xCoords_b, yCoords_b);
        // END: CONSTRUCTING FORWARD VIZUALIZER POINTS (3PI/4 -> 5PI/4PI)
        // START: CONSTRUCTING FRONT VISUALIZER POINTS (PI/4 -> 3PI/4)
        {
            // just inverting x coordinates
            for(int j = 0; j < xCoords_b.length; ++j){
                xCoords_b[j] *= (-1);
            }
        }
        // END: CONSTRUCTING RIGHT VISUALIZER POINTS (PI/4 -> 3PI/4)
        _shapes_list[5] = new ActionVisualizer(_pos.add(_diameter/2, _diameter/2), 0.0, xCoords_b, yCoords_b);

        xCoords_b = new float[4*key_points];
        yCoords_b = new float[4*key_points];
        // START: CONSTRUCTING RIGHT VISUALIZER POINTS (PI/4 -> 3PI/4)
        {
            from = 1F/4F + angle_offset;
            to = 1F/2F - angle_offset;
            // START: WITH DIAMETER OFFSET (PI/4 -> 3PI/4)
            // loop never reaches last element
            xCoords_b[key_points-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.cos(to * Math.PI));
            yCoords_b[key_points-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.sin(to * Math.PI));
            for (int k = key_points - 1; k >= 1; --k) {
                double curr_multiplier = from + (to - from) / (double) k;
                double angle = curr_multiplier * Math.PI;
                xCoords_b[key_points-k-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.cos(angle));
                yCoords_b[key_points-k-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.sin(angle));
            }
            // END: WITH DIAMETER OFFSET (2PI/4 -> PI)
            from = 1F/2F + angle_offset;
            to = 3F/4F - angle_offset;
            // START: WITH DIAMETER OFFSET (PI -> 5PI/4)
            xCoords_b[2*key_points-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.cos(to * Math.PI));
            yCoords_b[2*key_points-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.sin(to * Math.PI));
            for (int k = key_points - 1; k >= 1; --k) {
                double curr_multiplier = from + (to - from) / (double) k;
                double angle = curr_multiplier * Math.PI;
                xCoords_b[2*key_points-k-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.cos(angle));
                yCoords_b[2*key_points-k-1] = (float) ((_diameter - visualizer_offset) / 2F * Math.sin(angle));
            }
            // END: WITH DIAMETER OFFSET (PI -> 5PI/4)
            // START: WITHOUT DIAMETER OFFSET (5PI/4 -> PI)
            xCoords_b[2*key_points] = (float) (_diameter / 2F * Math.cos(to * Math.PI));
            yCoords_b[2*key_points] = (float) (_diameter / 2F * Math.sin(to * Math.PI));
            for (int k = 1; k <= key_points-1; ++k) {
                double curr_multiplier = from + (to - from) / (double) k;
                double angle = curr_multiplier * Math.PI;
                xCoords_b[2*key_points+k] = (float) (_diameter / 2F * Math.cos(angle));
                yCoords_b[2*key_points+k] = (float) (_diameter / 2F * Math.sin(angle));
            }
            // END: WITHOUT DIAMETER OFFSET (5PI/4 -> PI)
            from = 1F/4F + angle_offset;
            to = 1F/2F - angle_offset;
            // START: WITHOUT DIAMETER OFFSET (PI/4 -> 3PI/4)
            xCoords_b[3*key_points] = (float) (_diameter / 2F * Math.cos(to * Math.PI));
            yCoords_b[3*key_points] = (float) (_diameter / 2F * Math.sin(to * Math.PI));
            for (int k = 1; k <= key_points-1; ++k) {
                double curr_multiplier = from + (to - from) / (double) k;
                double angle = curr_multiplier * Math.PI;
                xCoords_b[3*key_points+k] = (float) (_diameter / 2F * Math.cos(angle));
                yCoords_b[3*key_points+k] = (float) (_diameter / 2F * Math.sin(angle));
            }
            // END: WITHOUT DIAMETER OFFSET (PI/4 -> 3PI/4)
        }
        // END: CONSTRUCTING RIGHT VISUALIZER POINTS (PI/4 -> 3PI/4)
        _shapes_list[4] = new ActionVisualizer(_pos.add(_diameter/2, _diameter/2), 0.0, xCoords_b, yCoords_b);
        // START: CONSTRUCTING LEFT VISUALIZER POINTS (PI/4 -> 3PI/4)
        {
            // just inverting y coordinates
            for(int j = 0; j < yCoords_b.length; ++j){
                yCoords_b[j] *= (-1);
            }
        }
        // END: CONSTRUCTING RIGHT VISUALIZER POINTS (PI/4 -> 3PI/4)
        _shapes_list[6] = new ActionVisualizer(_pos.add(_diameter/2, _diameter/2), 0.0, xCoords_b, yCoords_b);
    }

    private void initIRSensors(){
        /**
         * DOES NOT HANDLE CAMERA ROTATION YET
         *
         * range of IR sensor: 1500 cm
         * real roomba diameter: 329.9 mm
         * the IR length is normalized w.r.t.
         * the simulated roomba's diameter
         * we set the IR angle at PI/4
         * IR sensors are symetrical
         * */
        float IRLength = 150 / 32F * _diameter;
        float IRLength_x = (float)(IRLength * Math.cos(Math.PI/4));
        float IRLength_y = (float)(IRLength * Math.sin(Math.PI/4));
        float this_x = 0F;
        float this_y = 0F;
        float[] xCoords = {this_x, this_x + IRLength_x};
        float[] yCoords = {this_y, this_y - IRLength_y};
        _shapes_list[7] = new IRSensor(_pos.add(_diameter/2, _diameter/2), 0, xCoords, yCoords);
        xCoords = new float[]{this_x, this_x + IRLength_x};
        yCoords = new float[]{this_y, this_y + IRLength_y};
        _shapes_list[8] = new IRSensor(_pos.add(_diameter/2, _diameter/2), 0, xCoords, yCoords);
    }

    private void initVision(){
        // diameter / 10 is the bumper offset
        float offset = _diameter / 2F + _diameter / 10F;
        double[] key_angles = new double[]{-Math.PI/4.0, -Math.PI/8, 0.0, Math.PI/8.0, Math.PI/4.0};
        float[] xCoords = new float[key_angles.length];
        float[] yCoords = new float[key_angles.length];
        for(int n = 0; n < key_angles.length; ++n){
            xCoords[n] = (float)(offset * Math.cos(key_angles[n]));
            yCoords[n] = (float)(offset * Math.sin(key_angles[n]));
        }
        _shapes_list[9] = new Vision(_pos.add(_diameter/2, _diameter/2), 0, xCoords, yCoords);
    }

    public void turnLeft(double angle){
        _theta_curr -= angle;
        _bVisualizerState = false;
        _rVisualizerState = false;
        _fVisualizerState = false;
        _lVisualizerState = true;
    }

    public void turnRight(double angle){
        _theta_curr += angle;
        _bVisualizerState = false;
        _rVisualizerState = true;
        _fVisualizerState = false;
        _lVisualizerState = false;
    }

    public void forward(){
        int new_x = (int)(_wheels_velocity * Math.cos(_theta_curr));
        int new_y = (int)(_wheels_velocity * Math.sin(_theta_curr));
        this.setPos(this._pos.add(new Vector2D(new_x, new_y)));
        _bVisualizerState = false;
        _rVisualizerState = false;
        _fVisualizerState = true;
        _lVisualizerState = false;
    }

    public void backward(){
        int new_x = -(int)(_wheels_velocity * Math.cos(_theta_curr));
        int new_y = -(int)(_wheels_velocity * Math.sin(_theta_curr));
        this.setPos(this._pos.add(new Vector2D(new_x, new_y)));
        _bVisualizerState = true;
        _rVisualizerState = false;
        _fVisualizerState = false;
        _lVisualizerState = false;
    }

    public void updateAgent(Walls walls, HeatSource heatsource, Graphics g){
        // START: WALLS COLLISION
        ArrayList<Vector2D> walls_coords = walls.getWallsPos();
        for (int n = 0; n < walls_coords.size() / 2; ++n) {
            Vector2D point1 = walls_coords.get(2 * n);
            Vector2D point2 = walls_coords.get(2 * n + 1);
            Point2D p = new Point2D.Double(_pos.getX() + _diameter / 2F, _pos.getY() + _diameter / 2F);
            Point2D p1 = new Point2D.Double(point1.getX(), point1.getY());
            Point2D p2 = new Point2D.Double(point2.getX(), point2.getY());
            double centerDist = PointToSegmentDist.distanceToSegment(p, p1, p2);
            if(centerDist <= _diameter/2F) {
                Point2D closestPoint = PointToSegmentDist.getClosestPoint(p, p1, p2);
                Vector2D intersection = new Vector2D((int) closestPoint.getX(), (int) closestPoint.getY());
                // intersection to center vector
                Vector2D dist_v = _pos.add(_diameter / 2, _diameter / 2).sub(intersection);
                double dist_v_norm = dist_v.getNorm();
                int offset_x = (int) ((_diameter / 2 - centerDist) * dist_v.getX() / dist_v_norm);
                int offset_y = (int) ((_diameter / 2 - centerDist) * dist_v.getY() / dist_v_norm);
                setPos(_pos.add(new Vector2D(offset_x, offset_y)));
            } else {
                setPos(_pos);
            }
        }
        // END: WALLS COLLISION
        // START: HEATSOURCE COLLISION
        // we add the first point to close the object thus +1
        Vector2D[] points_list = new Vector2D[heatsource.getSize()+1];
        for(int n = 0; n < heatsource.getSize(); ++n){
            points_list[n] = new Vector2D(heatsource.getPoint(n));
        }
        points_list[heatsource.getSize()] = new Vector2D(heatsource.getPoint(0));
        for(int n = 0; n < points_list.length-1; ++n){
            // Robot body point
            Point2D p = new Point2D.Double(_pos.getX() + _diameter / 2F, _pos.getY() + _diameter / 2F);
            // heatsource points
            Vector2D point1 = points_list[n];
            Vector2D point2 = points_list[n+1];
            Point2D p1 = new Point2D.Double(point1.getX(), point1.getY());
            Point2D p2 = new Point2D.Double(point2.getX(), point2.getY());
            double centerDist = PointToSegmentDist.distanceToSegment(p, p1, p2);
            if(centerDist <= _diameter/2F) {
                Point2D closestPoint = PointToSegmentDist.getClosestPoint(p, p1, p2);
                Vector2D intersection = new Vector2D((int) closestPoint.getX(), (int) closestPoint.getY());
                // intersection to center vector
                Vector2D dist_v = _pos.add(_diameter / 2, _diameter / 2).sub(intersection);
                double dist_v_norm = dist_v.getNorm();
                int offset_x = (int) ((_diameter / 2 - centerDist) * dist_v.getX() / dist_v_norm);
                int offset_y = (int) ((_diameter / 2 - centerDist) * dist_v.getY() / dist_v_norm);
                setPos(_pos.add(new Vector2D(offset_x, offset_y)));
            } else {
                setPos(_pos);
            }
        }
        // END: HEATSOURCE COLLISION
    }
    
    public void collisionState(Walls walls, HeatSource heatsource, Graphics g){
        // Body of the robot
        updateAgent(walls, heatsource, g);
        // Vision of the robot
        Vision vision = (Vision)_shapes_list[9];
        vision.updateDistances(heatsource);

        // right bumpers
        Bumper lBumper = (Bumper)_shapes_list[1];
        // right bumper
        Bumper rBumper = (Bumper)_shapes_list[0];
        // left IR
        IRSensor lIR = (IRSensor)_shapes_list[7];
        // right IR
        IRSensor rIR = (IRSensor)_shapes_list[8];

        boolean lCollision = lBumper.isColliding(walls, heatsource);
        if(lCollision){ _lBumperState = true;}
        else { _lBumperState = false; }

        boolean rCollision = rBumper.isColliding(walls, heatsource);
        if(rCollision){ _rBumperState = true; }
        else { _rBumperState = false; }

        lIR.updateIR(walls, heatsource, g);
        rIR.updateIR(walls, heatsource, g);
    }

    private void drawBumpers(Graphics2D g2){
        // START: DRAWING LBUMPER
        GeneralPath rbumperFig = _shapes_list[0].getPolyline();
        if(!_rBumperState){
            g2.setPaint(Color.green);
        } else { g2.setPaint(Color.red); }
        g2.fill(rbumperFig);
        g2.draw(rbumperFig);
        // END: DRAWING LBUMPER
        // START: DRAWING RBUMPER
        GeneralPath lbumperFig = _shapes_list[1].getPolyline();
        if(!_lBumperState){
            g2.setPaint(Color.green);
        } else { g2.setPaint(Color.red); }
        g2.fill(lbumperFig);
        g2.draw(lbumperFig);
        // END: DRAWING RBUMPER
    }

    private void drawCamera(Graphics2D g2){
        GeneralPath cameraFig = _shapes_list[2].getPolyline();
        g2.setPaint(Color.blue);
        g2.fill(cameraFig);
        //g2.setStroke(new BasicStroke(10F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(cameraFig);
    }

    private void drawVisualizers(Graphics2D g2){
        GeneralPath bVisuFig = _shapes_list[3].getPolyline();
        if(!_bVisualizerState){
            g2.setPaint(Color.lightGray);
        } else {
            g2.setPaint(Color.blue);
        }
        g2.fill(bVisuFig);
        g2.draw(bVisuFig);
        
        GeneralPath rVisuFig = _shapes_list[4].getPolyline();
        if(!_rVisualizerState){
            g2.setPaint(Color.lightGray);
        } else {
            g2.setPaint(Color.blue);
        }
        g2.fill(rVisuFig);
        g2.draw(rVisuFig);

        GeneralPath fVisuFig = _shapes_list[5].getPolyline();
        if(!_fVisualizerState){
            g2.setPaint(Color.lightGray);
        } else {
            g2.setPaint(Color.blue);
        }
        g2.fill(fVisuFig);
        g2.draw(fVisuFig);

        GeneralPath lVisuFig = _shapes_list[6].getPolyline();
        if(!_lVisualizerState){
            g2.setPaint(Color.lightGray);
        } else {
            g2.setPaint(Color.blue);
        }
        g2.fill(lVisuFig);
        g2.draw(lVisuFig);

    }

    private void drawIRSensors(Graphics2D g2){
        GeneralPath leftIR = _shapes_list[7].getOpenedPolyline();
        g2.setPaint(Color.blue);
        g2.draw(leftIR);

        GeneralPath rightIR = _shapes_list[8].getOpenedPolyline();
        g2.setPaint(Color.blue);
        g2.draw(rightIR);
    }

    private void drawVision(Graphics2D g2){
        Vision vision = (Vision)_shapes_list[9];
        vision.transform2D();
        float[] pixels = vision.getPixels();
        ColorPalette custom_col = new ColorPalette(255);
        int light_radius = _diameter/20;
        for(int n = 0; n < vision.getSize()-1; ++n){
            Vector2D curr_point = vision.getPoint(n);
            Vector2D nextPoint = vision.getPoint(n+1);
            g2.setColor(custom_col.getHeatMapColorFromProbability(0.9999F - pixels[n]));
            g2.fillOval((curr_point.getX()+nextPoint.getX())/2-light_radius, (curr_point.getY()+nextPoint.getY())/2-light_radius, 2*light_radius, 2*light_radius);
            g2.drawLine(curr_point.getX(), curr_point.getY(), nextPoint.getX(), nextPoint.getY());
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        // START: DRAWING TRACE
        // need at least 2 points to draw a line
        if(_trace.size() > 2 && _show_trace){
            for(int i = 1; i < _trace.size(); ++i){
                g.setColor(Color.white);
                g.drawLine(_trace.get(i-1).getX(), _trace.get(i-1).getY(), _trace.get(i).getX(), _trace.get(i).getY());
            }
        }
        // END: DRAWING TRACE
        // right bumper
        _shapes_list[0].setOrigin(_pos.add(_diameter/2, _diameter/2));
        _shapes_list[0].setAngle(_theta_curr);
        // left bumper
        _shapes_list[1].setOrigin(_pos.add(_diameter/2, _diameter/2));
        _shapes_list[1].setAngle(_theta_curr);
        // camera
        _shapes_list[2].setOrigin(_pos.add(_diameter/2, _diameter/2));
        PRNG prng = new PRNG();
        _shapes_list[2].setAngle(_theta_curr/* + 2.0*Math.PI*(double)prng.uniform(0, 1) */);
        // back visualizer
        _shapes_list[3].setOrigin(_pos.add(_diameter/2, _diameter/2));
        _shapes_list[3].setAngle(_theta_curr);
        // left visualizer
        _shapes_list[4].setOrigin(_pos.add(_diameter/2, _diameter/2));
        _shapes_list[4].setAngle(_theta_curr);
        // front visualizer
        _shapes_list[5].setOrigin(_pos.add(_diameter/2, _diameter/2));
        _shapes_list[5].setAngle(_theta_curr);
        // right visualizer
        _shapes_list[6].setOrigin(_pos.add(_diameter/2, _diameter/2));
        _shapes_list[6].setAngle(_theta_curr);
        // left IR sensor
        _shapes_list[7].setOrigin(_pos.add(_diameter/2, _diameter/2));
        _shapes_list[7].setAngle(_theta_curr);
        // right IR sensor
        _shapes_list[8].setOrigin(_pos.add(_diameter/2, _diameter/2));
        _shapes_list[8].setAngle(_theta_curr);
        // vision
        _shapes_list[9].setOrigin(_pos.add(_diameter/2, _diameter/2));
        _shapes_list[9].setAngle(_theta_curr);

        g.setColor(getColor());
        g.fillOval(getPosX(), getPosY(), getDiameter(), getDiameter());
        drawBumpers(g2);
        drawIRSensors(g2);
        drawCamera(g2);
        // vizualizers
        drawVisualizers(g2);
        // vision
        drawVision(g2);
    }
}