package org.boncey.cdripper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Year and Genre info parsed from CDDB data.
 * Copyright (c) 2005 Darren Greaves.
 * @author Darren Greaves
 * @version $Id: CDDBData.java,v 1.2 2008-11-14 11:48:58 boncey Exp $
 */
public class CDDBData
{
    /**
     * Version details.
     */
    public static final String CVSID =
        "$Id: CDDBData.java,v 1.2 2008-11-14 11:48:58 boncey Exp $";

    /** 
     * The CDDB entry in the CDDB file.
     */
    private static final String DISCID_KEY = "DISCID=";

    /** 
     * The CDDB entry in the CDDB file.
     */
    private static final String DYEAR = "DYEAR=";

    /** 
     * The CDDB entry in the CDDB file.
     */
    private static final String DGENRE = "DGENRE=";

    /**
     * The cddb id of the playlist.
     */
    private String _cddbId;

    /** 
     * The year of the album.
     */
    private String _year;

    /** 
     * The genre of the album.
     */
    private String _genre;

    /**
     * Public constructor.
     * @param cddbFile the file to parse.
     * @throws IOException if there was an IO problem.
     */
    public CDDBData(File cddbFile)
        throws IOException
    {
        parseCDDBInfo(cddbFile);
    }

    /**
     * Get the year of the playlist.
     * @return The year of the playlist.
     */
    public String getYear()
    {
        return _year;
    }

    /**
     * Get the genre of the playlist.
     * @return The genre of the playlist.
     */
    public String getGenre()
    {
        return _genre;
    }

    /**
     * Get the cddb id of the playlist.
     * @return The cddb id of the playlist.
     */
    public String getCddbId()
    {
        return _cddbId;
    }

    /** 
     * Parse the CDDB id from the CDDB file.
     * @param cddbFile the file to parse.
     * @throws IOException if unable to read the cddbFile.
     */
    private void parseCDDBInfo(File cddbFile)
        throws IOException
    {
        if (cddbFile.canRead())
        {
            BufferedReader in =
                new BufferedReader(new FileReader(cddbFile));
            String entry = in.readLine();
            while (entry != null)
            {
                if (entry.startsWith(DISCID_KEY))
                {
                    _cddbId = entry.substring(DISCID_KEY.length());
                }
                if (entry.startsWith(DYEAR))
                {
                    _year = entry.substring(DYEAR.length());
                }
                if (entry.startsWith(DGENRE))
                {
                    _genre = entry.substring(DGENRE.length());
                }
                entry = in.readLine();
            }
            in.close();
        }
    }

    /**
     * Return a String representing this object.
     * @return a String representing this object.
     */
    @Override
    public String toString()
    {
        return "cddbId = " + _cddbId;
    }
    
}
