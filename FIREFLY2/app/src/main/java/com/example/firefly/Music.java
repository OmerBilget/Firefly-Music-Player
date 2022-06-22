package com.example.firefly;

import android.graphics.Bitmap;
import android.net.Uri;

public class Music {
    private final long id;
    private final String trackName;
    private final String artistName;
    private final String albumName;
    private final String trackDuration;
    private final String trackDurationMilliseconds;
    private final String fullPath;
    private final String fileName;
    private final long album_id;
    private final Uri uri;

    public Music(long id, String trackName, String artistName, String albumName, String trackDuration,String trackDurationMilliseconds, String fullPath, String fileName,long album_id, Uri uri) {
        this.id = id;
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumName = albumName;
        this.trackDuration = trackDuration;
        this.trackDurationMilliseconds=trackDurationMilliseconds;
        this.fullPath = fullPath;
        this.fileName = fileName;
        this.album_id=album_id;
        this.uri = uri;

    }

    public String getTrackDurationMilliseconds() {
        return trackDurationMilliseconds;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public long getId() {
        return id;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getTrackDuration() {
        return trackDuration;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getFileName() {
        return fileName;
    }

    public Uri getUri() {
        return uri;
    }



}
