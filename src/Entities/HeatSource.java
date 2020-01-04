package Entities;

import Math.Vector2D;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class HeatSource extends Shapes {

    public HeatSource(Vector2D origin, double offset_angle, float[] xCoord, float[] yCoord) {
        super(origin, offset_angle, xCoord, yCoord);
        transform2D();
    }

    public void draw(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        GeneralPath heatSource = getPolyline();
        Color start = Color.red;
        Color end = Color.green;
        int centerX = (getPoint(0).getX() + getPoint(2).getX()) / 2;
        int centerY = (getPoint(2).getX() + getPoint(2).getY()) / 2;
        GradientPaint gradient = new GradientPaint(centerX, centerY, start, getPoint(2).getX(), getPoint(2).getY(), end, true);
        g2.setPaint(gradient);
        g2.fill(heatSource);
        g2.draw(heatSource);
    }
}
