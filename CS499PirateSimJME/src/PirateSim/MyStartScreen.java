/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PirateSim;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class MyStartScreen extends AbstractAppState implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Screen screenHud;
    private SimpleApplication app;
    static Application myApp = new Application();
    float simSpeed = 1; // ties to timeAcceleration in Application
    boolean simPaused = true; // ties to simPaused in Application
    boolean singleStep = false;
    // Initialize to true, when start is pressed, it will "unpause" 
    // Objects for XML Control
    private Slider cargoProb;
    private Slider patrolProb;
    private Slider pirateProb;
    private int speedIndex = 0;
    private float[] simSpeeds;
    private Label simSpeedLabel;
//    private Button slowButton;
//    private Button fastButton;
    Label cargoLabel;
    Label patrolLabel;
    Label pirateLabel;
    String cargoLabelText;
    String patrolLabelText;
    String pirateLabelText;

    /**
     * custom methods
     */
    public void startGame(String nextScreen) {
        nifty.gotoScreen(nextScreen);  // switch to another screen
        simPaused = false; // Will start running the simulation
    }

    public void quitGame() {
        myApp.stop();
    }

    public void increaseSpeed() {
        if(speedIndex+1 == simSpeeds.length){
            // Do nothing
        }
        else{
            speedIndex++;
            simSpeed = simSpeeds[speedIndex];
            updateSpeedLabel();
            
        }
    }

    public void decreaseSpeed() {
        if (speedIndex == 0) {
            // Do nothing
        } else {
            speedIndex--;
            simSpeed = simSpeeds[speedIndex];
            updateSpeedLabel();
        }
    }
    
    public float getSimSpeed(){
        return simSpeed;
    }

    public MyStartScreen() {
        /**
         * Your custom constructor, can accept arguments
         */
    }

    public void changePauseState() {
        if (simPaused) {
            simPaused = false;
        } else {
            simPaused = true;
        }
    }

    /**
     * Nifty GUI ScreenControl methods
     */
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
    
    private void updateSpeedLabel(){
        String speedLabelText = "Sim Speed: " + simSpeeds[speedIndex];
        System.out.println("Sim speed changed to " + 
                String.format("%.1f", simSpeeds[speedIndex]));
        simSpeedLabel.setText(speedLabelText);
    }

    /**
     * jME3 AppState methods
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        myApp = (SimpleApplication) app;
        
        screenHud = nifty.getScreen("hud");
        
        // Initialize array of speeds
        simSpeeds = new float[4];
        simSpeeds[0] = (float) 1.0;
        simSpeeds[1] = (float) 2.0;
        simSpeeds[2] = (float) 5.0;
        simSpeeds[3] = (float) 10.0;

        // Initialize simulation to 1x speed.
        speedIndex = 0;

        // Create objects for XML Controls
        cargoProb = screen.findNiftyControl("cargoSlider", Slider.class);
        patrolProb = screen.findNiftyControl("patrolSlider", Slider.class);
        pirateProb = screen.findNiftyControl("pirateSlider", Slider.class);

        cargoLabel = screen.findNiftyControl("cargoSliderLabel", Label.class);
        patrolLabel = screen.findNiftyControl("patrolSliderLabel", Label.class);
        pirateLabel = screen.findNiftyControl("pirateSliderLabel", Label.class);
        simSpeedLabel = screenHud.findNiftyControl("speedLabel", Label.class);

        // Initilize string representations of labels
        cargoLabelText = "Cargo Probability: " + String.format("%.2f", cargoProb.getValue());
        patrolLabelText = "Patrol Probability: " + String.format("%.2f", patrolProb.getValue());
        pirateLabelText = "Pirate Probability: " + String.format("%.2f", pirateProb.getValue());
        

        // Initialize Slider values
        cargoProb.setMax((float) 1.00);
        cargoProb.setMin((float) 0.00);
        cargoProb.setStepSize((float) .050);
        cargoProb.setValue((float) 0.40);
        cargoProb.setButtonStepSize((float) .050);

        patrolProb.setMax((float) 1.00);
        patrolProb.setMin((float) 0.00);
        patrolProb.setStepSize((float) 0.05);
        patrolProb.setValue((float) 0.20);
        patrolProb.setButtonStepSize((float) 0.05);

        pirateProb.setMax((float) 1.00);
        pirateProb.setMin((float) 0.00);
        pirateProb.setStepSize((float) .05);
        pirateProb.setValue((float) 0.25);
        pirateProb.setButtonStepSize((float) 0.05);

    }

    public double getCargoProb() {
        return (double) this.cargoProb.getValue();
    }

    public double getPatrolProb() {
        return (double) this.patrolProb.getValue();
    }

    public double getPirateProb() {
        return (double) this.pirateProb.getValue();
    }

    @NiftyEventSubscriber(id = "cargoSlider")
    public void onCargoSliderChangedEvent(String id, SliderChangedEvent event) {
        cargoLabelText = "Cargo Probability: " + String.format("%.2f", cargoProb.getValue());
        cargoLabel.setText(cargoLabelText);
    }

    @NiftyEventSubscriber(id = "patrolSlider")
    public void onPatrolSliderChangedEvent(String id, SliderChangedEvent event) {
        patrolLabelText = "Patrol Probability: " + String.format("%.2f", patrolProb.getValue());
        patrolLabel.setText(patrolLabelText);
    }

    @NiftyEventSubscriber(id = "pirateSlider")
    public void onPirateSliderChangedEvent(String id, SliderChangedEvent event) {
        pirateLabelText = "Pirate Probability: " + String.format("%.2f", pirateProb.getValue());
        pirateLabel.setText(pirateLabelText);
    }

    @Override
    public void update(float tpf) {
        /**
         * jME update loop!
         */
    }
}