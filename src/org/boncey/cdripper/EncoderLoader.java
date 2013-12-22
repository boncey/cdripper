package org.boncey.cdripper;


import org.boncey.cdripper.encoder.AbstractEncoder;
import org.boncey.cdripper.encoder.Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Load the {@link Encoder}s from the properties file.
 * 
 * @author Darren Greaves
 * @version $Id$ Copyright (c) 2010 Darren Greaves.
 */
public class EncoderLoader
{

    /**
     * The key for the encoder class in the properties file.
     */
    private static final String ENCODER_CLASS_KEY = "encoder.class";


    /**
     * The key for the encoder location in the properties file.
     */
    private static final String ENCODER_LOCATION_KEY = "encoder.location";


    /**
     * Read the Encoders from the properties file.
     * 
     * @param propFile the details of the Encoders.
     * @param encoded the {@link Encoded} implementation to track files being encoded.
     * @return the Set of {@link Encoder}s.
     * @throws IOException if there was an IO problem.
     * 
     */
    public List<Encoder> loadEncoders(File propFile, Encoded encoded) throws IOException
    {

        Properties properties = new Properties();
        properties.load(new FileInputStream(propFile));

        List<Encoder> encoders = new ArrayList<Encoder>();
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
                    File location = new File((String)properties.get(locationKey));
                    if (location == null)
                    {
                        throw new RuntimeException("No value for key " + locationKey);
                    }
                    if (!location.exists() || !location.isDirectory())
                    {
                        throw new RuntimeException("Directory not found: " + locationKey);
                    }

                    Class<?> encoderClass = Class.forName(value);
                    Constructor<?> c = encoderClass.getConstructor(Encoded.class, File.class);
                    Encoder encoder = (AbstractEncoder)c.newInstance(encoded, location);

                    encoders.add(encoder);
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

        return encoders;
    }
}
