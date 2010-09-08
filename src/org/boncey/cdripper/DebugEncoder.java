package org.boncey.cdripper;


/**
 * For encoding an audio file to FLAC.
 * Copyright (c) 2000-2005 Darren Greaves.
 * @author Darren Greaves
 * @version $Id: DebugEncoder.java,v 1.6 2008-11-14 11:48:58 boncey Exp $
 */
public class DebugEncoder extends Encoder
{
    /**
     * Version details.
     */
    public static final String CVSID =
        "$Id: DebugEncoder.java,v 1.6 2008-11-14 11:48:58 boncey Exp $";

    /** 
     * The encode command.
     */
    private static final String CMD = "/bin/echo ";

    /** 
     * The file extension for encoded files.
     */
    private static final String EXT = ".flac";

    /**
     * Public constructor.
     * @param encoded the class to notify once encoding is finished.
     * @param location the location to save the files to.
     */
    public DebugEncoder(Encoded encoded, String location)
    {
        super(encoded, location);
    }

    /** 
     * Get the file extension for encoded files.
     * @return the file extension.
     */
    @Override
    protected String getExt()
    {
        return EXT;
    }

    /** 
     * Get the command to encode.
     * @param track the track to encode.
     * @param encodedFilename the filename to encode to.
     * @param wavFile the file to encode from.
     * @return the command to encode.
     */
    @Override
    protected String[] getEncodeCommand(Track track,
                                        String encodedFilename,
                                        String wavFile)
    {
        String[] args = {CMD,
                         track.getTrackNum(),
                         track.getTrackName()};

         return args;
    }
}
