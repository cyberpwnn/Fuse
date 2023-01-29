package com.volmit.fuse.bukkit;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.channels.FileLock;

public class Fuse extends JavaPlugin {
    public static Fuse instance;
    long lastAlive = System.currentTimeMillis();

    @Override
    public void onEnable() {
        instance = this;
        lastAlive = System.currentTimeMillis();
        File stopper = new File("STOPPER");

        if(stopper.exists()) {
            stopper.delete();
        }

        getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            if((stopper.exists() || System.currentTimeMillis() - lastAlive > 30000) && getServer().getOnlinePlayers().isEmpty()) {
                System.exit(0);
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player) && command.getName().equals("fuse") && args.length > 0) {
            lastAlive = System.currentTimeMillis();
            if(args.length == 1 && args[0].equals("inject")) {
                File dir = new File("plugins/fuse");
                if(dir.exists()) {
                    for(File i : dir.listFiles()) {
                        if(i.getName().endsWith(".jar")) {
                            try {
                                hotload(i);
                                i.delete();
                            } catch(Throwable e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    if(dir.listFiles().length == 0) {
                        dir.delete();
                    }
                }
            }

            if(args.length == 1 && args[0].equals("keepalive")) {
                lastAlive = System.currentTimeMillis();
            }

            return true;
        }

        return super.onCommand(sender, command, label, args);
    }

    private File findExistingPlugin(String name) throws Throwable {
        for(File i : new File("plugins").listFiles()) {
            if(i.getName().equals(name + ".jar")) {
                return i;
            }
        }

        for(Plugin i : Bukkit.getPluginManager().getPlugins()) {
            if(i.getName().equals(name)) {
                Method m = JavaPlugin.class.getDeclaredMethod("getFile");
                m.setAccessible(true);
                return (File) m.invoke(i);
            }
        }

        return null;
    }

    private Plugin getPluginFor(String name) {
        for(Plugin i : Bukkit.getPluginManager().getPlugins()) {
            if(i.getName().equals(name)) {
                return i;
            }
        }
        return null;
    }

    private void forceUnlock(File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileLock lock = raf.getChannel().lock();

        if(lock != null) {
            lock.release();
        }
    }

    private void unload(Plugin plugin, File existing) {
        PluginUtil.unload(plugin);

        if(!existing.delete()) {
            System.out.println("Failed to delete " + existing.getName() + "BUT I THOUGHT THIS WORKED?");
        }
    }

    private void hotload(File plugin) throws Throwable {
        String name = plugin.getName().replace(".jar", "");
        File file = findExistingPlugin(name);

        if(file != null) {
            unload(getPluginFor(name), file);
        }

        file = new File("plugins/" + plugin.getName());

        FileUtils.copyFile(plugin, file);
        Plugin p = Bukkit.getPluginManager().loadPlugin(file);

        if(p == null) {
            System.out.println("FAILED TO LOAD PLUGIN " + name + " from " + file.getName());
            return;
        }

        if(!p.isEnabled()) {
            Bukkit.getPluginManager().enablePlugin(p);
        }
    }
}
