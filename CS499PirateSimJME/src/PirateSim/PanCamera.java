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
 *
 * @author Owen
 */
public class PanCamera implements AnalogListener, ActionListener {
    InputManager inputMan;
    Camera cam;
    Vector3f up;
    boolean canPan;
    String[] mappings = new String[]{"PANCAM_Left", "PANCAM_Right", "PANCAM_Up", "PANCAM_Down",
                                     "PANCAM_KeyLeft", "PANCAM_KeyRight", "PANCAM_KeyUp", "PANCAM_KeyDown", "PANCAM_ZoomIn", "PANCAM_ZoomOut", "PANCAM_Drag"};

    FlyByCamera flyByCam;
    boolean flyBy = false;
    boolean flyByReg = false;
    
    PanCamera(Camera pCam, InputManager pInputMan, FlyByCamera pFlyByCam) {//TODO fly cam debug only
        cam = pCam;
        inputMan = pInputMan;
        flyByCam = pFlyByCam;
        up = Vector3f.UNIT_Y;
        canPan = false;
        
        cam.setLocation(new Vector3f(5f, 5f, 20f));
        cam.lookAtDirection(Vector3f.UNIT_Z.mult(-1), up);
        
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
        pos = pos.add(axis.mult(value*pos.z));
        cam.setLocation(pos);
    }
    void zoom(float value) {
        Vector3f pos = cam.getLocation();
        float speed = pos.z/10*value;
        pos = pos.subtract(Vector3f.UNIT_Z.mult(speed));
        if (pos.z > 1 && pos.z < 200) cam.setLocation(pos);
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