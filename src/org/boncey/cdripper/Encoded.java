package org.boncey.cdripper;

import java.io.File;

/**
 * Mark a file as encoded for cleaning up etc.
 * @author Darren Greaves
 * @version $Id: Encoded.java,v 1.1 2005/05/08 13:59:08 boncey Exp $
 */
public interface Encoded
{
    /**
     * Version details.
     */
    public static final String CVSID =
        "$Id: Encoded.java,v 1.1 2005/05/08 13:59:08 boncey Exp $";

    /** 
     * Mark the file as successfully encoded.
     * @param rawFile the file that was encoded.
     */
    public void encoded(File rawFile);
}

