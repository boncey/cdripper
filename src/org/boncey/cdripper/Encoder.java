package org.boncey.cdripper;


/**
 * Interface for encoding a wav file.
 * @author Darren Greaves
 * @version $Id$
 * Copyright (c) 2010 Darren Greaves.
 */
public interface Encoder extends Runnable
{

    /**
     * Queue this track for encoding.
     * @param track the track to encode.
     */
    void queue(Track track);

    /**
     * Shutdown this Encoder.
     */
    void shutdown();

}