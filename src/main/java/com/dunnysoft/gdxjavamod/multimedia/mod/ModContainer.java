/*
 * @(#)ModContainer.java
 *
 * Created on 12.10.2007 by Daniel Becker
 * 
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */
package com.dunnysoft.gdxjavamod.multimedia.mod;

import java.net.URL;
import java.util.Properties;

import com.dunnysoft.gdxjavamod.mixer.Mixer;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainerManager;
import com.dunnysoft.gdxjavamod.multimedia.mod.loader.Module;
import com.dunnysoft.gdxjavamod.multimedia.mod.loader.ModuleFactory;
import com.dunnysoft.gdxjavamod.system.Log;

/**
 * @author: Daniel Becker
 * @since: 12.10.2007
 */
public class ModContainer extends MultimediaContainer
{
	public static final String PROPERTY_PLAYER_BITSPERSAMPLE = "javamod.player.bitspersample"; 
	public static final String PROPERTY_PLAYER_STEREO = "javamod.player.stereo"; 
	public static final String PROPERTY_PLAYER_FREQUENCY = "javamod.player.frequency"; 
	public static final String PROPERTY_PLAYER_MSBUFFERSIZE = "javamod.player.msbuffersize"; 
	public static final String PROPERTY_PLAYER_ISP = "javamod.player.ISP"; 
	public static final String PROPERTY_PLAYER_WIDESTEREOMIX = "javamod.player.widestereomix"; 
	public static final String PROPERTY_PLAYER_NOISEREDUCTION = "javamod.player.noisereduction"; 
	public static final String PROPERTY_PLAYER_MEGABASS = "javamod.player.megabass"; 
	public static final String PROPERTY_PLAYER_NOLOOPS = "javamod.player.noloops"; 
	public static final String PROPERTY_PLAYER_MAXNNACHANNELS = "javamod.player.max_nna_channels"; 
	public static final String PROPERTY_PLAYER_DITHERFILTER = "javamod.player.ditherfilter"; 
	public static final String PROPERTY_PLAYER_DITHERTYPE = "javamod.player.dithertype"; 
	public static final String PROPERTY_PLAYER_DITHERBYPASS = "javamod.player.ditherbypass"; 
	/* GUI Constants ---------------------------------------------------------*/
	public static final String DEFAULT_BITSPERSAMPLE = "16";
   	public static final String DEFAULT_CHANNEL = "2";
	public static final String DEFAULT_SAMPLERATE = "48000";
	public static final String DEFAULT_MSBUFFERSIZE = "250";
	public static final String DEFAULT_WIDESTEREOMIX = "false";
	public static final String DEFAULT_NOISEREDUCTION = "false";
	public static final String DEFAULT_MEGABASS = "false";
	public static final String DEFAULT_NOLOOPS = "0";
	public static final String DEFAULT_MAXNNACHANNELS  = "200";
	public static final String DEFAULT_INTERPOLATION_INDEX = "1";
	public static final String DEFAULT_DITHERFILTER = "4";
	public static final String DEFAULT_DITHERTYPE = "2";
	public static final String DEFAULT_DITHERBYPASS = "false";
	public static final String[] SAMPLERATE = new String[]
	{
		"8000", "11025", "16000", "22050", "33075", "44100", DEFAULT_SAMPLERATE, "96000", "192000"
	};
	public static final String[] CHANNELS = new String[]
   	{
   		"1", DEFAULT_CHANNEL
   	};
	public static final String[] BITSPERSAMPLE = new String[]
	{
		"8", DEFAULT_BITSPERSAMPLE, "24", "32"
	};
	public static final String[] INTERPOLATION = new String[]
  	{
  		"none", "linear", "cubic spline", "windowed FIR"
  	};
	public static final String[] BUFFERSIZE = new String[]
  	{
  		"30", "50", "75", "100", "125", "150", "175", "200", "225", "250", "500", "750"
  	};
	public static final String[] MAX_NNA_CHANNELS= new String[]
  	{
  		"25", "50", "75", "100", "125", "150", "175", "200", "225", "250", "275", "300", "325", "350", "375", "400", "1000"
  	};

	private Module currentMod;
	private ModMixer currentMixer;

	/**
	 * Will be executed during class load
	 */
	static
	{
		MultimediaContainerManager.registerContainer(new ModContainer());
	}
	
	/**
	 * @since: 12.10.2007
	 */
	public ModContainer()
	{
		super();
	}
	/**
	 * @param url
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(URL url)
	{
		MultimediaContainer result = super.getInstance(url); 
		try
		{
			currentMod = ModuleFactory.getInstance(url);
			if (currentMod==null)
			{
				Log.error("[ModContainer] Failed with loading of " + url.toString(), new Exception("Modfile "+url.toString()+" is obviously corrupt!"));
				result = null;
			}
			else
			{
			}
		}
		catch (Exception ex)
		{
			currentMod = null;
			throw new RuntimeException(ex);
		}
		return result;
	}
	/**
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getSongName()
	 */
	@Override
	public String getSongName()
	{
		if (currentMod!=null)
		{
			String songName = currentMod.getSongName();
			if (songName!=null && songName.trim().length()!=0)
				return songName;
		}
		return super.getSongName();
	}
	/**
	 * @param url
	 * @return gentleman agreement: Object[] { String::songname, Long::duration }
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getSongInfosFor(java.net.URL)
	 */
	@Override
	public Object[] getSongInfosFor(URL url)
	{
		String songName = MultimediaContainerManager.getSongNameFromURL(url);
		Long duration = Long.valueOf(-1);
		try
		{
			final Module theMod = ModuleFactory.getInstance(url);
			final String modSongName = theMod.getSongName();
			if (modSongName!=null && modSongName.trim().length()!=0) songName = modSongName;
			ModMixer theMixer = new ModMixer(theMod, 8, 1, 22050, 0, false, false, false, 1, 0, 500, 0, 0, true);
			duration = Long.valueOf(theMixer.getLengthInMilliseconds());
		}
		catch (Throwable ex)
		{
			/* NOOP */
		}
		return new Object[] { songName, duration };
	}
	/**
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#canExport()
	 */
	@Override
	public boolean canExport()
	{
		return true;
	}
	/**
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getFileExtensionList()
	 * @since: 12.10.2007
	 * @return
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return ModuleFactory.getSupportedFileExtensions();
	}
	/**
	 * @return the name of the group of files this container knows
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "Mod-File";
	}

	@Override
	public void configurationChanged(Properties newProps) {

	}

	@Override
	public void configurationSave(Properties props) {

	}

	/**
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#createNewMixer()
	 * @since: 12.10.2007
	 * @return
	 */
	@Override
	public Mixer createNewMixer()
	{
		if (currentMod==null) return null;

		Properties props = new Properties();
		configurationSave(props);
		
		final int frequency = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_FREQUENCY, DEFAULT_SAMPLERATE)); 
		final int bitsPerSample = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_BITSPERSAMPLE, DEFAULT_BITSPERSAMPLE)); 
		final int channels = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_STEREO, DEFAULT_CHANNEL));
		final int isp = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_ISP, DEFAULT_INTERPOLATION_INDEX)); 
		final boolean wideStereoMix = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_WIDESTEREOMIX, DEFAULT_WIDESTEREOMIX)); 
		final boolean noiseReduction = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_NOISEREDUCTION, DEFAULT_NOISEREDUCTION));
		final boolean megaBass = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_MEGABASS, DEFAULT_MEGABASS));
		final int loopValue = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_NOLOOPS, DEFAULT_NOLOOPS));
		final int maxNNAChannels = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_MAXNNACHANNELS, DEFAULT_MAXNNACHANNELS));
		final int msBufferSize = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_MSBUFFERSIZE, DEFAULT_MSBUFFERSIZE));
		final int ditherFilter = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_DITHERFILTER, DEFAULT_DITHERFILTER));
		final int ditherType = Integer.parseInt(props.getProperty(PROPERTY_PLAYER_DITHERTYPE, DEFAULT_DITHERTYPE));
		boolean ditherByPass = Boolean.parseBoolean(props.getProperty(PROPERTY_PLAYER_DITHERBYPASS, DEFAULT_DITHERBYPASS));
		currentMixer = new ModMixer(currentMod, bitsPerSample, channels, frequency, isp, wideStereoMix, noiseReduction, megaBass, loopValue, maxNNAChannels, msBufferSize, ditherFilter, ditherType, ditherByPass);
		return currentMixer;
	}
	/**
	 * @since 14.10.2007
	 * @return
	 */
	public ModMixer getCurrentMixer()
	{
		return currentMixer;
	}
	public Module getCurrentMod()
	{
		return currentMod;
	}
}
