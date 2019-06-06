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

public class MacOSRipper extends CDRipper
{

    /**
     * The command for getting CD info.
     */
    private static final String CD_INFO_CMD = "cddb_query read";


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
     * @throws IOException          if unable to interact with the external processes.
     * @throws InterruptedException if this thread is interrupted.
     */
    public MacOSRipper(File baseDir) throws IOException, InterruptedException
    {
        super(baseDir);
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
        CDInfo cdInfo = null;
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(getInfoCommand(), null, dir);
        Pattern albumPattern = Pattern.compile(ALBUM_PATTERN);
        Pattern artistPattern = Pattern.compile(ARTIST_PATTERN);
        Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
        Pattern multiPattern = Pattern.compile(MULTI_PATTERN);
        Pattern choosePattern = Pattern.compile(MULTI_CHOICE_PATTERN);
        List<String> tracks = new ArrayList<>();
        String album = null;
        String artist = null;
        boolean multiple = false;

        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
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
                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                String input = stdin.readLine();

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
                out.write(input);
                out.close();

                multiple = false;
            }

            Matcher albumMatcher = albumPattern.matcher(line);
            if (albumMatcher.matches())
            {
                album = albumMatcher.group(1);
                albumMatched = true;
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
        in.close();

        if (artistMatched && albumMatched)
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
