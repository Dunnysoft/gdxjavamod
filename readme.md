gdxjavamod - A port of JavaMod 2.9 by Daniel Becker (https://www.quippy.de/mod_en.php)

Specifically designed for use with LibGDX, with more generic usage also available and to be expanded.

**Usage:**

*Gradle*

Check [jitpack](https://jitpack.io/#Dunnysoft/gdxjavamod)
Add the following line to your *build.properties* in the *root* project folder

```
gdxJavaModVer=VERSION
```
Change 'VERSION' to the latest jitpack version

Add the following to your *build.gradle* in the *core* project folder:


```
api "com.github.Dunnysoft:gdxjavamod:$gdxJavaModVer"
```

**How to Use** (to be further refined)

```
import com.dunnysoft.gdxjavamod.MusicPlayer;

...

private MusicPlayer musicPlayer = new MusicPlayer(); // Initialises everything
private FileHandle fh = Gdx.files.internal("songname.mod");

...

musicPlayer.createMixer(fh);    // Creates a new mixer with the given song
musicPlayer.play();     // starts playback
musicPlayer.stop();     // stops playback
musicPlayer.pause():    // pauses and resumes playback
```

Please note that at this time to play a new song you must create a new mixer (including replaying a previous song).

**To-do:**
* Create playlists and automatically play through them
* Enable use of Graphic EQ
* provide more control over playback.