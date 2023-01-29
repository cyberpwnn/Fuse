package com.volmit.fuse.management.data;

import com.volmit.fuse.Fuse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Workspace
{
    private List<Project> projects = new ArrayList<>();

    public void onTick() {
        Fuse.service.getExecutor().getBurst().burst(projects.stream()
            .map((e) -> (Runnable) e::onTick).collect(Collectors.toList()));
    }
}
