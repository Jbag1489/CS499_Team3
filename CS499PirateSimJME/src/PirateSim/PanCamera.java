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
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;


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
        
        cam.setLocation(new Vector3f(0f, pSimApp.scene.terrainScale*12, 0f));
        bound();
        cam.lookAtDirection(Vector3f.UNIT_Y.mult(-1), up);
        
        flyByCam.setDragToRotate(true);
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
            inputMan.addMapping("SWITCH_CAM", new KeyTrigger(KeyInput.KEY_C));
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
            pan(-value, Vector3f.UNIT_X, canPan);
        } else if (name.equals("PANCAM_Right")){
            pan(value, Vector3f.UNIT_X, canPan);
        } else if (name.equals("PANCAM_Up")){
            pan(-value, Vector3f.UNIT_Z, canPan);
        } else if (name.equals("PANCAM_Down")){
            pan(value, Vector3f.UNIT_Z, canPan);
        } else if (name.equals("PANCAM_KeyLeft")){
            pan(value, Vector3f.UNIT_X, true);
        } else if (name.equals("PANCAM_KeyRight")){
            pan(-value, Vector3f.UNIT_X, true);
        } else if (name.equals("PANCAM_KeyUp")){
            pan(value, Vector3f.UNIT_Z, true);
        } else if (name.equals("PANCAM_KeyDown")){
            pan(-value, Vector3f.UNIT_Z, true);
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
        cam.setLocation(pos);
        
        bound();
    }
    final void bound() {
        float scale = pSimApp.scene.terrainScale/5;
        float height = scale*35*2;
        Vector3f center = new Vector3f(5*scale, 1, -scale/5);
        Vector3f size = new Vector3f(-45*scale, 0, -35*scale);
        Vector3f localCamPos = cam.getLocation().subtract(center);
        if (localCamPos.y < 0) localCamPos.y = 0;
        if (localCamPos.y > height) localCamPos.y = height;
        float heightScale = (height - localCamPos.y)/height;
        size = size.mult(heightScale);
        if (localCamPos.x < size.x) {localCamPos.x = size.x;}
        if (localCamPos.x > -size.x) {localCamPos.x = -size.x;}
        if (localCamPos.z < size.z) {localCamPos.z = size.z;}
        if (localCamPos.z > -size.z) {localCamPos.z = -size.z;}
        cam.setLocation(localCamPos.add(center));
        float panUp = 0;
        float panUpHeight = 8;
        if (localCamPos.y < panUpHeight) panUp = (panUpHeight - localCamPos.y)/panUpHeight;
        Matrix4f panUpRot = new Matrix4f();
        panUpRot.fromAngleAxis(-panUp*FastMath.PI/3f, Vector3f.UNIT_X);
        cam.lookAtDirection(panUpRot.mult(Vector3f.UNIT_Y.mult(-1)), up);
    }
    public void onAction(String name, boolean value, float tpf) {
        if (name.equals("PANCAM_Drag")){
            canPan = value;
        } else if (name.equals("SWITCH_CAM")) { //TODO debug only
            if (!value) return;
            if (flyBy) {
                flyByCam.setEnabled(false);
                flyBy = false;
                register();
            } else {
                unregister();
                flyByCam.setEnabled(true);
                flyBy = true;
            }
        }
    }
}