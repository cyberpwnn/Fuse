package com.volmit.fuse.management;

import com.volmit.fuse.Fuse;
import com.volmit.fuse.util.ProcessRelogger;
import lombok.Builder;
import lombok.Singular;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
public class JarExecutor extends Thread {
    private final File jar;
    private final File directory;

    @Singular
    private final List<String> args;

    public JarExecutor execute(){
        start();
        try {
            join();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public void run() {
        String javaRoot = new File(Fuse.service.getJdkLocation(), "bin/java").getAbsolutePath();
        List<String> args = new ArrayList<>();
        args.add(javaRoot);
        args.add("-jar");
        args.add(jar.getAbsolutePath());

        if(this.args != null) {
            args.addAll(this.args);
        }

        Fuse.log("Executing: " + String.join(" ", args));
        Fuse.log(Fuse.service.getFuseDataFolder().getAbsolutePath());
        try {
            Process p = new ProcessBuilder()
                .command(args.toArray(new String[0]))
                .directory(Optional.of(directory).orElse(Fuse.service.getFuseDataFolder()))
                .start();
            new ProcessRelogger(p.getInputStream(), false).start();
            new ProcessRelogger(p.getErrorStream(), false).start();
            int code = p.waitFor();
            Fuse.log("Process Exited With Code " + code);
        } catch(IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
