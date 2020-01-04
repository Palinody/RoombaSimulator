package Entities;

import Math.Vector2D;
import Utils.PRNG;
import Math.RayToSegIntersection;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class Vision extends Shapes {
    // distances from the origin of the robot to a seen object
    private float[] _vision_distances;
    // distances converted to pixel intensity [0, 1]
    // the closer the object the closer the pixel intensity is to 1
    // for each pixel: N(distance)
    // there is 1 less element than in _vision_dist because each
    // pixel is computed by averaging 2 neighbour distances
    private float[] _pixel_intensity;
    private float _ray_range = 3000F;

    public Vision(Vector2D origin, double offset_angle, float[] xCoord, float[] yCoord) {
        super(origin, offset_angle, xCoord, yCoord);
        _vision_distances = new float[xCoord.length];
        updateDistances();
        _pixel_intensity = new float[xCoord.length-1];
        updatePixels();
    }

    public float[] getDistances(){
        return _vision_distances;
    }

    public float[] getPixels(){
        return _pixel_intensity;
    }

    public void updateDistances(){
        //PRNG prng = new PRNG();
        for(int n = 0; n < _vision_distances.length; ++n){
            _vision_distances[n] = _ray_range; //prng.uniform(0, 5);
        }
    }

    public void updateDistances(HeatSource heatsource){
        // we add the first point to close the object thus +1
        Vector2D[] points_list = new Vector2D[heatsource.getSize()+1];
        for(int n = 0; n < heatsource.getSize(); ++n){
            points_list[n] = new Vector2D(heatsource.getPoint(n));
        }
        points_list[heatsource.getSize()] = new Vector2D(heatsource.getPoint(0));
        for(int r = 0; r < this.getSize(); ++r){
            Vector2D[] curr_ray_direction = new Vector2D[]{_origin, this.getPoint(r).sub(_origin)};
            float smallest_dist = _ray_range;
            for(int n = 0; n < points_list.length-1; ++n){
                Vector2D p1 = points_list[n];
                Vector2D p2 = points_list[n+1];
                Vector2D[] heatLine = new Vector2D[]{p1, p2};
                RayToSegIntersection ray = new RayToSegIntersection(curr_ray_direction, heatLine);
                Vector2D intersection = ray.getIntersection();
                if(intersection.getX() != -1 && intersection.getY() != -1){
                    float measured_dist = (float)intersection.sub(_origin).getNorm();
                    if(measured_dist < smallest_dist){
                        smallest_dist = measured_dist;
                    }
                }
            }
            _vision_distances[r] = smallest_dist;
        }
        updatePixels();
    }

    public void updatePixels(){
        //updateDistances();
        for(int n = 0; n < _pixel_intensity.length; ++n){
            _pixel_intensity[n] = ((1F - 1F / _ray_range * _vision_distances[n]) + (1F - 1F / _ray_range * _vision_distances[n+1])) / 2F;
        }
    }
}
