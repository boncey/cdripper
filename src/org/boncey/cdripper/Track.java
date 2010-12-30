package org.boncey.cdripper;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Track info.
 * Copyright (c) 2000-2005 Darren Greaves.
 * @author Darren Greaves
 * @version $Id: Track.java,v 1.4 2008-11-14 11:48:58 boncey Exp $
 */
public class Track
{
    /**
     * Version details.
     */
    public static final String CVSID =
        "$Id: Track.java,v 1.4 2008-11-14 11:48:58 boncey Exp $";

    /**
     * The track's wav file.
     */
    private File _wavFile;

    /**
     * The track name.
     */
    private String _artist;

    /**
     * The track name.
     */
    private String _album;

    /**
     * The track name.
     */
    private String _trackName;


    /**
     * The base path of the Track file, relative to the base path.
     */
    private String _relativeBasePath;


    /**
     * The Pattern for matching the track name without the track number.
     */
    private static final String TRACK_PATTERN = "^(\\d+) (- |)(.+)$";


    /**
     * Create a Track object from the track File.
     * @param wavFile the track File.
     * @param ext
     */
    public Track(File wavFile, String ext)
    {
        File parentFile = wavFile.getParentFile();
        String trackName = wavFile.getName().replaceFirst(ext, "");
        String[] parent = parentFile.getName().split(" -", 2);
        String artist = null;
        String album = null;

        if (parent.length >= 2)
        {
            // cdripper style 'Artist - Album'
            artist = parent[0].trim();
            album = parent[1].trim();
            _relativeBasePath = parentFile.getName();
        }
        else
        {
            // iTunes style 'Artist/Album'
            artist = parentFile.getParentFile().getName();
            album = parentFile.getName();
            _relativeBasePath = String.format("%s%s%s", artist, File.separator, album);
        }

        _wavFile = wavFile;
        _trackName = trackName;
        _artist = artist;
        _album = album;
    }

    /**
     * Set the wavFile.
     * @param wavFile the wavFile.
     */
    public void setWavFile(File wavFile)
    {
        _wavFile = wavFile;
    }

    /**
     * Get the wavFile.
     * @return the wavFile.
     */
    public File getWavFile()
    {
        return _wavFile;
    }

    /**
     * Set the track.
     * @param trackName the track.
     */
    public void setTrackName(String trackName)
    {
        _trackName = trackName;
    }

    /**
     * Get the track.
     * @return the track.
     */
    public String getTrackName()
    {
        return parseTrackName(3, "name");
    }

    /**
     * Get the track.
     * @return the track.
     */
    public String getTrackNum()
    {
        return parseTrackName(1, "number");
    }

    /**
     * Parse the required field from the track name.
     * @param group
     * @param field
     * @return
     */
    private String parseTrackName(int group, String field)
    {
        Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
        String ret;
        Matcher m = trackPattern.matcher(_trackName);
        if (m.matches())
        {
            ret = m.group(group);
        }
        else
        {
            throw new RuntimeException(String.format("Unable to parse %s from %s", field, _trackName));
        }

        return ret;
    }

    /**
     * Set the artist.
     * @param artist the artist.
     */
    public void setArtist(String artist)
    {
        _artist = artist;
    }

    /**
     * Get the artist.
     * @return the artist.
     */
    public String getArtist()
    {
        return _artist;
    }

    /**
     * Set the album.
     * @param album the album.
     */
    public void setAlbum(String album)
    {
        _album = album;
    }

    /**
     * Get the album.
     * @return the album.
     */
    public String getAlbum()
    {
        return _album;
    }

    /**
     * Get the relativeBasePath.
     * @return the relativeBasePath.
     */
    public String getRelativeBasePath()
    {
        return _relativeBasePath;
    }

    /**
     * Construct the filename for the encoded file.
     * @param basePath
     * @param extension
     * @return the newly created filename.
     */
    public File constructFilename(String basePath, String extension)
    {
        File wavFile = getWavFile();
        StringBuffer trackName = new StringBuffer();
        String wavFilename = wavFile.getName();
        int dot = wavFilename.lastIndexOf(".");
        if (dot != -1)
        {
            trackName.append(basePath);
            trackName.append("/");
            trackName.append(getRelativeBasePath());
            trackName.append("/");
            trackName.append(wavFilename.substring(0, dot));
            trackName.append(extension);
        }
        else
        {
            throw new IllegalArgumentException(
                    wavFile + " does not have a file extension");
        }

        return new File(trackName.toString());
    }
}
