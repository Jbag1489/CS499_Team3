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
public class Application extends SimpleApplication {
    Simulation sim;
    Scene scene;
    PanCamera panCam; //SimpleApplication's FlyCam is not suitable for this application, so this camera is used
    float timeSinceLastFrame, timeSinceLastTick;
    static final int targetFPS = 30;
    private MyStartScreen startScreen;

    //program entry point
    public static void main(String[] args) {
        Application app = new Application();
    }

    //initialize application settings
    Application() {
        //note to Owen:
        //public void setResizable(boolean resizable)
        //Allows the display window to be resized by dragging its edges. Only supported for JmeContext.Type.Display contexts which are in windowed mode, ignored for other types. The default value is false.
        setDisplayStatView(false); //TODO fix F5 behavior so this doesn't ever show up
        setDisplayFps(true);
        setShowSettings(true);
        settings = new AppSettings(true);
        settings.setFrameRate(targetFPS);
        settings.setTitle("Somali Pirate Simulation");
        setSettings(settings);
        start();
    }

    //initialize application controlled objects like the simulation state (sim), the scene controller (scene), the camera, the GUI, etc.
    @Override
    public void simpleInitApp() {
        sim = new Simulation(20, 10, 0.4, 0.25, 0.2, 6545);
        scene = new Scene(sim, rootNode, assetManager);
        panCam = new PanCamera(cam, inputManager, getFlyByCamera());
        panCam.register();
        timeSinceLastFrame = 0;

        /* Josh Test Addition */
        startScreen = new MyStartScreen();
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
        
        /* Josh Test Addition End */
        
    }

    //Per frame update function
    @Override
    public void simpleUpdate(float tpf) {
        sim.setProbCargo(startScreen.getCargoProb());
        sim.setProbPatrol(startScreen.getPatrolProb());
        sim.setProbPirate(startScreen.getPirateProb());
        
        //Fun code to compute which simulation timestep should be rendered at which alpha
        if (!startScreen.simPaused) {
            timeSinceLastTick += tpf;
            timeSinceLastFrame += tpf;
            float simSpeed = 1;//startScreen.simSpeed;
            //continue ticking the simulation until timeSinceLastTick is less than the length of a tick (1/timeAcceleration, since ticks are one second each).
            while (timeSinceLastTick > 1/simSpeed) {
                sim.tick();
                timeSinceLastTick -= 1/simSpeed;
            }
            float alpha = timeSinceLastTick*simSpeed;
            //update the scene now that the simulation state is correct and alpha has been found
            scene.update(alpha);
        }
    }
    
    //We hopefully we will not need to use this
    @Override
    public void simpleRender(RenderManager rm) {}
}