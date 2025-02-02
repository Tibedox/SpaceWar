package ru.spacewar;

public class SpaceObject {
    public float x, y;
    public float width, height;
    public float vx, vy;

    public SpaceObject(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void move(){
        x += vx;
        y += vy;
    }
}
