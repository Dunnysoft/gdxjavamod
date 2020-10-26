/*
 * @(#) MP3Container.java
 *
 * Created on 17.10.2007 by Daniel Becker
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
package com.dunnysoft.gdxjavamod.multimedia.mp3;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import com.dunnysoft.gdxjavamod.io.RandomAccessInputStreamImpl;
import com.dunnysoft.gdxjavamod.mixer.Mixer;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainerEvent;
import com.dunnysoft.gdxjavamod.multimedia.MultimediaContainerManager;
import com.dunnysoft.gdxjavamod.multimedia.mp3.id3.MP3FileID3Controller;
import com.dunnysoft.gdxjavamod.multimedia.mp3.streaming.TagParseEvent;
import com.dunnysoft.gdxjavamod.multimedia.mp3.streaming.TagParseListener;
import com.dunnysoft.gdxjavamod.system.Log;
import com.dunnysoft.mp3.decoder.Bitstream;
import com.dunnysoft.mp3.decoder.BitstreamException;
import com.dunnysoft.mp3.decoder.Header;

/**
 * @author Daniel Becker
 * @since 17.10.2007
 */
public class MP3Container extends MultimediaContainer implements TagParseListener
{
	private static final String[] MP3FILEEXTENSION = new String [] 
  	{
  		"mp1", "mp2", "mp3"
  	};

	private MP3Mixer currentMixer;
	private MP3FileID3Controller mp3FileIDTags = null;
	
	private boolean isStreaming;
	
	/**
	 * Will be executed during class load
	 */
	static
	{
		MultimediaContainerManager.registerContainer(new MP3Container());
	}
	/**
	 * Constructor for MP3Container
	 */
	public MP3Container()
	{
		super();
	}
	/**
	 * @param mp3FileUrl
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getInstance(java.net.URL)
	 */
	@Override
	public MultimediaContainer getInstance(URL mp3FileUrl)
	{
		MultimediaContainer result = super.getInstance(mp3FileUrl);
		isStreaming = !mp3FileUrl.getProtocol().equalsIgnoreCase("file"); 
		if (!isStreaming)
		{
			Header h = getHeaderFrom(mp3FileUrl);
			mp3FileIDTags = new MP3FileID3Controller(mp3FileUrl);
		}
		else
		{
			mp3FileIDTags = null;
		}
		return result;
	}
	@Override
	public String getSongName()
	{
		if (mp3FileIDTags!=null)
			return mp3FileIDTags.getShortDescription();
		else
			return super.getSongName();
	}
	private Header getHeaderFrom(URL url)
	{
		Header result = null;
		RandomAccessInputStreamImpl inputStream = null;
		Bitstream bitStream = null;
		try
		{
			if (url.getProtocol().equalsIgnoreCase("file"))
			{
				inputStream = new RandomAccessInputStreamImpl(url);
				bitStream = new Bitstream(inputStream);
				result = bitStream.readFrame();
			}
		}
		catch (Throwable ex)
		{
		}
		finally
		{
			if (bitStream != null) try { bitStream.close();  } catch (BitstreamException ex) { Log.error("IGNORED", ex); }
			if (inputStream != null) try { inputStream.close(); } catch (IOException ex) { Log.error("IGNORED", ex); }
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
		RandomAccessInputStreamImpl inputStream = null;
		Bitstream bitStream = null;
		try
		{
			if (url.getProtocol().equalsIgnoreCase("file"))
			{
				inputStream = new RandomAccessInputStreamImpl(url);
				bitStream = new Bitstream(inputStream);
				Header h = bitStream.readFrame();
				if (h!=null) duration = Long.valueOf((long)(h.total_ms(inputStream.available()) + 0.5));
				mp3FileIDTags = new MP3FileID3Controller(inputStream);
				if (mp3FileIDTags!=null) songName = mp3FileIDTags.getShortDescription();
			}
		}
		catch (Throwable ex)
		{
		}
		finally
		{
			if (bitStream != null) try { bitStream.close();  } catch (BitstreamException ex) { Log.error("IGNORED", ex); }
			if (inputStream != null) try { inputStream.close(); } catch (IOException ex) { Log.error("IGNORED", ex); }
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
	 * @return
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getFileExtensionList()
	 */
	@Override
	public String[] getFileExtensionList()
	{
		return MP3FILEEXTENSION;
	}
	/**
	 * @return the name of the group of files this container knows
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#getName()
	 */
	@Override
	public String getName()
	{
		return "MP3-File";
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
	 * @see com.dunnysoft.gdxjavamod.multimedia.MultimediaContainer#createNewMixer()
	 */
	@Override
	public Mixer createNewMixer()
	{
		currentMixer = new MP3Mixer(getFileURL());
		currentMixer.setTagParserListener(this);
		return currentMixer;
	}

	@Override
	public void tagParsed(TagParseEvent tpe) {
		
	}
}
