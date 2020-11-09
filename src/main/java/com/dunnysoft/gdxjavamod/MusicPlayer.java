package com.dunnysoft.gdxjavamod;

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
import com.dunnysoft.gdxjavamod.multimedia.mod.loader.ModuleFactory;
import com.dunnysoft.gdxjavamod.multimedia.mod.loader.Module;
import com.dunnysoft.gdxjavamod.multimedia.mod.loader.pattern.Pattern;
import com.dunnysoft.gdxjavamod.playlist.PlayList;
import com.dunnysoft.gdxjavamod.playlist.PlayListEntry;
import com.dunnysoft.gdxjavamod.system.Helpers;
import com.dunnysoft.gdxjavamod.system.Log;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
    private PlayList playlist;
    private transient SoundOutputStream soundOutputStream;
    private float currentVolume; /* 0.0 - 1.0 */
    private float currentBalance; /* -1.0 - 1.0 */
    private boolean useGaplessAudio;
    private boolean paused;
    private URL currUrl;
    private ArrayList<URL> lastLoaded;
    private Module currentMod;

    // Mod Details;
    private String modFileName;
    private String modSongName;
    private String modTrackerName;
    private int modSongLength;
    private int modArrangement[];
    private String modInstruments;
    private Pattern patterns[];
    private StringBuilder songInfos;

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
        this.currUrl = url;
        Thread t = new Thread(() -> {
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
        });
        t.start();
        setModDetails();
        currentBalance = 0;
        currentVolume = 1;
        setVolume(currentVolume);
        setBalance(currentBalance);
    }

    /**
     * Load a LibGDX FileHandle into the music player.
     * @param fh
     */
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
     * Decrement the volume by 0.1
     */
    public void decVolume() {
        if (currentVolume > 0) {
            currentVolume -= 0.1f;
        }
        if (currentVolume < 0) {
            currentVolume = 0;
        }
        if (playerThread != null) {
            mixer.setVolume(currentVolume);
        }
    }

    /**
     * Decrement the volume by the given amount
     * @param decAmount
     */
    public void decVolume(float decAmount) {
        if (currentVolume > 0) {
            currentVolume -= decAmount;
        }
        if (currentVolume < 0) {
            currentVolume = 0;
        }
        if (playerThread != null) {
            mixer.setVolume(currentVolume);
        }
    }

    /**
     * Increment the volume by 0.1
     */
    public void incVolume() {
        if (currentVolume < 1) {
            currentVolume += 0.1f;
        }
        if (currentVolume > 1) {
            currentVolume = 1;
        }
        if (playerThread != null) {
            mixer.setVolume(currentVolume);
        }
    }

    /**
     * Increment the volume by the given amount
     * @param incAmount
     */
    public void incVolume(float incAmount) {
        if (currentVolume < 1) {
            currentVolume += incAmount;
        }
        if (currentVolume > 1) {
            currentVolume = 1;
        }
        if (playerThread != null) {
            mixer.setVolume(currentVolume);
        }
    }

    /**
     * Reset the balance to centre
     */
    public void resetBalance() {
        currentBalance = 0;
        mixer.setBalance(0);
    }

    /**
     * Increment the balance by 0.1
     */
    public void decBalance() {
        if (currentBalance > -1) {
            currentBalance -= 0.1f;
        }
        if (currentBalance < -1) {
            currentBalance = -1;
        }
        if (playerThread != null) {
            mixer.setBalance(currentBalance);
        }
    }

    /**
     * Increment the balance by the given amount
     * @param decAmount
     */
    public void decBalance(float decAmount) {
        if (currentBalance > -1) {
            currentBalance -= decAmount;
        }
        if (currentBalance < -1) {
            currentBalance = -1;
        }
        if (playerThread != null) {
            mixer.setBalance(currentBalance);
        }
    }

    /**
     * Increment the balance by 0.1
     */
    public void incBalance() {
        if (currentBalance < 1) {
            currentBalance += 0.1f;
        }
        if (currentBalance > 1) {
            currentBalance = 1;
        }
        if (playerThread != null) {
            mixer.setBalance(currentBalance);
        }
    }

    /**
     * Increment the balance by the given amount
     * @param incAmount
     */
    public void incBalance(float incAmount) {
        if (currentBalance < 1) {
            currentBalance += incAmount;
        }
        if (currentBalance > 1) {
            currentBalance = 1;
        }
        if (playerThread != null) {
            mixer.setBalance(currentBalance);
        }
    }

    public float getVolume() {
        return currentVolume;
    }

    public float getBalance() {
        return currentBalance;
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

    /**
     * Return the length of the song in milliseconds
     * @return
     */
    public long getSongLength() {
        return mixer.getLengthInMilliseconds();
    }

    /**
     * Playback from the start of the given pattern index (not implemented)
     * @param patternIndex
     */
    public void seekPattern(int patternIndex) {
    }

    public void setModDetails() {
        if (playlist != null) {
            try {
                currUrl = playlist.getCurrentEntry().getFile().toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        try {
            currentMod = ModuleFactory.getInstance(currUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        modFileName = currentMod.getFileName();
        modSongName = currentMod.getSongName();
        modTrackerName = currentMod.toShortInfoString();
        modSongLength = currentMod.getSongLength();
        modArrangement = currentMod.getArrangement();
        patterns = currentMod.getPatternContainer().getPattern();
        songInfos = new StringBuilder();
        String songMessage = currentMod.getSongMessage();
        if (songMessage!=null && songMessage.length()>0) songInfos.append("Song Message:\n").append(songMessage).append("\n\n");
        modInstruments = currentMod.getInstrumentContainer().toString();
    }

    /**
     * Dumps the gathered details about the current mod to the standard output
     */
    public void printModDetails() {
        System.out.println("Filename: "+ modFileName);
        System.out.println("Song name: " + modSongName);
        System.out.println("Tracker name: " + modTrackerName);
        System.out.println("Song length: " + modSongLength);
        System.out.println("Number of Patterns: " + patterns.length);
        System.out.println("Information: \n" + songInfos);
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

    public void setPlaylist(PlayList newPlaylist) {
        if (newPlaylist != null) {
            if (newPlaylist != playlist) {
                boolean playlistEmpty = playlist == null;
                playlist = newPlaylist;
                if (playlistEmpty) doNextPlaylistEntry();
            }
        }
    }

    public boolean doNextPlaylistEntry() {
        boolean ok = false;
        while (playlist != null && playlist.hasNext() && !ok) {
            playlist.next();
            setModDetails();
            ok = loadMultimediaFile(playlist.getCurrentEntry());
        }
        return ok;
    }

    public boolean doPreviousPlaylistEntry() {
        boolean ok = false;
        while (playlist != null && playlist.hasPrevious() && !ok) {
            playlist.previous();
            setModDetails();
            ok = loadMultimediaFile(playlist.getCurrentEntry());
        }
        return ok;
    }

    public void playSelectedPlaylistEntry() {
        boolean ok = false;
        while (playlist != null && !ok) {
            final PlayListEntry entry = playlist.getCurrentEntry();
            ok = loadMultimediaFile(entry);
            if (!ok) playlist.next();
            else
                if (playerThread == null) doStartPlaying(true, entry.getTimeIndex());
        }
        setModDetails();
    }

    /**
     * @since 14.09.2008
     * @param url
     */
    private void addFileToLastLoaded(URL url)
    {
        if (lastLoaded.contains(url)) lastLoaded.remove(url);
        lastLoaded.add(0, url);
    }

    /**
     * @since 14.09.2008
     * @param mediaPLSFileURL
     */
    private boolean loadMultimediaOrPlayListFile(URL mediaPLSFileURL)
    {
        Log.info(Helpers.EMPTY_STING);
        addFileToLastLoaded(mediaPLSFileURL);
        playlist = null;
        try
        {
            playlist = PlayList.createFromFile(mediaPLSFileURL, false, false);
            if (playlist!=null)
            {
                return doNextPlaylistEntry();
            }
        }
        catch (Throwable ex)
        {
            Log.error("[MainForm::loadMultimediaOrPlayListFile]", ex);
            playlist = null;
        }
        return false;
    }


    /**
     * load a mod file and display it - modified by David Pardy for gdxjavamod 2020-11-09
     * @since 01.07.2006
     * @param playListEntry
     * @return boolean if loading succeeded
     */
    private boolean loadMultimediaFile(PlayListEntry playListEntry) {
        final URL mediaFileURL = playListEntry.getFile();
        final boolean reuseMixer = (currentContainer!=null &&
                Helpers.isEqualURL(currentContainer.getFileURL(), mediaFileURL) &&
                playerThread != null && playerThread.isRunning());

        if (!reuseMixer)
        {
            try
            {
                if (mediaFileURL!=null)
                {
                    MultimediaContainer newContainer = MultimediaContainerManager.getMultimediaContainer(mediaFileURL);
                    if (newContainer!=null)
                    {
                        currentContainer = newContainer;
                    }
                }
            }
            catch (Throwable ex)
            {
                Log.error("[MainForm::loadMultimediaFile] Loading of " + mediaFileURL + " failed!", ex);
                return false;
            }
        }
        // if we are currently playing, start the current piece:
        if (playerThread!=null) doStartPlaying(reuseMixer, playListEntry.getTimeIndex());
        return true;
    }

    public String getModFileName() {
        return modFileName;
    }

    public String getModSongName() {
        return modSongName;
    }

    public String getModTrackerName() {
        return modTrackerName;
    }

    public int getModSongLength() {
        return modSongLength;
    }

    public StringBuilder getSongInfos() {
        return songInfos;
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
                boolean ok = doNextPlaylistEntry();  // do this later
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
