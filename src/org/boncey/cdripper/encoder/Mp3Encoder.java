package org.boncey.cdripper.encoder;


import org.boncey.cdripper.Encoded;
import org.boncey.cdripper.model.Track;

import java.io.File;
import java.io.IOException;

/**
 * For encoding an audio file to MP3.
 * 
 * Copyright (c) 2000-2005 Darren Greaves.
 * 
 * @author Darren Greaves
 * @version $Id: Mp3Encoder.java,v 1.2 2008-11-14 11:48:58 boncey Exp $
 */
public class Mp3Encoder extends AbstractEncoder
{
    /**
     * Version details.
     */
    public static final String CVSID = "$Id: Mp3Encoder.java,v 1.2 2008-11-14 11:48:58 boncey Exp $";


    /**
     * The encode command.
     */
    private static final String MP3_CMD = "lame";


    /**
     * The file extension for encoded files.
     */
    private static final String EXT = ".mp3";


    /**
     * Public constructor.
     * 
     * @param encoded the class to notify once encoding is finished.
     * @param location the location to save the files to.
     */
    public Mp3Encoder(Encoded encoded, File location)
    {

        super(encoded, location);
    }


    /**
     * Get the file extension for encoded files.
     * 
     * @return the file extension.
     */
    @Override
    protected String getExt()
    {

        return EXT;
    }


    /**
     * Get the command to encode.
     * 
     * @param track the track to encode.
     * @param encodedFilename the filename to encode to.
     * @param wavFile the file to encode from.
     * @return the command to encode.
     */
    @Override
    protected String[] getEncodeCommand(Track track, String encodedFilename, String wavFile)
    {

        String[] args =
        {
                MP3_CMD, "--quiet", "--vbr-new", "-h", "-b", "192", "--add-id3v2", "--tt", track.getTrackName(), "--tl", track.getAlbum(), "--ta",
                track.getArtist(), "--tn", track.getTrackNum(), wavFile, encodedFilename
        };

        return args;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dependenciesInstalled() throws IOException, InterruptedException
    {

        return exec(new String[]
        {
                MP3_CMD, "--help"
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String command()
    {

        return MP3_CMD;
    }
}
