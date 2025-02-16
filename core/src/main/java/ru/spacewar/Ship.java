package ru.spacewar;

import static ru.spacewar.Main.*;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

public class Ship extends SpaceObject{
    public int phase, nPhases = 12;
    private long timeLastPhase, timePhaseInterval = 50;

    public Ship(float x, float y) {
        super(x, y);
        width = 200;
        height = 200;
    }

    @Override
    public void move() {
        super.move();
        changePhase();
        outOfScreen();
    }

    private void outOfScreen(){
        if(x<width/2){
            vx = 0;
            x = width/2;
        }
        if(x>SCR_WIDTH-width/2){
            vx = 0;
            x = SCR_WIDTH-width/2;
        }
        if(y<height/2){
            vy = 0;
            y = height/2;
        }
        if(y>SCR_HEIGHT-height/2){
            vy = 0;
            y = SCR_HEIGHT-height/2;
        }
    }

    private void changePhase(){
        if(TimeUtils.millis()>timeLastPhase+timePhaseInterval) {
            if (++phase == nPhases) phase = 0;
            timeLastPhase = TimeUtils.millis();
        }
    }

    public void touchScreen(Vector3 t){
        vx = (t.x - x)/30;
        vy = (t.y - y)/30;
    }

    public void touchJoystick(Vector3 t, Joystick j){
        vx = (t.x - j.x)/10;
        vy = (t.y - j.y)/10;
    }
}
