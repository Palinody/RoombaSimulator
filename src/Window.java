import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFrame;

import Entities.Agent;
import Math.Vector2D;
import Utils.ColorPalette;
import Utils.PRNG;
import Math.Matrix;

public class Window extends JFrame {

    public static void main(String[] args){
        new Window();
    }
    private int MAX_POPULATION = 10;
    private Panel _panel = new Panel(MAX_POPULATION);
    // adjust frame width and height according to your preference
    private int _frameWidth = 1080;//1940;
    private int _frameHeight = 720;//1030;
    private int _panelWidth, _panelHeight;
    private PRNG _prng = new PRNG();

    public Window(){
        this.setTitle("Roomba simulator");
        // START: get screen dimensions and set JFrame accordingly
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        //this.pack();
        //this.setSize(screenSize.width,screenSize.height);
        // END: get screen dimensions and set JFrame accordingly
        this.setSize(_frameWidth, _frameHeight);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //this.pack();
        this.setLocationRelativeTo(null);
        this.setContentPane(_panel);
        this.setVisible(true);
        
        _panelWidth = _panel.getWidth();
        _panelHeight = _panel.getHeight();
        this.simulate();
    }
    /**
     * Initializes the entire population with same init pos and angle
     * @param pos x: [radius, panel_width-radius]
     *            y: [radius, panel_height-radius]
     * @param angle [0, 2*PI]
     * @param diameter diameter of the agents body
     * */
    private void initAgents(Vector2D pos, double angle, int diameter){
        for(int i = 0; i < MAX_POPULATION; ++i){
            // random width / height
            int pos_x = pos.getX();
            int pos_y = pos.getY();
            Agent curr_agent = new Agent(pos_x, pos_y, diameter, Color.lightGray, false, 5);
            curr_agent.setInitialConditions(angle, Math.PI/2);
            _panel.addAgent(curr_agent);
        }
    }
    /**
     * Example
     * method addHeatSource: a heat source that the agents vision will detect (thermal camera)
     *
     * method addWalls: each Vector2D is a point of a wall, so define 2 Vector2D per wall
     *                  the method takes a variadic array of Vector2D points so feel free
     *                  to add as many walls as you want.
     *
     * method addRandWalls: places walls at random locations. If you initialise Agents first
     *                      the method will place walls in such a way that they do not cross
     *                      any Agent, automatically.
     *
     * */
    private void initEnvironment(){
        _panel.addHeatSource(new Vector2D(_panelWidth/2, _panelHeight/2), 100, 50, Math.PI/4);
        int margin = 0;
        _panel.addWalls(new Vector2D(margin, margin), new Vector2D(margin, _panelHeight-1-margin),
                new Vector2D(margin, _panelHeight-1-margin), new Vector2D(_panelWidth-1-margin, _panelHeight-1-margin),
                new Vector2D(_panelWidth-1-margin, _panelHeight-1-margin), new Vector2D(_panelWidth-1-margin, margin),
                new Vector2D(_panelWidth-1-margin, margin), new Vector2D(margin, margin));
        //_panel.addWalls(new Vector2D(_panelWidth-1, 0), new Vector2D(0, 0));
        _panel.addRandWalls(_panel.getAgentList(), 5);
    }
    /**
     * Example
     * */
    private void randomBehavior(Agent curr_agent){

        int r = _prng.randInt(0, 2);
        if(r == 0){ curr_agent.turnLeft(10 * Math.PI/180.0); }
        else if(r == 1) {
            curr_agent.turnRight(10 * Math.PI/180.0);
        } else if(r == 2){
            curr_agent.forward();
        } else if(r == 3){
            curr_agent.backward();
        }
    }
    /**
     * COMPUTATIONALLY EXPENSIVE USE WITH CAUTION
     * counter is incremented at every time step of the simulation
     * */
    private void saveState(int counter){
        BufferedImage bi = new BufferedImage(_panel.getSize().width, _panel.getSize().height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        _panel.saveImage(new File("files/simulation_captures/test"+Integer.toString(counter)+".png"));
    }

    private void simulate(){
        System.out.println("x: " + _panel.getWidth() + ", y: " + _panel.getHeight());
        // START: place your agents on the panel
        int diameter = 100;
        Vector2D pos = new Vector2D(
                _prng.randInt(diameter/2, _panelWidth-diameter/2),
                _prng.randInt(diameter/2, _panelHeight-diameter/2)
        );
        double angle = 2 * Math.PI * _prng.uniform(0, 1);
        initAgents(pos, angle, diameter);
        // END: place your agents on the panel
        // place walls etc
        initEnvironment();

        int simulation_counter = 0;
        ColorPalette custom_col = new ColorPalette(255);
        float[] fitnesses = new float[MAX_POPULATION];
        while(true){
            for(int i = 0; i < MAX_POPULATION; ++i){
                Agent curr_agent = _panel.getAgent(i);
                // launching behavior function on current agent
                {
                    randomBehavior(curr_agent);
                }
                // setting the color of the body of the robot w.r.t. the local fitness
                // the color can be mapped from any value in a [0, 0.9999] range
                float curr_fitness = curr_agent.getLocalFitness();
                fitnesses[i] = curr_fitness;
                curr_agent.setColor(custom_col.getHeatMapColorFromProbability(curr_fitness));
                Matrix localState = curr_agent.getLocalState();
            }
            // to know the available CPUs for multithreading
            //int processors = Runtime.getRuntime().availableProcessors();
            //System.out.printf("Available processors: %d \r", processors);
            _panel.repaint();
            try {
                Thread.sleep(5);
                // uncomment to save the simulation state as png in files/simulation_captures/ folder
                // try to make the thread sleep according to your computer performance
                /*
                saveState(simulation_counter);
                ++simulation_counter;
                Thread.sleep(100);
                */
            } catch(Exception e){ e.printStackTrace(); }
        }
    }
}
