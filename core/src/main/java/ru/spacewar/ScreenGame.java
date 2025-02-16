package ru.spacewar;

import static ru.spacewar.Main.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;

public class ScreenGame implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Vector3 touch;
    private BitmapFont font;
    private Main main;

    Texture imgJoystick;
    Texture imgBackGround;
    Texture imgShipsAtlas;
    TextureRegion[] imgShip = new TextureRegion[12];

    SunButton btnBack;

    Space[] space = new Space[2];
    Ship ship;

    public ScreenGame(Main main) {
        this.main = main;
        batch = main.batch;
        camera = main.camera;
        touch = main.touch;
        font = main.font70white;

        imgJoystick = new Texture("joystick.png");
        imgBackGround = new Texture("space0.png");
        imgShipsAtlas = new Texture("ships_atlas.png");
        for (int i = 0; i < imgShip.length; i++) {
            imgShip[i] = new TextureRegion(imgShipsAtlas, (i<6?i:12-i)*400, 0, 400, 400);
        }

        btnBack = new SunButton("x", font, 850, 1600);

        space[0] = new Space(0, 0);
        space[1] = new Space(0, SCR_HEIGHT);
        ship = new Ship(SCR_WIDTH/2, 200);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new SunInputProcessor());
    }

    @Override
    public void render(float delta) {
        // касания
        if(Gdx.input.justTouched()){
            touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touch);

            if(btnBack.hit(touch)){
                main.setScreen(main.screenMenu);
            }
        }
        if(controls == ACCELEROMETER){
            ship.vx = -Gdx.input.getAccelerometerX()*2;
            ship.vy = -Gdx.input.getAccelerometerY()*2;
        }

        // события
        for(Space s: space) s.move();
        ship.move();

        // отрисовка
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for(Space s: space) batch.draw(imgBackGround, s.x, s.y, s.width, s.height);
        batch.draw(imgShip[ship.phase], ship.scrX(), ship.scrY(), ship.width, ship.height);
        btnBack.font.draw(batch, btnBack.text, btnBack.x, btnBack.y);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        imgBackGround.dispose();
        imgShipsAtlas.dispose();
    }

    class SunInputProcessor implements InputProcessor{

        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if(controls == SCREEN) {
                touch.set(screenX, screenY, 0);
                camera.unproject(touch);
                ship.touch(touch);
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if(controls == SCREEN) {
                touch.set(screenX, screenY, 0);
                camera.unproject(touch);
                ship.touch(touch);
            }
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return false;
        }
    }
}
