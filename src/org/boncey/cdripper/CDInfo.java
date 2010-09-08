package org.boncey.cdripper;

import java.util.List;

/**
 * The information about a CD.
 * Copyright (c) 2000-2005 Darren Greaves.
 * @author Darren Greaves
 * @version $Id: CDInfo.java,v 1.4 2008-11-14 11:48:58 boncey Exp $
 */
public class CDInfo
{
    /**
     * Version details.
     */
    public static final String CVSID =
        "$Id: CDInfo.java,v 1.4 2008-11-14 11:48:58 boncey Exp $";

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
     * Public constructor.
     */
    public CDInfo()
    {
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
     * Set the tracks.
     * @param tracks the tracks.
     */
    public void setTracks(List<String> tracks)
    {
        _tracks = tracks;
    }

    /**
     * Get the tracks.
     * @return the tracks.
     */
    public List<String> getTracks()
    {
        return _tracks;
    }

    /**
     * Get the relative dir for this CD.
     * @return the relative dir.
     */
    public String getDir()
    {
        return _artist + " - " + _album;
    }

    /**
     * Return a String representing this object.
     * @return a String representing this object.
     */
    @Override
    public String toString()
    {
        StringBuffer buff = new StringBuffer(_album + " by " + _artist + "\n");
        for (String track : _tracks)
        {
            buff.append(track + "\n");
        }
        return buff.toString();
    }
}
