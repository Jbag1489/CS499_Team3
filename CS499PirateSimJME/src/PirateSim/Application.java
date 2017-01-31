package PirateSim;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.renderer.RenderManager;

public class Application extends SimpleApplication {
    Simulation sim;
    Scene scene;
    float timeSinceLastFrame, timeSinceLastTick;
    float timeAcceleration = 1;
    static final int targetFPS = 30;
    boolean simPaused = false;
    
    public static void main(String[] args) {
        Application app = new Application();
    }

    Application() {        
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
        timeSinceLastFrame = 0;
    }
    @Override
    public void simpleUpdate(float tpf) {
        if (!simPaused) {
            timeSinceLastTick += tpf;
            timeSinceLastFrame += tpf;
        }
        while (timeSinceLastTick > 1/timeAcceleration) {
            sim.tick();
            timeSinceLastTick -= 1/timeAcceleration;
        }
        float alpha = timeSinceLastTick*timeAcceleration;
        scene.update(alpha);
    }
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}