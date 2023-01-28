package com.volmit.fuse.management;

import com.volmit.fuse.Fuse;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FuseService {
    private static final String BUILD_TOOLS_URL = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar";
    private final File fuseDataFolder;
    private final File buildToolsJar;
    private final File buildToolsFolder;
    private final File jdkLocation;
    private final File jdkPackage;
    private final JobExecutor executor;

    public FuseService(File fuseDataFolder) {
        this.fuseDataFolder = fuseDataFolder;
        executor = new JobExecutor();
        buildToolsFolder = new File(fuseDataFolder, "buildtools");
        jdkPackage = new File(fuseDataFolder, "cache/jdkpkg");
        jdkLocation = new File(fuseDataFolder, "jdk");
        buildToolsJar = new File(buildToolsFolder, "BuildTools.jar");
    }

    public void open() {
        initialize();
    }

    public void close() {

    }

    private void initialize() {
        executor.queue(() -> {
            Fuse.LOGGER.info("Initializing Service");
            fuseDataFolder.mkdirs();
            installJDK();
            installBuildTools();
        });
    }

    private void installBuildTools() {
        executor.queue(() -> {
            Fuse.LOGGER.info("Installing BuildTools");
            buildToolsFolder.mkdirs();
            downloadBuildTools();
        });
    }

    private void downloadBuildTools() {
        executor.queue(() -> {
            if(!buildToolsJar.exists()) {
                Fuse.LOGGER.info("Downloading BuildTools");
                download(BUILD_TOOLS_URL, buildToolsJar);
            }
        });
    }

    private void installJDK() {
        executor.queue(() -> {
            Fuse.LOGGER.info("Installing JDK");
            downloadJDK();
        });
    }

    private void downloadJDK() {
        executor.queue(() -> {
            if(!jdkPackage.exists()) {
                Fuse.LOGGER.info("Downloading JDK");
                jdkPackage.getParentFile().mkdirs();
                download(JDKDownloadUrl.getAutoUrl(), jdkPackage);
            }

            extractJDK(!JDKDownloadUrl.getAutoUrl().endsWith("zip"));
        });
    }

    private void extractJDK(boolean tar) {
        executor.queue(() -> {
            Fuse.LOGGER.info("Extracting JDK");
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
                    ZipUtil.unpack(jdkPackage, jdkLocation);
                }
            }
        });
    }

    private void download(String url, File file) {
        executor.queue(() -> {
            Fuse.LOGGER.info("Downloading " + url + " into " + file.getPath());
            file.getParentFile().mkdirs();

            try {
                URL at = new URL(url);
                ReadableByteChannel rbc = Channels.newChannel(at.openStream());
                FileOutputStream fos = new FileOutputStream(file);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
            }

            catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }
}
