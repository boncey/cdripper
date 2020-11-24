package org.boncey.cdripper;

import org.boncey.cdripper.model.CDInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for ripping Audio CDs.
 * Copyright (c) 2000-2005 Darren Greaves.
 * @author Darren Greaves
 * @version $Id: CDRipper.java,v 1.8 2008-11-14 11:48:58 boncey Exp $
 */
public class MacOSRipper extends CDRipper
{

    /**
     * The command for getting CD info.
     */
    private static final String CD_INFO_CMD = "cddb_query -s gnudb.gnudb.org -p 8880 read";


    /**
     * The command for getting CD info.
     */
    private static final String CD_RIP_CMD = "cdparanoia";


    /**
     * The command for ejecting a CD when done.
     */
    private static final String CD_EJECT_CMD = "drutil eject";


    /**
     * The pattern for parsing Album info.
     *
     * E.g.
     *
     * Title: Holy Fire
     */
    private static final String ALBUM_PATTERN = "Title:\\s+(.+)$";


    /**
     * The pattern for parsing Artist info.
     *
     * E.g.
     *
     * Artist: Foals
     */
    private static final String ARTIST_PATTERN = "Artist:\\s+(.+)$";


    /**
     * The pattern for parsing Track info.
     *
     * E.g. [01] 'Prelude' by Foals (4:07)
     */
    private static final String TRACK_PATTERN = "^\\s+\\[\\d+\\] '(.+)' by .+$";


    /**
     * The pattern for counting how many Tracks.
     *
     * E.g. CD contains 14 track(s)
     */
    private static final String TRACK_COUNT_PATTERN = "^CD contains (\\d+) track\\(s\\)$";


    /**
     * The pattern for parsing the message indicating multiple matches.
     */
    private static final String MULTI_PATTERN = "^\\d+ entries found:";


    /**
     * The pattern for parsing the message asking user to choose between multiple matches.
     */
    private static final String MULTI_CHOICE_PATTERN = "^\\d+: ignore$";

    /**
     * Public constructor.
     *
     * @param baseDir the directory to create the CD directory within.
     * @param trackListing
     * @throws IOException          if unable to interact with the external processes.
     * @throws InterruptedException if this thread is interrupted.
     */
    public MacOSRipper(File baseDir, List<String> trackListing)
    {
        super(baseDir, trackListing);
    }

    @Override
    protected String getInfoCommand()
    {
        return CD_INFO_CMD;
    }

    @Override
    protected String getEjectCommand()
    {
        return CD_EJECT_CMD;
    }

    @Override
    protected String getRipCommand()
    {
        return CD_RIP_CMD;
    }

    /**
     * Get the CD info.
     *
     * @param dir the directory to create within.
     * @return a populated CD info object.
     * @throws IOException if unable to interact with the cdda process.
     * @throws InterruptedException if this thread is interrupted.
     */
    @Override
    protected CDInfo getCDInfo(File dir) throws IOException, InterruptedException
    {

        boolean artistMatched = false;
        boolean albumMatched = false;
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(getInfoCommand(), null, dir);
        Pattern albumPattern = Pattern.compile(ALBUM_PATTERN);
        Pattern artistPattern = Pattern.compile(ARTIST_PATTERN);
        Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
        Pattern multiPattern = Pattern.compile(MULTI_PATTERN);
        Pattern choosePattern = Pattern.compile(MULTI_CHOICE_PATTERN);
        Pattern countPattern = Pattern.compile(TRACK_COUNT_PATTERN);
        List<String> tracks = new ArrayList<>();
        String album = null;
        String artist = null;
        boolean multiple = false;
        int trackCount = 0;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream())))
        {
            String line = in.readLine();

            while (line != null)
            {
                Matcher multiMatcher = multiPattern.matcher(line);
                if (multiMatcher.matches())
                {
                    multiple = true;
                }

                Matcher chooseMatcher = choosePattern.matcher(line);
                if (chooseMatcher.matches())
                {
                    String input;
                    try (BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in)))
                    {
                        input = stdin.readLine();
                    }

                    try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())))
                    {
                        out.write(input);
                    }

                    multiple = false;
                }

                Matcher albumMatcher = albumPattern.matcher(line);
                if (albumMatcher.matches())
                {
                    album = albumMatcher.group(1);
                    albumMatched = true;
                }

                Matcher countMatcher = countPattern.matcher(line);
                if (countMatcher.matches())
                {
                    trackCount = Integer.parseInt(countMatcher.group(1));
                }

                Matcher artistMatcher = artistPattern.matcher(line);
                if (artistMatcher.matches())
                {
                    artist = artistMatcher.group(1);
                    artistMatched = true;
                }

                Matcher trackMatcher = trackPattern.matcher(line);
                if (trackMatcher.matches())
                {
                    tracks.add(trackMatcher.group(1));
                }

                if (multiple)
                {
                    System.out.println(line);
                }

                line = in.readLine();
            }
        }

        CDInfo cdInfo;
        if (artistMatched && albumMatched)
        {
            cdInfo = new CDInfo();

            cdInfo.setAlbum(tidyFilename(album));
            cdInfo.setArtist(tidyFilename(artist));
            cdInfo.setTracks(tracks);
        }
        else
        {
            cdInfo = CDInfo.unknown(trackCount);
        }

        proc.waitFor();

        return cdInfo;
    }
}