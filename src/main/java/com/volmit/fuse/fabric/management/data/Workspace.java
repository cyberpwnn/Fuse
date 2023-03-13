package com.volmit.fuse.fabric.management.data;

import com.volmit.fuse.fabric.Fuse;
import com.volmit.fuse.fabric.util.GlfwUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Workspace {
    private List<Project> projects = new ArrayList<>();
    private List<String> onJoin = new ArrayList<>();

    public void onTick() {
        if (!GlfwUtils.isWindowFocused()) {
            return;
        }

        Fuse.service.getExecutor().getBurst().burst(projects.stream()
                .map((e) -> (Runnable) e::onTick).collect(Collectors.toList()));
    }
}
