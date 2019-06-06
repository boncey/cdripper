package org.boncey.cdripper.encoder;


import org.boncey.cdripper.Encoded;
import org.boncey.cdripper.model.Track;

import java.io.File;
import java.io.IOException;

/**
 * For encoding an audio file to Apple Lossless.
 *
 * Copyright (c) 2000-2005 Darren Greaves.
 * 
 * @author Darren Greaves
 * @version $Id: FlacEncoder.java,v 1.6 2008-11-14 11:48:58 boncey Exp $
 */
public class AppleLosslessEncoder extends AbstractEncoder
{
    /**
     * Version details.
     */
    public static final String CVSID = "$Id: FlacEncoder.java,v 1.6 2008-11-14 11:48:58 boncey Exp $";


    /**
     * The encode command.
     */
    private static final String CMD = "ffmpeg";


    /**
     * The file extension for encoded files.
     */
    private static final String EXT = ".m4a";


    /**
     * Public constructor.
     *
     * @param encoded the class to notify once encoding is finished.
     * @param location the location to save the files to.
     */
    public AppleLosslessEncoder(Encoded encoded, File location)
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
        //       run("ffmpeg -y -loglevel warning -i \"#{alac}\" -c:a libfdk_aac -b:a 320k -c:v copy \"#{tmpfile.path}\"")

        String[] args =
        {
                CMD, "-y", "-metadata", "title=" + track.getTrackName(), "-metadata", "album=" + track.getAlbum(), "-metadata",
                "artist=" + track.getArtist(), "-metadata", "tracknumber=" + track.getTrackNum(), "-i", wavFile, "-c:a", "alac", encodedFilename
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
                CMD, "-version"
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String command()
    {

        return CMD;
    }

}
