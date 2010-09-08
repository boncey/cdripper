package org.boncey.cdripper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

/**
 * Abstract parent class for encoding an audio file.
 * Copyright (c) 2000-2005 Darren Greaves.
 * @author Darren Greaves
 * @version $Id: Encoder.java,v 1.6 2008-11-14 11:48:58 boncey Exp $
 */
public abstract class Encoder implements Runnable
{
    /**
     * Version details.
     */
    public static final String CVSID =
        "$Id: Encoder.java,v 1.6 2008-11-14 11:48:58 boncey Exp $";

    /** 
     * How long to sleep for when waiting for encoding to finish.
     */
    private static final int WAIT = 1000;

    /** 
     * The class to notify once encoding is finished.
     */
    private Encoded _encoded;
    
    
    /**
     * The location to save the files to.
     */
    private String _location;

    /** 
     * Whether or not this thread is alive.
     */
    private boolean _alive = true;

    /** 
     * The List of Tracks to encode.
     */
    private List<Track> _tracks;

    /**
     * Private constructor.
     */
    private Encoder()
    {
        _tracks = new Vector<Track>();
    }

    /**
     * Public constructor.
     * @param encoded the class to notify once encoding is finished.
     * @param location the location to save the files to.
     */
    public Encoder(Encoded encoded, String location)
    {
        this();

        _encoded = encoded;
        _location = location;
    }

    /** 
     * Encode the provided file.
     */
    public void run()
    {
        try
        {
            while (_alive || _tracks.size() > 0)
            {
                if (_tracks.size() > 0)
                {
                    Track track = _tracks.remove(0);
                    File wavFile = track.getWavFile();
                    if (encode(track))
                    {
                        _encoded.encoded(wavFile);
                    }
                    else
                    {
                        System.err.println("Unable to encode " +
                                wavFile.getName() + " to "  + getExt());
                        System.exit(-1);
                    }
                }
                else
                {
                    try
                    {
                        Thread.sleep(WAIT);
                    }
                    catch (InterruptedException e)
                    {
                        // Ignore
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** 
     * Queue this track for encoding.
     * @param track the track to encode.
     */
    public void queue(Track track)
    {
        _tracks.add(track);
    }

    /** 
     * Shutdown this Encoder.
     */
    public void shutdown()
    {
        _alive = false;
    }

    /** 
     * Encode the CD track.
     * @param track the track to encode.
     * @return whether or not the encoding was successful.
     * @throws IOException if unable to interact with the cdda process.
     * @throws InterruptedException if this thread is interrupted.
     */
    private boolean encode(Track track)
        throws IOException, InterruptedException
    {
        boolean success = true;
        File wavFile = track.getWavFile();
        String filename = makeFilename(wavFile);
        File destFile = new File(filename);

        System.out.println(
            "Encoding (" +
            wavFile.getParentFile().getName() + ") " +
            wavFile.getName() + " to " +
            destFile.getName());

        Runtime rt = Runtime.getRuntime();
        File tempDest = File.createTempFile("dest-", null, wavFile.getParentFile());
        String[] args = getEncodeCommand(track, tempDest.toString(), wavFile.toString());
        Process proc = rt.exec(args);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(proc.getErrorStream()));
        String line = in.readLine();

        while (line != null)
        {
            System.err.println(line);
            line = in.readLine();
        }
        proc.waitFor();

        success = (proc.exitValue() == 0);

        if (success)
        {
            File parentDir = destFile.getParentFile();
            parentDir.mkdir();
            if (!tempDest.renameTo(destFile))
            {
                System.err.println("Unable to rename " +
                        tempDest.getName() + " to " + destFile.getName());
            }
        }

        return success;
    }
    
    /** 
     * Get the command to encode.
     * @param track the track to encode.
     * @param encodedFilename the filename to encode to.
     * @param wavFile the file to encode from.
     * @return the command to encode.
     */
    protected abstract String[] getEncodeCommand(Track track,
                                                 String encodedFilename,
                                                 String wavFile);

    /** 
     * Get the file extension for encoded files.
     * @return the file extension.
     */
    protected abstract String getExt();

    /** 
     * Make the filename for the encoded file.
     * @param wavFile the wav file to enode from.
     * @return the newly created filename.
     */
    private String makeFilename(File wavFile)
    {
        StringBuffer trackName = new StringBuffer();
        String wavFilename = wavFile.getName();
        int dot = wavFilename.lastIndexOf(".");
        if (dot != -1)
        {
            trackName.append(_location);
            trackName.append("/");
            trackName.append(wavFile.getParentFile().getName());
            trackName.append("/");
            trackName.append(wavFilename.substring(0, dot));
            trackName.append(getExt());
        }
        else
        {
            throw new IllegalArgumentException(
                    wavFile + " does not have a file extension");
        }

        return trackName.toString();
    }

    /** 
     * Display the command args as a String.
     * @param args the command args to display.
     */
    @SuppressWarnings("unused")
    private void outputCommand(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            System.out.print("\"" + args[i] + "\" ");
        }
        System.out.println();
    }

    /**
     * Get the location.
     * @return the location.
     */
    public String getLocation()
    {
        return _location;
    }
    
}
