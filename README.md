# CDRipper

## Java project to rip and encode CDs

It contains two command line programs.

One to rip a music CD, pulling music info from CDDB.

The other encodes the CD to a variety of formats (currently flac, Apple Lossless, mp3 and ogg). Encoding is done in parallel to speed things up.

All ripping and encoding is done by executing binaries:

### Linux

binaries used: cdparanoia cdda2wav flac lame oggenc ffmpeg


### MacOS
Setting up the tools (assumes homebrew usage).

    brew install cdrtools cdparanoia libcddb libcdio flac lame ffmpeg

