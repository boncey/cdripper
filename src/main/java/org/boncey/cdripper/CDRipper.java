package org.boncey.cdripper;

import org.boncey.cdripper.model.CDInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
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
public abstract class CDRipper
{

    /**
     * The file extension for encoded files.
     */
    private static final String EXT = ".wav";


    /**
     * The temporary directory where we create the CD files.
     */
    private static final String TEMP_DIR = "TempDir";

    private final File _baseDir;

    private final List<String> _trackListing;

    public CDRipper(File baseDir, List<String> trackListing)
    {
        _baseDir = baseDir;
        _trackListing = trackListing;
    }

    /**
     * Rip the CD.
     *
     * @throws IOException          if unable to interact with the external processes.
     * @throws InterruptedException if this thread is interrupted.
     */
    public void start() throws IOException, InterruptedException
    {
        File tmpDir = new File(_baseDir, TEMP_DIR);
        boolean exists = tmpDir.exists() && !tmpDir.delete();

        if (!exists)
        {
            tmpDir.mkdir();

            CDInfo cdInfo = getCDInfo(tmpDir);
            File dir;

            if (!cdInfo.recognised() && !_trackListing.isEmpty())
            {
                cdInfo.fromTrackListing(_trackListing);
            }
            else if (!cdInfo.recognised())
            {
                fail("Unable to recognise disk - provide a track listing file; aborting");
            }

            System.out.println(String.format("%s by %s", cdInfo.getAlbum(), cdInfo.getArtist()));
            dir = new File(_baseDir, cdInfo.getDir());
            rip(cdInfo, tmpDir);

            dir.mkdir();
            tmpDir.renameTo(dir);
        }
        else
        {
            fail(String.format("%s exists; clean up required", tmpDir));
        }
    }


    /**
     * Fail with an error message.
     *
     * @param message
     * @throws IOException
     */
    private void fail(String message)
    {
        System.err.println(message);
        System.exit(-1);
    }


    /**
     * Rip the tracks from the CD.
     *
     * @param cdInfo  the CD info.
     * @param baseDir the base directory to rip and encode within.
     * @throws IOException          if unable to interact with the cdparanoia process.
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
            String[] args = {getRipCommand(), "--quiet", String.valueOf(index), tempFile.getAbsolutePath()};
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

        rt.exec(getEjectCommand());
    }

    /**
     * Strip characters that can't be used in a filename.
     *
     * @param filename the filename to tidy.
     * @return the tidied filename.
     */
    protected String tidyFilename(String filename)
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

    protected abstract String getInfoCommand();

    protected abstract String getEjectCommand();

    protected abstract String getRipCommand();

    protected abstract CDInfo getCDInfo(File dir)
            throws IOException, InterruptedException;

    /**
     * Rip and encode the CD.
     *
     * @param args the base dir.
     */
    public static void main(String[] args) throws Exception
    {

        if (args.length < 1)
        {
            System.err.println("Usage: CDRipper <base dir> [track names text file]");
            System.exit(-1);
        }

        File baseDir = new File(args[0]);
        if (!baseDir.canRead() || !baseDir.isDirectory())
        {
            System.err.printf("Unable to access %s as a directory%n", baseDir);
            System.exit(-1);
        }

        List<String> trackListing = Collections.EMPTY_LIST;
        if (args.length > 1)
        {
            trackListing = Files.readAllLines(Paths.get(args[1]));
        }

        try
        {
            // TODO Select based on OS
            CDRipper cdr = new MacOSRipper(baseDir, trackListing);
            cdr.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
