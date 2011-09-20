package org.boncey.cdripper;


import org.boncey.cdripper.encoder.Encoder;
import org.boncey.cdripper.model.Track;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * For managing a queue of Encoders, flac, ogg etc. Copyright (c) 2005 Darren Greaves.
 * 
 * @author Darren Greaves
 * @version $Id: EncoderQueue.java,v 1.5 2008-11-14 11:48:58 boncey Exp $
 */
public class EncoderQueue
{
    /**
     * Version details.
     */
    public static final String CVSID = "$Id: EncoderQueue.java,v 1.5 2008-11-14 11:48:58 boncey Exp $";


    /**
     * A count of tracks encoded.
     */
    private int _tracksEncoded;


    /**
     * The List of Encoders that can encode.
     */
    private final List<Encoder> _encoders;


    /**
     * The base dir to encode from.
     */
    private final File _baseDir;


    /**
     * The {@link TrackMonitor} to monitor tracks being encoded.
     */
    private final TrackMonitor _monitor;


    /**
     * The extension for unencoded files.
     */
    public static final String WAV_EXT = ".wav";


    /**
     * Public constructor.
     * 
     * @param baseDir the base directory to read the raw files from.
     * @param encoders the List of {@link Encoder}s.
     * @param monitor
     * @throws IOException if there was an IO problem.
     * @throws InterruptedException
     */
    public EncoderQueue(File baseDir, List<Encoder> encoders, TrackMonitor monitor) throws IOException, InterruptedException
    {

        try
        {
            _monitor = monitor;
            _encoders = encoders;
            _baseDir = baseDir;

            dependenciesInstalled(encoders);

            List<File> files = findRawFiles(_baseDir);
            Collections.sort(files);

            if (files.size() > 0)
            {

                for (File file : files)
                {
                    Track track = new Track(file, WAV_EXT);
                    queue(track);
                }
            }
            else
            {
                System.err.println("No wav files found in " + baseDir);
            }
        }
        finally
        {
            shutdown();
        }
    }


    /**
     * Are the {@link Encoder} dependencies installed?
     * 
     * @param encoders
     * @throws InterruptedException
     * @throws IOException
     */
    private void dependenciesInstalled(List<Encoder> encoders) throws IOException, InterruptedException
    {

        for (Encoder encoder : encoders)
        {

            if (!encoder.dependenciesInstalled())
            {

                throw new IllegalStateException(String.format("Encoder %s does not have %s installed", encoder, encoder.command()));
            }
        }
    }


    /**
     * Find any files that require encoding.
     * 
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
     * 
     * @param track the track to encode.
     */
    public void queue(Track track)
    {

        _monitor.monitor(track.getWavFile(), _encoders.size());

        for (Encoder encoder : _encoders)
        {
            encoder.queue(track);
            _tracksEncoded++;
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
     * Get the tracksEncoded.
     * 
     * @return the tracksEncoded.
     */
    private int getTracksEncoded()
    {

        return _tracksEncoded;
    }


    /**
     * Search for unencoded tracks and encode accordingly.
     * 
     * @param args the base directory.
     */
    @SuppressWarnings("boxing")
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
            System.err.println("Unable to access " + baseDir + " as a directory");
            System.exit(-1);
        }
        if (!props.canRead())
        {
            System.err.println("Unable to access " + props);
            System.exit(-1);
        }

        try
        {
            TrackMonitor monitor = new TrackMonitor();
            List<Encoder> encoders = new EncoderLoader().initEncoders(props, monitor);
            EncoderQueue encoderQueue = new EncoderQueue(baseDir, encoders, monitor);

            if (encoderQueue.getTracksEncoded() == 0)
            {
                // Return -1 so we don't trigger success notifications in any caller
                System.exit(-1);
            }
            else
            {
                System.out.println(String.format("Encoded %d tracks", encoderQueue.getTracksEncoded()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
