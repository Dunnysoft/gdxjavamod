package com.dunnysoft.gdxjavamod;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.dunnysoft.gdxjavamod.io.GaplessSoundOutputStreamImpl;
import com.dunnysoft.gdxjavamod.io.SoundOutputStream;
import com.dunnysoft.gdxjavamod.io.SoundOutputStreamImpl;
import com.dunnysoft.gdxjavamod.mixer.Mixer;
import com.dunnysoft.gdxjavamod.mixer.dsp.AudioProcessor;
import com.dunnysoft.gdxjavamod.mixer.dsp.iir.GraphicEQ;
import com.dunnysoft.gdxjavamod.mixer.dsp.pitchshift.PitchShift;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainerEvent;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainerEventListener;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainerManager;
import com.dunnysoft.gdxjavamod.multimedia.mod.ModContainer;
import com.dunnysoft.gdxjavamod.system.Helpers;
import com.dunnysoft.gdxjavamod.system.Log;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class MusicPlayer implements PlayThreadEventListener, MultimediaContainerEventListener {
    private Mixer mixer;
    private PlayThread playerThread;
    private FileHandle fileHandle;
    private Properties properties;
    private MultimediaContainer currentContainer;
    private GraphicEQ currentEqualizer;
    private PitchShift currentPitchShift;
    private AudioProcessor audioProcessor;
    private transient SoundOutputStream soundOutputStream;
    private float currentVolume; /* 0.0 - 1.0 */
    private float currentBalance; /* -1.0 - 1.0 */
    private boolean useGaplessAudio;
    private boolean paused;

    /**
     *  Make sure you run the constructor to initialise the Helpers class, which does things.
     */
    public MusicPlayer() {
        try {
            Helpers.registerAllClasses();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void createMixer(URL url) {
        properties = new Properties();

        properties.setProperty(ModContainer.PROPERTY_PLAYER_ISP, "3");
        properties.setProperty(ModContainer.PROPERTY_PLAYER_STEREO, "2");
        properties.setProperty(ModContainer.PROPERTY_PLAYER_WIDESTEREOMIX, "FALSE");
        properties.setProperty(ModContainer.PROPERTY_PLAYER_NOISEREDUCTION, "FALSE");
        properties.setProperty(ModContainer.PROPERTY_PLAYER_NOLOOPS, "1");

        properties.setProperty(ModContainer.PROPERTY_PLAYER_MEGABASS, "TRUE");
        properties.setProperty(ModContainer.PROPERTY_PLAYER_BITSPERSAMPLE, "16");
        properties.setProperty(ModContainer.PROPERTY_PLAYER_FREQUENCY, "48000");
        properties.setProperty(ModContainer.PROPERTY_PLAYER_MSBUFFERSIZE, "30");

        MultimediaContainerManager.configureContainer(properties);
        try {
            MultimediaContainer newContainer = MultimediaContainerManager.getMultimediaContainer(url);
            currentContainer = newContainer;
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

    }

    public void createMixer(FileHandle fh) {
        createMixer(convertUrl(fh));
    }


    /**
     * Change the Balance.  -1.0f to 1.0f
     * @param bal
     */
    public void setBalance(float bal) {
        if (playerThread != null) {
            mixer = playerThread.getCurrentMixer();
            mixer.setBalance(bal);
        }
    }

    /**
     * Change the volume. 0 to 1.0f
     * @param vol
     */
    public void setVolume(float vol) {
        if (playerThread != null) {
            mixer = playerThread.getCurrentMixer();
            mixer.setVolume(vol);
        }
    }


    /**
     * start playback of a audio file
     * @since 01.07.2006
     */
    public void play()
    {
        if (playerThread == null) {
            doStartPlaying(false, 0);
        }
    }

    /**
     * Stop playback
     */
    public void stop() {
        doStopPlaying();
    }

    /**
     * Pause and Resume playback.
     */
    public void pause() {
        doPausePlaying();
    }

    /**
     * @param initialSeek
     * @since 13.02.2012
     */
    public void doStartPlaying(final boolean reuseMixer, final long initialSeek)
    {
        try
        {
            if (currentContainer!=null)
            {
                if (playerThread!=null && !reuseMixer)
                {
                    playerThread.stopMod();
                    playerThread = null;
                }

                if (playerThread == null)
                {
                    Mixer mixer = createNewMixer();
                    if (mixer!=null)
                    {
                        if (initialSeek>0) mixer.setMillisecondPosition(initialSeek);
                        playerThread = new PlayThread(mixer, this);
                        playerThread.start();
                    }
                }
                else
                {
                    playerThread.getCurrentMixer().setMillisecondPosition(initialSeek);
                }
            }
        }
        catch (Throwable ex)
        {
            if (playerThread!=null)
            {
                playerThread.stopMod();
                playerThread = null;
            }
            Log.error("Starting playback did not succeed!", ex);
        }
    }

    /**
     * stop playback of a mod
     * @since 01.07.2006
     */
    private void doStopPlaying()
    {
        if (playerThread!=null)
        {
            playerThread.stopMod();
            getSoundOutputStream().closeAllDevices();
            playerThread = null;
        }
    }
    /**
     * pause the playing of a mod
     * @since 01.07.2006
     */
    private void doPausePlaying()
    {
        if (playerThread!=null)
        {
            playerThread.pausePlay();
        }
    }

    /**
     * @since 14.09.2008
     * @return
     */
    private MultimediaContainer getCurrentContainer()
    {
        if (currentContainer == null)
        {
            try
            {
                currentContainer = MultimediaContainerManager.getMultimediaContainerForType("mod");
            }
            catch (Exception ex)
            {
                Log.error("getCurrentContainer()", ex);
            }
        }
        return currentContainer;
    }

    /**
     * Creates a new Mixer for playback
     * @since 01.07.2006
     * @return
     */
    private Mixer createNewMixer()
    {
        Mixer mixer = getCurrentContainer().createNewMixer();
        if (mixer!=null)
        {
            mixer.setAudioProcessor(audioProcessor);
            mixer.setVolume(currentVolume);
            mixer.setBalance(currentBalance);
            mixer.setSoundOutputStream(getSoundOutputStream());
        }
        return mixer;
    }

    /**
     * @return the useGaplessAudio
     */
    public boolean useGaplessAudio()
    {
        return useGaplessAudio;
    }
    /**
     * @param useGaplessAudio the useGaplessAudio to set
     */
    public void setUseGaplessAudio(boolean useGaplessAudio)
    {
        this.useGaplessAudio = useGaplessAudio;
    }
    private SoundOutputStream getSoundOutputStream()
    {
        if (soundOutputStream==null)
        {
            if (useGaplessAudio())
                soundOutputStream = new GaplessSoundOutputStreamImpl();
            else
                soundOutputStream = new SoundOutputStreamImpl();
        }
        return soundOutputStream;
    }

    /**
     * Converts a FileHandle to a URL
     * @param fh
     * @return
     */

    public URL convertUrl(FileHandle fh) {
        File f = fh.file();
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public float getCurrentVolume() {
        return currentVolume;
    }

    public float getCurrentBalance() {
        return currentBalance;
    }

    @Override
    public void playThreadEventOccurred(PlayThread thread) {
        if (thread.isRunning())
        {
        }
        else // Signaling: not running-->Piece finished...
        {
            if (thread.getHasFinishedNormally())
            {
                // boolean ok = doNextPlayListEntry();  // do this later
                boolean ok = false;                     // We'll stop the thread here
                if (!ok) doStopPlaying();
            }
        }

        mixer = thread.getCurrentMixer();
        if (mixer!=null)
        {
            paused = mixer.isPaused();
        }

    }

    @Override
    public void multimediaContainerEventOccured(MultimediaContainerEvent event) {

    }

    public void dispose() {
        MultimediaContainerManager.removeMultimediaContainerEventListener(this);
    }


}
