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
public abstract class AbstractEncoder implements Encoder
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
    private AbstractEncoder()
    {
        // Use Vector as it's 'synchronized'.
        _tracks = new Vector<Track>();
    }

    /**
     * Public constructor.
     * @param encoded the class to notify once encoding is finished.
     * @param location the location to save the files to.
     */
    public AbstractEncoder(Encoded encoded, String location)
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
     * {@inheritDoc}
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
     * @throws IOException if unable to interact with the file system.
     * @throws InterruptedException if this thread is interrupted.
     */
    private boolean encode(Track track)
        throws IOException, InterruptedException
    {
        boolean success = true;
        File wavFile = track.getWavFile();
        File destFile = track.constructFilename(_location, getExt());

        System.out.println(String.format("Encoding (%s) %s to %s", track.getRelativeBasePath(), wavFile.getName(), destFile.getName()));

        File tempDest = File.createTempFile("dest-", null, wavFile.getParentFile());
        String[] args = getEncodeCommand(track, tempDest.toString(), wavFile.getAbsolutePath());
        success = exec(args);

        if (success)
        {
            File parentDir = destFile.getParentFile();
            parentDir.mkdirs();
            if (!tempDest.renameTo(destFile))
            {
                System.err.println("Unable to rename " +
                        tempDest.getName() + " to " + destFile.getName());
            }
        }

        return success;
    }

    /**
     *
     * @param args
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    protected boolean exec(String[] args) throws IOException,
            InterruptedException
    {
        boolean success;
        Runtime rt = Runtime.getRuntime();
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
     * Display the command arguments as a String.
     * @param args the command arguments to display.
     */
    protected void outputCommand(String[] args)
    {
        for (String arg : args)
        {
            System.out.print("\"" + arg + "\" ");
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
