import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.Graphics;
import java.awt.Color;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Vector;

import Entities.Agent;
import Entities.HeatSource;
import Objects.Walls;

import Math.Bezier;
import Math.Vector2D;
import Utils.PRNG;
import Entities.Shapes;
import Math.RayToSegIntersection;

public class Panel extends JPanel {
    private Graphics _g;
    private int MAX_POPULATION;
    private Agent[] _agents_list;
    // where to place next Entity in array
    // when _next_index >= _entities_list.length -> no space left
    private static int _next_index = 0;

    private Walls _walls;
    private Walls _randWalls;
    private HeatSource _heatSource;

    public Panel(int max_population){
        super();
        MAX_POPULATION = max_population;
        _agents_list = new Agent[MAX_POPULATION];
    }

    public void paintComponent(Graphics g){
        _g = g;
        _g.setColor(Color.black);
        _g.fillRect(0, 0, this.getWidth(), this.getHeight());

        if(_walls != null){ _walls.draw(_g); }
        if(_heatSource != null){ _heatSource.draw(_g); }

        for(int i = 0; i < _next_index; ++i){
            Agent curr_agent = _agents_list[i];
            curr_agent.draw(_g);

            if(_walls != null) {
                curr_agent.collisionState(_walls, _heatSource, _g);
            }
        }
    }

    public Agent getAgent(int index){
        if(index < MAX_POPULATION){
            return _agents_list[index];
        } else {
            System.out.println("Index out of bound in method getAgent");
        }
        return new Agent();
    }

    public Agent[] getAgentList(){ return _agents_list; }
    public Walls getWalls(){ return _walls; }

    public void addHeatSource(Vector2D pos, int width, int height, double angle){
        float[] xCoords = {-width/2F, width/2F, width/2F, -width/2F};
        float[] yCoords = {-height/2F, -height/2F, height/2F, height/2F};
        _heatSource = new HeatSource(pos, angle, xCoords, yCoords);
    }

    public void addAgent(Agent new_entity) {
        if (_next_index < MAX_POPULATION) {
            _agents_list[_next_index++] = new_entity;
        } else {
            System.out.println("Index out of bound in method addAgent");
        }
    }

    public void addWalls(Vector2D ...wallsCoords){
        if(_walls != null) {
            _walls.addWalls(new Walls(wallsCoords));
        } else {
            _walls = new Walls(wallsCoords);
        }
    }

    public void addRandWalls(Agent[] agent_list, int n_walls){
        if(_walls != null) {
            _randWalls = new Walls(n_walls);
            _randWalls.generateWalls(agent_list, this.getWidth(), this.getHeight());
            _walls.addWalls(_randWalls);
        } else {
            _randWalls = new Walls(n_walls);
            _randWalls.generateWalls(agent_list, this.getWidth(), this.getHeight());
            _walls = _randWalls;
        }
    }

    public void saveImage(File file){
        BufferedImage bi = new BufferedImage(this.getSize().width, this.getSize().height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        this.paint(g);
        g.dispose();
        try{
            ImageIO.write(bi,"png",file);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
