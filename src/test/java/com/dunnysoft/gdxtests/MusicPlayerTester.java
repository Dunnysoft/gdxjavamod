package com.dunnysoft.gdxtests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dunnysoft.gdxjavamod.MusicPlayer;
import com.dunnysoft.gdxjavamod.system.Helpers;


public class MusicPlayerTester extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 512;
    public static final int SCREEN_HEIGHT = 512;
    private FileHandle fh;
    private MusicPlayer mp;
    private boolean initialPlay;

    private Viewport vp;
    private SpriteBatch batch;
    BitmapFont font;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("gdxjavamod Test App");
        config.setWindowedMode(SCREEN_WIDTH,SCREEN_HEIGHT);

        final MusicPlayerTester app = new MusicPlayerTester();

        new Lwjgl3Application(app, config);
    }

    @Override
    public void create() {
        super.create();
        initialPlay = false;
        mp = new MusicPlayer();
        fh = Gdx.files.internal("firestorm.it");
        mp.createMixer(fh);

        font = new BitmapFont();
        vp = new ScreenViewport();
        vp.getCamera().position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        vp.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        vp.update(width,height);
        vp.getCamera().position.set(width / 2, height / 2, 0);
        vp.getCamera().update();
    }

    @Override
    public void render() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        super.render();

        batch.begin();
        font.draw(batch,"GdxJavaMod Demo Program.  Uses LibGDX FileHandle and Input Processor.",10,40);
        font.draw(batch, "Press 'P' to play/pause song.  S to stop.  X to exit", 10, 20);
        batch.end();
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (initialPlay) {
                mp.pause();
            } else {
                mp.play();
                initialPlay = true;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            mp.stop();
            initialPlay = false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            mp.stop();
            Gdx.app.exit();
        }
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
