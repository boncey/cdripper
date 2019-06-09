package org.boncey.cdripper.encoder;

import org.boncey.cdripper.Encoded;
import org.boncey.cdripper.model.Track;

import java.io.File;
import java.io.IOException;

public abstract class AppleEncoder extends AbstractEncoder
{
    /**
     * The encode command.
     */
    private static final String CMD = "ffmpeg";

    /**
     * The file extension for encoded files.
     */
    private static final String EXT = ".m4a";

    /**
     * Public constructor.
     *
     * @param encoded  the class to notify once encoding is finished.
     * @param location the location to save the files to.
     */
    protected AppleEncoder(Encoded encoded, File location)
    {
        super(encoded, location);
    }

    /**
     * Get the file extension for encoded files.
     *
     * @return the file extension.
     */
    @Override
    protected String getExt()
    {

        return EXT;
    }

    /**
     * Get the command to encode.
     *
     * @param track the track to encode.
     * @param encodedFilename the filename to encode to.
     * @param wavFile the file to encode from.
     * @return the command to encode.
     */
    @Override
    protected String[] getEncodeCommand(Track track, String encodedFilename, String wavFile)
    {
        String[] args =
        {
                CMD, "-y", "-loglevel", "warning", "-ac", "2", "-i", wavFile, "-metadata", "title=" + track.getTrackName(), "-metadata", "album=" + track.getAlbum(), "-metadata",
                "artist=" + track.getArtist(), "-metadata", "track=" + track.getTrackNum(), "-c:a", getCodecName(), encodedFilename
        };

        return args;
    }

    protected abstract String getCodecName();

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public boolean dependenciesInstalled() throws IOException, InterruptedException
    {
        return exec(new String[]
        {
                CMD, "-version"
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String command()
    {
        return CMD;
    }

    @Override
    protected String getTempFileSuffix()
    {
        return ".mp4";
    }

}
