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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for ripping Audio CDs.
 * Copyright (c) 2000-2005 Darren Greaves.
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
    public static final String CVSID =
        "$Id: CDRipper.java,v 1.8 2008-11-14 11:48:58 boncey Exp $";

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
    private static final String MULTI_PATTERN =
        "^\\d+ entries found:";

    /**
     * The pattern for parsing the message asking user to choose between
     * multiple matches.
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
     * @param baseDir the directory to create the CD directory within.
     * @throws IOException if unable to interact with the external processes.
     * @throws InterruptedException if this thread is interrupted.
     */
    public CDRipper(File baseDir)
        throws IOException, InterruptedException
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
                System.out.println(cdInfo.getAlbum() + " by " +
                             cdInfo.getArtist());
                dir = new File(baseDir, cdInfo.getDir());
            }
            else
            {
                cdInfo = new CDInfo();

                cdInfo.setAlbum("Unknown Album");
                cdInfo.setArtist("Unknown Artist");

                dir = new File(baseDir, cdInfo.getDir());
            }

            if (!dir.exists())
            {
                if (cdInfo.getTracks() == null)
                {
                    List<String> tracks = new ArrayList<String>();
                    int trackCount = countInfFiles(tmpDir);
                    for (int i = 0; i < trackCount; i++)
                    {
                        tracks.add("Unknown track");
                    }

                    cdInfo.setTracks(tracks);
                }

                rip(cdInfo, tmpDir);

                dir.mkdir();
                tmpDir.renameTo(dir);
            }
            else
            {
                System.err.println(dir + " exists");
                Runtime rt = Runtime.getRuntime();
                rt.exec(CD_EJECT_CMD);
            }
        }
        else
        {
            System.err.println(tmpDir + " exists; clean up required");
        }
    }

    /**
     * Return a count of .inf files in the given directory.
     * @param dir the dir to count from within.
     * @return a count of .inf files.
     */
    private int countInfFiles(File dir)
    {
        File[] files = dir.listFiles(new FilenameFilter(){
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".inf");
                    }
                });

        return (files != null) ? files.length : 0;
    }

    /**
     * Get the CD info.
     * @param dir the directory to create within.
     * @return a populated CD info object.
     * @throws IOException if unable to interact with the cdda process.
     * @throws InterruptedException if this thread is interrupted.
     */
    private CDInfo getCDInfo(File dir)
        throws IOException, InterruptedException
    {
        boolean matched = false;
        CDInfo cdInfo = null;
        Runtime rt = Runtime.getRuntime();
        _log.debug("Execing " + CD_INFO_CMD + " " + dir);
        Process proc = rt.exec(CD_INFO_CMD, null, dir);
        Pattern albumPattern = Pattern.compile(ALBUM_PATTERN);
        Pattern trackPattern = Pattern.compile(TRACK_PATTERN);
        Pattern multiPattern = Pattern.compile(MULTI_PATTERN);
        Pattern choosePattern = Pattern.compile(CHOOSE_PATTERN);
        List<String> tracks = new ArrayList<String>();
        String album = null;
        String artist = null;
        boolean multiple = false;

        BufferedReader in = new BufferedReader(
                new InputStreamReader(proc.getErrorStream()));
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
                BufferedReader stdin = new BufferedReader(
                        new InputStreamReader(System.in));
                String input = stdin.readLine();

                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(proc.getOutputStream()));
                out.write(input);
                out.close();

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
        in.close();

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

    /**
     * Rip the tracks from the CD.
     * @param cdInfo the CD info.
     * @param baseDir the base directory to rip and encode within.
     * @throws IOException if unable to interact with the cdparanoia process.
     * @throws InterruptedException if this thread is interrupted.
     */
    private void rip(CDInfo cdInfo, File baseDir)
        throws IOException, InterruptedException
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
            System.out.println("Ripping " + tempFile.getName() +
                    " (" + wavFile.getName() + ")");
            String[] args = {CD_RIP_CMD,
                             "--quiet",
                             String.valueOf(index),
                             tempFile.getAbsolutePath()};
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
                    System.err.println("Unable to rename " +
                            tempFile.getName() + " to " + wavFile.getName());
                }
            }
        }

        rt.exec(CD_EJECT_CMD);
    }

    /**
     * Strip characters that can't be used in a filename.
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
            System.err.println(
                    "Unable to access " + baseDir + " as a directory");
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
        PatternLayout layout = new PatternLayout(
                "%m%n");
        Appender app = new ConsoleAppender(layout);
        BasicConfigurator.configure(app);

        _log = Logger.getLogger(CDRipper.class);
    }

}
