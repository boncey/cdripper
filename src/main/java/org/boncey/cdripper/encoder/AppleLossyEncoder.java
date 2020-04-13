package org.boncey.cdripper.encoder;


import org.boncey.cdripper.Encoded;

import java.io.File;

/**
 * For encoding an audio file to Apple Lossless.
 *
 * Copyright (c) 2000-2005 Darren Greaves.
 * 
 * @author Darren Greaves
 * @version $Id: FlacEncoder.java,v 1.6 2008-11-14 11:48:58 boncey Exp $
 */
public class AppleLossyEncoder extends AppleEncoder
{
    /**
     * Public constructor.
     *
     * @param encoded the class to notify once encoding is finished.
     * @param location the location to save the files to.
     */
    public AppleLossyEncoder(Encoded encoded, File location)
    {
        super(encoded, location);
    }

    @Override
    protected String getCodecName()
    {
        return "libfdk_aac";
    }

}
