package PirateSim;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.Geometry;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;

import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Box;
import com.jme3.scene.debug.Grid;

import PirateSim.Simulation.Ship;

//import java.util.concurrent.TimeUnit;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main(); 
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(30);
        settings.setTitle("Somali Pirate Simulation");
        app.setSettings(settings);
        app.start();
    }
    
    Box boatCargo, boatPirate, boatPatrol, boatCaptured, boatWreck;
    final static float cargoCenterX = 0, cargoCenterY = 0.25f, pirateCenterX = 0.25f, pirateCenterY = 0.0f,
            patrolCenterX = 0, patrolCenterY = -0.25f, capturedCenterX = -0.25f, capturedCenterY = 0, wreckCenterX = 0.25f, wreckCenterY = 0.0f;
    Material matCargo, matPirate, matPatrol, matCaptured, matWreck;
    Simulation sim;
    Node backgroundNode;
    Node shipNode;
    float timeSinceLastFrame, timeSinceLastTick;
    float timeAcceleration = 1;
    float targetFPS = 30;
    boolean simPaused = false;
    
    @Override
    public void simpleInitApp() {
        sim = new Simulation(20, 10, 1, 0.24, 0.21, 653745);
        initializeScene();
    }
    
    void translateMesh(Mesh mesh, float x, float y) {
        VertexBuffer verts = mesh.getBuffer(VertexBuffer.Type.Position);
        for (int i = 0; i < verts.getNumElements(); i++) {
            float newX = (Float) verts.getElementComponent(i, 0) + x;
            float newY = (Float) verts.getElementComponent(i, 1) + y;
            verts.setElementComponent(i, 0, newX);
            verts.setElementComponent(i, 1, newY);
        }
    }

    public void initializeScene() {
        Quad ground = new Quad(sim.size.x, sim.size.y);
        Geometry seaGeom = new Geometry("Quad", ground); 
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        seaGeom.setMaterial(mat);
        
        Grid mapGrid = new Grid(sim.size.y + 1, sim.size.x + 1, 1);
        Geometry gridGeom = new Geometry("Grid", mapGrid);
        Material matGrid = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matGrid.setColor("Color", ColorRGBA.Black);
        matGrid.getAdditionalRenderState().setWireframe(true);
        gridGeom.setMaterial(matGrid);
        gridGeom.center().getLocalTranslation().set(0.0f, sim.size.y, 0.01f);
        gridGeom.getLocalRotation().fromAngles(FastMath.PI/2, 0.0f, 0.0f);
        
        boatCargo = new Box(0.5f, 0.2f, 0.4f);
        boatPatrol = new Box(0.5f, 0.2f, 0.5f);
        boatPirate = new Box(0.2f, 0.2f, 0.3f);
        boatCaptured = new Box(0.5f, 0.2f, 0.39f);
        boatWreck = new Box(0.2f, 0.2f, 0.29f);
        
        matCargo = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matCargo.setColor("Color", ColorRGBA.White);
        matPatrol = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matPatrol.setColor("Color", ColorRGBA.Green);
        matPirate = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matPirate.setColor("Color", ColorRGBA.Black);
        matCaptured = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matCaptured.setColor("Color", ColorRGBA.Orange);
        matWreck = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matWreck.setColor("Color", ColorRGBA.Red);
        
        backgroundNode = new Node("background");
        shipNode = new Node("ships");
        rootNode.attachChild(backgroundNode);
        rootNode.attachChild(shipNode);
        backgroundNode.attachChild(seaGeom);
        backgroundNode.attachChild(gridGeom);
        
        timeSinceLastFrame = 0;
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!simPaused) {
            timeSinceLastTick += tpf;
            timeSinceLastFrame += tpf;
        }
        if (timeSinceLastFrame > 1/targetFPS) {
            timeSinceLastFrame -= 1/targetFPS;
            if (timeSinceLastTick > 1/timeAcceleration) {
                sim.tick();
                timeSinceLastTick -= 1/timeAcceleration;
            }
            float alpha = timeSinceLastTick/1;
            shipNode.detachAllChildren();
            for (Ship ship : sim.ships) {
                Mesh boatMesh = boatCargo;
                Material matBoat = matCargo; //default case
                float zPos = 0;
                float centerX = 0;
                float centerY = 0;
                float type;
                switch (ship.previousState.type) {
                case Simulation.CARGO:
                    boatMesh = boatCargo;
                    matBoat = matCargo;
                    centerX = cargoCenterX;
                    centerY = cargoCenterY;
                    break;
                case Simulation.PIRATE:
                    boatMesh = boatPirate;
                    matBoat = matPirate;
                    centerX = pirateCenterX;
                    centerY = pirateCenterY;
                    break;
                case Simulation.PATROL:
                    boatMesh = boatPatrol;
                    matBoat = matPatrol;
                    centerX = patrolCenterX;
                    centerY = patrolCenterY;
                    break;
                case Simulation.CAPTURED:
                    boatMesh = boatCaptured;
                    matBoat = matCaptured;
                    centerX = capturedCenterX;
                    centerY = capturedCenterY;
                    break;
                case Simulation.WRECK:
                    boatMesh = boatWreck;
                    matBoat = matWreck;
                    centerX = wreckCenterX;
                    centerY = wreckCenterY;
                    zPos = -((ship.age/4.9f - ship.previousState.age/4.9f)*alpha + ship.previousState.age/4.9f);
                    break;
                }
                Geometry boatGeo = new Geometry("boat", boatMesh);
                boatGeo.setMaterial(matBoat);
                shipNode.attachChild(boatGeo);
                float xPos = 0.5f + centerX + (ship.position.x - ship.previousState.position.x)*alpha + ship.previousState.position.x;
                float yPos = 0.5f + centerY + (ship.position.y - ship.previousState.position.y)*alpha + ship.previousState.position.y;
                boatGeo.center().getLocalTranslation().set(xPos, yPos, zPos);
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}