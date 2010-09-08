package org.boncey.cdripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * For managing a queue of Encoders, flac, ogg etc.
 * Copyright (c) 2005 Darren Greaves.
 * @author Darren Greaves
 * @version $Id: EncoderQueue.java,v 1.5 2008-11-14 11:48:58 boncey Exp $
 */
public class EncoderQueue implements Encoded
{
    /**
     * Version details.
     */
    public static final String CVSID =
        "$Id: EncoderQueue.java,v 1.5 2008-11-14 11:48:58 boncey Exp $";

    /**
     * The List of Tracks to encode.
     */
    private Map<File, Integer> _tracks;

    /**
     * The List of Encoders that can encode.
     */
    private List<Encoder> _encoders;

    /**
     * The base dir to encode from.
     */
    private File _baseDir;

    /**
     * The CDDB info held for each different album.
     */
    private Map<File, CDDBData> _cddbInfoAlbums;


    /**
     * The extension for unencoded files.
     */
    public static final String WAV_EXT = ".wav";

    /**
     * The file name for CDDB info.
     */
    public static final String CDDB_FILE = "audio.cddb";


    /**
     * The key for the encoder class in the properties file.
     */
    private static final String ENCODER_CLASS_KEY = "encoder.class";


    /**
     * The key for the encoder location in the properties file.
     */
    private static final String ENCODER_LOCATION_KEY = "encoder.location";

    /**
     * Public constructor.
     * @param baseDir the base directory to read the raw files from.
     * @param properties the details of the Encoders.
     * @throws IOException if there was an IO problem.
     */
    public EncoderQueue(File baseDir, File properties)
        throws IOException
    {
        _baseDir = baseDir;
        _cddbInfoAlbums = new HashMap<File, CDDBData>();
        _tracks = new HashMap<File, Integer>();
        _encoders = new ArrayList<Encoder>();

        List<File> files = findRawFiles(_baseDir);
        Collections.sort(files);

        if (files.size() > 0)
        {
            initEncoders(properties);

            for (File file : files)
            {
                File parent = file.getParentFile();

                CDDBData cddb = _cddbInfoAlbums.get(parent);
                if (cddb == null)
                {
                    cddb = new CDDBData(new File(parent, CDDB_FILE));
                    _cddbInfoAlbums.put(parent, cddb);
                }

                assert (cddb != null);

                Track track = getTrackInfo(file, cddb);
                queue(track);
            }

            shutdown();
        }
    }


    /**
     * Read the Encoders from the properties file.
     * @param propFile the details of the Encoders.
     * @throws IOException if there was an IO problem.
     *
     */
    private void initEncoders(File propFile)
        throws IOException
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(propFile));

        for (Object entry : properties.keySet())
        {
            String key = String.valueOf(entry);
            String value = properties.getProperty(key);

            if (key.startsWith(ENCODER_CLASS_KEY))
            {
                try
                {
                    int lastDot = key.lastIndexOf('.');
                    if (lastDot == -1)
                    {
                        throw new RuntimeException("Invalid format for entry " + key);
                    }

                    String label = key.substring(lastDot);
                    String locationKey = ENCODER_LOCATION_KEY + label;
                    String location = (String)properties.get(locationKey);
                    if (location == null)
                    {
                        throw new RuntimeException("No value for key " + locationKey);
                    }

                    Class<?> encoderClass = Class.forName(value);
                    Constructor<?> c = encoderClass.getConstructor(Encoded.class, String.class);
                    Encoder encoder = (Encoder)c.newInstance(this, location);
                    Thread thread = new Thread(encoder, label);
                    thread.start();
                    _encoders.add(encoder);

                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
                catch (InstantiationException e)
                {
                    throw new RuntimeException(e);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
                catch (SecurityException e)
                {
                    throw new RuntimeException(e);
                }
                catch (NoSuchMethodException e)
                {
                    throw new RuntimeException(e);
                }
                catch (IllegalArgumentException e)
                {
                    throw new RuntimeException(e);
                }
                catch (InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * Create a Track object from the track File.
     * @param file the track File.
     * @param cddb the CDDBData object.
     * @return the Track object.
     */
    private Track getTrackInfo(File file, CDDBData cddb)
    {
        String trackName = file.getName().replaceFirst(WAV_EXT, "");
        String[] parent = file.getParentFile().getName().split(" -", 2);
        String artist = null;
        String album = null;

        if (parent.length >= 2)
        {
            artist = parent[0].trim();
            album = parent[1].trim();
        }
        else
        {
            System.err.println("Unable to match artist/album from " +
                    file.getParentFile().getName());
        }

        return new Track(file, trackName, artist, album, cddb);
    }

    /**
     * Find any files that require encoding.
     * @param dir the directory to search from.
     * @return a List of files found.
     */
    private List<File> findRawFiles(File dir)
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
                    files.addAll(findRawFiles(file));
                }
                else if (filename.endsWith(WAV_EXT))
                {
                    files.add(file);
                }
            }
        }

        return files;
    }

    /**
     * Queue this track for encoding.
     * @param track the track to encode.
     */
    public void queue(Track track)
    {
        _tracks.put(track.getWavFile(), new Integer(_encoders.size()));

        for (Encoder encoder : _encoders)
        {
            encoder.queue(track);
        }
    }

    /**
     * Shutdown the encoders.
     */
    public void shutdown()
    {
        for (Encoder encoder : _encoders)
        {
            encoder.shutdown();
        }
    }

    /**
     * Mark the file as successfully encoded.
     * <p>Delete if all encodings have finished.
     * @param wavFile the file that was encoded.
     */
    public synchronized void encoded(File wavFile)
    {
        Integer num = _tracks.get(wavFile);
        if (num != null)
        {
            int number = num.intValue();
            number--;

            if (number == 0)
            {
                if (wavFile.delete())
                {
                    System.out.println("Deleted " + wavFile.getName());
                }
                else
                {
                    System.err.println("Unable to delete " + wavFile);
                }
            }
            else
            {
                _tracks.put(wavFile, new Integer(number));
            }
        }
        else
        {
            System.err.println(
                    "Unable to locate " + wavFile + " in tracks map");
        }
    }

    /**
     * Search for unencoded tracks and encode accordingly.
     * @param args the base dir.
     */
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            System.err.println("Usage: Encode <base dir> <encoder properties>");
            System.exit(-1);
        }

        File baseDir = new File(args[0]);
        File props = new File(args[1]);
        if (!baseDir.canRead() || !baseDir.isDirectory())
        {
            System.err.println(
                    "Unable to access " + baseDir + " as a directory");
            System.exit(-1);
        }
        if (!props.canRead())
        {
            System.err.println(
                    "Unable to access " + props);
            System.exit(-1);
        }

        try
        {
            @SuppressWarnings("unused")
            EncoderQueue encode = new EncoderQueue(baseDir, props);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
