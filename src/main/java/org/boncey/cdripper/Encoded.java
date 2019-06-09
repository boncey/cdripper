package org.boncey.cdripper;

import java.io.File;

/**
 * Mark a file as encoded for cleaning up etc.
 * @author Darren Greaves
 */
public interface Encoded
{
    /**
     * Mark the file as successfully encoded.
     * @param rawFile the file that was encoded.
     */
    void successfullyEncoded(File rawFile);

    void monitor(File wavFile, int size);
}

