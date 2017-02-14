/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package PirateSim;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class MyStartScreen extends AbstractAppState implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private SimpleApplication app;
    static Application myApp = new Application();
    private TextField simSpeedTextField;
    
      
    
    
    // Need values for SimSpeed (0 for pause)
    float simSpeed = 1; // ties to timeAcceleration in Application
    boolean simPaused = true; // ties to simPaused in Application
    // Initialize to true, when start is pressed, it will "unpause" 

    /**
     * custom methods
     */
    public void startGame(String nextScreen) {

        // Get simulation speed from text box...hopefully
        TextField textField = screen.findNiftyControl("simSpeedTextField", TextField.class);
        String text = textField.getDisplayedText();
        System.out.println(text);

        nifty.gotoScreen(nextScreen);  // switch to another screen
        simPaused = false; // Will start running the simulation
    }
    
    /*
     * Not yet implemented, but this works to update the labels
     */
    public void updateLabel(){
        Label label = screen.findNiftyControl("simSpeedLabel", Label.class);
        label.setText("the text updated");
    }

    public void quitGame() {
        myApp.stop();
    }

    public MyStartScreen() {
        /**
         * Your custom constructor, can accept arguments
         */
    }

    public void changePauseState() {
        if (simPaused) {
            simPaused = false;
        } else {
            simPaused = true;
        }
    }

    /**
     * Nifty GUI ScreenControl methods
     */
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }

    /**
     * jME3 AppState methods
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        myApp = (SimpleApplication) app;
    }

    @Override
    public void update(float tpf) {
        /**
         * jME update loop!
         */
    }
}