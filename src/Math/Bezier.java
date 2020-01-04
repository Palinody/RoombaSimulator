package Math;

public class Bezier {
    /**
     * Determines the existence of any line segment intersection
     * then calculates its exact point
     * We define 2 line segments in termes of 1st degree Bezier
     * parameters:
     * L1 = [[x1], + t [[x2-x1],
     *       [y1]]      [y2-y1]]
     *
     * L2 = [[x3], + u [[x4-x3],
     *      [y3]]       [y4-y3]]
     * */
    // L1 points
    private int _x1; private int _y1;
    private int _x2; private int _y2;
    // L2 points
    private int _x3; private int _y3;
    private int _x4; private int _y4;
    // Bezier parameters t and u
    private float _t, _u;

    public Bezier(Vector2D[] segment1, Vector2D[] segment2){
        // segment 1
        _x1 = segment1[0].getX(); _y1 = segment1[0].getY();
        _x2 = segment1[1].getX(); _y2 = segment1[1].getY();
        // segment 2
        _x3 = segment2[0].getX(); _y3 = segment2[0].getY();
        _x4 = segment2[1].getX(); _y4 = segment2[1].getY();

        _t = (float)((_x1-_x3)*(_y3-_y4)-(_y1-_y3)*(_x3-_x4))
                / ((_x1-_x2)*(_y3-_y4)-(_y1-_y2)*(_x3-_x4));

        _u = - (float)((_x1-_x2)*(_y1-_y3)-(_y1-_y2)*(_x1-_x3))
                / ((_x1-_x2)*(_y3-_y4)-(_y1-_y2)*(_x3-_x4));

        //System.out.println("t: " + _t + ", u: " + _u);
    }

    public float getT(){ return _t; }
    public float getU(){ return _u; }

    public Vector2D getIntersection(){
        if((_t >= 0 && _t <= 1) && (_u >= 0 && _u <= 1)){
            int px = (int)(_x1+_t*(_x2-_x1));
            int py = (int)(_y1+_t*(_y2-_y1));
            return new Vector2D(px, py);
        }
        return new Vector2D(-1, -1);
    }
}
