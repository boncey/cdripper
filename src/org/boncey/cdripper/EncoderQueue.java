package org.boncey.cdripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * For managing a queue of Encoders, flac, ogg etc.
 * Copyright (c) 2005 Darren Greaves.
 * @author Darren Greaves
 * @version $Id: EncoderQueue.java,v 1.5 2008-11-14 11:48:58 boncey Exp $
 */
public class EncoderQueue
{
    /**
     * Version details.
     */
    public static final String CVSID =
        "$Id: EncoderQueue.java,v 1.5 2008-11-14 11:48:58 boncey Exp $";

    /**
     * The List of Encoders that can encode.
     */
    private List<Encoder> _encoders;

    /**
     * The base dir to encode from.
     */
    private File _baseDir;


    /**
     * The {@link TrackMonitor} to monitor tracks being encoded.
     */
    private TrackMonitor _monitor;


    /**
     * The extension for unencoded files.
     */
    public static final String WAV_EXT = ".wav";


    /**
     * Public constructor.
     * @param baseDir the base directory to read the raw files from.
     * @param encoders the List of {@link Encoder}s.
     * @param monitor
     * @throws IOException if there was an IO problem.
     */
    public EncoderQueue(File baseDir, List<Encoder> encoders, TrackMonitor monitor)
        throws IOException
    {
        _monitor = monitor;
        _encoders = encoders;
        _baseDir = baseDir;

        List<File> files = findRawFiles(_baseDir);
        Collections.sort(files);

        if (files.size() > 0)
        {

            for (File file : files)
            {
                Track track = new Track(file, WAV_EXT);
                queue(track);
            }

            shutdown();
        }
        else
        {
            System.err.println("No wav files found in " + baseDir);
        }
    }


    /**
     * Find any files that require encoding.
     * @param dir the directory to search from.
     * @return a List of files found.
     */
    private List<File> findRawFiles(File dir)
    {
        List<File> files = new ArrayList<File>();

        File[] fileArray = dir.listFiles();
        if (fileArray != null)
        {
            for (File file : fileArray)
            {
                String filename = file.getName();
                if (file.isDirectory() && !filename.startsWith("."))
                {
                    files.addAll(findRawFiles(file));
                }
                else if (filename.endsWith(WAV_EXT))
                {
                    files.add(file);
                }
            }
        }

        return files;
    }

    /**
     * Queue this track for encoding.
     * @param track the track to encode.
     */
    public void queue(Track track)
    {
        _monitor.monitor(track.getWavFile(), _encoders.size());

        for (Encoder encoder : _encoders)
        {
            encoder.queue(track);
        }
    }

    /**
     * Shutdown the encoders.
     */
    public void shutdown()
    {
        for (Encoder encoder : _encoders)
        {
            encoder.shutdown();
        }
    }

    /**
     * Search for unencoded tracks and encode accordingly.
     * @param args the base directory.
     */
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            System.err.println("Usage: Encode <base dir> <encoder properties>");
            System.exit(-1);
        }

        File baseDir = new File(args[0]);
        File props = new File(args[1]);
        if (!baseDir.canRead() || !baseDir.isDirectory())
        {
            System.err.println(
                    "Unable to access " + baseDir + " as a directory");
            System.exit(-1);
        }
        if (!props.canRead())
        {
            System.err.println(
                    "Unable to access " + props);
            System.exit(-1);
        }

        try
        {
            TrackMonitor monitor = new TrackMonitor();
            List<Encoder> encoders = new EncodersReader().initEncoders(props, monitor);
            new EncoderQueue(baseDir, encoders, monitor);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
