package ru.spacewar;

import static ru.spacewar.Main.LEFT;
import static ru.spacewar.Main.SCR_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Joystick {
    public float x, y;
    public float width, height;
    public boolean side;

    public Joystick(float diameter, boolean side) {
        width = height = diameter;
        setSide(side);
        y = height/2;
    }

    public void setSide(boolean side){
        this.side = side;
        x = side == LEFT ? width/2 : SCR_WIDTH-width/2;
    }

    public String getText(){
        return side == LEFT ? "Joystick Left" : "Joystick Right";
    }

    public boolean isTouchInside(Vector3 t){
        return Math.pow(t.x-x, 2) + Math.pow(t.y-y, 2) <= Math.pow(width/2, 2);
    }

    public float scrX(){
        return x-width/2;
    }

    public float scrY(){
        return y-height/2;
    }
}
