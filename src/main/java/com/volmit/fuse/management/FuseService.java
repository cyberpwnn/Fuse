package com.volmit.fuse.management;

import art.arcane.chrono.PrecisionStopwatch;
import art.arcane.multiburst.MultiBurst;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.volmit.fuse.Fuse;
import com.volmit.fuse.util.Looper;
import lombok.Data;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.world.biome.Biome;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
public class FuseService {
    private static final String BUILD_TOOLS_URL = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar";
    private final File fuseDataFolder;
    private final File buildToolsJar;
    private final File buildToolsFolder;
    private final File jdkLocation;
    private final File jdkPackage;
    private final File serverFolder;
    private final JobExecutor executor;
    public static final String VERSION = "1.19.3";
    private final File serverExecutableBuildToolsOut;
    private final File serverExecutable;
    private DevServer devServer;
    private boolean ready;

    public FuseService(File fuseDataFolder) {
        this.fuseDataFolder = fuseDataFolder;
        ready = false;
        executor = new JobExecutor();
        buildToolsFolder = new File(fuseDataFolder, "buildtools");
        jdkPackage = new File(fuseDataFolder, "cache/jdkpkg");
        jdkLocation = new File(fuseDataFolder, "jdk");
        buildToolsJar = new File(buildToolsFolder, "BuildTools.jar");
        serverFolder = new File(fuseDataFolder, "dev-server");
        serverExecutable = new File(serverFolder, "spigot-"+VERSION+".jar");
        serverExecutableBuildToolsOut = new File(buildToolsFolder, VERSION + "/spigot-"+VERSION+".jar");
        devServer = new DevServer(this::onServerStarted);
        Fuse.log("Fuse Service Initialized with data folder: " + fuseDataFolder.getAbsolutePath());
        Fuse.log("Spigot Version: " + VERSION);
    }

    private void onServerStarted() {
        ready = true;
    }

    public void open() {
        MultiBurst.burst.lazy(this::initialize);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public void close() {
        devServer.stopServer();
    }

    private void initialize() {
        executor.queue(() -> {
            Fuse.log("Initializing Service");
            fuseDataFolder.mkdirs();
            installJDK();
            executor.after(() -> {
                if(true)
                {
                    installBuildTools();
                }

                executor.after(() -> {
                    devServer.start();
                });
            });
        });
    }

    private void installBuildTools() {
        executor.queue(() -> {
            Fuse.log("Installing BuildTools");
            buildToolsFolder.mkdirs();
            downloadBuildTools();
        });
    }

    private void downloadBuildTools() {
        executor.queue(() -> {
            if(!buildToolsJar.exists()) {
                Fuse.log("Downloading BuildTools");
                download(BUILD_TOOLS_URL, buildToolsJar);
            }

            buildSpigotJar();
        });
    }

    private void setupServer() {
        executor.queue(() -> {
            Fuse.log("Setting up Development Spigot Server...");
            serverFolder.mkdirs();
            installServerJar();
        });
    }

    private void installServerJar() {
        executor.queue(() -> {
            Fuse.log("Installing Spigot Jar");
            try {
                FileUtils.copyFile(serverExecutableBuildToolsOut, serverExecutable);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        });

        executor.queue(() -> installServerFile("bukkit.yml"));
        executor.queue(() -> installServerFile("eula.txt"));
        executor.queue(() -> installServerFile("server.properties"));
        executor.queue(() -> installServerFile("spigot.yml"));
    }

    private void buildSpigotJar() {
        executor.queue(() -> {
            Fuse.log("Building Server Jars (Build Tools)");
            JarExecutor.builder()
                .directory(buildToolsFolder)
                .jar(buildToolsJar)
                .arg("--rev")
                .arg(VERSION)
                .arg("--output-dir")
                .arg(VERSION)
                .arg("--compile-if-changed")
                .build().execute();
            setupServer();
        });
    }

    private void installJDK() {
        executor.queue(() -> {
            Fuse.log("Installing JDK");
            downloadJDK();
        });
    }

    private void downloadJDK() {
        executor.queue(() -> {
            if(!jdkPackage.exists()) {
                Fuse.log("Downloading JDK");
                jdkPackage.getParentFile().mkdirs();
                download(JDKDownloadUrl.getAutoUrl(), jdkPackage);
            }

            extractJDK(!JDKDownloadUrl.getAutoUrl().endsWith("zip"));
        });
    }

    private void extractJDK(boolean tar) {
        executor.queue(() -> {
            Fuse.log("Extracting JDK");
            if(!jdkLocation.exists()) {
                if(tar) {
                    try (InputStream fi = Files.newInputStream(Paths.get(jdkLocation.getAbsolutePath()));
                         InputStream bi = new BufferedInputStream(fi);
                         InputStream gzi = new GzipCompressorInputStream(bi);
                         ArchiveInputStream i = new TarArchiveInputStream(gzi)) {
                        ArchiveEntry entry = null;
                        while ((entry = i.getNextEntry()) != null) {
                            if (!i.canReadEntryData(entry)) {
                                // log something?
                                continue;
                            }
                            File f = new File(jdkLocation, entry.getName());
                            if (entry.isDirectory()) {
                                if (!f.isDirectory() && !f.mkdirs()) {
                                    throw new IOException("failed to create directory " + f);
                                }
                            } else {
                                File parent = f.getParentFile();
                                if (!parent.isDirectory() && !parent.mkdirs()) {
                                    throw new IOException("failed to create directory " + parent);
                                }
                                try (OutputStream o = Files.newOutputStream(f.toPath())) {
                                    IOUtils.copy(i, o);
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    ZipUtil.unwrap(jdkPackage, jdkLocation);
                }
            }
        });
    }

    private void download(String url, File file) {
        Fuse.log("Downloading " + url + " into " + file.getPath());
        file.getParentFile().mkdirs();
        PrecisionStopwatch p = PrecisionStopwatch.start();

        try {
            URL at = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(at.openStream());
            FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            Fuse.log("Downloaded " + file.getName() + " in " + p.getMilliseconds() + "ms");
        }

        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void installServerFile(String fileName, String... replacements) {
        try {
            Fuse.log("Installing " + fileName);
            InputStream input = getClass().getResourceAsStream("/server/" + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter pw = new PrintWriter(new File(serverFolder, fileName));
            String line;

            while((line = reader.readLine()) != null) {
                for(int i = 0; i < replacements.length; i += 2) {
                    line = line.replaceAll("\\Q$" + replacements[i] + "\\E", replacements[i + 1]);
                }

                pw.println(line);
            }

            reader.close();
            pw.close();
        }

        catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
