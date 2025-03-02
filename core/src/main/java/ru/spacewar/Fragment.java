package ru.spacewar;

import static ru.spacewar.Main.*;

import com.badlogic.gdx.math.MathUtils;

public class Fragment extends SpaceObject{
    public int num;
    public float rotation;
    private float vRotation;

    public Fragment(float x, float y, int type, int lengthImgFragments) {
        super(x, y);
        this.type = type;
        this.num = MathUtils.random(0, lengthImgFragments-1);
        width = MathUtils.random(20, 40);
        height = MathUtils.random(20, 40);
        float a = MathUtils.random(0f, 360f);
        float v = MathUtils.random(2f, 12f);
        vx = v*MathUtils.sin(a);
        vy = v*MathUtils.cos(a);
        vRotation = MathUtils.random(-5f, 5f);
    }

    @Override
    public void move() {
        super.move();
        rotation += vRotation;
    }

    public boolean outOfScreen(){
        return y<-height/2 || y>SCR_HEIGHT+height/2 || x<-width/2 || x>SCR_WIDTH+width/2;
    }
}
