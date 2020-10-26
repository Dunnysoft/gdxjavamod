/*
 * @(#) Helpers.java
 *
 * Created on 22.04.2006 by Daniel Becker
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
package com.dunnysoft.gdxjavamod.system;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * @author Daniel Becker
 * @since 22.04.2006
 */
public class Helpers
{
    /**
	 * Constructor for Helpers, not used!
	 */
	private Helpers()
	{
		super();
	}

	/** Version Information */
	public static final String VERSION = "V2.9";
	public static final String PROGRAM = "Java Mod Player";
	public static final String FULLVERSION = PROGRAM+' '+VERSION;
	public static final String COPYRIGHT = "© by Daniel Becker since 2006";
	
	public static final String USER_AGENT = "JavaMod/2.7";
	
	/** Codepages used when reading mod files */
	public static final String CODING_GUI = "cp850";
	public static final String CODING_COMMANLINE = "cp1252";
	/** Default value - changes whether SWING or commandline */
	public static String currentCoding = CODING_GUI;
	/** Codepages used when reading playlist files */
	public static final String CODING_M3U = "ISO-8859-1";
	/** Codepage used when reading from the web */
	public static final String CODING_HTTP = "UTF-8";
	/** Codepage used when reading from ICY Input-Streams*/
	public static final String CODING_ICY = "ISO-8859-15";


    // This is needed only once...
    public static final String EMPTY_STING = "";

	/** 
	 * HomeDir is the property of the system value "user.home".
	 * With Applets this value is unreadable (SecurityException)
	 */
	public static String HOMEDIR;
	static
	{
		try
		{
			HOMEDIR = System.getProperty("user.home");
		}
		catch (Throwable ex1)
		{
			try
			{
				HOMEDIR = System.getProperty("java.io.tmpdir");
			}
			catch (Throwable ex2)
			{
				Log.error("Could not set home dir", ex2);
				HOMEDIR = "";
			}
		}
	}
	

	/**
	 * It's ridiculous, but with the GUI we need cp850 but with
	 * the commandline we need cp1252. Unbelievable...
	 * @since 02.07.2006
	 * @param gui
	 */
	public static void setCoding(boolean gui)
	{
		currentCoding = (gui)?CODING_GUI:CODING_COMMANLINE;
	}

	/**
	 * VU-Meter constants
	 */
	public static final String SCROLLY_BLANKS = "     ";
	

	/* SERVICE METHODS -------------------------------------------------------*/
	/**
	 * Loads a C-Type String. In C/C++ strings end with a NULL-byte. We search for it
	 * and skip the rest. Furthermore we convert to UNICODE
	 * By the way: sometimes the C-Strings are not terminated. We need to do that
	 * ourself than!
	 * @param input
	 * @param start
	 * @param length
	 * @return
	 */
	public static String retrieveAsString(final byte [] input, final int start, final int length)
	{
		return retrieveAsString(input, start, length, currentCoding);
	}
	/**
	 * Loads a C-Type String. In C/C++ strings end with a NULL-byte. We search for it
	 * and skip the rest. Furthermore we convert to UNICODE
	 * By the way: sometimes the C-Strings are not terminated. We need to do that
	 * ourself than!
	 * @param input
	 * @param start
	 * @param len
	 * @param coding
	 * @return
	 */
	public static String retrieveAsString(final byte [] input, final int start, final int len, final String coding)
	{
		int length = len;
		if (length<=0) return EMPTY_STING;
		// keep in bounds of the source array...
		if (start+length>=input.length) length = input.length - start;
		
		String str = null;
		if (coding!=null)
		{
			try
			{
				str = new String(input, start, length, coding);
			}
			catch (UnsupportedEncodingException ex)
			{
				/* NOOP */
			}
		}
		
		if (str==null) str = new String(input, start, length);
		
		// find a null byte and delete it
		StringBuilder b = new StringBuilder(str);
		int index = 0;
		while (index<b.length() && b.charAt(index)!=0)
		{
			if (b.charAt(index)=='\r')
			{
				// check for '\r\n' or '\r'
				if (index+1<b.length() && b.charAt(index+1)=='\n')
					b.setCharAt(index, ' ');
				else
					b.setCharAt(index, '\n');
			}
			else
			if (b.charAt(index)<0x20) b.setCharAt(index, ' ');
			index++;
		}
		b.delete(index, b.length());
		return b.toString();
	}
	/**
	 * Converts a string to an array of bytes with the length specified
	 * @since 23.12.2008
	 * @param str the string to convert
	 * @param length the size of the byte array
	 * @return the array of bytes converted from the string
	 */
	public static byte[] getBytesFromString(final String str, final int length, final String coding)
	{
		byte [] result = new byte [length];
		int len = str.length();
		if (len>length) len = length;
		try
		{
			System.arraycopy(str.getBytes(coding), 0, result, 0, len);
		}
		catch (UnsupportedEncodingException ex)
		{
			System.arraycopy(str.getBytes(), 0, result, 0, len);
		}
		return result;
	}
	/**
	 * Search the data byte array for the first occurrence of the byte array pattern within given boundaries.
	 * Implementation of the Knuth-Morris-Pratt Pattern Matching Algorithm
	 * @param data
	 * @param start First index in data
	 * @param stop Last index in data so that stop-start = length
	 * @param pattern What is being searched. '*' can be used as wildcard for "ANY character"
	 * @return
	 */
	public static int indexOf(byte[] data, int start, int stop, byte[] pattern)
	{
		if (data == null || pattern == null) return -1;
		final int[] failure = computeFailure(pattern);
		int j = 0;
		for (int i=start; i<stop; i++)
		{
			while (j>0 && (pattern[j]!='*' && pattern[j]!=data[i])) j = failure[j-1];
			if (pattern[j]=='*' || pattern[j]==data[i]) j++;
			if (j == pattern.length) return i - pattern.length + 1;
		}
		return -1;
	}

	/**
	 * Computes the failure function using a boot-strapping process,
	 * where the pattern is matched against itself.
	 */
	private static int[] computeFailure(byte[] pattern)
	{
		final int[] failure = new int[pattern.length];

		int j = 0;
		for (int i=1; i<pattern.length; i++)
		{
			while (j>0 && pattern[j]!=pattern[i]) j = failure[j-1];
			if (pattern[j] == pattern[i]) j++;
			failure[i] = j;
		}

		return failure;
	}

	/**
	 * @since 11.07.2020
	 * @param bReader
	 * @return
	 */
	public static List<File> readLinesFromFlavor(BufferedReader bReader)
	{
		List<File> list = new ArrayList<File>();
		try
		{
			String line;
			while ((line = bReader.readLine()) != null)
			{
				try
				{
					// kde seems to append a 0 char to the end of the reader
					if (line.charAt(0)=='\0') continue;
					
					// Furthermore KDE get's confused with encoding the URL of
					// some files. So we do it manually.
					// Funny is that File.toURL() works fine!
					String fileString = Helpers.createStringFromURLString(line);
					if (fileString.startsWith("file://")) fileString = fileString.substring(7);

					File file = new File(fileString);
					list.add(file);
				}
				catch (Exception ex)
				{
					Log.error("Error with " + line + ": ", ex);
				}
			}
		}
		catch (IOException ex)
		{
			Log.error("Helpers:readLinesFromFlavor", ex);
		}
		return list;
	}

	/**
	 * Compares to URLs and returns true, if they are equal and OK. This is done via
	 * the URI - as URLs do a domain name lookup which will block if no
	 * DNS Lookup is possible 
	 * @since 01.04.2012
	 * @param url1
	 * @param url2
	 * @return
	 * @throws URISyntaxException
	 */
	public static boolean isEqualURL(final URL url1, final URL url2)
	{
		if (url1!=null && url2!=null)
		{
			try
			{
				URI uri1 = url1.toURI();
				URI uri2 = url2.toURI();
				return uri1.equals(uri2);
			}
			catch (URISyntaxException ex)
			{
				/* NOOP */
			}
		}
		return false;
	}
	/**
	 * @param file
	 * @return
	 * @since 14.02.2012
	 */
	public static URL createURLfromFile(final File file)
	{
		if (!file.exists())
		{
			try
			{
				String path = file.getPath();
				StringBuilder b = new StringBuilder((File.separatorChar != '/') ? path.replace(File.separatorChar, '/') : path);
				if (file.isDirectory() && b.charAt(b.length() - 1) != '/') b.append('/');
				if (b.length()>2 && b.charAt(0)=='/' && b.charAt(1)=='/') b.insert(0, "//");
				if (b.charAt(0)!='/') b.insert(0, "/");
				URI uri = new URI("file", null, b.toString(), null);
				return uri.toURL();
			}
			catch (URISyntaxException e)
			{
				// cannot happen...
			}
			catch (MalformedURLException ex)
			{
				// should not happen ;)
			}
		}
		try
		{
			return file.toURI().toURL();
		}
		catch (MalformedURLException ex)
		{
		}
		return null;
	}
	/**
	 * @since 01.05.2011
	 * @param urlLine
	 * @return a URL in correct form
	 */
	public static URL createURLfromString(final String urlLine)
	{
		try
		{
			if (urlLine == null || urlLine.length()==0) return null;
			URL url = new URL(urlLine);
			try
			{
				URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
				return uri.toURL();
			}
			catch (URISyntaxException e)
			{
				return url;
			}
		}
		catch (MalformedURLException ex)
		{
			return createURLfromFile(new File(urlLine));
		}
	}
	/**
	 * @since 22.07.2015
	 * @param url
	 * @return
	 */
	public static String createStringFomURL(final URL url)
	{
		if (url==null) return "";
		return createStringFromURLString(url.toExternalForm());
	}
	/**
	 * @since 22.07.2015
	 * @param url
	 * @return
	 */
	public static String createStringFromURLString(final String url)
	{
		try
		{
			return URLDecoder.decode(url, CODING_HTTP);
		}
		catch (UnsupportedEncodingException ex)
		{
			Log.error("Helpers::createStringRomURLString", ex);
		}
		return url;
	}
	/**
	 * @since 22.07.2015
	 * @param fileName
	 * @return
	 */
	public static String getFileNameFrom(final String fileName)
	{
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}
	/**
	 * @since 22.07.2015
	 * @param url
	 * @return
	 */
	public static String getFileNameFromURL(final URL url)
	{
		return getFileNameFrom(createStringFomURL(url));
	}
	/**
	 * @since 22.07.2015
	 * @param fileName
	 * @return
	 */
	public static String getExtensionFrom(final String fileName)
	{
		return fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase();
	}
	/**
	 * @since 22.07.2015
	 * @param url
	 * @return
	 */
	public static String getExtensionFromURL(final URL url)
	{
		return getExtensionFrom(createStringFomURL(url));
	}
	/**
	 * @since 22.07.2015
	 * @param fileName
	 * @return
	 */
	public static String getPreceedingExtensionFrom(String fileName)
	{
		fileName = fileName.substring(fileName.lastIndexOf('\\')+1);
		fileName = fileName.substring(fileName.lastIndexOf('/')+1);
		int dot = fileName.indexOf('.');
		if (dot>0)
			return fileName.substring(0, dot).toLowerCase();
		else
			return "";
	}
	/**
	 * @since 22.07.2015
	 * @param url
	 * @return
	 */
	public static String getPreceedingExtensionFromURL(URL url)
	{
		return getPreceedingExtensionFrom(url.getPath());
	}
	/**
	 * @since 05.01.2018
	 * @param protocol
	 * @return
	 */
	public static boolean isHTTP(final String protocol)
	{
		return protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https");
	}
	/**
	 * @since 05.01.2018
	 * @param url
	 * @return
	 */
	public static boolean isHTTP(final URL url)
	{
		return isHTTP(url.getProtocol());
	}
	/**
	 * @since 05.01.2018
	 * @param protocol
	 * @return
	 */
	public static boolean isFile(final String protocol)
	{
		return protocol.equalsIgnoreCase("file");
	}
	/**
	 * @since 05.01.2018
	 * @param url
	 * @return
	 */
	public static boolean isFile(final URL url)
	{
		return isFile(url.getProtocol());
	}
	/**
	 * @since 22.07.2015
	 * @param url
	 * @param stayLocal
	 * @return
	 */
	public static String createLocalFileStringFromURL(final URL url, final boolean stayLocal)
	{
		String suggestedPath = createStringFomURL(url);
		
		if (url!=null)
		{
			if (isFile(url))
			{
				try
				{
					suggestedPath = new File(url.toURI()).toString();
				}
				catch (URISyntaxException ex)
				{
					Log.error("Helpers::createLocalFileStringFromURL", ex);
				}
			}
			else
			if (!isHTTP(url) && stayLocal)
			{
				try
				{
					suggestedPath = HOMEDIR + File.pathSeparator + getFileNameFromURL(url);
				}
				catch (SecurityException ex)
				{
					Log.error("Helpers::createLocalFileStringFromURL", ex);
				}
			}
		}
		return suggestedPath;
	}
	/**
	 * @since 22.07.2015
	 * @param url
	 * @return
	 */
	public static boolean urlExists(final URL url)
	{
		if (url==null) return false;
		
		if (url.getProtocol().equalsIgnoreCase("file"))
		{
			try
			{
				File f = new File(url.toURI());
				return f.exists();
			}
			catch (Throwable ex)
			{
			}
			return false;
		}
		try
		{
			InputStream in = null;
			try 
			{ 
				in = url.openStream(); 
			} 
			catch (Throwable ex) 
			{ 
				return false;
			}
			finally
			{
				if (in!=null) try { in.close(); } catch (Throwable e) { /*NOOP */}
			}
			return true;
		}
		catch (Throwable ex)
		{
		}
		return false;
	}
	/**
	 * Checks first the File exists method - will try URL than
	 * @since 01.05.2011
	 * @param url
	 * @return
	 */
	public static boolean urlExists(final String url)
	{
		return urlExists(Helpers.createURLfromString(url));
	}
	/**
	 * Creates a relativized path that is relativ to the given basepath.
	 * For instance:
	 * relToPath = C:\Path1\Path2
	 * Path = C:\Path1\Path3\FILE
	 * Return is "..\Path3\File"
	 * @since 05.01.2018
	 * @param basePathName
	 * @param inputFileName
	 * @return
	 */
	public static String createRelativePathForFile(final String basePathName, final String inputFileName)
	{
		try
		{
			Path basePath = Paths.get(basePathName);
			Path inputFilePath = Paths.get(inputFileName);
			return basePath.relativize(inputFilePath).toString();
		}
		catch (Throwable ex)
		{
			return inputFileName;
		}
	}
	/**
	 * If the baseURL provided is not absolut this method will generate an absolute file path
	 * based on the inputFileName string
	 * This works only for protocol "file"!!
	 * @param baseURL
	 * @param inputFileName
	 * @return
	 * @since 23.03.2011
	 */
	public static URL createAbsolutePathForFile(final URL baseURL, final String inputFileName)
	{
		String fileName = inputFileName;
		final URL fileURL = createURLfromString(inputFileName);
		if (!fileURL.getProtocol().equalsIgnoreCase("file")) return fileURL;
		try
		{
			if (Helpers.urlExists(fileName))
				return fileURL;
			else
			{
				fileName = fileName.replace('\\', '/');

				// Create a URL object to the file
				String path = Helpers.createStringFomURL(baseURL);
				
				// get rid of playlist file name
				int lastSlash = path.lastIndexOf('/');
				StringBuilder relPath = new StringBuilder(path.substring(0, lastSlash + 1));
				
				if (fileName.charAt(0) == '/') fileName = fileName.substring(1);
				int iterations = 0;
				URL fullURL = Helpers.createURLfromString(((new StringBuilder(relPath)).append(fileName)).toString());
				while (fullURL!=null && !urlExists(fullURL) && iterations<256)
				{
					relPath.append("../");
					fullURL = Helpers.createURLfromString(((new StringBuilder(relPath)).append(fileName)).toString());
					iterations++;
				}
				if (iterations<256 && fullURL!=null)
				{
					try
					{
						return (fullURL.toURI().normalize()).toURL();
					}
					catch (URISyntaxException x)
					{
						Log.error("[createAbsolutePathForFile]", x);
					}
					return fullURL;
				}
				else
				{
					Log.info("File not found: " + inputFileName + " in relation to " + baseURL);
					return Helpers.createURLfromString(inputFileName);
				}
			}
		}
		catch (Throwable ex)
		{
			Log.error("createAbsolutePathForFile", ex);
		}
		Log.info("Illegal filename specification: " + inputFileName + " in playlist " + baseURL);
		return null;
	}

	/**
	 * Convert from decimalValue to DeciBel
	 * @since 14.01.2012
	 * @param dbValue
	 * @return
	 */
	public static double getDecimalValueFrom(double dbValue)
	{
		return Math.pow(10, dbValue / 20.0d);
	}
	/**
	 * convert from DeciBel to decimalValue
	 * @since 14.01.2012
	 * @param decimalValue
	 * @return
	 */
	public static double getDBValueFrom(final double decimalValue)
	{
		return Math.log10(decimalValue)*20.0d;
	}
	/**
	 * @since 06.02.2011
	 * @param millis
	 * @return
	 */
	public static String getTimeStringFromMilliseconds(final long millis)
	{
		int sec = (int)((millis/1000L)%60L);
		int min = (int)(millis/60000L);
		return ((min<10)?"  ":"") + Integer.toString(min) + ':' + ((sec<10)?"0":"") + Integer.toString(sec);
	}
	/**
	 * @since 03.04.2011
	 * @param timeString
	 * @return
	 */
	public static long getMillisecondsFromTimeString(final String timeString)
	{
		int minIndex = timeString.indexOf(':');
		int min = Integer.parseInt(timeString.substring(0, minIndex).trim());
		String secString = timeString.substring(minIndex + 1);
		int secIndex = secString.indexOf(':');
		if (secIndex == -1) secIndex = secString.length();
		int sec = Integer.parseInt(secString.substring(0, secIndex).trim());
		return (min*60+sec) * 1000L;
	}
	/**
	 * Prints the info about all installed and available audio lines
	 * @since 09.07.2006
	 */
	private static String AudioInfos = null;
	public static String getAudioInfos()
	{
		if (AudioInfos ==null)
		{
			final StringBuilder result = (new StringBuilder("Running on ")).append(System.getProperty("os.arch"));
			result.append("\nMixerInfo:\n");
			final Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
			for (int i = 0; i < mixerInfo.length; i++)
			{
				result.append("MixerInfo: ").append(mixerInfo[i]).append('\n');
				Mixer mixer = AudioSystem.getMixer(mixerInfo[i]);
				result.append("Mixer: ").append(mixer).append('\n');
				result.append("Devices for recording:\n");
				Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
				if (targetLineInfos.length==0) 
					result.append("\tNONE\n");
				else
				{
					for (int j = 0; j < targetLineInfos.length; j++)
					{
						result.append("\tTargetline(").append(j).append("): ").append(targetLineInfos[j]).append('\n');
						if (targetLineInfos[j] instanceof DataLine.Info)
						{
							AudioFormat audioFormats[] = ((DataLine.Info) targetLineInfos[j]).getFormats();
							for (int u = 0; u < audioFormats.length; u++)
							{
								result.append("\t\tAudioformat(").append(u).append("): ").append(audioFormats[u]);
								DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormats[u]);
								if (!AudioSystem.isLineSupported(info))
								{
									result.append(" --> NOT supported.\n");
								}
								else
								{
									result.append(" --> supported.\n");
								}
							}
						}
					}
				}
				result.append("\nDevices for sound output:\n");
				Line.Info[] sourceLineInfos = mixer.getSourceLineInfo();
				if (sourceLineInfos.length==0) 
					result.append("\tNONE\n");
				else
				{
					for (int j = 0; j < sourceLineInfos.length; j++)
					{
						result.append("\tSourceline(").append(j).append("): ").append(sourceLineInfos[j]).append('\n');
						if (sourceLineInfos[j] instanceof DataLine.Info)
						{
							AudioFormat audioFormats[] = ((DataLine.Info) sourceLineInfos[j]).getFormats();
							for (int u = 0; u < audioFormats.length; u++)
							{
								result.append("\t\tAudioformat(").append(u).append("): ").append(audioFormats[u]);
								DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormats[u]);
								if (!AudioSystem.isLineSupported(info))
								{
									result.append(" --> NOT supported.\n");
								}
								else
								{
									result.append(" --> supported.\n");
								}
							}
						}
					}
				}
				result.append("---------------------------------------------------------------------\n");
			}
			AudioInfos = result.toString(); 
		}
		return AudioInfos;
	}
	/**
	 * Registers all Classes that should not load during playback
	 * @since 26.12.2007
	 * @throws ClassNotFoundException
	 */
	public static void registerAllClasses() throws ClassNotFoundException
	{
		Class.forName("com.dunnysoft.gdxjavamod.system.Log");
		Class.forName("com.dunnysoft.gdxjavamod.system.Helpers");
		
		// Interpolation Routines - doing pre-calculations
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.mod.mixer.interpolation.CubicSpline");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.mod.mixer.interpolation.WindowedFIR");
		
		// The following are essential for registration at the ModuleFactory
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.mod.loader.tracker.ProTrackerMod");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.mod.loader.tracker.XMMod");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.mod.loader.tracker.ScreamTrackerOldMod");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.mod.loader.tracker.ScreamTrackerMod");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.mod.loader.tracker.ImpulseTrackerMod");

		// The following are essential for registration at the MultimediaContainerManager
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.mod.ModContainer"); // ModContainer uses the ModFactory!!
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.wav.WavContainer");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.mp3.MP3Container");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.ogg.OGGContainer");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.ape.APEContainer");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.flac.FLACContainer");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.midi.MidiContainer");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.sid.SIDContainer");
		Class.forName("com.dunnysoft.gdxjavamod.multimedia.opl3.OPL3Container");
		
		// SID WAVE Loading
		Class.forName("com.dunnysoft.sidplay.resid_builder.resid.Wave");
	}
	/**
	 * @since 22.04.2012
	 * @param destination
	 * @param fromURL
	 */
	/**
	 * Create a copy of an Object by serialization
	 * This is needed by NNAs
	 * @since 10.06.2020
	 * @return
	 */
	public static Object deepCopy(final Serializable fromMe)
	{
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(fromMe);
			oos.flush();
			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bin);
			// return the new object
			return ois.readObject();
		}
		catch (Throwable ex)
		{
			Log.error("Cloning failed", ex);
		}
		finally
		{
			if (oos!=null) try { oos.close(); } catch (IOException ex) { /*NOOP*/}
			if (ois!=null) try { ois.close(); } catch (IOException ex) { /*NOOP*/}
		}
		return null;
	}
}
