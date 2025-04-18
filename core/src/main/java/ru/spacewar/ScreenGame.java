package ru.spacewar;

import static ru.spacewar.Main.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScreenGame implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Vector3 touch;
    private BitmapFont font50, font70;
    private Main main;

    Texture imgJoystick;
    Texture imgBackGround;
    Texture imgShipsAtlas;
    Texture imgShotsAtlas;
    TextureRegion[] imgShip = new TextureRegion[12];
    TextureRegion[][] imgEnemy = new TextureRegion[4][12];
    TextureRegion[] imgShot = new TextureRegion[4];
    TextureRegion[][] imgFragment = new TextureRegion[5][36];

    Sound sndBlaster;
    Sound sndExplosion;

    SunButton btnBack;
    SunButton btnSwitchGlobal;
    SunButton btnRestart;

    Space[] space = new Space[2];
    Ship ship;
    List<Enemy> enemies = new ArrayList<>();
    List<Shot> shots = new ArrayList<>();
    List<Fragment> fragments = new ArrayList<>();
    Player[] players = new Player[10];
    List<DataFromDB> db = new ArrayList<>();

    private long timeLastSpawnEnemy, timeSpawnEnemyInterval = 1500;
    private long timeLastShoot, timeShootInterval = 800;
    private int nFragments = 150;
    private boolean gameOver;
    private boolean showGlobalRecords;

    public ScreenGame(Main main) {
        this.main = main;
        batch = main.batch;
        camera = main.camera;
        touch = main.touch;
        font70 = main.font70white;
        font50 = main.font50white;

        sndBlaster = Gdx.audio.newSound(Gdx.files.internal("blaster.mp3"));
        sndExplosion = Gdx.audio.newSound(Gdx.files.internal("explosion.mp3"));

        imgJoystick = new Texture("joystick.png");
        imgBackGround = new Texture("space0.png");
        imgShipsAtlas = new Texture("ships_atlas.png");
        imgShotsAtlas = new Texture("shots.png");
        for (int i = 0; i < imgShip.length; i++) {
            imgShip[i] = new TextureRegion(imgShipsAtlas, (i<7?i:12-i)*400, 0, 400, 400);
        }
        for(int j = 0; j<imgEnemy.length; j++) {
            for (int i = 0; i < imgEnemy[j].length; i++) {
                imgEnemy[j][i] = new TextureRegion(imgShipsAtlas, (i < 7 ? i : 12 - i) * 400, (j+1)*400, 400, 400);
            }
        }
        for (int i = 0; i < imgShot.length; i++) {
            imgShot[i] = new TextureRegion(imgShotsAtlas, i*100, 0, 100, 350);
        }
        int k = (int) Math.sqrt(imgFragment[0].length);
        int size = 400/k;
        for(int j = 0; j<imgFragment.length; j++) {
            for (int i = 0; i < imgFragment[j].length; i++) {
                if(j==imgFragment.length-1) imgFragment[j][i] = new TextureRegion(imgShip[0], i%k*size, i/k*size, size, size);
                else imgFragment[j][i] = new TextureRegion(imgEnemy[j][0], i%k*size, i/k*size, size, size);
            }
        }

        btnBack = new SunButton("x", font70, 850, 1600);
        btnSwitchGlobal = new SunButton("Local", font70, 1300);
        btnRestart = new SunButton("restart", font70, 300);

        space[0] = new Space(0, 0);
        space[1] = new Space(0, SCR_HEIGHT);
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player();
        }
        loadTableOfRecords();
    }

    @Override
    public void show() {
        Gdx.graphics.setForegroundFPS(60);
        Gdx.input.setInputProcessor(new SunInputProcessor());
        gameStart();
    }

    @Override
    public void render(float delta) {
        // касания
        if(Gdx.input.justTouched()){
            touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touch);

            if(btnSwitchGlobal.hit(touch) && gameOver){
                showGlobalRecords = !showGlobalRecords;
                if(showGlobalRecords){
                    btnSwitchGlobal.setText("Global");
                    loadFromInternetDB();
                } else {
                    btnSwitchGlobal.setText("Local");
                }
            }
            if(btnBack.hit(touch)){
                main.setScreen(main.screenMenu);
            }
            if(gameOver && btnRestart.hit(touch)){
                gameStart();
            }
        }
        if(controls == ACCELEROMETER){
            ship.vx = -Gdx.input.getAccelerometerX()*2;
            ship.vy = -Gdx.input.getAccelerometerY()*2;
        }

        // события
        for(Space s: space) s.move();
        spawnEnemy();
        if(!gameOver) {
            ship.move();
            spawnShots();
        }
        for(int i=enemies.size()-1; i>=0; i--) {
            enemies.get(i).move();
            if(enemies.get(i).outOfScreen()){
                enemies.remove(i);
                if(!gameOver) gameOver();
                break;
            }
            if(enemies.get(i).overlap(ship)){
                spawnFragments(enemies.get(i));
                enemies.remove(i);
                gameOver();
            }
        }
        for(int i=shots.size()-1; i>=0; i--){
            shots.get(i).move();
            if(shots.get(i).outOfScreen()) {
                shots.remove(i);
                break;
            }
            for (int j = enemies.size()-1; j >= 0; j--) {
                if(shots.get(i).overlap(enemies.get(j))){
                    if(isSoundOn) sndExplosion.play();
                    shots.remove(i);
                    if(--enemies.get(j).hp == 0) {
                        spawnFragments(enemies.get(j));
                        if(!gameOver) {
                            main.player.kills++;
                            main.player.score += enemies.get(j).price;
                        }
                        enemies.remove(j);
                    }
                    break;
                }
            }
        }
        for (int i = fragments.size()-1; i >= 0; i--) {
            fragments.get(i).move();
            if(fragments.get(i).outOfScreen()) {
                fragments.remove(i);
            }
        }

        // отрисовка
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for(Space s: space) batch.draw(imgBackGround, s.x, s.y, s.width, s.height);
        if(controls == JOYSTICK){
            batch.draw(imgJoystick, main.joystick.scrX(), main.joystick.scrY(), main.joystick.width, main.joystick.height);
        }
        for(Fragment f: fragments){
            batch.draw(imgFragment[f.type][f.num], f.scrX(), f.scrY(), f.width/2, f.height/2, f.width, f.height, 1, 1, f.rotation);
        }
        for(Enemy e: enemies){
            batch.draw(imgEnemy[e.type][e.phase], e.scrX(), e.scrY(), e.width, e.height);
        }
        for(Shot s: shots){
            batch.draw(imgShot[0], s.scrX(), s.scrY(), s.width, s.height);
        }
        batch.draw(imgShip[ship.phase], ship.scrX(), ship.scrY(), ship.width, ship.height);
        btnBack.font.draw(batch, btnBack.text, btnBack.x, btnBack.y);
        font50.draw(batch, "score:"+main.player.score, 10, 1590);
        if(gameOver){
            font70.draw(batch, "GAME OVER", 0, 1400, SCR_WIDTH, Align.center, true);
            btnSwitchGlobal.font.draw(batch, btnSwitchGlobal.text, btnSwitchGlobal.x, btnSwitchGlobal.y);
            font50.draw(batch, "score", 500, 1200, 200, Align.right, false);
            font50.draw(batch, "kills", 620, 1200, 200, Align.right, false);
            if(showGlobalRecords){
                for (int i = 0; i < Math.min(db.size(), players.length); i++) {
                    font50.draw(batch, i + 1 + "", 100, 1100 - i * 70);
                    font50.draw(batch, db.get(i).name, 200, 1100 - i * 70);
                    font50.draw(batch, db.get(i).score + "", 500, 1100 - i * 70, 200, Align.right, false);
                    font50.draw(batch, db.get(i).kills + "", 620, 1100 - i * 70, 200, Align.right, false);
                }
            } else {
                for (int i = 0; i < players.length; i++) {
                    font50.draw(batch, i + 1 + "", 100, 1100 - i * 70);
                    font50.draw(batch, players[i].name, 200, 1100 - i * 70);
                    font50.draw(batch, players[i].score + "", 500, 1100 - i * 70, 200, Align.right, false);
                    font50.draw(batch, players[i].kills + "", 620, 1100 - i * 70, 200, Align.right, false);
                }
            }
            btnRestart.font.draw(batch, btnRestart.text, btnRestart.x, btnRestart.y);
        }
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
        imgJoystick.dispose();
        imgShotsAtlas.dispose();
    }

    private void spawnEnemy(){
        if(TimeUtils.millis()>timeLastSpawnEnemy+timeSpawnEnemyInterval){
            enemies.add(new Enemy());
            timeLastSpawnEnemy = TimeUtils.millis();
        }
    }

    private void spawnShots(){
        if(TimeUtils.millis()>timeLastShoot+timeShootInterval){
            shots.add(new Shot(ship.x-60, ship.y));
            shots.add(new Shot(ship.x+60, ship.y));
            timeLastShoot = TimeUtils.millis();
            if(isSoundOn) sndBlaster.play();
        }
    }

    private void spawnFragments(SpaceObject o){
        for (int i = 0; i < nFragments; i++) {
            fragments.add(new Fragment(o.x, o.y, o.type, imgFragment[0].length));
        }
    }

    private void gameStart(){
        gameOver = false;
        ship = new Ship(SCR_WIDTH/2, 200);
        enemies.clear();
        fragments.clear();
        shots.clear();
        main.player.score = 0;
        main.player.kills = 0;
    }

    private void gameOver(){
        if(isSoundOn) sndExplosion.play();
        spawnFragments(ship);
        ship.x = -10000;
        gameOver = true;
        if(main.player.score > players[players.length-1].score) {
            players[players.length - 1].clone(main.player);
            sortTableOfRecords();
            saveTableOfRecords();
        }
        sendToInternetDB();
    }

    private void sortTableOfRecords(){
        for (int j = 0; j < players.length; j++) {
            for (int i = 0; i < players.length-1; i++) {
                if(players[i].score < players[i+1].score){
                    Player tmp = players[i];
                    players[i] = players[i+1];
                    players[i+1] = tmp;
                }
            }
        }
    }

    public void saveTableOfRecords(){
        Preferences prefs = Gdx.app.getPreferences("SpaceWarRecords");
        for (int i = 0; i < players.length; i++) {
            prefs.putString("name"+i, players[i].name);
            prefs.putInteger("score"+i, players[i].score);
            prefs.putInteger("kills"+i, players[i].kills);
        }
        prefs.flush();
    }

    private void loadTableOfRecords(){
        Preferences prefs = Gdx.app.getPreferences("SpaceWarRecords");
        for (int i = 0; i < players.length; i++) {
            players[i].name = prefs.getString("name"+i, "Noname");
            players[i].score = prefs.getInteger("score"+i, 0);
            players[i].kills = prefs.getInteger("kills"+i, 0);
        }
    }

    public void clearTableOfRecords(){
        for (Player player : players) player.clear();
    }

    private void sortRecordsInternetDB(){
        class Cmp implements Comparator<DataFromDB> {
            @Override
            public int compare(DataFromDB o1, DataFromDB o2) {
                return o2.score-o1.score;
            }
        }
        db.sort(new Cmp());
    }

    public void loadFromInternetDB(){
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://sch120.ru")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        SpaceAPI api = retrofit.create(SpaceAPI.class);
        Call<List<DataFromDB>> call = api.sendQuery("ask");
        try {
            Response<List<DataFromDB>> response = call.execute();
            db = response.body();
            sortRecordsInternetDB();
        } catch (Exception e){

        }
    }

    public void sendToInternetDB(){
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://sch120.ru")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        SpaceAPI api = retrofit.create(SpaceAPI.class);
        Call<List<DataFromDB>> call = api.sendQuery(main.player.name, main.player.score, main.player.kills);
        try {
            Response<List<DataFromDB>> response = call.execute();
            db = response.body();
            sortRecordsInternetDB();
        } catch (Exception e){

        }
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
            touch.set(screenX, screenY, 0);
            camera.unproject(touch);
            if(controls == SCREEN) {
                ship.touchScreen(touch);
            }
            if(controls == JOYSTICK) {
                if(main.joystick.isTouchInside(touch)){
                    ship.touchJoystick(touch, main.joystick);
                }
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
            touch.set(screenX, screenY, 0);
            camera.unproject(touch);
            if(controls == SCREEN) {
                ship.touchScreen(touch);
            }
            if(controls == JOYSTICK) {
                if(main.joystick.isTouchInside(touch)){
                    ship.touchJoystick(touch, main.joystick);
                }
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
