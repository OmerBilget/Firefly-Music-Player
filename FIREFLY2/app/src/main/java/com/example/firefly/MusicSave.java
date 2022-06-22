package com.example.firefly;

public class MusicSave {
    private final String fileName;
    private final long ID;

    public MusicSave(String fileName, long ID) {
        this.fileName = fileName;
        this.ID = ID;
    }

    public String getFileName() {
        return fileName;
    }

    public long getID() {
        return ID;
    }
}
