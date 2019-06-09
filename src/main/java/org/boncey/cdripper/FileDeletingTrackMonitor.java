package org.boncey.cdripper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monitor to count tracks that have been encoded.
 *
 * @author Darren Greaves
 * @version $Id$ Copyright (c) 2010 Darren Greaves.
 */
public class FileDeletingTrackMonitor implements Encoded
{

    /**
     * The count of tracks queued, once the count reaches zero the original file can be deleted.
     */
    private final Map<File, AtomicInteger> _trackCount;

    /**
     * Default constructor.
     */
    public FileDeletingTrackMonitor()
    {
        _trackCount = new HashMap<>();
    }

    /**
     * Add a track to the queue we are monitoring.
     *
     * @param wavFile      the file to monitor.
     * @param encoderCount the number of encoders, once this reaches zero file will be deleted.
     */
    public synchronized void monitor(File wavFile, int encoderCount)
    {
        _trackCount.put(wavFile, new AtomicInteger(encoderCount));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void successfullyEncoded(File rawFile)
    {
        AtomicInteger num = _trackCount.get(rawFile);
        if (num != null)
        {
            num.decrementAndGet();

            if (num.intValue() == 0)
            {
                boolean deleted = rawFile.delete();
                if (deleted)
                {
                    System.out.println("Deleted " + rawFile.getName());
                }
                else
                {
                    System.err.println("Unable to delete " + rawFile);
                }
            }
        }
        else
        {
            System.err.println(String.format("Unable to locate '%s' in tracks map", rawFile));
        }
    }
}
