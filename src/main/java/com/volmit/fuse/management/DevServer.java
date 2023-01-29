package com.volmit.fuse.management;

import com.volmit.fuse.Fuse;
import com.volmit.fuse.util.ProcessRelogger;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DevServer extends Thread {
    private OutputStream output;
    private PrintWriter outputWriter;
    private Runnable onStarted;
    private final Consumer<String> serverLog;
    private String player;
    private boolean closeClient = false;

    public DevServer(Runnable onStarted) {
        this.onStarted = onStarted;
        this.serverLog = this::onServerLog;
    }

    private void onServerOnline() {
        Fuse.log("Server Online");
        serverCommand("say Fuse Management Online!");
        onStarted.run();
    }

    private void onServerLog(String line) {
        if(line.contains("Done") && line.contains("s)! For help, type \"help\"")) {
            onServerOnline();
        }

        if(line.contains(player + " issued server command: /")) {
            onPlayerCommand(line.split("\\Q"+player+ " issued server command: /\\E")[1]);
        }

        if(line.endsWith("left the game")) {
            onPlayerLeft();
        }

        if(line.endsWith("joined the game")) {
            String player = "";
            for(String i : line.split("\\Q \\E")) {
                if(i.endsWith("joined")) {
                    serverCommand("op " + player);
                    this.player = player;
                    onPlayerJoined(player);
                    break;
                }

                player = i;
            }
        }
    }

    public void messagePlayer(String message) {
        serverCommand("tell " + player + " " + message);
    }

    private void onPlayerLeft() {

    }

    private void onPlayerJoined(String player) {
        messagePlayer("Fuse Management is Online! Happy Hacking!");
        messagePlayer("Configure your workspace with [CTRL + W]");
    }

    public void onPlayerCommand(String command) {
        Fuse.log("Player Command: " + command);

        if(command.toLowerCase().equals("crash")) {
            stopServer();
            closeClient = true;
        }
    }

    public void serverCommand(String command) {
        if(outputWriter != null) {
            outputWriter.println(command);
            outputWriter.flush();
        }
    }

    public void reloadServer() {
        serverCommand("reload");
    }

    public void stopServer() {
        serverCommand("stop");
    }

    public void run() {
        String javaRoot = new File(Fuse.service.getJdkLocation(), "bin/java").getAbsolutePath();
        List<String> args = new ArrayList<>();
        args.add(javaRoot);
        //-Xmx8g -Xms8g -XX:+UnlockExperimentalVMOptions -XX:+UseZGC
        args.add("-Xmx8g");
        args.add("-Xms1m");
        args.add("-XX:+UnlockExperimentalVMOptions");
        args.add("-XX:+UseZGC");
        args.add("-XX:SoftMaxHeapSize=2G");
        args.add("-XX:ZUncommitDelay=30");
        args.add("-DIReallyKnowWhatIAmDoingISwear");
        args.add("-jar");
        args.add(Fuse.service.getServerExecutable().getAbsolutePath());
        args.add("nogui");

        Fuse.log("Executing: " + String.join(" ", args));
        Fuse.log(Fuse.service.getFuseDataFolder().getAbsolutePath());
        try {
            Process p = new ProcessBuilder()
                .command(args.toArray(new String[0]))
                .directory(Fuse.service.getServerFolder())
                .start();
            new ProcessRelogger(p.getInputStream(), false, serverLog).start();
            new ProcessRelogger(p.getErrorStream(), false, serverLog).start();
            output = p.getOutputStream();
            outputWriter = new PrintWriter(output);
            int code = p.waitFor();
            Fuse.log("Process Exited With Code " + code);

            if(closeClient) {
                Fuse.service.close();
                MinecraftClient.getInstance().stop();
            }

        } catch(IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
