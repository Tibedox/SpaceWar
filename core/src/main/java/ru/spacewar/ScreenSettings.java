package ru.spacewar;

import static ru.spacewar.Main.ACCELEROMETER;
import static ru.spacewar.Main.JOYSTICK;
import static ru.spacewar.Main.RIGHT;
import static ru.spacewar.Main.SCREEN;
import static ru.spacewar.Main.SCR_HEIGHT;
import static ru.spacewar.Main.SCR_WIDTH;
import static ru.spacewar.Main.controls;
import static ru.spacewar.Main.isSoundOn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public class ScreenSettings implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Vector3 touch;
    private BitmapFont font70white, font70gray;
    private Main main;

    Texture imgBackGround;

    SunButton btnSettings;
    SunButton btnControl;
    SunButton btnScreen;
    SunButton btnJoystick;
    SunButton btnAccelerometer;
    SunButton btnSound;
    SunButton btnBack;

    public ScreenSettings(Main main) {
        this.main = main;
        batch = main.batch;
        camera = main.camera;
        touch = main.touch;
        font70white = main.font70white;
        font70gray = main.font70gray;

        imgBackGround = new Texture("space3.png");

        loadSettings();
        btnSettings = new SunButton("Settings", font70white, 1500);
        btnControl = new SunButton("Control", font70white, 100, 1200);
        btnScreen = new SunButton("Screen", fontByControls(SCREEN), 200, 1100);
        btnJoystick = new SunButton(main.joystick.getText(), fontByControls(JOYSTICK), 200, 1000);
        btnAccelerometer = new SunButton("Accelerometer", fontByControls(ACCELEROMETER), 200, 900);
        btnSound = new SunButton(isSoundOn ? "Sound ON" : "Sound OFF", font70white, 100, 750);
        btnBack = new SunButton("Back", font70white, 150);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        // касания
        if(Gdx.input.justTouched()){
            touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touch);

            if(btnScreen.hit(touch)){
                btnScreen.setFont(font70white);
                btnJoystick.setFont(font70gray);
                btnAccelerometer.setFont(font70gray);
                controls = SCREEN;
            }
            if(btnJoystick.hit(touch)){
                btnScreen.setFont(font70gray);
                btnJoystick.setFont(font70white);
                btnAccelerometer.setFont(font70gray);
                if(controls == JOYSTICK){
                    main.joystick.setSide(!main.joystick.side);
                    btnJoystick.setText(main.joystick.getText());
                }
                else {
                    controls = JOYSTICK;
                }
            }
            if(btnAccelerometer.hit(touch)){
                if(Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer)) {
                    btnScreen.setFont(font70gray);
                    btnJoystick.setFont(font70gray);
                    btnAccelerometer.setFont(font70white);
                    controls = ACCELEROMETER;
                }
            }
            if(btnSound.hit(touch)){
                isSoundOn = !isSoundOn;
                btnSound.setText(isSoundOn ? "Sound ON" : "Sound OFF");
            }
            if(btnBack.hit(touch)){
                main.setScreen(main.screenMenu);
            }
        }
        // отрисовка
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(imgBackGround, 0, 0, SCR_WIDTH, SCR_HEIGHT);
        btnSettings.font.draw(batch, btnSettings.text, btnSettings.x, btnSettings.y);
        btnControl.font.draw(batch, btnControl.text, btnControl.x, btnControl.y);
        btnScreen.font.draw(batch, btnScreen.text, btnScreen.x, btnScreen.y);
        btnJoystick.font.draw(batch, btnJoystick.text, btnJoystick.x, btnJoystick.y);
        btnAccelerometer.font.draw(batch, btnAccelerometer.text, btnAccelerometer.x, btnAccelerometer.y);
        btnSound.font.draw(batch, btnSound.text, btnSound.x, btnSound.y);
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
        saveSettings();
    }

    @Override
    public void dispose() {

    }

    private BitmapFont fontByControls(int SCR){
        return controls == SCR ? font70white : font70gray;
    }

    private void saveSettings(){
        Preferences prefs = Gdx.app.getPreferences("SpaceWarSettings");
        prefs.putString("name", main.player.name);
        prefs.putInteger("controls", controls);
        prefs.putBoolean("joystick", main.joystick.side);
        prefs.putBoolean("sound", isSoundOn);
        prefs.flush();
    }

    private void loadSettings(){
        Preferences prefs = Gdx.app.getPreferences("SpaceWarSettings");
        main.player.name = prefs.getString("name", "Noname");
        controls = prefs.getInteger("controls", SCREEN);
        main.joystick.setSide(prefs.getBoolean("joystick", RIGHT));
        isSoundOn = prefs.getBoolean("sound", true);
    }
}
