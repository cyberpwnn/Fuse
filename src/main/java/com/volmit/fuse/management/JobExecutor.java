package com.volmit.fuse.management;

import art.arcane.multiburst.MultiBurst;
import com.volmit.fuse.Fuse;
import com.volmit.fuse.util.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class JobExecutor {
    private final MultiBurst burst;
    private final List<Runnable> queue;
    private int queued;
    private int completed;
    private boolean draining;
    private Looper progressUpdater;
    private final List<Runnable> after;

    public JobExecutor() {
        this.queue = new CopyOnWriteArrayList<>();
        this.after = new CopyOnWriteArrayList<>();
        burst = new MultiBurst("Fuse", 1);
        queued = 0;
        completed = 0;
        draining = false;
    }

    public double getProgress() {
        if(getQueued() <= 0) {
            return 0;
        }

        return (double)((int)((((double)getCompleted()) / (double) getQueued()) * 1000))/1000.0;
    }

    public int getCompleted() {
        return completed;
    }

    public int getQueued() {
        return queued;
    }

    public JobExecutor after(Runnable r) {
        after.add(r);

        return this;
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
        Fuse.log("Work Started");
        tickProgress();
        progressUpdater = new Looper() {
            @Override
            protected long loop() {
                if(System.currentTimeMillis() - Fuse.ll > 1000) {
                    tickProgress();
                    return 500;
                }

                return 1000;
            }
        };
        progressUpdater.start();

        while(!queue.isEmpty()) {
            List<Runnable> q = new ArrayList<>(queue).stream().map((i) -> (Runnable) () -> {
                    i.run();
                    completed = completed + 1;
                }
            ).collect(Collectors.toList());
            queue.clear();
            burst.burst(q);
        }

        progressUpdater.interrupt();
        tickProgress();
        int cc = getCompleted();

        MultiBurst.burst.lazy(() -> {
            try {
                Thread.sleep(3000);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
            Fuse.log("Work Completed. " + getCompleted() + " jobs completed.");
        });

        completed = 0;
        queued = 0;
        draining = false;

        if(!after.isEmpty()) {
            List<Runnable> a = new ArrayList<>(after);
            after.clear();
            a.forEach(Runnable::run);
        }
    }

    private void tickProgress() {
        Fuse.log("Working: " + getProgress() * 100 + "% (" + getCompleted() + "/" + getQueued() + ")");
    }
}
