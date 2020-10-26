/*
 * @(#) FLACContainer.java
 *
 * Created on 01.01.2011 by Daniel Becker
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
package com.dunnysoft.gdxjavamod.multimedia.flac;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;

import com.dunnysoft.gdxjavamod.io.FileOrPackedInputStream;
import com.dunnysoft.gdxjavamod.mixer.Mixer;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainerManager;
import com.dunnysoft.gdxjavamod.system.Helpers;
import com.dunnysoft.gdxjavamod.system.Log;
import com.dunnysoft.jflac.FLACDecoder;
import com.dunnysoft.jflac.metadata.VorbisComment;

/**
 * @author Daniel Becker
 * @since 01.01.2011
 */
public class FLACContainer extends MultimediaContainer
{
	private static final String[] FLACFILEEXTENSION = new String [] 
 	{
 		"flac"
 	};

 	private VorbisComment vorbisComment;
 	private long duration;
 	
	/**
	 * Will be executed during class load
	 */
	static
	{
		MultimediaContainerManager.registerContainer(new FLACContainer());
	}
	/**
	 * Constructor for FLACContainer
	 */
	public FLACContainer()
	{
		super();
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
	 * @param url
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(URL url)
	{
		MultimediaContainer result = super.getInstance(url);
		InputStream inputStream = null;
		try
		{
			inputStream = new FileOrPackedInputStream(url);
			final FLACDecoder decoder = new FLACDecoder(inputStream);
			decoder.readMetadata();
			vorbisComment = decoder.getVorbisComment();
			final AudioFormat audioFormat = decoder.getStreamInfo().getAudioFormat();
			final long sampleRate = (long)audioFormat.getSampleRate();
			duration = (long)decoder.getStreamInfo().getTotalSamples() * 1000L / sampleRate;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
		finally
		{
			if (inputStream!=null) try { inputStream.close(); } catch (IOException ex) { Log.error("IGNORED", ex); }
		}
		return result;
	}
	private String getSongName(VorbisComment vorbisComment, URL forURL)
	{
		if (vorbisComment!=null)
		{
			try
			{
				String artist = vorbisComment.getArtist();
				String album = vorbisComment.getAlbum();
				String title = vorbisComment.getTitle();
				if (title==null || title.length()==0) title = MultimediaContainerManager.getSongNameFromURL(forURL);

				StringBuilder str = new StringBuilder();
				if (artist!=null && artist.length()!=0)
				{
					str.append(artist).append(" - ");
				}
				if (album!=null && album.length()!=0)
				{
					str.append(album).append(" - ");
				}
				return str.append(title).toString();
			}
			catch (Throwable ex) // we can get the runtime exception "Unsupported Function"
			{
			}
		}
		return MultimediaContainerManager.getSongNameFromURL(forURL);
	}
	/**
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getSongName()
	 */
	@Override
	public String getSongName()
	{
		if (vorbisComment!=null)
			return getSongName(vorbisComment, getFileURL());
		else
			return super.getSongName();
	}
	/**
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		return new FLACMixer(getFileURL());
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
		InputStream inputStream = null;
		try
		{
			inputStream = new FileOrPackedInputStream(url);
			FLACDecoder decoder = new FLACDecoder(inputStream);
			decoder.readMetadata();
			VorbisComment vorbisComment = decoder.getVorbisComment();
			songName = getSongName(vorbisComment, url);
			AudioFormat audioFormat = decoder.getStreamInfo().getAudioFormat();
			long sampleRate = (long)audioFormat.getSampleRate();
			duration = Long.valueOf((long)decoder.getStreamInfo().getTotalSamples() * 1000L / sampleRate);
		}
		catch (Throwable ex)
		{
		}
		finally
		{
			if (inputStream!=null) try { inputStream.close(); } catch (IOException ex) { Log.error("IGNORED", ex); }
		}
		return new Object[] { songName, duration };
	}
	/**
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "FLAC-File";
	}
	/**
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return FLACFILEEXTENSION;
	}
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
}
