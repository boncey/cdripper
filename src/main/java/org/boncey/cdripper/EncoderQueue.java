package org.boncey.cdripper;


import org.boncey.cdripper.encoder.Encoder;
import org.boncey.cdripper.model.Track;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
     * The {@link Encoded} implementation to monitor tracks being encoded.
     */
    private final Encoded _monitor;


    /**
     * For cleaning up the empty directories after encoding.
     */
    private FileSystemCleaner _fileSystemCleaner;


    /**
     * The extension for unencoded files.
     */
    public static final String WAV_EXT = ".wav";


    /**
     * Perform a "dry run", don't encode tracks or change file-system.
     */
    private final boolean _dryRun;


    /**
     * Public constructor.
     * 
     * @param baseDir the base directory to read the raw files from.
     * @param encoders the List of {@link Encoder}s.
     * @param monitor
     * @param dryRun
     * @throws IOException if there was an IO problem.
     * @throws InterruptedException
     */
    public EncoderQueue(File baseDir, List<Encoder> encoders, Encoded monitor, boolean dryRun) throws IOException, InterruptedException
    {

        _fileSystemCleaner = new FileSystemCleaner();
        try
        {
            _monitor = monitor;
            _encoders = encoders;
            _baseDir = baseDir;
            _dryRun = dryRun;

            dependenciesInstalled(encoders);

            List<File> files = findRawFiles(_baseDir);
            Collections.sort(files);

            if (files.size() > 0)
            {

                for (File file : files)
                {
                    Track track = Track.createTrack(file, baseDir, WAV_EXT);
                    if (track != null)
                    {
                        queue(track);
                    }
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
     * Find any files that require encoding.
     * 
     * @param dir the directory to search from.
     * @return a List of files found.
     */
    private List<File> findRawFiles(File dir)
    {

        List<File> files = new ArrayList<>();

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
                else if (filename.endsWith(EncoderQueue.WAV_EXT))
                {
                    files.add(file);
                }
            }
        }

        return files;
    }


    /**
     * Clean up empty directories.
     * 
     * @param baseDir
     * @param dryRun
     * 
     */
    private void cleanup(File baseDir, boolean dryRun)
    {

        _fileSystemCleaner.cleanup(baseDir, dryRun);
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
     * Queue this track for encoding.
     * 
     * @param track the track to encode.
     */
    public void queue(Track track)
    {

        if (!_dryRun)
        {
            _monitor.monitor(track.getWavFile(), _encoders.size());
        }

        for (Encoder encoder : _encoders)
        {
            encoder.queue(track, _dryRun);
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
            usage();
        }

        int argIndex = 0;
        boolean dryRun = false;
        if (args.length == 3)
        {
            dryRun = "--dry-run".equals(args[argIndex++]);
        }

        File baseDir = new File(args[argIndex++]);
        File props = new File(args[argIndex++]);
        if (!baseDir.canRead() || !baseDir.isDirectory())
        {
            System.err.println("Unable to access " + baseDir + " as a directory");
            usage();
        }
        if (!props.canRead())
        {
            System.err.println("Unable to access " + props);
            usage();
        }

        try
        {
            Encoded monitor = new FileDeletingTrackMonitor();
            List<Encoder> encoders = new EncoderLoader().loadEncoders(props, monitor);
            ExecutorService executor = executeEncoders(encoders);
            EncoderQueue encoderQueue = new EncoderQueue(baseDir, encoders, monitor, dryRun);
            executor.awaitTermination(30, TimeUnit.MINUTES);
            executor.shutdown();

            encoderQueue.cleanup(baseDir, dryRun);
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


    /**
     * Start the encoder threads.
     * 
     * @param encoders
     * @return
     */
    private static ExecutorService executeEncoders(List<Encoder> encoders)
    {
        ExecutorService executor = Executors.newFixedThreadPool(encoders.size());

        for (Encoder encoder : encoders)
        {
            executor.execute(encoder);
        }

        return executor;
    }


    /**
     * 
     */
    private static void usage()
    {
        System.err.println("Usage: Encode [--dry-run] <base dir> <encoder properties>");
        System.exit(-1);
    }
}
