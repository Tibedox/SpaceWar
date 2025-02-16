package ru.spacewar;

import static ru.spacewar.Main.LEFT;

public class Joystick {
    public float x, y;
    public float width, height;
    public boolean side;
    public String text;

    public Joystick(float diameter, boolean side) {
        width = height = diameter;
        this.side = side;
        y = height/2;

    }

    public String getText(){
        return side == LEFT ? "Joystick Left" : "Joystick Right";
    }
}
