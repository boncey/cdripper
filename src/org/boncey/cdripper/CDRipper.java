package org.boncey.cdripper;


import org.boncey.cdripper.model.CDInfo;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for ripping Audio CDs. Copyright (c) 2000-2005 Darren Greaves.
 * 
 * @author Darren Greaves
 * @version $Id: CDRipper.java,v 1.8 2008-11-14 11:48:58 boncey Exp $
 */
public class CDRipper
{

    /**
     * Log4j logger.
     */
    private static Logger _log;


    /**
     * Version details.
     */
    public static final String CVSID = "$Id: CDRipper.java,v 1.8 2008-11-14 11:48:58 boncey Exp $";


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
    private static final String CHOOSE_PATTERN = "^\\d+: ignore$";


    /**
     * The file extension for encoded files.
     */
    private static final String EXT = ".wav";


    /**
     * The temporary directory where we create the CD files.
     */
    private static final String TEMP_DIR = "TempDir";


    /**
     * Public constructor.
     * 
     * @param baseDir the directory to create the CD directory within.
     * @throws IOException if unable to interact with the external processes.
     * @throws InterruptedException if this thread is interrupted.
     */
    public CDRipper(File baseDir) throws IOException, InterruptedException
    {

        File tmpDir = new File(baseDir, TEMP_DIR);
        boolean exists = tmpDir.exists() && !tmpDir.delete();

        if (!exists)
        {
            tmpDir.mkdir();

            CDInfo cdInfo = getCDInfo(tmpDir);
            File dir;

            if (cdInfo != null)
            {
                System.out.println(String.format("%s by %s", cdInfo.getAlbum(), cdInfo.getArtist()));
                dir = new File(baseDir, cdInfo.getDir());
                rip(cdInfo, tmpDir);

                dir.mkdir();
                tmpDir.renameTo(dir);
            }
            else
            {
                fail("Unable to recognise disk; aborting");
            }
        }
        else
        {
            fail(String.format("%s exists; clean up required", tmpDir));
        }
    }


    /**
     * Fail with an error message and eject the CD.
     * 
     * @param message
     * @throws IOException
     */
    private void fail(String message) throws IOException
    {

        System.err.println(message);

        // TODO Does it make sense to abort when it fails?
        // Runtime rt = Runtime.getRuntime();
        // rt.exec(CD_EJECT_CMD);
    }


    /**
     * Get the CD info.
     * 
     * @param dir the directory to create within.
     * @return a populated CD info object.
     * @throws IOException if unable to interact with the cdda process.
     * @throws InterruptedException if this thread is interrupted.
     */
    private CDInfo getCDInfo(File dir) throws IOException, InterruptedException
    {

        boolean artistMatched = false;
        boolean albumMatched = false;
        CDInfo cdInfo = null;
        Runtime rt = Runtime.getRuntime();
        _log.debug("Execing " + CD_INFO_CMD + " " + dir);
        Process proc = rt.exec(CD_INFO_CMD, null, dir);
        Pattern albumPattern = Pattern.compile(ALBUM_PATTERN);
        Pattern artistPattern = Pattern.compile(ARTIST_PATTERN);
        Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
        Pattern multiPattern = Pattern.compile(MULTI_PATTERN);
        Pattern choosePattern = Pattern.compile(CHOOSE_PATTERN);
        List<String> tracks = new ArrayList<String>();
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


    /**
     * Rip the tracks from the CD.
     * 
     * @param cdInfo the CD info.
     * @param baseDir the base directory to rip and encode within.
     * @throws IOException if unable to interact with the cdparanoia process.
     * @throws InterruptedException if this thread is interrupted.
     */
    private void rip(CDInfo cdInfo, File baseDir) throws IOException, InterruptedException
    {

        Runtime rt = Runtime.getRuntime();
        int index = 1;
        for (Iterator<String> i = cdInfo.getTracks().iterator(); i.hasNext(); index++)
        {
            String trackName = i.next();
            String indexStr = ((index < 10) ? "0" : "") + index;
            String filename = tidyFilename(indexStr + " - " + trackName + EXT);
            File wavFile = new File(baseDir, filename);
            File tempFile = File.createTempFile("wav", null, baseDir);
            System.out.println(String.format("Ripping %s (%s)", tempFile.getName(), wavFile.getName()));
            String[] args =
            {
                    CD_RIP_CMD, "--quiet", String.valueOf(index), tempFile.getAbsolutePath()
            };
            Process proc = rt.exec(args);
            proc.waitFor();

            if (proc.exitValue() != 0)
            {
                System.err.println("Unable to rip " + tempFile);
            }
            else
            {
                if (!tempFile.renameTo(wavFile))
                {
                    System.err.println("Unable to rename " + tempFile.getName() + " to " + wavFile.getName());
                }
            }
        }

        rt.exec(CD_EJECT_CMD);
    }


    /**
     * Strip characters that can't be used in a filename.
     * 
     * @param filename the filename to tidy.
     * @return the tidied filename.
     */
    private String tidyFilename(String filename)
    {

        String ret;

        Pattern bad = Pattern.compile("[\\:*?\"`<>|]");
        Matcher badMatcher = bad.matcher(filename);
        ret = badMatcher.replaceAll("");

        Pattern slash = Pattern.compile("/");
        Matcher slashMatcher = slash.matcher(ret);
        ret = slashMatcher.replaceAll("-");

        return ret;
    }


    /**
     * Rip and encode the CD.
     * 
     * @param args the base dir.
     */
    public static void main(String[] args)
    {

        configureLogging();

        if (args.length < 1)
        {
            System.err.println("Usage: CDRipper <base dir>");
            System.exit(-1);
        }

        File baseDir = new File(args[0]);
        if (!baseDir.canRead() || !baseDir.isDirectory())
        {
            System.err.println("Unable to access " + baseDir + " as a directory");
            System.exit(-1);
        }

        try
        {
            @SuppressWarnings("unused")
            CDRipper cdr = new CDRipper(baseDir);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Configure log4j before we use the logger in this class.
     */
    private static void configureLogging()
    {

        PatternLayout layout = new PatternLayout("%m%n");
        Appender app = new ConsoleAppender(layout);
        BasicConfigurator.configure(app);

        _log = Logger.getLogger(CDRipper.class);
    }

}
