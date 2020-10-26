/*
 * @(#) WavContainer.java
 *
 * Created on 14.10.2007 by Daniel Becker
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
package com.dunnysoft.gdxjavamod.multimedia.wav;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.dunnysoft.gdxjavamod.io.FileOrPackedInputStream;
import com.dunnysoft.gdxjavamod.mixer.Mixer;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainerManager;
import com.dunnysoft.gdxjavamod.system.Log;

/**
 * @author Daniel Becker
 * @since 14.10.2007
 */
public class WavContainer extends MultimediaContainer
{
	private static final String[] wavefile_Extensions;

	private WavMixer currentMixer;
	
	/**
	 * Will be executed during class load
	 */
	static
	{
		AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
		wavefile_Extensions = new String[types.length];
		for (int i=0; i<types.length; i++)
			wavefile_Extensions[i] = types[i].getExtension();
		MultimediaContainerManager.registerContainer(new WavContainer());
	}
	/**
	 * Constructor for WavContainer
	 */
	public WavContainer()
	{
		super();
	}
	/**
	 * @param waveFileUrl
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(URL waveFileUrl)
	{
		MultimediaContainer result = super.getInstance(waveFileUrl);
		AudioInputStream audioInputStream = null;
		try
		{
			audioInputStream = AudioSystem.getAudioInputStream(new FileOrPackedInputStream(waveFileUrl));
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			if (audioInputStream!=null) try { audioInputStream.close(); } catch (IOException ex) { Log.error("IGNORED", ex); }
		}
		return result;
	}
	/**
	 * @param url
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getSongInfosFor(java.net.URL)
	 */
	@Override
	public Object[] getSongInfosFor(URL url)
	{
		String songName = MultimediaContainerManager.getSongNameFromURL(url);
		Long duration = Long.valueOf(-1);
		try
		{
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new FileOrPackedInputStream(url));
			AudioFormat audioFormat = audioInputStream.getFormat();
			float frameRate = audioFormat.getFrameRate();
			if (frameRate != AudioSystem.NOT_SPECIFIED)
			{
				duration = Long.valueOf((long)(((float)audioInputStream.getFrameLength() * 1000f / frameRate)+0.5));
			}
			else
			{
				int channels = audioFormat.getChannels();
				int sampleSizeInBits = audioFormat.getSampleSizeInBits();
				int sampleSizeInBytes = sampleSizeInBits>>3;
				int sampleRate = (int)audioFormat.getSampleRate();
				duration = Long.valueOf(((long)audioInputStream.available() / ((long)sampleSizeInBytes) / (long)channels) * 1000L / (long)sampleRate);
			}
		}
		catch (Throwable ex)
		{
		}
		return new Object[] { songName, duration };
	}

	@Override
	public boolean canExport() {
		return false;
	}
	/**
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#canExport()
	 */
	/**
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return wavefile_Extensions;
	}
	/**
	 * @return the name of the group of files this container knows
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "Wave-File";
	}
	/**
	 * @param newProps
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#configurationChanged(java.util.Properties)
	 */
	@Override
	public void configurationChanged(Properties newProps)
	{
	}
	/**
	 * @param props
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#configurationSave(java.util.Properties)
	 */
	@Override
	public void configurationSave(Properties props)
	{
	}
	/**
	 * @return
	 * @throws UnsupportedAudioFileException
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		currentMixer = new WavMixer(getFileURL());
		return currentMixer;
	}
}
