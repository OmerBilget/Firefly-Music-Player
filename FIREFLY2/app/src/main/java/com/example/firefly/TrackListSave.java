package com.example.firefly;

import java.util.ArrayList;
import java.util.List;

public class TrackListSave {
    private String listName;
    private List<MusicSave> list;

    public TrackListSave(String listName) {
        this.listName = listName;
        this.list=new ArrayList<>();
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public List<MusicSave> getList() {
        return list;
    }

    public void setList(List<MusicSave> list) {
        this.list = list;
    }
}
