package PirateSim;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.Camera;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Transform;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.texture.Texture;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.water.WaterFilter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.CartoonEdgeFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.texture.Texture2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.jme3.util.TangentBinormalGenerator;
/**
 *
 * Class to create a JME scenegraph for a given simulation state.
 *
 * Still under construction so no javadoc for now
 *
 */
public class Scene {
    AssetManager assetMan;
    Simulation sim;
    ViewPort viewPort;
    Node rootNode;
    Boats boats;
    Background background;
    WaterFilter water;
    Lighting lighting;
    static final int SEA_BORDER_SIZE = 6;
    float terrainScale;
    PirateSimApp pApp;
    
    Scene(Simulation pSim, Node pRootNode, AssetManager pAssetMan, ViewPort pViewPort, PirateSimApp app) {
        pApp = app;
        sim = pSim;
        rootNode = pRootNode;
        assetMan = pAssetMan;
        viewPort = pViewPort;
        boats = new Boats();
        lighting = new Lighting();
        background = new Background();
        if (sim.size.x / sim.size.y > 4) {
            terrainScale = sim.size.x * 5 / 40;
        } else {
            terrainScale = sim.size.y * 5 / 10;
        }
    }
    //update all the scene elements, just boats and the background for now
    void update(float alpha) {
        boats.update(alpha);
        lighting.update(alpha);
        background.update(alpha);
    }
    //This class renders the boats, called boats instead of ships to differentiate it from Simulation.Ship
    class Boats {
        //these arrays track the properties of each type of boat
        Spatial models[];
        Material mats[];
        Vector3f centers[]; //the position within a cell of the boat's center
        Node shipNode; //node in the scenegraph that all boat nodes live under
        //initialize meshes, materials, and center postions for each type of boat
        Boats() {
            models = new Spatial[Simulation.NUM_SHIP_TYPES];
            mats = new Material[Simulation.NUM_SHIP_TYPES];
            centers = new Vector3f[Simulation.NUM_SHIP_TYPES];
            generateBoatType(Simulation.CARGO, new Box(0.5f, 0.2f, 0.5f), ColorRGBA.White, 0f, 0.25f);
            generateBoatType(Simulation.CAPTURED, new Box(0.5f, 0.2f, 0.49f), ColorRGBA.Orange, -0.35f, -0.1f);
            generateBoatType(Simulation.PATROL, new Box(0.5f, 0.2f, 0.4f), ColorRGBA.Green, 0f, 0.0f);
            generateBoatType(Simulation.PIRATE, new Box(0.2f, 0.2f, 0.3f), ColorRGBA.Black, 0f, 0f);
            generateBoatType(Simulation.ESCORTPIRATE, new Box(0.2f, 0.2f, 0.29f), ColorRGBA.DarkGray, 0.4f, 0f);
            generateBoatType(Simulation.WRECK, new Box(0.2f, 0.2f, 0.28f), ColorRGBA.Red, -0.05f, -0.75f);
            generateBoatType(Simulation.ESCORTWRECK, new Box(0.2f, 0.2f, 0.28f), ColorRGBA.Pink, 0f, 0.75f);
            shipNode = new Node("ships");
            shipNode.getLocalRotation().fromAngleAxis(-2.8f, Vector3f.UNIT_Y);
            shipNode.setLocalTranslation(5 * terrainScale, 0, -terrainScale / 5f);
            rootNode.attachChild(shipNode);
        }
        //Convenience function to generate boat meshes etc. 
        final void generateBoatType(int type, Mesh mesh, ColorRGBA color, float centerX, float centerY) {
            centers[type] = new Vector3f(centerX, centerY, 0);
            if (type == Simulation.PATROL) {
                models[type] = assetMan.loadModel("Models/patrol/patrol.j3o");
                mats[type] = new Material(assetMan, "Common/MatDefs/Light/Lighting.j3md");
                mats[type].setTexture("DiffuseMap", assetMan.loadTexture("Textures/patrol.png"));
            } else if (type == Simulation.CARGO || type == Simulation.CAPTURED) {
                models[type] = assetMan.loadModel("Models/cargo/cargo.j3o");
                mats[type] = new Material(assetMan, "Common/MatDefs/Light/Lighting.j3md");
                mats[type].setTexture("DiffuseMap", assetMan.loadTexture("Textures/cargo.png"));
            } else if (type == Simulation.PIRATE || type == Simulation.ESCORTPIRATE || type == Simulation.WRECK || type == Simulation.ESCORTWRECK) {
                models[type] = assetMan.loadModel("Models/pirate/pirate.j3o");
                mats[type] = new Material(assetMan, "Common/MatDefs/Light/Lighting.j3md");
                mats[type].setTexture("DiffuseMap", assetMan.loadTexture("Textures/pirate.png"));
                mats[type].setTexture("NormalMap", assetMan.loadTexture("Textures/pirate normal.png"));
                mats[type].setTexture("SpecularMap", assetMan.loadTexture("Textures/pirate spec.png"));
            } else {
                models[type] = new Geometry("boat", mesh);
                mats[type] = new Material(assetMan, "Common/MatDefs/Light/Lighting.j3md");
                centers[type] = new Vector3f(centerX, centerY, 0);
                mats[type].setColor("Ambient", color);
                mats[type].setColor("Diffuse", color);
                return;
            }
            mats[type].setColor("Diffuse", ColorRGBA.White);
            mats[type].setBoolean("UseMaterialColors", true);
            mats[type].setColor("Specular", ColorRGBA.White);
            mats[type].setFloat("Shininess", 1f);  // [0,128]
        }
        //updates the positions of boat nodes in scenegraph based on the current state of sim.ships
        //alpha is the amount of time since the last tick, alpha = 1 is the next tick
        void update(float alpha) {
            shipNode.detachAllChildren();
            lighting.removeParticles();
            shipNode.setLocalTranslation(5 * terrainScale, 0, -terrainScale / 5f);
            for (Simulation.Ship ship : sim.ships) {
                Simulation.Ship visualShip = ship.previousStates.get(ship.previousStates.size() - 2);
                Spatial boatModel = models[visualShip.type].clone();
                Material matBoat = mats[visualShip.type];
                boatModel.setMaterial(matBoat);
                Transform transform = interpolateShipTransform(ship, alpha);
                boatModel.getLocalTransform().set(transform);
                shipNode.attachChild(boatModel);
                if (ship.type == Simulation.WRECK) {
                    lighting.addParticles(boatModel.getWorldTranslation());
                }
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
                delta = FastMath.interpolateCatmullRom(alpha - 0.1f * step(alpha - 0.5f), 0.5f, sample3, sample2, sample1, sample0).subtract(translation);
                delta = delta.mult(-step(alpha - 0.5f));
            } else {
                int i = 1;
                do {
                    sample1 = sample2.clone();
                    sample2 = sampleShipPos(ship, i + 2);
                    i++;
                } while (sample1.x == sample2.x && sample1.y == sample2.y);
                delta = FastMath.interpolateCatmullRom(0.9f, 0.5f, sampleShipPos(ship, 3 + i), sample2, sample1, sampleShipPos(ship, 0 + i)).subtract(translation);
                delta = delta.mult(-1f);
            }
            Quaternion rotation = new Quaternion().fromAngleAxis(FastMath.atan2(delta.y, delta.x), Vector3f.UNIT_Y);
            //translation.x = (int) translation.x; translation.y = (int) translation.y;
            return new Transform(new Vector3f(translation.x, translation.z, sim.size.y - translation.y), rotation);
        }
        //samples the ships position from the previousStates arrayList at the current time minus tMinus.
        Vector3f sampleShipPos(Simulation.Ship ship, int tMinus) {
            Vector3f posOut = new Vector3f();
            Simulation.Ship previousShip = ship.previousStates.get(ship.previousStates.size() - tMinus - 1);
            posOut.x = 0.5f + centers[previousShip.type].x + previousShip.position.x;
            posOut.y = 0.5f + centers[previousShip.type].y + previousShip.position.y;
            if (previousShip.type == Simulation.WRECK || previousShip.type == Simulation.ESCORTWRECK) {
                posOut.z = -previousShip.ticksSinceLastMove * 1f/10f;
            }
            return posOut;
        }
        private float step(float x) {
            if (x > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    }
    /**
     *
     * This class renders scenery like the sea and surounding terrain
     *
     */
    class Background {
        Node backgroundNode;
        Background() {
            Vector3f lightDir = new Vector3f(0, -1, 0);
            water = new WaterFilter(rootNode, lightDir);
            FilterPostProcessor fpp = new FilterPostProcessor(assetMan);
            //        LightScatteringFilter lsf = new LightScatteringFilter(lightDir.mult(-300));
            //        lsf.setLightDensity(1.0f);
            //        fpp.addFilter(lsf);
            //        
            //fpp.addFilter(new TranslucentBucketFilter());
            //com.jme3.post.filters.TranslucentBucketFilter
            //
            // fpp.setNumSamples(4);
            water.setWaveScale(0.03f);
            water.setMaxAmplitude(0.03f);
            water.setUseFoam(false);
            water.setFoamExistence(new Vector3f(1f, 4, 0.025f));
            water.setFoamTexture((Texture2D) assetMan.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
            water.setNormalScale(20f);
            water.setShininess(0.25f);
            //water.setSunScale(1f);
            water.setColorExtinction(new Vector3f(.05f, .12f, .27f));
            water.setDeepWaterColor(new ColorRGBA(0.03f, 0.06f, 0.1f, 1f));
            water.setRadius(SEA_BORDER_SIZE * terrainScale * 5);
            water.setWaterColor(new ColorRGBA(0.4f, 0.9f, 1f, 1f));
            water.setRefractionConstant(0.25f);
            water.setRefractionStrength(0.1f);
            water.setUseCaustics(false);
            water.setUseRefraction(false);
            water.setFoamHardness(0.6f);
            water.setReflectionMapSize(1024);
            water.setWaterHeight(0f);
            
            Spatial terrain = assetMan.loadModel("Models/terrain/terrain.j3o");
            Material terrainMat = new Material(assetMan, "Common/MatDefs/Light/Lighting.j3md");
            terrainMat.setTexture("DiffuseMap", assetMan.loadTexture("Textures/gulf of aden color.png"));
            terrainMat.setColor("Specular", ColorRGBA.White);
            terrainMat.setFloat("Shininess", 100f);  // [0,128]
            terrain.setMaterial(terrainMat);
            
            Quad ground = new Quad(sim.size.x + SEA_BORDER_SIZE * 2, sim.size.y + SEA_BORDER_SIZE * 2);
            Geometry seaGeom = new Geometry("Quad", ground);
            Material mat = new Material(assetMan, "Common/MatDefs/Light/Lighting.j3md");
            mat.setColor("Ambient", ColorRGBA.Blue);
            mat.setColor("Diffuse", ColorRGBA.Blue);
            mat.setBoolean("UseMaterialColors", true);
            seaGeom.setMaterial(mat);
            seaGeom.setLocalTranslation(-SEA_BORDER_SIZE, -3f, -SEA_BORDER_SIZE);
            
            Grid mapGrid = new Grid(sim.size.y + 1, sim.size.x + 1, 1);
            Geometry gridGeom = new Geometry("Grid", mapGrid);
            gridGeom.setShadowMode(RenderQueue.ShadowMode.Off);
            Material matGrid = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            matGrid.setColor("Color", ColorRGBA.Black);
            matGrid.getAdditionalRenderState().setWireframe(true);
            gridGeom.setMaterial(matGrid);
            //gridGeom.center().getLocalTranslation().set(0.0f, 0*sim.size.y, 0.01f);
            //gridGeom.getLocalRotation().fromAngles(FastMath.PI/2, 0f, 0f);
            backgroundNode = new Node("background");
            rootNode.attachChild(backgroundNode);
            if (sim.size.x / sim.size.y > 4) {
                terrainScale = sim.size.x * 5 / 40;
            } else {
                terrainScale = sim.size.y * 5 / 10;
            }
            terrain.setLocalScale(terrainScale, terrainScale / 1.5f, terrainScale);
            terrain.getLocalRotation().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
            terrain.setLocalTranslation(0, -0.03f, 0);
            //terrain.center();
            backgroundNode.attachChild(terrain);
            //backgroundNode.attachChild(seaGeom);
            gridGeom.getLocalRotation().fromAngleAxis(-2.8f, Vector3f.UNIT_Y);
            gridGeom.setLocalTranslation(5 * terrainScale, 0, -terrainScale / 5f);
            backgroundNode.attachChild(gridGeom);
            fpp.addFilter(water);
            viewPort.addProcessor(fpp);
            //createAxisReference();
        }
        void update(float alhpa) {
        }
    }
    class Lighting {
        Node lightingNode;
        DirectionalLight sunLight;
        DirectionalLight moonLight;
        DirectionalLight currentLight;
        DirectionalLightShadowRenderer dlsr;
        AmbientLight sunAmbient;
        AmbientLight moonAmbient;
        int maxEmitCount = 15;
        List<Emitter> emitters;
        Geometry sungeom;
        Lighting() {
            sunLight = new DirectionalLight();
            sunAmbient = new AmbientLight();
            moonLight = new DirectionalLight();
            moonAmbient = new AmbientLight();
            emitters = new LinkedList<Emitter>();
            final int SHADOWMAP_SIZE = 2048;
            dlsr = new DirectionalLightShadowRenderer(assetMan, SHADOWMAP_SIZE, 3);
            dlsr.setLight(sunLight);
            viewPort.addProcessor(dlsr);
            lightingNode = new Node("lighting");
            rootNode.addLight(sunLight);
            rootNode.addLight(sunAmbient);
            currentLight = sunLight;
            rootNode.attachChild(lightingNode);
            rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            Box sunbox = new Box(1, 1, 1);
            sungeom = new Geometry("Quad", sunbox);
            Material mat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Yellow);
            sungeom.setMaterial(mat);
            //rootNode.attachChild(sungeom);
        }
        class Emitter {
            int emitCount = 0; //how many times we've emitted
            Vector3f worldTranslation;
            ParticleEmitter particles;
            Emitter(Vector3f pWorldTranslation) {
                worldTranslation = pWorldTranslation;
                particles = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
                Material mat_red = new Material(assetMan, "Common/MatDefs/Misc/Particle.j3md");
                mat_red.setTexture("Texture", assetMan.loadTexture("Effects/Explosion/flame.png"));
                mat_red.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
                particles.setQueueBucket(Bucket.Translucent);
                particles.setMaterial(mat_red);
                particles.setImagesX(2);
                particles.setImagesY(2); // 2x2 texture animation
                particles.setEndColor(new ColorRGBA(.5f, 0f, 0f, 0.5f));   // red
                particles.setStartColor(new ColorRGBA(.01f, .01f, .01f, 1f)); // yellow
                //fire.setEndColor(  new ColorRGBA(0.3f, 0.3f, 0.3f, 0.4f));   // grey
                //fire.setStartColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.5f)); // black
                particles.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
                particles.setStartSize(.15f);
                particles.setEndSize(0.01f);
                particles.setGravity(0, 0, 0);
                particles.setLowLife(.5f);
                particles.setHighLife(1.5f);
                particles.setLocalTranslation(worldTranslation);
                particles.getParticleInfluencer().setVelocityVariation(0.05f);
                rootNode.attachChild(particles);
                //fire.emitAllParticles();
                //emitters.add(fire);
            }
            void emit() {
                emitCount++;
            }
            void remove() {
                rootNode.detachChild(particles);
            }
        }
        void removeParticles() {
            //for (String name : new ArrayList<String>(names)) {
            // Do something
            //names.remove(nameToRemove);
            for (Emitter emitter : new LinkedList<Emitter>(emitters)) {
                emitter.emit();
                if (emitter.emitCount > maxEmitCount) {
                    emitter.remove();
                    emitters.remove(emitter);
                }
            }
        }
        void addParticles(Vector3f worldTranslation) {
            //Emitter emitter = new Emitter(worldTranslation);
            //emitters.add(emitter);
        }
        void update(float alpha) {
            //solve the proportion for x: deltaAlpha / dayHours = x / 180
            float elapsedDays = sim.getElapsedDays(alpha);
            int day = (int) elapsedDays;
            float timeOfDay = elapsedDays - day;
            float sunAngle = timeOfDay * 360;
            float moonAngle = timeOfDay * 360 + 180;
            float sunIntensityScalar = 1.5f;
            float sunIntensity = sunIntensityScalar * FastMath.sin(sunAngle * FastMath.DEG_TO_RAD);
            if (sunIntensity < 0) {
                sunIntensity = 0;
            }
            float moonIntensityScalar = 0.5f;
            float moonIntensity = moonIntensityScalar * FastMath.sin(moonAngle * FastMath.DEG_TO_RAD);
            if (moonIntensity < 0) {
                moonIntensity = 0;
            }
            float angle;
            ColorRGBA sunColor = new ColorRGBA(1f, 1f, 1f, 0f);
            ColorRGBA sunAmbColor = new ColorRGBA(0.7f, 0.9f, 1f, 0f);
            ColorRGBA sunriseTint = new ColorRGBA(1.0f, 0.71f, 0.76f, 0f);
            ColorRGBA sunsetTint = new ColorRGBA(0.88f, 0.21f, 0.0f, 0f);
            ColorRGBA moonColor = new ColorRGBA(0.8f, 0.8f, 1f, 0f);
            ColorRGBA moonAmbColor = new ColorRGBA(1f, 1f, 1f, 0f);
            ColorRGBA maxDeepWaterColor = new ColorRGBA(.12f, .75f, 1f, 0f);
            float baseAmbient = 1.0f;
            //ColorRGBA defaultDeepWaterColor = new ColorRGBA(0.0039f, 0.00196f, 0.145f, 0f);
            float lightIntensity;
            if (sunIntensity > moonIntensity) {
                lightIntensity = sunIntensity;
                if (currentLight == moonLight) {
                    rootNode.removeLight(moonLight);
                    rootNode.removeLight(moonAmbient);
                    rootNode.addLight(sunLight);
                    rootNode.addLight(sunAmbient);
                    dlsr.setLight(sunLight);
                    currentLight = sunLight;
                }
                //calculate sunset tint to be applied, i don't know a better way to calculate this XD
//                ColorRGBA tint = new ColorRGBA(sunsetTint.r - (sunsetTint.r * sunIntensity / sunIntensityScalar),
//                        sunsetTint.g - (sunsetTint.g * sunIntensity / sunIntensityScalar),
//                        sunsetTint.b - (sunsetTint.b * sunIntensity / sunIntensityScalar),
//                        0f);
                
                // Computer interpolation factor
                ColorRGBA tint = sunriseTint;
                float betaSunAngle = sunAngle;
                if(sunAngle > 90){
                    betaSunAngle = 180 - betaSunAngle;
                    tint = sunsetTint;
                }
                float beta = 1;
                if(betaSunAngle < 10 )
                    beta = FastMath.cos(FastMath.DEG_TO_RAD*betaSunAngle*(90/10));
                else if(betaSunAngle < 45) {
                    beta = FastMath.sin(FastMath.DEG_TO_RAD*((betaSunAngle-10)*(90/35)));
                }
                
                ColorRGBA curSunColor = new ColorRGBA();
                curSunColor.interpolate(tint, sunColor, beta);
                System.out.println(curSunColor);
                sunLight.setColor(curSunColor.mult(sunIntensity));
                sunAmbient.setColor(curSunColor.mult(.2f * sunIntensity+baseAmbient));
                //sunAmbient.setColor(sunAmbColor.mult(.2f * sunIntensity).add(baseAmbient));
                dlsr.setShadowIntensity(sunIntensity-baseAmbient); //TODO
                angle = sunAngle;
            } else {
                lightIntensity = moonIntensity;
                if (currentLight == sunLight) {
                    rootNode.removeLight(sunLight);
                    rootNode.removeLight(sunAmbient);
                    rootNode.addLight(moonLight);
                    rootNode.addLight(moonAmbient);
                    dlsr.setLight(moonLight);
                    currentLight = moonLight;
                }
                //we need to add the tint to the moon as well or there will be an annoying jump between light transitions
//                ColorRGBA tint = new ColorRGBA(sunriseTint.r - (sunriseTint.r * moonIntensity / moonIntensityScalar),
//                        sunriseTint.g - (sunriseTint.g * moonIntensity / moonIntensityScalar),
//                        sunriseTint.b - (sunriseTint.b * moonIntensity / moonIntensityScalar),
//                        0f);
                moonLight.setColor(moonColor.mult(moonIntensity));
                moonAmbient.setColor(moonAmbColor.mult(.4f * moonIntensity+baseAmbient));
                angle = moonAngle;
            }
            Vector3f dir = new Vector3f(FastMath.cos(angle * FastMath.DEG_TO_RAD), -FastMath.sin(angle * FastMath.DEG_TO_RAD), 0f).normalizeLocal();
            currentLight.setDirection(dir);
            water.setLightColor(currentLight.getColor());
            water.setLightDirection(currentLight.getDirection());
            //sunIntensity needs to be divided by what it's multiplied by, sort of
            ColorRGBA temp = new ColorRGBA( maxDeepWaterColor.r, // + maxDeepWaterColor.r * lightIntensity / 4f,
                                            maxDeepWaterColor.g, // + maxDeepWaterColor.g * lightIntensity / 4f,
                                            maxDeepWaterColor.b, 1.0f);// + maxDeepWaterColor.b * lightIntensity / 4f, 1.0f);
            water.setDeepWaterColor(maxDeepWaterColor);
            water.setWindDirection(Vector2f.UNIT_XY.mult(0.2f*pApp.startScreen.getSimSpeed()));
        }
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
            g.setShadowMode(RenderQueue.ShadowMode.Off);
            Material mat = new Material(assetMan, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.getAdditionalRenderState().setWireframe(true);
            mat.setColor("Color", colors[i]);
            g.setMaterial(mat);
            g.getLocalTranslation().set(0f, 0f, 0.1f);
            rootNode.attachChild(g);
        }
    }
}






