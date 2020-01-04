package Math;

import Entities.Line;

import java.util.Vector;

/**
 * ray:
 *      origin o: [x0, y0]T
 *      direction d: [xd, yd]T
 *
 * line segment between
 *      p1: [x1, y1]T
 *      p2: [x2, y2]T
 *
 * 
 * */
public class RayToSegIntersection {
    // ray data
    private Vector2D o, d;
    // segment data
    private Vector2D a, b;
    // parameters
    private double t1, t2;

    public RayToSegIntersection(Vector2D[] ray, Vector2D[] segment){
        ray[1].normalize();
        o = ray[0]; d = ray[1];
        a = segment[0]; b = segment[1];
    }

    public double getT(){ return t1; }
    public double getU(){ return t2; }

    public float getDist(){
        Vector2D v1 = o.sub(a);
        Vector2D v2 = b.sub(a);
        Vector2D v3 = new Vector2D(-d.getY(), d.getX());
        v3.normalize();

        float dot = (float)(v2.getX()*v3.getXUnit() + v2.getY()*v3.getYUnit());
        if(Math.abs(dot) <= 0){
            return -1;
        }
        float t1 = (float)v2.crossProd(v1) / dot;
        float t2 = (float)(v1.getX()*v3.getXUnit() + v1.getY()*v3.getYUnit()) / dot;
        if(t1 >= 0F && (t2 >= 0F && t2 <= 1F)){
            return t1;
        }
        return -1;
    }

    public Vector2D getIntersection(){
        float t1 = this.getDist();
        if(t1 != -1){
            int x_inter = (int) (o.getX() + t1 * d.getXUnit());
            int y_inter = (int) (o.getY() + t1 * d.getYUnit());
            return new Vector2D(x_inter, y_inter);
        }
        return new Vector2D(-1, -1);
    }
}
