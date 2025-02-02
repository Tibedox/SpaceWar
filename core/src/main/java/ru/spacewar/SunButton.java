package ru.spacewar;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class SunButton {
    String text;
    BitmapFont font;
    float x, y;
    float width, height;

    public SunButton(String text, BitmapFont font, float x, float y) {
        this.text = text;
        this.font = font;
        this.x = x;
        this.y = y;
        GlyphLayout glyphLayout = new GlyphLayout(font, text);
        width = glyphLayout.width;
        height = glyphLayout.height;
    }

    boolean hit(float tx, float ty){
        return x<tx && tx<x+width && y>ty && ty>y-height;
    }
}
