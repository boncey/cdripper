package org.boncey.cdripper.encoder;


import org.boncey.cdripper.model.Track;

import java.io.IOException;

/**
 * Interface for encoding a wav file.
 * 
 * @author Darren Greaves
 * @version $Id$ Copyright (c) 2010 Darren Greaves.
 */
public interface Encoder extends Runnable
{

    /**
     * Queue this track for encoding.
     * 
     * @param track the track to encode.
     */
    void queue(Track track);


    /**
     * Shutdown this Encoder.
     */
    void shutdown();


    /**
     * Are the {@link Encoder}s dependencies installed?
     * 
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    boolean dependenciesInstalled() throws IOException, InterruptedException;


    /**
     * Get the command name.
     * 
     * @return
     */
    String command();
}