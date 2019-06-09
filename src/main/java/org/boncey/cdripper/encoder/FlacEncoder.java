package org.boncey.cdripper.encoder;


import org.boncey.cdripper.Encoded;
import org.boncey.cdripper.model.Track;

import java.io.File;
import java.io.IOException;

/**
 * For encoding an audio file to FLAC. Copyright (c) 2000-2005 Darren Greaves.
 * 
 * @author Darren Greaves
 * @version $Id: FlacEncoder.java,v 1.6 2008-11-14 11:48:58 boncey Exp $
 */
public class FlacEncoder extends AbstractEncoder
{
    /**
     * Version details.
     */
    public static final String CVSID = "$Id: FlacEncoder.java,v 1.6 2008-11-14 11:48:58 boncey Exp $";


    /**
     * The encode command.
     */
    private static final String FLAC_CMD = "flac";


    /**
     * The file extension for encoded files.
     */
    private static final String EXT = ".flac";


    /**
     * Public constructor.
     * 
     * @param encoded the class to notify once encoding is finished.
     * @param location the location to save the files to.
     */
    public FlacEncoder(Encoded encoded, File location)
    {

        super(encoded, location);
    }

    @Override
    protected String getTempFileSuffix()
    {
        return ".flac";
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
                FLAC_CMD, "--silent", "--force", "--verify", "--tag", "title=" + track.getTrackName(), "--tag", "album=" + track.getAlbum(), "--tag",
                "artist=" + track.getArtist(), "--tag", "tracknumber=" + track.getTrackNum(), "-o", encodedFilename, wavFile
        };

        return args;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public boolean dependenciesInstalled() throws IOException, InterruptedException
    {

        return exec(new String[]
        {
                FLAC_CMD, "-v"
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String command()
    {

        return FLAC_CMD;
    }

}
