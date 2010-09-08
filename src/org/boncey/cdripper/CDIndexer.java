package org.boncey.cdripper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Store CD Index files according to their CDDBID.
 * @author Darren Greaves
 * @version $Id: CDIndexer.java,v 1.2 2008-11-14 11:48:58 boncey Exp $
 */
public class CDIndexer
{
    /**
     * Version details.
     */
    public static final String CVSID =
        "$Id: CDIndexer.java,v 1.2 2008-11-14 11:48:58 boncey Exp $";

    /**
     * The CDDB entry in the CDDB file.
     */
    private static final String DISCID_KEY = "DISCID=";

    /**
     * The file name for CDDB info.
     */
    public static final String CDDB_FILE = "audio.cddb";

    /**
     * Public constructor.
     * @param srcDir the directory to read the files from.
     * @param destDir the directory to put the files in to.
     * @throws IOException if there was an IO problem.

     */
    public CDIndexer(File srcDir, File destDir)
        throws IOException
    {
        List<File> files = findFiles(srcDir);
        for (File srcFile : files)
        {
            String cddbId = parseCDDBId(srcFile);
            if (cddbId != null)
            {
                File destFile = new File(destDir, cddbId);
                if (!destFile.exists())
                {
                    storeCDDBFile(srcFile, destFile);
                }
            }
        }
    }

    /**
     * Store the CDDB file to disk.
     * @param srcFile the file to read.
     * @param destFile the file to write.
     * @throws IOException if there was an IO problem.
     */
    private void storeCDDBFile(File srcFile, File destFile)
        throws IOException
    {
        FileInputStream in = new FileInputStream(srcFile);
        FileOutputStream out = new FileOutputStream(destFile);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }

    /**
     * Parse the CDDB id from the CDDB file.
     * @param cddbFile the file to parse.
     * @return the CDDB id or null if none found.
     * @throws IOException if unable to read the cddbFile.
     */
    private String parseCDDBId(File cddbFile)
        throws IOException
    {
        String id = null;

        if (cddbFile.canRead())
        {
            BufferedReader in =
                new BufferedReader(new FileReader(cddbFile));
            String entry = in.readLine();
            while (entry != null)
            {
                if (entry.startsWith(DISCID_KEY))
                {
                    id = entry.substring(DISCID_KEY.length());
                }
                entry = in.readLine();
            }

            in.close();
        }

        return id;
    }

    /**
     * Find any files that require encoding.
     * @param dir the directory to search from.
     * @return a List of files found.
     */
    private List<File> findFiles(File dir)
    {
        List<File> files = new ArrayList<File>();

        File[] fileArray = dir.listFiles();
        if (fileArray != null)
        {
            for (int i = 0; i < fileArray.length; i++)
            {
                File file = fileArray[i];
                String filename = file.getName();
                if (file.isDirectory() && !filename.startsWith("."))
                {
                    files.addAll(findFiles(file));
                }
                else if (filename.equals(CDDB_FILE))
                {
                    files.add(file);
                }
            }
        }

        return files;
    }

    /**
     * Rip and encode the CD.
     * @param args the base dir.
     */
    public static void main(String[] args)
    {

        if (args.length < 2)
        {
            System.err.println("Usage: CDIndexer <source dir> <dest dir>");
            System.exit(-1);
        }

        File srcDir = new File(args[0]);
        if (!srcDir.canRead() || !srcDir.isDirectory())
        {
            System.err.println(
                    "Unable to access " + srcDir + " as a directory");
            System.exit(-1);
        }

        File destDir = new File(args[1]);
        if (!destDir.canRead() || !destDir.isDirectory())
        {
            System.err.println(
                    "Unable to access " + destDir + " as a directory");
            System.exit(-1);
        }

        try
        {
            @SuppressWarnings("unused")
            CDIndexer cdr = new CDIndexer(srcDir, destDir);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
