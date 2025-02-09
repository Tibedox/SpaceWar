package ru.spacewar;

import static ru.spacewar.Main.*;

public class Space extends SpaceObject{
    public Space(float x, float y){
        super(x, y);
        width = SCR_WIDTH;
        height = SCR_HEIGHT+6;
        vy = -5;
    }

    @Override
    public void move() {
        super.move();
        outOfScreen();
    }

    private void outOfScreen(){
        if(y<-SCR_HEIGHT) y = SCR_HEIGHT;
    }
}
