package PirateSim;

import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import com.jme3.renderer.RenderManager;
import de.lessvoid.nifty.Nifty;

public class Application extends SimpleApplication {

    Simulation sim;
    Scene scene;
    PanCamera panCam;
    float timeSinceLastFrame, timeSinceLastTick;
    float timeAcceleration = 1;
    static final int targetFPS = 30;
    boolean simPaused = false;
    private MyStartScreen startScreen;

    public static void main(String[] args) {
        Application app = new Application();
    }

    Application() {
        setDisplayFps(true);
        setShowSettings(true);
        settings = new AppSettings(true);
        settings.setFrameRate(targetFPS);
        settings.setTitle("Somali Pirate Simulation");
        setSettings(settings);
        start();
    }

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

        flyCam.setDragToRotate(true); // you need the mouse for clicking now 
        
        /* Josh Test Addition End */
    }

    @Override
    public void simpleUpdate(float tpf) {
        
        this.simPaused = startScreen.simPaused;
        this.timeAcceleration = startScreen.simSpeed;
        
        if (!simPaused) {
            timeSinceLastTick += tpf;
            timeSinceLastFrame += tpf;
        }
        while (timeSinceLastTick > 1 / timeAcceleration) {
            sim.tick();
            timeSinceLastTick -= 1 / timeAcceleration;
        }
        float alpha = timeSinceLastTick * timeAcceleration;
        scene.update(alpha);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}