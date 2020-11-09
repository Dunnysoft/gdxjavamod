package com.dunnysoft.gdxtests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dunnysoft.gdxjavamod.MusicPlayer;
import com.dunnysoft.gdxjavamod.playlist.PlayList;

public class MusicPlayerTester extends ApplicationAdapter {
    public static final int SCREEN_WIDTH = 512;
    public static final int SCREEN_HEIGHT = 600;
    private FileHandle fh, fh2;
    private MusicPlayer mp;
    private boolean initialPlay;
    private PlayList playlist;

    private Viewport vp;
    private SpriteBatch batch;
    private BitmapFont font;
    private String volString;
    private String balString;
    private int balPercent;
    private int volPercent;
    private boolean usingPlaylist;

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

        // This is how I'll create a new mixer to play a single file.
        // This is all you actually need to do, and can access the .play(), .pause()., etc. methods.
        mp = new MusicPlayer();
        fh = Gdx.files.internal("firestorm.it");
        mp.createMixer(fh);

        // Let's add a second file and create a playlist with it.  You can also load a playlist file, e.g., .M3U format
        // See the JavaDocs for the PlayList class to see possible file selection methods.
        // JavaMod converts file names to URLs for loading and playback.

        fh2 = Gdx.files.internal("aryx.s3m");
        String[] files = new String[] {fh.toString(), fh2.toString()};
        playlist = new PlayList(files, false, true);
        mp.setPlaylist(playlist);

        font = new BitmapFont();
        vp = new ScreenViewport();
        vp.getCamera().position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        vp.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        volString = new String();
        balString = new String();

    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        vp.update(width,height);
        vp.getCamera().position.set(width / 2, height / 2, 0);
        vp.getCamera().update();
    }

    private void doStrings() {
        balPercent = (int)(mp.getBalance() * 100);
        volPercent = (int)(mp.getVolume() * 100);
        volString = "Volume: "+ volPercent + "%    Adjust with - (decrement) and = (increment)";
        if (balPercent == 0)
            balString = "Balance: Centre";

        if (balPercent < 0)
            balString = "Balance: "+Math.abs(balPercent)+"% Left";

        if (balPercent > 0)
            balString = "Balance: "+Math.abs(balPercent)+"% Right";

        balString += "     Adjust with , and .  Re-centre with C";
    }

    @Override
    public void render() {
        super.render();
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        doStrings();
        batch.begin();
        font.draw(batch,"GdxJavaMod Demo Program.  Uses LibGDX FileHandle and Input Processor.",10,60);
        font.draw(batch, "Press 'P' to play/pause song.  S to stop.  X to exit", 10, 40);
        font.draw(batch, "Press 'N' to skip to the next song.", 10, 20);
        font.draw(batch, volString, 10, 100);
        font.draw(batch, balString, 10, 120);

        if (mp.getModSongName() != null) font.draw(batch, "Current song: "+mp.getModSongName(), 10, 580);

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            mp.doNextPlaylistEntry();
            initialPlay = false;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            mp.stop();
            Gdx.app.exit();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            mp.decVolume();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
            mp.incVolume();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.COMMA)) {
            mp.decBalance();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) {
            mp.incBalance();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            mp.resetBalance();
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
