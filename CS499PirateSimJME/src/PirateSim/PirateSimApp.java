package PirateSim;

import com.jme3.app.SimpleApplication;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import com.jme3.renderer.RenderManager;
import de.lessvoid.nifty.Nifty;

/**
 * Class to create a JME SimplaApplication for the program to run in.
 * Also still under construction so no javadoc for now
 */
public class PirateSimApp extends SimpleApplication {
    Simulation sim;
    Scene scene;
    PanCamera panCam; //SimpleApplication's FlyCam is not suitable for this application, so this camera is used
    float timeSinceLastFrame, timeSinceLastTick;
    static final int targetFPS = 30;
    private MyStartScreen startScreen;

    //program entry point
    public static void main(String[] args) {
        PirateSimApp app = new PirateSimApp();
    }

    //initialize application settings
    PirateSimApp() {
        //note to Owen:
        //public void setResizable(boolean resizable)
        //Allows the display window to be resized by dragging its edges. Only supported for JmeContext.Type.Display contexts which are in windowed mode, ignored for other types. The default value is false.
        setDisplayStatView(false); //TODO fix F5 behavior so this doesn't ever show up
        setDisplayFps(true);
        setShowSettings(true);
        settings = new AppSettings(true);
        settings.setFrameRate(targetFPS);
        settings.setTitle("Somali Pirate Simulation");
        settings.setResolution(1024, 720);
        setSettings(settings);
        start();
    }

    //initialize application controlled objects like the simulation state (sim), the scene controller (scene), the camera, the GUI, etc.
    @Override
    public void simpleInitApp() {
        setSim(new Simulation(20, 10, 0.4, 0.25, 0.2, 6545));
        panCam = new PanCamera(cam, inputManager, getFlyByCamera());
        panCam.register();
        timeSinceLastFrame = 0;
        /* Josh Test Addition */
        startScreen = new MyStartScreen(this);
        stateManager.attach(startScreen);

        /**
         * Åctivate the Nifty-JME integration:
         */
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                assetManager, inputManager, audioRenderer, guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        nifty.fromXml("Interface/newNiftyGui.xml", "start", startScreen);
        //nifty.setDebugOptionPanelColors(true);

        //Note to Josh: panCamera is currently controlling FlyCam and sets this value.
        //flyCam.setDragToRotate(true); // you need the mouse for clicking now       
    }
    
    void setSim(Simulation pSim) {
        sim = pSim;
        rootNode.detachAllChildren();
        viewPort.clearProcessors();
        scene = new Scene(pSim, rootNode, assetManager, viewPort);
    }
    Simulation getSim() {return sim;}

    //Per frame update function
    @Override
    public void simpleUpdate(float tpf) {
        //if (sim.timeStep == 10) setSim(new Simulation(20, 10, 0.4, 0.25, 0.2, 6545)); //what?
        sim.setProbCargo(startScreen.getCargoProb());
        sim.setProbPatrol(startScreen.getPatrolProb());
        sim.setProbPirate(startScreen.getPirateProb());
        if (startScreen.singleStep) {
            startScreen.singleStep = false;
            startScreen.simPaused = true;
            sim.tick();
            timeSinceLastTick = 0;
            timeSinceLastFrame = 0;
            scene.update(0);
        }
        //Fun code to compute which simulation timestep should be rendered at which alpha
        if (!startScreen.simPaused) {
            timeSinceLastTick += tpf;
            timeSinceLastFrame += tpf;
            float simSpeed = startScreen.getSimSpeed();
            //continue ticking the simulation until timeSinceLastTick is less than the length of a tick (1/timeAcceleration, since ticks are one second each).
            while (timeSinceLastTick > 1/simSpeed) {
                sim.tick();
                timeSinceLastTick -= 1/simSpeed;
            }
            float alpha = timeSinceLastTick*simSpeed;
            updateStatisticStrings();
            //update the scene now that the simulation state is correct and alpha has been found
            scene.update(alpha);
        }
        
        if (startScreen.singleTick) {
            timeSinceLastTick = 0;
            timeSinceLastFrame = 0;
            float simSpeed = startScreen.getSimSpeed();
            //continue ticking the simulation until timeSinceLastTick is less than the length of a tick (1/timeAcceleration, since ticks are one second each).
            sim.tick();
            float alpha = timeSinceLastTick*simSpeed;
            //update the scene now that the simulation state is correct and alpha has been found
            scene.update(alpha);
            updateStatisticStrings();
            
            startScreen.singleTick = false;
        }
    }
    
    //We hopefully we will not need to use this
    @Override
    public void simpleRender(RenderManager rm) {}
    
    public void updateStatisticStrings(){
        startScreen.setCargoEnteredString(sim.shipsEntered[sim.CARGO]);
        startScreen.setPatrolEnteredString(sim.shipsEntered[sim.PATROL]);
        startScreen.setPirateEnteredString(sim.shipsEntered[sim.PIRATE]);
        startScreen.setCargoExitedString(sim.shipsExited[sim.CARGO]);
        startScreen.setPatrolExitedString(sim.shipsExited[sim.PATROL]);
        startScreen.setPirateExitedString(sim.shipsExited[sim.PIRATE]);
        startScreen.setCargoCapturedString(sim.captures);
        startScreen.setCargoRescuedString(sim.rescues);
        startScreen.setPirateDefeatedString(sim.defeats);
        startScreen.setTimeStepsString(sim.timeStep);
        
    }
}