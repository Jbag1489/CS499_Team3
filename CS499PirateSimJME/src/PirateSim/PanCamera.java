/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PirateSim;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.renderer.Camera;
import com.jme3.input.FlyByCamera;
import com.jme3.math.Vector3f;

/**
 * This class implements a camera that pans around.
 * It is very boring so I wouldn't worry about it.
 * TODO make flyCam debug only
 */
public class PanCamera implements AnalogListener, ActionListener {
    InputManager inputMan;
    Camera cam;
    Vector3f up;
    boolean canPan;
    String[] mappings = new String[]{"PANCAM_Left", "PANCAM_Right", "PANCAM_Up", "PANCAM_Down",
                                     "PANCAM_KeyLeft", "PANCAM_KeyRight", "PANCAM_KeyUp", "PANCAM_KeyDown", "PANCAM_ZoomIn", "PANCAM_ZoomOut", "PANCAM_Drag"};
    PirateSimApp pSimApp;

    FlyByCamera flyByCam;
    boolean flyBy = false;
    boolean flyByReg = false;
    
    PanCamera(PirateSimApp simApp, Camera pCam, InputManager pInputMan, FlyByCamera pFlyByCam) {//TODO fly cam debug only
        pSimApp = simApp;
        cam = pCam;
        inputMan = pInputMan;
        flyByCam = pFlyByCam;
        up = Vector3f.UNIT_Z;
        canPan = false;
        
        cam.setLocation(new Vector3f(0f, 20f, 0f));
        cam.lookAtDirection(Vector3f.UNIT_Y.mult(-1), up);
        
        flyByCam.setDragToRotate(true); //TODO debug only
        flyByCam.setMoveSpeed(30f);
        flyByCam.setZoomSpeed(30f);
        flyByCam.setEnabled(false);
    }
    public void register() {
        inputMan.addMapping("PANCAM_Left", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputMan.addMapping("PANCAM_KeyLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        inputMan.addMapping("PANCAM_Right", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputMan.addMapping("PANCAM_KeyRight", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputMan.addMapping("PANCAM_Up", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputMan.addMapping("PANCAM_KeyUp", new KeyTrigger(KeyInput.KEY_UP));
        inputMan.addMapping("PANCAM_Down", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputMan.addMapping("PANCAM_KeyDown", new KeyTrigger(KeyInput.KEY_DOWN));
        inputMan.addMapping("PANCAM_ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputMan.addMapping("PANCAM_ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputMan.addMapping("PANCAM_Drag", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
if (!flyByReg) {
inputMan.addMapping("SWITCH_CAM", new KeyTrigger(KeyInput.KEY_C)); //TODO debug only
inputMan.addListener(this, "SWITCH_CAM");
flyByReg = true;
}
        inputMan.addListener(this, mappings);
    }
    public void unregister() {
        for (String mapping : mappings) if (inputMan.hasMapping(mapping)) inputMan.deleteMapping(mapping);
    }
    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("PANCAM_Left")){
            pan(-value, cam.getLeft(), canPan);
        } else if (name.equals("PANCAM_Right")){
            pan(value, cam.getLeft(), canPan);
        } else if (name.equals("PANCAM_Up")){
            pan(-value, cam.getUp(), canPan);
        } else if (name.equals("PANCAM_Down")){
            pan(value, cam.getUp(), canPan);
        } else if (name.equals("PANCAM_KeyLeft")){
            pan(value, cam.getLeft(), true);
        } else if (name.equals("PANCAM_KeyRight")){
            pan(-value, cam.getLeft(), true);
        } else if (name.equals("PANCAM_KeyUp")){
            pan(value, cam.getUp(), true);
        } else if (name.equals("PANCAM_KeyDown")){
            pan(-value, cam.getUp(), true);
        } else if (name.equals("PANCAM_ZoomIn")){
            zoom(value);
        } else if (name.equals("PANCAM_ZoomOut")){
            zoom(-value);
        }
    }
    void pan(float value, Vector3f axis, boolean can) {
        if (!can) return;
        Vector3f pos = cam.getLocation();
        pos = pos.add(axis.mult(value*pos.y));
        cam.setLocation(pos);
        bound();
    }
    void zoom(float value) {
        Vector3f pos = cam.getLocation();
        float speed = pos.y/10*value;
        pos = pos.subtract(Vector3f.UNIT_Y.mult(speed));
        if (pos.y > 1 && pos.y < 400) cam.setLocation(pos);
    }
    void bound() {
        float scale = 1 ;//pSimApp.scene.background.terrainScale;
        Vector3f center = new Vector3f(5*scale, 0, -scale/5);
        Vector3f size = new Vector3f(-45*scale, 0, -35*scale);
        Vector3f localCamPos = cam.getLocation().subtract(center);
        //float heightScale = localCamPos.y/;
        
        if (localCamPos.x < size.x) {localCamPos.x = size.x; System.out.println("neg x edge");}
        if (localCamPos.x > -size.x) {localCamPos.x = -size.x; System.out.println("pos x edge");}
        if (localCamPos.z < size.z) {localCamPos.z = size.z; System.out.println("neg z edge");}
        if (localCamPos.z > -size.z) {localCamPos.z = -size.z; System.out.println("pos z edge");}
        cam.setLocation(localCamPos.add(center));
        System.out.println(localCamPos.x + ", " + localCamPos.z);
//            terrain.setLocalScale(terrainScale, terrainScale/1.5f, terrainScale);
//            terrain.getLocalRotation().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
//            terrain.setLocalTranslation(-5*terrainScale, 0, terrainScale/5f);
    }
    public void onAction(String name, boolean value, float tpf) {
        if (name.equals("PANCAM_Drag")){
            canPan = value;
        } else if (name.equals("SWITCH_CAM")) { //TODO debug only
            if (!value) return;
            if (flyBy) {
                System.out.println("disable flycam");
                flyByCam.setEnabled(false);
                flyBy = false;
                register();
            } else {
                System.out.println("enable flycam");
                unregister();
                flyByCam.setEnabled(true);
                flyBy = true;
            }
        }
    }
}