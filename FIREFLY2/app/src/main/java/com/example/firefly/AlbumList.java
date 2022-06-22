package com.example.firefly;

import android.graphics.Bitmap;

public class AlbumList {
    private final String albumArtist;
    private final long album_ID;
    private final TrackList albumTracks;

    public AlbumList(String albumArtist, long album_ID, TrackList albumTracks) {
        this.albumArtist = albumArtist;
        this.album_ID = album_ID;
        this.albumTracks = albumTracks;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }


    public long getAlbum_ID() {
        return album_ID;
    }

    public TrackList getAlbumTracks() {
        return albumTracks;
    }
}
