package pirateSim;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;

import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Box;
import com.jme3.scene.debug.Grid;

import pirateSim.Simulation.Ship;

import java.util.ArrayList;

//import java.util.concurrent.TimeUnit;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) { 
        Main app = new Main();
        app.start();
    }
    
    ArrayList<Geometry> geoList;
    Box boat;
    Material matCargo, matPirate, matPatrol, matCaptured, matWreck;
    
    
    @Override
    public void simpleInitApp() {
        Quad ground = new Quad(10, 10);
        Geometry geom = new Geometry("Quad", ground);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        
        Grid mapGrid = new Grid(11, 11, 1);
        Geometry mapGridGeo = new Geometry("Grid", mapGrid);
        Material matGrid = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matGrid.setColor("Color", ColorRGBA.Black);
        matGrid.getAdditionalRenderState().setWireframe(true);
        mapGridGeo.setMaterial(matGrid);
        mapGridGeo.center().getLocalTranslation().set(0.0f, 10.0f, 0.01f);
        mapGridGeo.getLocalRotation().fromAngles(FastMath.PI/2, 0.0f, 0.0f);
        
        boat = new Box(0.5f, 0.5f, 0.5f);
        geoList = new ArrayList();
        
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
        
        rootNode.attachChild(geom);
        rootNode.attachChild(mapGridGeo);
    }

    Simulation sim = new Simulation(10, 10, 0.3, 0.24, 0.21, 653745);
    float elapsedTime = 0;
    @Override
    public void simpleUpdate(float tpf) {
        
        
        elapsedTime += tpf;
        if (elapsedTime > 1) {
            elapsedTime--;
            sim.tick();
            for (Geometry geo : geoList) {rootNode.detachChild(geo);}
            for (Ship ship : sim.ships) {
        Geometry boatGeo = new Geometry("boat", boat);
        Material matBoat = matCargo; //default case
        float height = 0;
        switch (ship.type) {
        case Simulation.CARGO:
            matBoat = matCargo;
            break;
        case Simulation.PIRATE:
            matBoat = matPirate;
            break;
        case Simulation.PATROL:
            matBoat = matPatrol;
            break;
        case Simulation.CAPTURED:
            matBoat = matCaptured;
            break;
        case Simulation.WRECK:
            matBoat = matPirate;
            height = -ship.age/4.9f;
            break;
        }
        boatGeo.setMaterial(matBoat);
        boatGeo.center().getLocalTranslation().set(0.5f + ship.position.x, 0.5f + ship.position.y, height);
        rootNode.attachChild(boatGeo);
        geoList.add(boatGeo);
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}