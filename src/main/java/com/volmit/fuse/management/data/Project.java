package com.volmit.fuse.management.data;

import com.volmit.fuse.Fuse;
import com.volmit.fuse.util.FolderWatcher;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Project {
    private String name;
    private String location;
    private List<String> watchList;
    private String main;
    private transient Map<String, FolderWatcher> _watchers;

    public Project(String location) {
        this.location = location;
        this.watchList = new ArrayList<>();
        watchList.add(new File(location, "src").getAbsolutePath());
        watchList.add(new File(location, "gradle.properties").getAbsolutePath());
        watchList.add(new File(location, "settings.gradle").getAbsolutePath());
        watchList.add(new File(location, "build.gradle").getAbsolutePath());
        _watchers = new HashMap<>();
    }

    public void onTick() {
        boolean modified = updateWatchers();
        modified |= name == null || name.isEmpty();
        modified |= main == null || main.isEmpty();
        modified |= checkWatchers();

        if(modified) {
            build();
        }
    }

    private void build() {
        Fuse.log("Building " + new File(location).getName());
    }

    private boolean checkWatchers()
    {
        boolean modified = false;
        for(FolderWatcher i : _watchers.values()) {
            modified |= i.checkModifiedFast();
        }

        return modified;
    }

    private boolean updateWatchers() {
        List<String> removals = new ArrayList<>();
        List<String> adds = new ArrayList<>();

        for(String i : _watchers.keySet()) {
            if(!watchList.contains(i)) {
                removals.add(i);
            }
        }

        for(String i : watchList) {
            if(!_watchers.containsKey(i)) {
                adds.add(i);
            }
        }

        for(String i : removals) {
            _watchers.remove(i);
        }

        for(String i : adds) {
            _watchers.put(i, new FolderWatcher(new File(i)));
        }

        return !removals.isEmpty() || !adds.isEmpty();
    }
}
