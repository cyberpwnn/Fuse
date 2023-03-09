package com.volmit.fuse.bukkit;

import art.arcane.curse.Curse;
import com.google.common.graph.MutableGraph;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Plugins {
    public static File getPluginFile(Plugin plugin) {
        try {
            return Curse.on(plugin).get("file");
        } catch(Throwable e) {

        }

        return getPluginFile(plugin.getName());
    }

    public static Plugin getLoadedPluginFromFile(File file) {
        return getPlugin(getPluginDescription(file).getName());
    }

    public static PluginDescriptionFile getPluginDescription(File file) {
        try {
            return Fuse.instance.getPluginLoader().getPluginDescription(file);
        } catch(InvalidDescriptionException e) {
            throw new RuntimeException(e);
        }
    }

    public static Plugin getPlugin(String query) {
        for(Plugin i : Bukkit.getPluginManager().getPlugins()) {
            if(i.getName().equalsIgnoreCase(query)) {
                return i;
            }
        }

        for(Plugin i : Bukkit.getPluginManager().getPlugins()) {
            if(i.getName().toLowerCase().startsWith(query.toLowerCase())) {
                return i;
            }
        }

        for(Plugin i : Bukkit.getPluginManager().getPlugins()) {
            if(i.getName().toLowerCase().contains(query.toLowerCase())) {
                return i;
            }
        }

        return null;
    }

    public static File getPluginFile(String query) {
        File f = new File("plugins/" + query + ".jar");

        if(f.exists()) {
            return f;
        }

        Map<File, PluginDescriptionFile> pdfs = new HashMap<>();

        for(File i : new File("plugins").listFiles()) {
            try {
                PluginDescriptionFile pdf = pdfs.computeIfAbsent(i, Plugins::getPluginDescription);

                if(pdf.getName().equalsIgnoreCase(query)) {
                    return i;
                }
            } catch(Throwable e) {

            }
        }

        for(File i : new File("plugins").listFiles()) {
            try {
                PluginDescriptionFile pdf = pdfs.computeIfAbsent(i, (k) -> {
                    try {
                        return Fuse.instance.getPluginLoader().getPluginDescription(k);
                    } catch(InvalidDescriptionException e) {
                        throw new RuntimeException(e);
                    }
                });

                if(pdf.getName().toLowerCase().startsWith(query.toLowerCase())) {
                    return i;
                }
            } catch(Throwable e) {

            }
        }

        for(File i : new File("plugins").listFiles()) {
            try {
                PluginDescriptionFile pdf = pdfs.computeIfAbsent(i, (k) -> {
                    try {
                        return Fuse.instance.getPluginLoader().getPluginDescription(k);
                    } catch(InvalidDescriptionException e) {
                        throw new RuntimeException(e);
                    }
                });

                if(pdf.getName().toLowerCase().contains(query.toLowerCase())) {
                    return i;
                }
            } catch(Throwable e) {

            }
        }

        return null;
    }

    public static void delete(Plugin pp) {
        String n = pp.getName();
        File f = getPluginFile(n);
        unload(pp);
        f.delete();
    }

    public static void reload(Plugin pp) {
        String n = pp.getName();
        File f = getPluginFile(n);
        unload(pp);
        load(f);
    }

    public static Plugin load(File file) {
        try {
            Plugin p = Bukkit.getPluginManager().loadPlugin(file);
            p.onLoad();
            Bukkit.getPluginManager().enablePlugin(p);
            return p;
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void unload(Plugin plugin) {
        Fuse.info("Unloading " + plugin.getName() + " v" + plugin.getDescription().getVersion());
        JavaPluginLoader loader = (JavaPluginLoader) plugin.getPluginLoader();
        SimplePluginManager manager = (SimplePluginManager) Bukkit.getServer().getPluginManager();
        Fuse.info("Disabling " + plugin.getName() + " v" + plugin.getDescription().getVersion());
        manager.disablePlugin(plugin);
        Fuse.info("Disabled " + plugin.getName() + " v" + plugin.getDescription().getVersion());

        synchronized(manager) {
            Fuse.info("Unregistering Listeners for " + plugin.getName() + " v" + plugin.getDescription().getVersion());
            HandlerList.unregisterAll(plugin);
            Fuse.info("Cancelling Tasks for " + plugin.getName() + " v" + plugin.getDescription().getVersion());
            Bukkit.getScheduler().cancelTasks(plugin);
            List<Plugin> plugins = Curse.on(manager).get("plugins");
            Map<String, Plugin> lookupNames = Curse.on(manager).get("lookupNames");
            MutableGraph<String> dependencyGraph = Curse.on(manager).get("dependencyGraph");
            Map<String, Permission> permissions = Curse.on(manager).get("permissions");
            Map<Boolean, Set<Permission>> defaultPerms = Curse.on(manager).get("defaultPerms");

            if(plugins.remove(plugin)) {
                Fuse.info("Removed " + plugin.getName() + " from plugin list");
            } else {
                Fuse.warn("Couldn't find " + plugin.getName() + " in plugin list");
            }

            if(plugins.removeIf(i -> i.getClass().equals(plugin.getClass()))) {
                Fuse.info("Removed refs by class " + plugin.getName() + " from plugin list");
            } else {
                Fuse.warn("Couldn't find refs by class " + plugin.getName() + " in plugin list");
            }

            if(lookupNames.remove(plugin.getDescription().getName()) != null) {
                Fuse.info("Removed " + plugin.getName() + " from lookup names");
            } else {
                Fuse.warn("Couldn't find " + plugin.getName() + " in lookup names");
            }

            for(String i : new HashMap<>(lookupNames).keySet()) {
                if(lookupNames.get(i).getClass().equals(plugin.getClass())) {
                    lookupNames.remove(i);
                    Fuse.info("Removed '" + i + "' from lookup names");
                }
            }

            if(dependencyGraph.removeNode(plugin.getDescription().getName())) {
                Fuse.info("Removed " + plugin.getName() + " from dependency graph");
            } else {
                Fuse.warn("Couldn't find " + plugin.getName() + " in dependency graph");
            }
            dependencyGraph.edges().stream().filter(i ->
                    i.nodeU().equals(plugin.getDescription().getName())
                        || i.nodeV().equals(plugin.getDescription().getName()))
                .forEach(i -> {
                    if(dependencyGraph.removeEdge(i)) {
                        Fuse.info("Removed " + plugin.getName() + " from dependency graph edge " + i);
                    } else {
                        Fuse.warn("Couldn't find " + plugin.getName() + " in dependency graph edge " + i);
                    }
                });
            Set<Permission> p = new HashSet<>(plugin.getDescription().getPermissions());

            for(String i : new HashSet<>(permissions.keySet())) {
                if(p.contains(permissions.get(i))) {
                    permissions.remove(i);
                    Fuse.info("Removed " + plugin.getName() + ":" + i + " from permissions");
                }
            }

            if(defaultPerms.get(true).removeAll(p)) {
                Fuse.info("Removed " + plugin.getName() + " from default perms TRUE");
            } else {
                Fuse.warn("Couldn't find " + plugin.getName() + " in default perms TRUE");
            }
            if(defaultPerms.get(false).removeAll(p)) {
                Fuse.info("Removed " + plugin.getName() + " from default perms FALSE");
            } else {
                Fuse.warn("Couldn't find " + plugin.getName() + " in default perms FALSE");
            }
        }

        synchronized(loader) {
            List<?> loaders = Curse.on(loader).get("loaders");

            for(Object i : new ArrayList<>(loaders)) {
                JavaPlugin p = Curse.on(i).get("plugin");
                if(p != null) {
                    if(p.getClass().equals(plugin.getClass())) {
                        if(loaders.remove(i)) {
                            Fuse.info("Removed " + plugin.getName() + " from loaders");
                        } else {
                            Fuse.warn("Couldn't remove " + plugin.getName() + " in loaders?");
                        }
                    }
                }
            }
        }

        Fuse.info("Calling GC to unlock dangling file locks");
        System.gc();
        Fuse.info("Unloaded " + plugin.getName() + "!");
    }
}
