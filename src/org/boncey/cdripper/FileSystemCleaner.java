package org.boncey.cdripper;


import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * For cleaning up empty directories after files have been encoded.
 * 
 * @author Darren Greaves
 * @version $Id$ Copyright (c) 2011 Darren Greaves.
 */
public class FileSystemCleaner
{

    /**
     * Logger for log4j.
     */
    @SuppressWarnings("unused")
    private static Logger _log = Logger.getLogger(FileSystemCleaner.class);


    /**
     * Delete empty directories recursively.
     * 
     * @param baseDir
     * @param dryRun
     */
    public void cleanup(File baseDir, boolean dryRun)
    {

        // Run twice to remove the two expected levels of directories.
        cleanup(baseDir, baseDir, dryRun);
    }


    /**
     * Delete empty directories recursively.
     * 
     * @param baseDir
     * @param dir
     * @param dryRun
     */
    private void cleanup(File baseDir, File dir, boolean dryRun)
    {

        List<File> subdirs = new ArrayList<File>(Arrays.asList(dir.listFiles(new DirectoryFilter())));
        if (subdirs.isEmpty())
        {
            deleteEmptyDirectory(baseDir, dir, dryRun);
        }
        else
        {

            List<File> deepSubdirs = new ArrayList<File>();
            for (File subdir : subdirs)
            {
                cleanup(baseDir, subdir, dryRun);
                deleteEmptyDirectory(baseDir, dir, dryRun);
            }
            subdirs.addAll(deepSubdirs);
        }

    }


    /**
     * Delete a Directory if it's empty.
     * 
     * @param baseDir
     * @param dir
     * @param dryRun
     */
    private void deleteEmptyDirectory(File baseDir, File dir, boolean dryRun)
    {

        if (!dir.equals(baseDir))
        {
            if (dir.list().length == 0)
            {
                System.out.println(String.format("Deleting %s on cleanup", dir));
                if (!dryRun)
                {
                    if (!dir.delete())
                    {
                        System.err.println(String.format("Unable to delete %s on cleanup", dir));
                    }
                }
            }
            else
            {
                System.out.println(String.format("Not deleting %s as it's not empty", dir));
            }
        }
    }


    /**
     * {@link FileFilter} for directories.
     * 
     * @author Darren Greaves
     * @version $Id$ Copyright (c) 2011 Darren Greaves.
     */
    private final class DirectoryFilter implements FileFilter
    {
        /**
         * {@inheritDoc}
         */
        public boolean accept(File f)
        {

            return f.isDirectory();
        }
    }

}
