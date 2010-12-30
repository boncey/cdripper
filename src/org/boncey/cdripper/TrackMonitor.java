package org.boncey.cdripper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Darren Greaves
 * @version $Id$
 * Copyright (c) 2010 Darren Greaves.
 */
public class TrackMonitor implements Encoded
{

    /**
     * The count of tracks queued, once the count reaches zero the original file will be deleted.
     */
    private Map<File, Integer> _trackCount;


    /**
     * Default constructor.
     */
    public TrackMonitor()
    {
        _trackCount = new HashMap<File, Integer>();
    }


    /**
     * Add a track to the queue we are monitoring.
     * @param wavFile the file to monitor.
     * @param encoderCount the number of encoders, once this reaches zero file will be deleted.
     */
    public synchronized void monitor(File wavFile, int encoderCount)
    {
        _trackCount.put(wavFile, new Integer(encoderCount));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void encoded(File rawFile)
    {
        Integer num = _trackCount.get(rawFile);
        if (num != null)
        {
            int number = num.intValue();
            number--;

            if (number == 0)
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
            else
            {
                _trackCount.put(rawFile, new Integer(number));
            }
        }
        else
        {
            System.err.println(
                    "Unable to locate " + rawFile + " in tracks map");
        }
    }
}
