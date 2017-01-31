/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PirateSim;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import java.lang.Math;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author philip
 */
public class Scene {
    AssetManager assetMan;
    Simulation sim;
    Node rootNode;
    Boats boats;
    Background background;
    static final int SEA_BORDER_SIZE = 6;
    
    Scene(Simulation pSim, Node pRootNode, AssetManager pAssetMan) {
        sim = pSim;
        rootNode = pRootNode;
        assetMan = pAssetMan;
        boats = new Boats();
        background = new Background();
    }
    void update(float alpha) {
        boats.update(alpha);
        background.update(alpha);
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
    
    class Boats {
        Mesh meshes[];
        Material mats[];
        Vector3f centers[];
        Node shipNode;
        
        Boats() {
            meshes = new Box[Simulation.NUM_SHIP_TYPES];
            mats = new Material[Simulation.NUM_SHIP_TYPES];
            centers = new Vector3f[Simulation.NUM_SHIP_TYPES];
            
            generateBoatType(Simulation.CARGO,          new Box(0.5f, 0.2f, 0.5f), ColorRGBA.White, 0f, 0.25f);
            generateBoatType(Simulation.CAPTURED,       new Box(0.5f, 0.2f, 0.49f), ColorRGBA.Orange, -0.25f, -0.1f);
            generateBoatType(Simulation.PATROL,         new Box(0.5f, 0.2f, 0.4f), ColorRGBA.Green, 0f, -0.25f);
            generateBoatType(Simulation.PIRATE,         new Box(0.2f, 0.2f, 0.3f), ColorRGBA.Black, 0f, 0f);
            generateBoatType(Simulation.ESCORTPIRATE,   new Box(0.2f, 0.2f, 0.29f), ColorRGBA.DarkGray, 0.3f, 0f);
            generateBoatType(Simulation.WRECK,          new Box(0.2f, 0.2f, 0.28f), ColorRGBA.Red, 0f, 0f);
            generateBoatType(Simulation.ESCORTWRECK,    new Box(0.2f, 0.2f, 0.28f), ColorRGBA.Pink, 0f, -0.25f);
            
            shipNode = new Node("ships");
            rootNode.attachChild(shipNode);
        }
        final void generateBoatType(int type, Mesh mesh, ColorRGBA color, float centerX, float centerY) {
            meshes[type] = mesh;
            mats[type] = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            mats[type].setColor("Color", color);
            centers[type] = new Vector3f(centerX, centerY, 0);
        }
        void update(float alpha) {
            if (rootNode == null) System.out.println("rootNode is null");
            shipNode.detachAllChildren();
            for (Simulation.Ship ship : sim.ships) {
                Simulation.Ship visualShip = ship.previousStates.get(ship.previousStates.size() - 2);
                Mesh boatMesh = meshes[visualShip.type];
                Material matBoat = mats[visualShip.type];
                Geometry boatGeo = new Geometry("boat", boatMesh);
                boatGeo.setMaterial(matBoat);
                shipNode.attachChild(boatGeo);
                
                Vector3f position = interpolateShipPos(ship, alpha);
                Vector3f delta = interpolateShipPos(ship, alpha - 0.1f*step(alpha - 0.5f)).subtract(position);
                boatGeo.getLocalRotation().set(new Quaternion().fromAngleAxis(FastMath.atan2(delta.y, delta.x), Vector3f.UNIT_Z));
                boatGeo.getLocalTranslation().set(position);
            }
        }
        Vector3f interpolateShipPos(Simulation.Ship ship, float alpha) {
            return FastMath.interpolateCatmullRom(alpha, 0.5f, sampleShipPos(ship, 3), sampleShipPos(ship, 2), sampleShipPos(ship, 1), sampleShipPos(ship, 0));
            //return FastMath.interpolateLinear(alpha, sampleShipPos(ship, 1), sampleShipPos(ship, 0));
            //return sampleShipPos(ship, 0);
        }
        Vector3f sampleShipPos(Simulation.Ship ship, int tMinus) {
            Vector3f posOut = new Vector3f();
            if (ship.previousStates.size() - tMinus < 0) {System.err.println("ERROR");}
            Simulation.Ship previousShip = ship.previousStates.get(ship.previousStates.size() - tMinus - 1);
            posOut.x = 0.5f + centers[previousShip.type].x + previousShip.position.x;
            posOut.y = 0.5f + centers[previousShip.type].y + previousShip.position.y;
            if (previousShip.type == Simulation.WRECK || previousShip.type == Simulation.ESCORTWRECK) posOut.z = -previousShip.age*0.3f/4.9f;
            return posOut;
        }
        private float step(float x) { if (x > 0) return 1; else return -1;}
    }
    
    class Background {
        Node backgroundNode;
        
        Background() {
           Quad ground = new Quad(sim.size.x + SEA_BORDER_SIZE*2, sim.size.y + SEA_BORDER_SIZE*2);
           Geometry seaGeom = new Geometry("Quad", ground);
           Material mat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
           mat.setColor("Color", ColorRGBA.Blue);
           seaGeom.setMaterial(mat);
           seaGeom.center().getLocalTranslation().set(-SEA_BORDER_SIZE, -SEA_BORDER_SIZE, 0f);


           Grid mapGrid = new Grid(sim.size.y + 1, sim.size.x + 1, 1);
           Geometry gridGeom = new Geometry("Grid", mapGrid);
           Material matGrid = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
           matGrid.setColor("Color", ColorRGBA.Black);
           matGrid.getAdditionalRenderState().setWireframe(true);
           gridGeom.setMaterial(matGrid);
           gridGeom.center().getLocalTranslation().set(0.0f, sim.size.y, 0.01f);
           gridGeom.getLocalRotation().fromAngles(FastMath.PI/2, 0f, 0f);

           backgroundNode = new Node("background");
           rootNode.attachChild(backgroundNode);
           backgroundNode.attachChild(seaGeom);
           backgroundNode.attachChild(gridGeom);
        }
        void update(float alhpa) {
            //TODO animated water
        }
    }
    float interpolate(float a, float b, float alpha) {return (-(a - b)*alpha + a);}
}
