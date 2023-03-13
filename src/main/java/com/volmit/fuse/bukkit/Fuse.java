package com.volmit.fuse.bukkit;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public class Fuse extends JavaPlugin {
    public static Fuse instance;
    long lastAlive = System.currentTimeMillis();

    @Override
    public void onEnable() {
        instance = this;
        lastAlive = System.currentTimeMillis();
        File stopper = new File("STOPPER");

        if (stopper.exists()) {
            stopper.delete();
        }

        getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            if ((stopper.exists() || System.currentTimeMillis() - lastAlive > 30000) && getServer().getOnlinePlayers().isEmpty()) {
                System.exit(0);
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player) && command.getName().equals("fuse") && args.length > 0) {
            System.out.println("Fuse command: " + String.join(" ", args));
            lastAlive = System.currentTimeMillis();
            if (args.length == 1 && args[0].equals("inject")) {
                File dir = new File("plugins/fuse");
                info("Looking in " + dir.getAbsolutePath() + " for jars to inject");
                if (dir.exists()) {
                    for (File i : dir.listFiles()) {
                        if (i.getName().endsWith(".jar")) {
                            try {
                                info("Injecting " + i.getName());
                                hotload(i);
                                i.delete();
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    if (dir.listFiles().length == 0) {
                        dir.delete();
                    }
                }
            } else if (args.length == 1 && args[0].equals("keepalive")) {
                lastAlive = System.currentTimeMillis();
            }

            return true;
        } else if (command.getName().equalsIgnoreCase("fuse")) {
            if (args.length == 2) {
                String c = args[0];
                String p = args[1];

                if (c.equalsIgnoreCase("unload")) {
                    Plugin pp = Plugins.getPlugin(p);

                    if (pp == null) {
                        sender.sendMessage("Plugin not found.");
                        return true;
                    }

                    Plugins.unload(pp);
                    sender.sendMessage("Unloaded " + pp.getName());
                    return true;
                } else if (c.equalsIgnoreCase("load")) {
                    File f = Plugins.getPluginFile(p);
                    if (f == null) {
                        sender.sendMessage("Plugin not found.");
                        return true;
                    }

                    try {
                        Plugins.load(f);
                        sender.sendMessage("Loaded " + p);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        sender.sendMessage("Failed to load " + p + " see the console");
                    }
                    return true;
                } else if (c.equalsIgnoreCase("reload")) {
                    Plugin pp = Plugins.getPlugin(p);

                    if (pp == null) {
                        sender.sendMessage("Plugin not found. Trying to just load it...");
                        File f = Plugins.getPluginFile(p);
                        if (f == null) {
                            sender.sendMessage("Plugin not found.");
                            return true;
                        }

                        try {
                            Plugins.load(f);
                            sender.sendMessage("Loaded " + p);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            sender.sendMessage("Failed to load " + p + " see the console");
                        }
                        return true;
                    }

                    Plugins.reload(pp);
                    sender.sendMessage("Reloaded " + pp.getName());

                    return true;
                } else if (c.equalsIgnoreCase("delete")) {
                    Plugin pp = Plugins.getPlugin(p);

                    if (pp == null) {
                        sender.sendMessage("Plugin not found.");
                        return true;
                    }

                    Plugins.delete(pp);
                    sender.sendMessage("Deleted " + pp.getName());

                    return true;
                } else {
                    sender.sendMessage("/fuse unload <plugin>");
                    sender.sendMessage("/fuse load <plugin>");
                    sender.sendMessage("/fuse reload <plugin>");
                    sender.sendMessage("/fuse delete <plugin>");
                }
            } else {
                sender.sendMessage("/fuse unload <plugin>");
                sender.sendMessage("/fuse load <plugin>");
                sender.sendMessage("/fuse reload <plugin>");
                sender.sendMessage("/fuse delete <plugin>");
            }
        }

        return super.onCommand(sender, command, label, args);
    }

    private void hotload(File plugin) throws Throwable {
        PluginDescriptionFile pds = Plugins.getPluginDescription(plugin);
        File file = Plugins.getPluginFile(pds.getName());

        if (file != null) {
            info("Found existing plugin file for " + pds.getName() + " at " + file.getAbsolutePath() + " cleaning up...");
            Plugin p = Plugins.getLoadedPluginFromFile(file);

            if (p != null) {
                info("Unloading " + p.getName());
                Plugins.delete(p);
                info("Deleting " + pds.getName());
            }

            if(file.exists()) {
                info("Deleting " + file.getAbsolutePath() + " (still existed)");
                if(file.delete())
                {
                    info("Deleted " + file.getAbsolutePath());
                } else {
                    info("Failed to delete " + file.getAbsolutePath());
                }
            }
        }
        else {
            info("No existing plugin file for " + pds.getName());
        }

        file = new File("plugins/" + pds.getName() + ".jar");
        info("Copying " + plugin.getAbsolutePath() + " to " + file.getAbsolutePath());
        FileUtils.copyFile(plugin, file);
        info("Loading " + pds.getName());
        Plugin p = Plugins.load(file);
        info("Loaded " + p.getName());
    }

    public static void msg(String string) {
        try {
            if (instance == null) {
                System.out.println("[Fuse]: " + string);
                return;
            }

            String msg = ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "Fuse" + ChatColor.GRAY + "]: " + string;
            Bukkit.getConsoleSender().sendMessage(msg);
        } catch (Throwable e) {
            System.out.println("[Fuse]: " + string);
        }
    }

    public static void actionbar(Player p, String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
    }

    public static void warn(String string) {
        msg(ChatColor.YELLOW + string);
    }

    public static void error(String string) {
        msg(ChatColor.RED + string);
    }

    public static void info(String string) {
        msg(ChatColor.WHITE + string);
    }
}
