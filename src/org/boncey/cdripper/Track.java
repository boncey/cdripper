package org.boncey.cdripper;

import java.io.File;
import java.util.Calendar;
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
     * The year of the album.
     */
    private String _year;

    /** 
     * The genre of the album.
     */
    private String _genre;

    /** 
     * The Pattern for matching the track name without the track number.
     */
    private static final String TRACK_PATTERN = "^(\\d+) (- |)(.+)$";


    /**
     * Public constructor.
     * @param wavFile the file to encode.
     * @param trackName the track.
     * @param artist the artist.
     * @param album the album.
     * @param cddb the CDDBData.
     */
    public Track(File wavFile,
                 String trackName,
                 String artist,
                 String album,
                 CDDBData cddb)
    {
        _wavFile = wavFile;
        _trackName = trackName;
        _artist = artist;
        _album = album;
        _genre = cddb.getGenre();
        _year = cddb.getYear();
        if (_year == null)
        {
            // If no year specified assume current year
            Calendar cal = Calendar.getInstance();
            _year = String.valueOf(cal.get(Calendar.YEAR));
        }
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
        Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
        String ret;
        Matcher m = trackPattern.matcher(_trackName);
        if (m.matches())
        {
            ret = m.group(3);
        }
        else
        {
            throw new RuntimeException("Unable to parse name from " + _trackName);
        }

        return ret;
    }

    /**
     * Get the track.
     * @return the track.
     */
    public String getTrackNum()
    {
        Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
        String ret;
        Matcher m = trackPattern.matcher(_trackName);
        if (m.matches())
        {
            ret = m.group(1);
        }
        else
        {
            throw new RuntimeException("Unable to parse number from " + _trackName);
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
     * Get the year of the playlist.
     * @return The year of the playlist.
     */
    public String getYear()
    {
        return _year;
    }

    /**
     * Get the genre of the playlist.
     * @return The genre of the playlist.
     */
    public String getGenre()
    {
        return _genre;
    }
}
