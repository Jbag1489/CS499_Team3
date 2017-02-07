package PirateSim;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Transform;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;

/**
 * Class to create a JME scenegraph for a given simulation state.
 * Still under construction so no javadoc for now
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
    //update all the scene elements, just boats and the background for now
    void update(float alpha) {
        boats.update(alpha);
        background.update(alpha);
    }
    //currently not being used
    void translateMesh(Mesh mesh, float x, float y) {
        VertexBuffer verts = mesh.getBuffer(VertexBuffer.Type.Position);
        for (int i = 0; i < verts.getNumElements(); i++) {
            float newX = (Float) verts.getElementComponent(i, 0) + x;
            float newY = (Float) verts.getElementComponent(i, 1) + y;
            verts.setElementComponent(i, 0, newX);
            verts.setElementComponent(i, 1, newY);
        }
    }
    //creates axis reference for debugging purposes
    void createAxisReference() {
        Arrow arrows[] = new Arrow[3];
        ColorRGBA colors[] = new ColorRGBA[3];
        arrows[0] = new Arrow(Vector3f.UNIT_X);
        colors[0] = ColorRGBA.Red;
        arrows[1] = new Arrow(Vector3f.UNIT_Y);
        colors[1] = ColorRGBA.Green;
        arrows[2] = new Arrow(Vector3f.UNIT_Z);
        colors[2] = ColorRGBA.White;
        
        for (int i = 0; i < 3; i++) {
            arrows[i].setLineWidth(4);
            Geometry g = new Geometry("coordinate axis", arrows[i]);
            Material mat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.getAdditionalRenderState().setWireframe(true);
            mat.setColor("Color", colors[i]);
            g.setMaterial(mat);
            g.getLocalTranslation().set(0f, 0f, 0.1f);
            rootNode.attachChild(g);
        }
    }
    
    //This class renders the boats, called boats instead of ships to differentiate it from Simulation.Ship
    class Boats {
        //these arrays track the properties of each type of boat
        Mesh meshes[];
        Material mats[];
        Vector3f centers[]; //the position within a cell of the boat's center
        Node shipNode; //node in the scenegraph that all boat nodes live under
        
        //initialize meshes, materials, and center postions for each type of boat
        Boats() {
            meshes = new Box[Simulation.NUM_SHIP_TYPES];
            mats = new Material[Simulation.NUM_SHIP_TYPES];
            centers = new Vector3f[Simulation.NUM_SHIP_TYPES];
            
            generateBoatType(Simulation.CARGO,          new Box(0.5f, 0.2f, 0.5f), ColorRGBA.White, 0f, 0.25f);
            generateBoatType(Simulation.CAPTURED,       new Box(0.5f, 0.2f, 0.49f), ColorRGBA.Orange, -0.35f, -0.1f);
            generateBoatType(Simulation.PATROL,         new Box(0.5f, 0.2f, 0.4f), ColorRGBA.Green, 0f, -0.25f);
            generateBoatType(Simulation.PIRATE,         new Box(0.2f, 0.2f, 0.3f), ColorRGBA.Black, 0f, 0f);
            generateBoatType(Simulation.ESCORTPIRATE,   new Box(0.2f, 0.2f, 0.29f), ColorRGBA.DarkGray, 0.4f, 0f);
            generateBoatType(Simulation.WRECK,          new Box(0.2f, 0.2f, 0.28f), ColorRGBA.Red, -0.05f, -0.75f);
            generateBoatType(Simulation.ESCORTWRECK,    new Box(0.2f, 0.2f, 0.28f), ColorRGBA.Pink, 0f, 0.75f);
            
            shipNode = new Node("ships");
            rootNode.attachChild(shipNode);
        }
        //Convenience function to generate boat meshes etc. 
        final void generateBoatType(int type, Mesh mesh, ColorRGBA color, float centerX, float centerY) {
            meshes[type] = mesh;
            mats[type] = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            mats[type].setColor("Color", color);
            centers[type] = new Vector3f(centerX, centerY, 0);
        }
        //updates the positions of boat nodes in scenegraph based on the current state of sim.ships
        //alpha is the amount of time since the last tick, alpha = 1 is the next tick
        void update(float alpha) {
            shipNode.detachAllChildren();
            for (Simulation.Ship ship : sim.ships) {
                Simulation.Ship visualShip = ship.previousStates.get(ship.previousStates.size() - 2);
                Mesh boatMesh = meshes[visualShip.type];
                Material matBoat = mats[visualShip.type];
                Geometry boatGeo = new Geometry("boat", boatMesh);
                boatGeo.setMaterial(matBoat);
                shipNode.attachChild(boatGeo);
                
                Transform transform = interpolateShipTransform(ship, alpha);
                boatGeo.getLocalTransform().set(transform);
            }
        }
        //returns the interpolated transform (heading and position in this case) of the given ship at time alpha
        Transform interpolateShipTransform(Simulation.Ship ship, float alpha) {
            Vector3f sample0 = sampleShipPos(ship, 0);
            Vector3f sample1 = sampleShipPos(ship, 1);
            Vector3f sample2 = sampleShipPos(ship, 2);
            Vector3f sample3 = sampleShipPos(ship, 3);
            Vector3f translation = FastMath.interpolateCatmullRom(alpha, 0.5f, sample3, sample2, sample1, sample0);
            Vector3f delta;
            //does this even work lol?
            if (!(sample1.x == sample2.x && sample1.y == sample2.y)) {
                delta = FastMath.interpolateCatmullRom(alpha - 0.1f*step(alpha - 0.5f), 0.5f, sample3, sample2, sample1, sample0).subtract(translation);
                delta = delta.mult(-step(alpha - 0.5f));
            } else {
                int i = 1;
                do {
                    sample1 = sample2.clone();
                    sample2 = sampleShipPos(ship, i + 2); //TODO efficiency?
                    i++;
                } while (sample1.x == sample2.x && sample1.y == sample2.y);
                delta = FastMath.interpolateCatmullRom(0.9f, 0.5f, sampleShipPos(ship, 3 + i), sample2, sample1, sampleShipPos(ship, 0 + i)).subtract(translation);
                delta = delta.mult(-1f);
            }
            Quaternion rotation = new Quaternion().fromAngleAxis(FastMath.atan2(delta.y, delta.x), Vector3f.UNIT_Z);
            return new Transform(translation, rotation);
            
            //return FastMath.interpolateLinear(alpha, sampleShipPos(ship, 1), sampleShipPos(ship, 0));
            //return sampleShipPos(ship, 0);
        }
        //samples the ships position from the previousStates arrayList at the current time minus tMinus.
        Vector3f sampleShipPos(Simulation.Ship ship, int tMinus) {
            Vector3f posOut = new Vector3f();
            Simulation.Ship previousShip = ship.previousStates.get(ship.previousStates.size() - tMinus - 1);
            posOut.x = 0.5f + centers[previousShip.type].x + previousShip.position.x;
            posOut.y = 0.5f + centers[previousShip.type].y + previousShip.position.y;
            if (previousShip.type == Simulation.WRECK || previousShip.type == Simulation.ESCORTWRECK) posOut.z = -previousShip.ticksSinceLastMove*0.3f/3.9f;
            return posOut;
        }
        private float step(float x) { if (x > 0) return 1; else return -1;}
    }
    
    /**
     * This class renders scenery like the sea and surounding terrain
     */
    class Background {
        Node backgroundNode;
        
        //creates map grid, quad for the sea, TODO terrain
        Background() {
            createAxisReference();
            
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
}
