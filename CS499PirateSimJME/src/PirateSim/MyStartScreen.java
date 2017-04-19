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
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 * Class that will be a controller for NiftyGUI created with newNiftyGui.xml
 */
public class MyStartScreen extends AbstractAppState implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Screen screenHud;
    long seed;
    float simSpeed = 1; // ties to timeAcceleration in Application
    boolean simPaused = true; // ties to simPaused in Application
    // Initialize to true, when start is pressed, it will "unpause"
    // Objects for XML Control
    int width;
    int height;
    private Slider simWidth;
    private Slider simHeight;
    private Slider cargoProb;
    private Slider patrolProb;
    private Slider pirateProb;
    
    private int speedIndex = 4;
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
    // Strings for Labels for statistics
    String pirateEnteredString;
    String pirateExitString;
    String patrolEnteredString;
    String patrolExitString;
    String cargoEnteredString;
    String cargoExitedString;
    // String for Label Interactions
    String cargoCapturedString;
    String cargoRescuedString;
    String pirateDefeatedString;
    String timeStepsString;
    // Labels for statistics
    Label pirateEnteredLabel;
    Label pirateExitLabel;
    Label patrolEnteredLabel;
    Label patrolExitLabel;
    Label cargoEnteredLabel;
    Label cargoExitedLabel;
    // Label Interactions
    Label cargoCapturedLabel;
    Label cargoRescuedLabel;
    Label pirateDefeatedLabel;
    Label timeStepsLabel;
    boolean singleTick = false;
    Button pauseButton;

    PirateSimApp pApp;


    /**
     * startGame is what will be processed when the user clicks the "Start Simulation" button.
     * @param nextScreen A string containing the name to advance the NiftyGUI to.
     */
    public void startGame(String nextScreen) {
        nifty.gotoScreen(nextScreen);  // switch to another screen
        // Simulation(int xSize, int ySize, double cProbNewCargo, double cProbNewPirate, double cProbNewPatrol, long seed)
        Simulation newSim = new Simulation((int) simWidth.getValue(), (int) simHeight.getValue(),
                (double) cargoProb.getValue(), (double) pirateProb.getValue(),
                (double) patrolProb.getValue(), seed);
        pApp.setSim(newSim);
        simPaused = false; // Will start running the simulation
    }

    /**
     * Stops the simulation and exits.
     */
    public void quitSim() {
        pApp.stop();
    }
    
    public void quitToMenu(){
        nifty.gotoScreen("start");
    }

    /**
     * Increases the running speed of the simulation when "Speed Up" button is clicked.
     */
    public void increaseSpeed() {
        if (speedIndex + 1 == simSpeeds.length) {
            // Speed is at maximum, so do nothing
        } else {
            speedIndex++;
            simSpeed = simSpeeds[speedIndex];
            updateSpeedLabel();
        }
    }

    /**
     * Decreases the running speed of the simulation when "Slow Down" button is clicked.
     */
    public void decreaseSpeed() {
        if (speedIndex == 0) {
            // Speed is at minimum, so do nothing
        } else {
            speedIndex--;
            simSpeed = simSpeeds[speedIndex];
            updateSpeedLabel();
        }
    }

    /**
     * Provides the simulation speed.
     * @return A float containing the speed the simulation is running at.
     */
    public float getSimSpeed() {
        return simSpeed;
    }

    /**
     * Constructor for MyStartScreen
     * @param paramsim A reference to the JMonkey application
     */
    public MyStartScreen(PirateSimApp paramsim) {
        pApp = paramsim;

    }

    /**
     * Toggles the simulation between paused and running.
     */
    public void changePauseState() {
        if (simPaused) {
            simPaused = false;
            pauseButton.setText("Pause");
        } else {
            simPaused = true;
            pauseButton.setText("Unpause");
        }
    }

    /**
     * Binds the screen controller to the NiftyGUI XML
     * @param nifty A NiftyGUI control object
     * @param screen A NiftyGUI screen that will be used
     */
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

    }

    /**
     * Controls what happens when the start screen is loaded.
     */
    public void onStartScreen() {
    }

    /**
     * Controls what happens when the ending screen is loaded.
     */
    public void onEndScreen() {
    }

    /**
     * Reads the current simulation speed and updates label to match.
     */
    private void updateSpeedLabel() {
        String speedLabelText = "Sim Speed: " + simSpeeds[speedIndex];
        System.out.println("Sim speed changed to "
                + String.format("%.1f", simSpeeds[speedIndex]));
        simSpeedLabel.setText(speedLabelText);
    }

    /**
     * Pauses the simulation if running, and then advances a single tick.
     */
    public void advanceSingleTick() {
        simPaused = true;
        singleTick = true;
        pauseButton.setText("Unpause");
    }

    /**
     * Initializes all control objects that the GUI will use.
     * @param stateManager Reference to object to control the state of the application.
     * @param app Reference to the application that is running.
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        screenHud = nifty.getScreen("hud");

        // Initialize array of speeds
        simSpeeds = new float[11];
        simSpeeds[0] = (float) .01;
        simSpeeds[1] = (float) .1;
        simSpeeds[2] = (float) .25;
        simSpeeds[3] = (float) .5;
        simSpeeds[4] = (float) 1.0;
        simSpeeds[5] = (float) 2.0;
        simSpeeds[6] = (float) 5.0;
        simSpeeds[7] = (float) 10.0;
        simSpeeds[8] = (float) 25.0;
        simSpeeds[9] = (float) 50.0;
        simSpeeds[10] = (float) 100.0;
        

        pauseButton = screenHud.findNiftyControl("PauseButton", Button.class);

        // Initialize simulation to 1x speed.
        speedIndex = 4;

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
        simWidth.setValue((float) 40);
        simWidth.setButtonStepSize((float) 10);

        simHeight.setMax((float) 100);
        simHeight.setMin((float) 10);
        simHeight.setStepSize((float) 10);
        simHeight.setValue((float) 10);
        simHeight.setButtonStepSize((float) 10);

        // Initialize Label Objects
        pirateEnteredLabel = screenHud.findNiftyControl("piratesEnteredLabel", Label.class);
        pirateExitLabel = screenHud.findNiftyControl("piratesExitedLabel", Label.class);
        patrolEnteredLabel = screenHud.findNiftyControl("patrolsEnteredLabel", Label.class);
        patrolExitLabel = screenHud.findNiftyControl("patrolsExitedLabel", Label.class);
        cargoEnteredLabel = screenHud.findNiftyControl("cargosEnteredLabel", Label.class);
        cargoExitedLabel = screenHud.findNiftyControl("cargosExitedLabel", Label.class);

        // Label Interactions
        cargoCapturedLabel = screenHud.findNiftyControl("capturedStatsLabel", Label.class);
        cargoRescuedLabel = screenHud.findNiftyControl("rescuedStatsLabel", Label.class);
        pirateDefeatedLabel = screenHud.findNiftyControl("piratesDefeatedLabel", Label.class);

        timeStepsLabel = screenHud.findNiftyControl("timeStepLabel", Label.class);

        // Initialize statistics labels text
        pirateEnteredString = "0 pirate ships have entered the simulation.";
        pirateExitString = "0 pirate ships have exited the simulation.";
        patrolEnteredString = "0 patrol ships have entered the simulation.";
        patrolExitString = "0 patrol ships have exited the simulation.";
        cargoEnteredString = "0 cargo ships have entered the simulation.";
        cargoExitedString = "0 cargo ships have exited the simulation.";
        cargoCapturedString = "There have been 0 ship captures.";
        cargoRescuedString = "There have been 0 ship rescues.";
        pirateDefeatedString = "There have been 0 pirates have been defeated.";
        timeStepsString = "There have been 0 time steps.";

    }

    /**
     * Returns the probability that a cargo ship will spawn each simulation tick.
     * @return The probability that a cargo ship will spawn each simulation tick.
     */
    public double getCargoProb() {
        return (double) this.cargoProb.getValue();
    }

    /**
     * Returns the probability that a patrol ship will spawn each simulation tick.
     * @return The probability that a patrol ship will spawn each simulation tick.
     */
    public double getPatrolProb() {
        return (double) this.patrolProb.getValue();
    }

    /**
     * Returns the probability that a pirate ship will spawn each simulation tick.
     * @return The probability that a pirate ship will spawn each simulation tick.
     */
    public double getPirateProb() {
        return (double) this.pirateProb.getValue();
    }

    /**
     * Listener to change values when Cargo Slider Probability is changed
     * @param id String containing the ID of the element in the XML representaion of GUI
     * @param event The event to listen for changes on.
     */
    @NiftyEventSubscriber(id = "cargoSlider")
    public void onCargoSliderChangedEvent(String id, SliderChangedEvent event) {
        cargoLabelText = "Cargo Probability: " + String.format("%.2f", cargoProb.getValue());
        cargoLabel.setText(cargoLabelText);
    }


    /**
     * Listener to change values when Patrol Slider Probability is changed
     * @param id String containing the ID of the element in the XML representaion of GUI
     * @param event The event to listen for changes on.
     */
    @NiftyEventSubscriber(id = "patrolSlider")
    public void onPatrolSliderChangedEvent(String id, SliderChangedEvent event) {
        patrolLabelText = "Patrol Probability: " + String.format("%.2f", patrolProb.getValue());
        patrolLabel.setText(patrolLabelText);
    }


    /**
     * Listener to change values when Pirate Slider Probability is changed
     * @param id String containing the ID of the element in the XML representaion of GUI
     * @param event The event to listen for changes on.
     */
    @NiftyEventSubscriber(id = "pirateSlider")
    public void onPirateSliderChangedEvent(String id, SliderChangedEvent event) {
        pirateLabelText = "Pirate Probability: " + String.format("%.2f", pirateProb.getValue());
        pirateLabel.setText(pirateLabelText);
    }


    /**
     * Listener to change values when simulation width is changed
     * @param id String containing the ID of the element in the XML representaion of GUI
     * @param event The event to listen for changes on.
     */
    @NiftyEventSubscriber(id = "widthSlider")
    public void onWidthSliderChangedEvent(String id, SliderChangedEvent event) {
        width = (int) simWidth.getValue();
        witdthSliderLabelText = "Sim width: " + width;
        widthLabel.setText(witdthSliderLabelText);
    }

    /**
     * Listener to change values when simulation height is changed
     * @param id String containing the ID of the element in the XML representaion of GUI
     * @param event The event to listen for changes on.
     */
    @NiftyEventSubscriber(id = "heightSlider")
    public void onHeightSliderChangedEvent(String id, SliderChangedEvent event) {
        height = (int) simHeight.getValue();
        witdthSliderLabelText = "Sim height: " + height;
        heightLabel.setText(witdthSliderLabelText);
    }

    /**
     * Returns an integer containing the width of simulation representing miles.
     * @return An integer containing the width of the simulation.
     */
    int getWidth() {
        return width;
    }

    /**
     * Returns an integer containing the height of simulation representing miles.
     * @return An integer containing the height of the simulation.
     */
    int getHeight() {
        return height;
    }

    /**
     * NOT USED. Handled by PirateSimApp.java
     * @param tpf
     */
    @Override
    public void update(float tpf) {
        /**
         * jME update loop!
         */
    }

    /**
     * Update the label for pirates entering the simulation.
     * @param num The number of pirates that have entered the simulation.
     */
    public void setPirateEnteredString(int num) {
        this.pirateEnteredString = num + " pirate ships have entered the simulation.";
        pirateEnteredLabel.setText(pirateEnteredString);
    }

    /**
     * Update the label for pirates exiting the simulation.
     * @param num The number of pirates that have exited the simulation.
     */
    public void setPirateExitedString(int num) {
        this.pirateExitString = num + " pirate ships have exited the simulation.";
        pirateExitLabel.setText(pirateExitString);
    }

    /**
     * Update the label for patrols entering the simulation.
     * @param num The number of patrols that have entered the simulation.
     */
    public void setPatrolEnteredString(int num) {
        this.patrolEnteredString = num + " patrol ships have entered the simulation.";
        patrolEnteredLabel.setText(patrolEnteredString);
    }

    /**
     * Update the label for patrols exiting the simulation.
     * @param num The number of patrols that have exited the simulation.
     */
    public void setPatrolExitedString(int num) {
        this.patrolExitString = num + " patrol ships have exited the simulation.";
        patrolExitLabel.setText(patrolExitString);
    }

    /**
     * Update the label for cargo ships entering the simulation.
     * @param num The number of cargo ships that have entered the simulation.
     */
    public void setCargoEnteredString(int num) {
        this.cargoEnteredString = num + " cargo ships have entered the simulation.";
        cargoEnteredLabel.setText(cargoEnteredString);
    }

    /**
     * Update the label for cargo ships exiting the simulation.
     * @param num The number of cargo ships that have exited the simulation.
     */
    public void setCargoExitedString(int num) {
        this.cargoExitedString = num + " cargo ships have exited the simulation.";
        cargoExitedLabel.setText(cargoExitedString);
    }

    /**
     * Update the label for the number of cargo ships captured.
     * @param num The number of cargo ships captured.
     */
    public void setCargoCapturedString(int num) {
        this.cargoCapturedString = "There have been " + num + " ship captures.";
        cargoCapturedLabel.setText(cargoCapturedString);
    }

    /**
     * Update the label for cargo ships rescued by patrols.
     * @param num The number of cargo ships rescued.
     */
    public void setCargoRescuedString(int num) {
        this.cargoRescuedString = "There have been " + num + " ship rescues.";
        cargoRescuedLabel.setText(cargoRescuedString);
    }

    /**
     * Update the label displaying how many pirates have been defeated by patrols.
     * @param num The number of pirates defeated.
     */
    public void setPirateDefeatedString(int num) {
        this.pirateDefeatedString = "There have been " + num + " pirates have been defeated.";
        pirateDefeatedLabel.setText(pirateDefeatedString);
    }

    /**
     * Update the label showing how many time steps have elapsed.
     * @param num The number of elapsed time steps.
     */
    public void setTimeStepsString(int num) {
        this.timeStepsString = "There have been " + num + " time steps.";
        timeStepsLabel.setText(timeStepsString);
    }

    /**
     * Updates all the statistic that are being displayed.
     */
    public void updateStatisticStrings() {
        setCargoEnteredString(pApp.sim.shipsEntered[pApp.sim.CARGO]);
        setPatrolEnteredString(pApp.sim.shipsEntered[pApp.sim.PATROL]);
        setPirateEnteredString(pApp.sim.shipsEntered[pApp.sim.PIRATE]);
        setCargoExitedString(pApp.sim.shipsExited[pApp.sim.CARGO]);
        setPatrolExitedString(pApp.sim.shipsExited[pApp.sim.PATROL]);
        setPirateExitedString(pApp.sim.shipsExited[pApp.sim.PIRATE]);
        setCargoCapturedString(pApp.sim.captures);
        setCargoRescuedString(pApp.sim.rescues);
        setPirateDefeatedString(pApp.sim.defeats);
        setTimeStepsString(pApp.sim.timeStep);
    }
}
