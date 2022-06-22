package com.example.firefly;

import java.util.ArrayList;
import java.util.List;

public class TrackList {
    private String name;
    private List<Music> tracks;

    public TrackList(String name) {
        this.name = name;
        tracks=new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Music> getTracks() {
        return tracks;
    }

    public void setTracks(List<Music> tracks) {
        this.tracks = tracks;
    }
}
