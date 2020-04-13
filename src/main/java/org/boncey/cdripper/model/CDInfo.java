package org.boncey.cdripper.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The information about a CD.
 * Copyright (c) 2000-2005 Darren Greaves.
 *
 * @author Darren Greaves
 * @version $Id: CDInfo.java,v 1.4 2008-11-14 11:48:58 boncey Exp $
 */
public class CDInfo
{

    /**
     * The artist name.
     */
    private String _artist;

    /**
     * The album.
     */
    private String _album;

    /**
     * The track titles.
     */
    private List<String> _tracks;

    /**
     * The track count.
     */
    private int _trackCount;

    /**
     * Public constructor.
     */
    public CDInfo()
    {
        _tracks = new ArrayList<>();
    }

    public boolean recognised()
    {
        return _artist != null && _album != null;
    }

    public static CDInfo unknown(int trackCount)
    {
        CDInfo cdInfo = new CDInfo();
        cdInfo._trackCount = trackCount;

        return cdInfo;
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
     * Set the tracks.
     *
     * @param tracks the tracks.
     */
    public void setTracks(List<String> tracks)
    {
        _tracks = tracks;
        _trackCount = tracks.size();
    }

    /**
     * Get the tracks.
     *
     * @return the tracks.
     */
    public List<String> getTracks()
    {
        return _tracks;
    }

    /**
     * Get the relative dir for this CD.
     *
     * @return the relative dir.
     */
    public String getDir()
    {
        return String.format("%s - %s", _artist, _album);
    }

    /**
     * Return a String representing this object.
     *
     * @return a String representing this object.
     */
    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        if (recognised())
        {
            buff.append(String.format("%s by %s\n", _album, _artist));
        }
        else
        {
            buff.append("Unknown album\n");
        }
        for (String track : _tracks)
        {
            buff.append(track + "\n");
        }
        return buff.toString();
    }

    // See src/main/resources/example-track-listing.txt for format
    public void fromTrackListing(List<String> trackListing)
    {
        if (trackListing.size() != _trackCount + 1)
        {
            throw new IllegalArgumentException(String.format("Expected to find %d tracks in file but found %d", _trackCount, trackListing.size() - 1));
        }

        List<String> headerWords = split(trackListing.remove(0));
        _album = headerWords.get(0);
        _artist = headerWords.get(1);

        _tracks = new ArrayList<>();
        _tracks.addAll(trackListing);
    }

    private List<String> split(String line)
    {
        String[] split = line.split(" - ");
        if (split.length != 2)
        {
            throw new IllegalArgumentException(String.format("Unable to split line '%s'", line));
        }

        return Arrays.asList(split);
    }
}
