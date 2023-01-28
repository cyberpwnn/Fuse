package com.volmit.fuse.management;

import art.arcane.multiburst.MultiBurst;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JobExecutor {
    private final MultiBurst burst;
    private final List<Runnable> queue;
    private int queued;
    private int completed;
    private boolean draining;

    public JobExecutor() {
        this.queue = new CopyOnWriteArrayList<>();
        burst = MultiBurst.burst;
        queued = 0;
        completed = 0;
        draining = false;
    }

    public double getProgress() {
        if(getQueued() <= 0) {
            return 0;
        }

        return ((double)getCompleted()) / (double) getQueued();
    }

    public int getCompleted() {
        return completed;
    }

    public int getQueued() {
        return queued;
    }

    public JobExecutor queue(Runnable r) {
        queue.add(r);
        queued = queued + 1;

        if(!draining) {
            draining = true;
            burst.lazy(this::drain);
        }

        return this;
    }

    public void drain() {
        draining = true;

        while(!queue.isEmpty()) {
            burst.burst(queue);
        }

        completed = 0;
        queued = 0;
        draining = false;
    }
}
