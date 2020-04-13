# CDRipper - Java project to rip and encode CDs

## Setup

It contains two command line programs.

One to rip a music CD, pulling music info from CDDB.

The other encodes the CD to a variety of formats (currently FLAC, Apple Lossless, mp3 and ogg). Encoding is done in parallel to speed things up.

All ripping and encoding is done by executing binaries:

### Linux

binaries used: 

    cdparanoia cdda2wav
 
Optional depending on which encoders you require.

    flac lame oggenc ffmpeg


### MacOS
Setting up the tools (assumes homebrew usage).

    brew install cdrtools cdparanoia libcddb libcdio

Optional depending on which encoders you require.

    brew install flac lame ffmpeg


## Usage

### Ripping a CD

See `cdripper` shell script in `/src/main/bash` as an example shell script to exec the Java process.

    cdripper ~/Music/ripped

Ripping an unrecognised CD (details not in CDDB) - see src/main/resources/example-track-listing.txt for format.

    cdripper ~/Music/ripped tracklisting.txt



### Encoding a ripped CD

See `encoder` shell script in `/src/main/bash` as an example shell script to exec the Java process.

    encoder ~/Music/wav my-encoder.properties