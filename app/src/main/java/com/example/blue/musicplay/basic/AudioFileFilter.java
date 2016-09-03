package com.example.blue.musicplay.basic;

import java.io.File;
import java.io.FileFilter;

public class AudioFileFilter implements FileFilter {

    protected static final String TAG = "AudioFileFilter";
    /**
     * allows Directories
     */
    private final boolean allowDirectories;

    public AudioFileFilter(boolean allowDirectories) {
        this.allowDirectories = allowDirectories;
    }

    public AudioFileFilter() {
        this(true);
    }

    @Override
    public boolean accept(File f) {
        if ( f.isHidden() || !f.canRead() ) {
            return false;
        }

        if ( f.isDirectory() ) {
            return allowDirectories;
        }
        String ext = getFileExtension(f);
        if ( ext == null) return false;
        try {
            if ( SupportedFileFormat.valueOf(ext.toUpperCase()) != null ) {
                return true;
            }
        } catch(IllegalArgumentException e) {
            //Not known enum value
            return false;
        }
        return false;
    }

    public String getFileExtension( File f ) {
        int i = f.getName().lastIndexOf('.');
        if (i > 0) {
            return f.getName().substring(i+1);
        } else
            return null;
    }

    /**
     * Files formats currently supported by Library
     */
    public enum SupportedFileFormat
    {
        _3GP("3gp"),
        MP4("mp4"),
        M4A("m4a"),
        AAC("aac"),
        TS("ts"),
        FLAC("flac"),
        MP3("mp3"),
        MID("mid"),
        XMF("xmf"),
        MXMF("mxmf"),
        RTTTL("rtttl"),
        RTX("rtx"),
        OTA("ota"),
        IMY("imy"),
        OGG("ogg"),
        MKV("mkv"),
        WAV("wav");
        private String filesuffix;

        SupportedFileFormat( String filesuffix ) {
            this.filesuffix = filesuffix;
        }

        public String getFilesuffix() {
            return filesuffix;
        }
    }

}