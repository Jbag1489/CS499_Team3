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
    Simulation simStartScreen;
    long seed;
    float simSpeed = 1; // ties to timeAcceleration in Application
    boolean simPaused = true; // ties to simPaused in Application
    boolean singleStep = false;
    // Initialize to true, when start is pressed, it will "unpause" 
    // Objects for XML Control
    int width = 20;
    int height = 10;
    private Slider simWidth;
    private Slider simHeight;
    private Slider cargoProb;
    private Slider patrolProb;
    private Slider pirateProb;
    private int speedIndex = 0;
    private float[] simSpeeds;
    private Label simSpeedLabel;
    Label cargoLabel;
    Label patrolLabel;
    Label pirateLabel;
    Label widthLabel;
    Label heightLabel;
    String cargoLabelText;
    String patrolLabelText;
    String pirateLabelText;
    String witdthSliderLabelText;
    String heightSliderLabelText;
    
    // Labels for statistics
    String pirateEnteredString;
    String pirateExitString;
    String patrolEnteredString;
    String patrolExitString;
    String cargoEnteredString;
    String cargoExitedString;
    
    // Interactions
    String cargoCapturedString;
    String cargoRescuedString;
    String pirateDefeatedString;
    
    String timeStepsString;
    
    boolean singleTick = false;

    /**
     * custom methods
     */
    public void startGame(String nextScreen) {
        nifty.gotoScreen(nextScreen);  // switch to another screen
        // Simulation(int xSize, int ySize, double cProbNewCargo, double cProbNewPirate, double cProbNewPatrol, long seed)        
        simStartScreen = new Simulation((int)simWidth.getValue(), (int)simHeight.getValue(),
                (double)cargoProb.getValue(), (double)pirateProb.getValue(), 
                (double)patrolProb.getValue(), seed);
                
        simPaused = false; // Will start running the simulation
    }

    public void quitGame() {
        myApp.stop();
    }

    public void increaseSpeed() {
        if(speedIndex+1 == simSpeeds.length){
            // Speed is at maximum, so do nothing
        }
        else{
            speedIndex++;
            simSpeed = simSpeeds[speedIndex];
            updateSpeedLabel();
        }
    }

    public void decreaseSpeed() {
        if (speedIndex == 0) {
            // Speed is at minimum, so do nothing
        } else {
            speedIndex--;
            simSpeed = simSpeeds[speedIndex];
            updateSpeedLabel();
        }
    }
    
    public float getSimSpeed(){
        return simSpeed;
    }

    public MyStartScreen( Simulation sim ) {
        /**
         * Your custom constructor, can accept arguments
         */
        /*
        this.simStartScreen = sim;
        this.seed = sim.seed;
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
    
    public void advanceSingleTick(){
        
        simPaused = true;
        singleTick = true;
        simStartScreen.tick();
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
        simWidth = screen.findNiftyControl("widthSlider", Slider.class);
        simHeight = screen.findNiftyControl("heightSlider", Slider.class);
        
        cargoProb = screen.findNiftyControl("cargoSlider", Slider.class);
        patrolProb = screen.findNiftyControl("patrolSlider", Slider.class);
        pirateProb = screen.findNiftyControl("pirateSlider", Slider.class);

        widthLabel = screen.findNiftyControl("widthSliderLabel", Label.class);
        heightLabel = screen.findNiftyControl("heightSliderLabel", Label.class);
        
        cargoLabel = screen.findNiftyControl("cargoSliderLabel", Label.class);
        patrolLabel = screen.findNiftyControl("patrolSliderLabel", Label.class);
        pirateLabel = screen.findNiftyControl("pirateSliderLabel", Label.class);
        simSpeedLabel = screenHud.findNiftyControl("speedLabel", Label.class);

        // Initilize string representations of labels
        cargoLabelText = "Cargo Probability: " + String.format("%.2f", cargoProb.getValue());
        patrolLabelText = "Patrol Probability: " + String.format("%.2f", patrolProb.getValue());
        pirateLabelText = "Pirate Probability: " + String.format("%.2f", pirateProb.getValue());
        
        witdthSliderLabelText = "Sim width: " + width;
        heightSliderLabelText = "Sim height: " + height;
        

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
        
        simWidth.setMax((float) 400);
        simWidth.setMin((float) 10);
        simWidth.setStepSize((float) 10);
        simWidth.setValue((float) 20);
        simWidth.setButtonStepSize((float) 10);
        
        simHeight.setMax((float) 100);
        simHeight.setMin((float) 10);
        simHeight.setStepSize((float) 10);
        simHeight.setValue((float) 10);
        simHeight.setButtonStepSize((float) 10);

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
    
    @NiftyEventSubscriber(id = "widthSlider")
    public void onWidthSliderChangedEvent(String id, SliderChangedEvent event) {
        width = (int)simWidth.getValue();
        witdthSliderLabelText = "Sim width: " + width;
        widthLabel.setText(witdthSliderLabelText);
    }
    
    @NiftyEventSubscriber(id = "heightSlider")
    public void onHeightSliderChangedEvent(String id, SliderChangedEvent event) {
        height = (int)simHeight.getValue();
        witdthSliderLabelText = "Sim height: " + height;
        heightLabel.setText(witdthSliderLabelText);
    }

    int getWidth(){
        return width;
    }
    
    int getHeight(){
        return height;
    }
    
    @Override
    public void update(float tpf) {
        /**
         * jME update loop!
         */
    }
    
    
    // Setters for statistic strings    
    public void setPirateEnteredString(int num) {
        this.pirateEnteredString = num + " pirate ships have entered the simulation.";
    }

    public void setPirateExitString(int num) {
        this.pirateExitString = num + " pirate ships have exited the simulation.";
    }

    public void setPatrolEnteredString(int num) {
        this.patrolEnteredString = num + " patrol ships have entered the simulation.";;
    }

    public void setPatrolExitString(int num) {
        this.patrolExitString = num + " patrol ships have exited the simulation.";
    }

    public void setCargoEnteredString(int num) {
        this.cargoEnteredString = num + " cargo ships have entered the simulation.";
    }

    public void setCargoExitedString(int num) {
        this.cargoExitedString = num + " cargo ships have exited the simulation.";
    }

    public void setCargoCapturedString(int num) {
        this.cargoCapturedString = "There have been " + num + " ship captures.";
    }

    public void setCargoRescuedString(int num) {
        this.cargoRescuedString = "There have been " + num + " ship rescues.";
    }

    public void setPirateDefeatedString(int num) {
        this.pirateDefeatedString = "There have been " + num + " pirates have been defeated.";
    }

    public void setTimeStepsString(int num) {
        this.timeStepsString = "There have been " + num + " time steps.";
    }
    
    
    
}