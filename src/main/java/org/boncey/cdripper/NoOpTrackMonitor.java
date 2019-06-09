package org.boncey.cdripper;

import java.io.File;

/**
 * Monitor to count tracks that have been encoded.
 *
 * @author Darren Greaves
 * @version $Id$ Copyright (c) 2010 Darren Greaves.
 */
public class NoOpTrackMonitor implements Encoded
{

    /**
     * Default constructor.
     */
    public NoOpTrackMonitor()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void successfullyEncoded(File rawFile)
    {
        System.out.println(String.format("File '%s' has been encoded", rawFile));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void monitor(File wavFile, int size)
    {

    }
}
