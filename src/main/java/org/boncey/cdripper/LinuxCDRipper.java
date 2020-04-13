package org.boncey.cdripper;

import org.boncey.cdripper.model.CDInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
public class LinuxCDRipper extends CDRipper
{

    /**
     * The command for getting CD info.
     */
    private static final String CD_INFO_CMD = "cdda2wav -L 0 -J -v titles";

    /**
     * The command for getting CD info.
     */
    private static final String CD_RIP_CMD = "cdparanoia";

    /**
     * The command for ejecting a CD when done.
     */
    private static final String CD_EJECT_CMD = "eject";

    /**
     * The pattern for parsing Album info.
     */
    private static final String ALBUM_PATTERN =
        "Album title: '(.+)'\\s+\\[from (.+)\\]$";

    /**
     * The pattern for parsing Track info.
     */
    private static final String TRACK_PATTERN =
        "^Track\\s+\\d+:\\s\'(.+)'.*";

    /**
     * The pattern for parsing the message indicating multiple matches.
     */
    private static final String MULTI_CHOICE_PATTERN =
        "^\\d+ entries found:";

    /**
     * The pattern for parsing the message asking user to choose between
     * multiple matches.
     */
    private static final String CHOOSE_PATTERN = "^\\d+: ignore$";


    /**
     * Public constructor.
     *
     * @param baseDir the directory to create the CD directory within.
     * @param trackListing
     * @throws IOException          if unable to interact with the external processes.
     * @throws InterruptedException if this thread is interrupted.
     */
    public LinuxCDRipper(File baseDir, List<String> trackListing) throws IOException, InterruptedException
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
     * @param dir the directory to create within.
     * @return a populated CD info object.
     * @throws IOException if unable to interact with the cdda process.
     * @throws InterruptedException if this thread is interrupted.
     */
    @Override
    protected CDInfo getCDInfo(File dir)
            throws IOException, InterruptedException
    {
        boolean matched = false;
        CDInfo cdInfo = null;
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(CD_INFO_CMD, null, dir);
        Pattern albumPattern = Pattern.compile(ALBUM_PATTERN);
        Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
        Pattern choosePattern = Pattern.compile(CHOOSE_PATTERN);
        Pattern multiPattern = Pattern.compile(MULTI_CHOICE_PATTERN);
        List<String> tracks = new ArrayList<>();
        String album = null;
        String artist = null;
        boolean multiple = false;

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(proc.getErrorStream())))
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
                    artist = albumMatcher.group(2);
                    matched = true;
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

        if (matched)
        {
            cdInfo = new CDInfo();

            cdInfo.setAlbum(tidyFilename(album));
            cdInfo.setArtist(tidyFilename(artist));
            cdInfo.setTracks(tracks);
        }

        proc.waitFor();

        return cdInfo;
    }
}