package org.boncey.cdripper.model;


import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Track info. Copyright (c) 2000-2005 Darren Greaves.
 * 
 * @author Darren Greaves
 * @version $Id: Track.java,v 1.4 2008-11-14 11:48:58 boncey Exp $
 */
public class Track
{
    /**
     * Version details.
     */
    public static final String CVSID = "$Id: Track.java,v 1.4 2008-11-14 11:48:58 boncey Exp $";


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
    private final String _relativeBasePath;


    /**
     * The Pattern for matching the track name and track number.
     */
    private static final String TRACK_PATTERN = "^(\\d-|)(\\d+) (- |)(.+)$";


    /**
     * Private constructor.
     * 
     * @param wavFile
     * @param artist
     * @param album
     * @param trackName
     * @param relativeBasePath
     */
    private Track(File wavFile, String artist, String album, String trackName, String relativeBasePath)
    {

        _wavFile = wavFile;
        _artist = artist;
        _album = album;
        _trackName = trackName;
        _relativeBasePath = relativeBasePath;
    }


    /**
     * Set the wavFile.
     * 
     * @param wavFile the wavFile.
     */
    public void setWavFile(File wavFile)
    {

        _wavFile = wavFile;
    }


    /**
     * Get the wavFile.
     * 
     * @return the wavFile.
     */
    public File getWavFile()
    {

        return _wavFile;
    }


    /**
     * Set the track.
     * 
     * @param trackName the track.
     */
    public void setTrackName(String trackName)
    {

        _trackName = trackName;
    }


    /**
     * Get the track.
     * 
     * @return the track.
     */
    public String getTrackName()
    {

        return parseTrackName(4, "name");
    }


    /**
     * Get the track.
     * 
     * @return the track.
     */
    public String getTrackNum()
    {

        return parseTrackName(2, "number");
    }


    /**
     * Factory method for creating a {@link Track}.
     * 
     * @param wavFile
     * @param baseDir
     * @param ext
     * @return
     */
    public static Track createTrack(File wavFile, File baseDir, String ext)
    {

        File wavFileParentDir = wavFile.getParentFile();
        String trackName = wavFile.getName().replaceFirst(ext, "");
        String[] parent = wavFileParentDir.getName().split(" -", 2);
        String artist;
        String album;
        String relativeBasePath;

        Track track = null;
        if (parent.length >= 2)
        {
            // cdripper style 'Artist - Album'
            artist = parent[0].trim();
            album = parent[1].trim();
            relativeBasePath = tidyTrackPath(wavFileParentDir.getName());
            track = new Track(wavFile, artist, album, trackName, relativeBasePath);
        }
        else
        {
            // iTunes style 'Artist/Album'
            if (wavFileParentDir.getParentFile().equals(baseDir))
            {
                System.err.println(String.format("File hierarchy incorrect for %s, ignoring", wavFile));
            }
            else
            {
                artist = wavFileParentDir.getParentFile().getName();
                album = wavFileParentDir.getName();
                relativeBasePath = tidyTrackPath(String.format("%s - %s", artist, album));
                track = new Track(wavFile, artist, album, trackName, relativeBasePath);
            }
        }

        return track;
    }


    /**
     * Parse the required field from the track name.
     * 
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
            throw new IllegalArgumentException(String.format("Unable to parse %s from %s", field, getWorkingTrackName()));
        }

        return ret;
    }


    /**
     * 
     * @return
     */
    private String getWorkingTrackName()
    {

        return new File(_relativeBasePath, _trackName).getPath();
    }


    /**
     * Tidy the track path (artist album) according to my idiosyncratic criteria.
     * 
     * @param trackPath
     * @return
     */
    private static String tidyTrackPath(String trackPath)
    {

        String tidiedPath = trackPath;

        tidiedPath = tidiedPath.replaceFirst("Compilations", "Various Artists");
        tidiedPath = tidiedPath.replaceFirst("_ ", " ");

        return tidiedPath;
    }


    /**
     * Set the artist.
     * 
     * @param artist the artist.
     */
    public void setArtist(String artist)
    {

        _artist = artist;
    }


    /**
     * Get the artist.
     * 
     * @return the artist.
     */
    public String getArtist()
    {

        return _artist;
    }


    /**
     * Set the album.
     * 
     * @param album the album.
     */
    public void setAlbum(String album)
    {

        _album = album;
    }


    /**
     * Get the album.
     * 
     * @return the album.
     */
    public String getAlbum()
    {

        return _album;
    }


    /**
     * Get the relativeBasePath.
     * 
     * @return the relativeBasePath.
     */
    public String getRelativeBasePath()
    {

        return _relativeBasePath;
    }


    /**
     * Construct the filename for the encoded file.
     * 
     * @param location
     * @param extension
     * @return the newly created filename.
     */
    public File constructFilename(File location, String extension)
    {

        String filename = String.format("%s - %s", getTrackNum(), getTrackName());
        File basePath = new File(location, getRelativeBasePath());

        return new File(basePath, filename + extension);
    }
}
