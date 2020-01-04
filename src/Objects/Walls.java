package Objects;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import Entities.Shapes;
import Entities.Agent;
import Math.Vector2D;
import Utils.PRNG;
import Math.Bezier;
import Entities.Line;
/**
 * Walls.procedural(number of wals, Entities[], Pillars)
 * */
public class Walls {

    private Line[] _walls_list;
    private int _n_shapes;


    private ArrayList<Vector2D> _walls_coords;

    public Walls(int n){
        _n_shapes = n;
        _walls_list = new Line[_n_shapes];

        _walls_coords = new ArrayList<Vector2D>(_n_shapes);
    }

    public Walls(Vector2D ...wallsCoords){
        _n_shapes = wallsCoords.length / 2;
        _walls_list = new Line[_n_shapes];
        _walls_coords = new ArrayList<Vector2D>(_n_shapes);
        for(int n = 0; n < _n_shapes; ++n){
            float[] xCoords = {0F, wallsCoords[2*n+1].getX() - wallsCoords[2*n].getX()};
            float[] yCoords = {0F, wallsCoords[2*n+1].getY() - wallsCoords[2*n].getY()};
            _walls_list[n] = new Line(new Vector2D(wallsCoords[2*n].getX(), wallsCoords[2*n].getY()), 0, xCoords, yCoords);

            _walls_coords.add(wallsCoords[2*n]);
            _walls_coords.add(wallsCoords[2*n+1]);
        }
    }
    
    public ArrayList<Vector2D> getWallsPos() { return _walls_coords; }
    public int getWallsNumber(){ return _n_shapes; }

    public void addWalls(Walls other){
        ArrayList<Vector2D> new_walls_coords = other.getWallsPos();
        Line[] walls_list_temp = new Line[_n_shapes + other.getWallsNumber()];
        for(int n = 0; n < _n_shapes; ++n){
            walls_list_temp[n] = this._walls_list[n];
        }
        for(int n = _n_shapes; n < _n_shapes + other.getWallsNumber(); ++n){
            int rel_n = n - _n_shapes;
            float[] xCoords = {0F, new_walls_coords.get(2*rel_n+1).getX() - new_walls_coords.get(2*rel_n).getX()};
            float[] yCoords = {0F, new_walls_coords.get(2*rel_n+1).getY() - new_walls_coords.get(2*rel_n).getY()};
            walls_list_temp[n] = new Line(new Vector2D(new_walls_coords.get(2*rel_n).getX(), new_walls_coords.get(2*rel_n).getY()), 0, xCoords, yCoords);
        }
        _walls_list = walls_list_temp;
        _walls_coords.addAll(new_walls_coords);
        _n_shapes += other.getWallsNumber();
    }

    /**
     * This method generates and places walls based on
     * space that is not occupied by agents. A wall may
     * traverse a human though.
     * @param agent_list contains all the agents
     *
     * free_space_buffer is an array containing the ranges
     * that can be occupied by the first node of a wall
     *
     * walls are objects made of 2 nodes (they are lines)
     *
     * we generate the second node of the wall after the
     * first point is generated by taking a random direction.
     * We draw an imaginary line until we reach either the
     * world boundaries or an agent and we set the second
     * node randomly on that imaginary line.
     *
     * 2 walls may not overlap.
     * the minimum distance between all the edges and any
     * point of every wall and the boundaries must be greater
     * than the diameter of the agents in order to make sure
     * that they don't get blocked. If it is not the case,
     * make a hole that is greater than the diameter of agents.
     * A broken wall becomes 2 walls.
     * */
    public void generateWalls(Agent[] agent_list, int to_width, int to_height){
        // contains all the vectorized indices that are occupied by (rectangular) agents
        Set<Integer> agents_occupied_space_buffer = new HashSet<>();
        ArrayList<Vector2D> walls_occupied_space_buffer = new ArrayList<Vector2D>();

        // Vector2D[] 2 points that form the lines forming the rectangles UP | right | DOWN | left
        ArrayList<Vector2D[]> occupied_lines = new ArrayList<>(4 * agent_list.length);

        for (Agent agent : agent_list) {
            // we consider that agents occupy a rectangle instead of a circle
            int curr_diameter = agent.getDiameter();
            Vector2D start = agent.getPos();
            Vector2D end = agent.getPos().add(new Vector2D(curr_diameter, curr_diameter));
            //System.out.println("Y: " + start.getY() + ", X: " + start.getX() + " | Y: " + end.getY() + ", X: " + end.getX());
            for(int i_tile = start.getY(); i_tile < end.getY(); ++i_tile){
                for(int j_tile = start.getX(); j_tile < end.getX(); ++j_tile){
                    // vectorized tile coordinates w.r.t. canvas coordinates
                    agents_occupied_space_buffer.add(j_tile + i_tile * to_width);
                }
            }
            Vector2D[] line_of_rect_up = {start, start.add(new Vector2D(curr_diameter, 0))};
            Vector2D[] line_of_rect_right = {start.add(new Vector2D(curr_diameter, 0)), end};
            Vector2D[] line_of_rect_down = {end, start.add(new Vector2D(0, curr_diameter))};
            Vector2D[] line_of_rect_left = {start.add(new Vector2D(0, curr_diameter)), start};
            occupied_lines.add(line_of_rect_up);
            occupied_lines.add(line_of_rect_right);
            occupied_lines.add(line_of_rect_down);
            occupied_lines.add(line_of_rect_left);
        }
        Set<Integer> available_coordinates = new HashSet<>(to_width * to_height);
        for(int i = 0; i < to_height; ++i){
            for(int j = 0; j < to_width; ++j){
                available_coordinates.add(j + i * to_width);
            }
        }
        available_coordinates.removeAll(agents_occupied_space_buffer);
        ArrayList<Integer> available_coordinates_arr = new ArrayList<Integer>(available_coordinates.size());


        available_coordinates_arr.addAll(available_coordinates);
        PRNG prng = new PRNG();
        int r_index;
        for(int n = 0; n < _n_shapes; ++n){
            // getting 1st node coordinates
            r_index = prng.randInt(0, available_coordinates_arr.size());
            int r_start_coord = available_coordinates_arr.get(r_index);
            available_coordinates_arr.remove(r_index);
            // getting 2nd node coordinates
            r_index = prng.randInt(0, available_coordinates_arr.size());
            int r_end_coord = available_coordinates_arr.get(r_index);
            available_coordinates_arr.remove(r_index);
            // we get the first point of the wall
            // converting vectorized coordinates back to matrix coordinates
            int j_start = r_start_coord % to_width;
            int i_start = (r_start_coord - j_start) / to_width;
            int j_end = r_end_coord % to_width;
            int i_end = (r_end_coord - j_end) / to_width;
            Vector2D[] imaginary_line = {new Vector2D(j_start, i_start), new Vector2D(j_end, i_end)};
            // get not available crossed coordinates. Select the one that is the closest to first node and create wall
            ArrayList<Vector2D> crossed_coordinates = new ArrayList<Vector2D>();
            for (Vector2D[] curr_occupied_line : occupied_lines) {
                Bezier bezier_line = new Bezier(imaginary_line, curr_occupied_line);
                Vector2D intersection = bezier_line.getIntersection();
                if (intersection.getX() != -1 && intersection.getY() != -1) {
                    crossed_coordinates.add(intersection);
                }
            }
            Vector2D curr_closest_intersection = imaginary_line[1];
            for (Vector2D crossed_coordinate : crossed_coordinates) {
                int deltaX_curr_closest = curr_closest_intersection.getX() - j_start;
                int deltaY_curr_closest = curr_closest_intersection.getY() - i_start;
                int deltaX_candidate = crossed_coordinate.getX() - j_start;
                int deltaY_candidate = crossed_coordinate.getY() - i_start;
                double dist_curr = Math.sqrt(deltaX_curr_closest * deltaX_curr_closest + deltaY_curr_closest * deltaY_curr_closest);
                double dist_candidate = Math.sqrt(deltaX_candidate * deltaX_candidate + deltaY_candidate * deltaY_candidate);
                if (dist_candidate < dist_curr) {
                    curr_closest_intersection = crossed_coordinate;
                }
            }
            walls_occupied_space_buffer.add(new Vector2D(j_start, i_start));
            walls_occupied_space_buffer.add(new Vector2D(curr_closest_intersection.getX(), curr_closest_intersection.getY()));
            for(int i = 0; i < walls_occupied_space_buffer.size(); i+=2){
                if(i == n*2){ continue; }
                Vector2D line_start = walls_occupied_space_buffer.get(i);
                Vector2D line_end = walls_occupied_space_buffer.get(i+1);
                Bezier bezier_line = new Bezier(new Vector2D[]{new Vector2D(j_start, i_start), curr_closest_intersection}, new Vector2D[]{line_start, line_end});
                Vector2D intersection = bezier_line.getIntersection();
                if (intersection.getX() != -1 && intersection.getY() != -1) {
                    curr_closest_intersection = intersection;
                }
            }
            walls_occupied_space_buffer.set(n*2+1, curr_closest_intersection);
            // finally creating the wall shape object
            float[] xCoords = {0F, curr_closest_intersection.getX() - j_start};
            float[] yCoords = {0F, curr_closest_intersection.getY() - i_start};
            _walls_list[n] = new Line(new Vector2D(j_start, i_start), 0, xCoords, yCoords);
        }
        _walls_coords = walls_occupied_space_buffer;
    }

    public void draw(Graphics g) {
        //g.setColor(Color.red);
        //int radius = 10;
        //for(Vector2D point : _walls){
        //    g.fillOval(point.getX()-radius, point.getY()-radius, 2*radius, 2*radius);
        //}

        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(Color.red);
        for(Shapes wall_shape : _walls_list){
            GeneralPath wall = wall_shape.getOpenedPolyline();
            g2.draw(wall);
        }
    }
}