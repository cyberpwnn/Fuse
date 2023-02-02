package com.volmit.fuse.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for managing plugins.
 *
 * @author rylinaux
 */
public class PluginUtil {

    /**
     * Enable a plugin.
     *
     * @param plugin
     *     the plugin to enable
     */
    public static void enable(Plugin plugin) {
        if(plugin != null && !plugin.isEnabled())
            Bukkit.getPluginManager().enablePlugin(plugin);
    }

    public static void enable(String s) {
        for(Plugin i : Bukkit.getPluginManager().getPlugins()) {
            if(i.getName().equals(s)) {
                enable(i);
                return;
            }
        }

        load(s);
    }

    /**
     * Enable all plugins.
     */
    public static void enableAll() {
        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            enable(plugin);
        }
    }

    /**
     * Disable a plugin.
     *
     * @param plugin
     *     the plugin to disable
     */
    public static void disable(Plugin plugin) {
        if(plugin != null && plugin.isEnabled()) {
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Disable all plugins.
     */
    public static void disableAll() {
        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            disable(plugin);
        }
    }

    /**
     * Returns the formatted name of the plugin.
     *
     * @param plugin
     *     the plugin to format
     * @return the formatted name
     */
    public static String getFormattedName(Plugin plugin) {
        return getFormattedName(plugin, false);
    }

    /**
     * Returns the formatted name of the plugin.
     *
     * @param plugin
     *     the plugin to format
     * @param includeVersions
     *     whether to include the version
     * @return the formatted name
     */
    public static String getFormattedName(Plugin plugin, boolean includeVersions) {
        ChatColor color = plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED;
        String pluginName = color + plugin.getName();
        if(includeVersions) {
            pluginName += " (" + plugin.getDescription().getVersion() + ")";
        }

        return pluginName;
    }

    /**
     * Returns a plugin from a String.
     *
     * @param name
     *     the name of the plugin
     * @return the plugin
     */
    public static Plugin getPluginByName(String name) {
        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if(name.equalsIgnoreCase(plugin.getName()))
                return plugin;
        }
        return null;
    }

    /**
     * Returns a List of plugin names.
     *
     * @return list of plugin names
     */
    public static List<String> getPluginNames(boolean fullName) {
        List<String> plugins = new ArrayList<String>();
        for(Plugin plugin : Bukkit.getPluginManager().getPlugins())
            plugins.add(fullName ? plugin.getDescription().getFullName() : plugin.getName());
        return plugins;
    }

    /**
     * Get the version of another plugin.
     *
     * @param name
     *     the name of the other plugin.
     * @return the version.
     */
    public static String getPluginVersion(String name) {
        Plugin plugin = getPluginByName(name);
        if(plugin != null && plugin.getDescription() != null)
            return plugin.getDescription().getVersion();
        return null;
    }

    /**
     * Find which plugin has a given command registered.
     *
     * @param command
     *     the command.
     * @return the plugin.
     */
    @SuppressWarnings("unchecked")
    public static List<String> findByCommand(String command) {

        List<String> plugins = new ArrayList<String>();

        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {

            // Map of commands and their attributes.
            Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();

            if(commands != null) {

                // Iterator for all the plugin's commands.
                Iterator<Map.Entry<String, Map<String, Object>>> commandIterator = commands.entrySet().iterator();

                while(commandIterator.hasNext()) {

                    // Current value.
                    Map.Entry<String, Map<String, Object>> commandNext = commandIterator.next();

                    // Plugin name matches - return.
                    if(commandNext.getKey().equalsIgnoreCase(command)) {
                        plugins.add(plugin.getName());
                        continue;
                    }

                    // No match - let's iterate over the attributes and see if
                    // it has aliases.
                    Iterator<Map.Entry<String, Object>> attributeIterator = commandNext.getValue().entrySet().iterator();

                    while(attributeIterator.hasNext()) {

                        // Current value.
                        Map.Entry<String, Object> attributeNext = attributeIterator.next();

                        // Has an alias attribute.
                        if(attributeNext.getKey().equals("aliases")) {

                            Object aliases = attributeNext.getValue();

                            if(aliases instanceof String) {
                                if(((String) aliases).equalsIgnoreCase(command)) {
                                    plugins.add(plugin.getName());
                                    continue;
                                }
                            } else {

                                // Cast to a List of Strings.
                                List<String> array = (List<String>) aliases;

                                // Check for matches here.
                                for(String str : array) {
                                    if(str.equalsIgnoreCase(command)) {
                                        plugins.add(plugin.getName());
                                        continue;
                                    }
                                }

                            }

                        }

                    }
                }

            }

        }

        // No matches.
        return plugins;

    }

    /**
     * Checks whether the plugin is ignored.
     *
     * @param plugin
     *            the plugin to check
     * @return whether the plugin is ignored
     */

    /**
     * Loads and enables a plugin.
     *
     * @param plugin
     *     plugin to load
     * @return status message
     */
    private static void load(Plugin plugin) {
        load(plugin.getName());
    }

    public static String getPluginFileName(String name) {
        File pluginDir = Fuse.instance.getDataFolder().getParentFile();

        for(File f : pluginDir.listFiles()) {
            if(f.getName().endsWith(".jar")) {
                try {
                    PluginDescriptionFile desc = Fuse.instance.getPluginLoader().getPluginDescription(f);
                    if(desc.getName().equalsIgnoreCase(name)) {
                        return f.getName();
                    }
                } catch(InvalidDescriptionException e) {

                }
            }
        }

        return null;
    }

    public static String getPluginFileNameUnsafe(String name, Plugin ins) {
        File pluginDir = ins.getDataFolder().getParentFile();

        for(File f : pluginDir.listFiles()) {
            if(f.getName().endsWith(".jar")) {
                try {
                    PluginDescriptionFile desc = ins.getPluginLoader().getPluginDescription(f);
                    if(desc.getName().equalsIgnoreCase(name)) {
                        return f.getName();
                    }
                } catch(InvalidDescriptionException e) {

                }
            }
        }

        return null;
    }

    /**
     * Loads and enables a plugin.
     *
     * @param name
     *     plugin's name
     * @return status message
     */
    public static void load(String name) {
        Plugin target = null;

        File pluginDir = new File("plugins");

        if(!pluginDir.isDirectory()) {
            return;
        }

        File pluginFile = new File(pluginDir, name + ".jar");

        if(!pluginFile.isFile()) {
            for(File f : pluginDir.listFiles()) {
                if(f.getName().endsWith(".jar")) {
                    try {
                        PluginDescriptionFile desc = Fuse.instance.getPluginLoader().getPluginDescription(f);
                        if(desc.getName().equalsIgnoreCase(name)) {
                            pluginFile = f;
                            break;
                        }
                    } catch(InvalidDescriptionException e) {
                        return;
                    }
                }
            }
        }

        try {
            target = Bukkit.getPluginManager().loadPlugin(pluginFile);
        } catch(InvalidDescriptionException e) {
            e.printStackTrace();
            return;
        } catch(InvalidPluginException e) {
            e.printStackTrace();
            return;
        }

        target.onLoad();
        Bukkit.getPluginManager().enablePlugin(target);

    }

    /**
     * Reload a plugin.
     *
     * @param plugin
     *     the plugin to reload
     */
    public static void reload(Plugin plugin) {
        if(plugin != null) {
            unload(plugin);
            load(plugin);
        }
    }

    /**
     * Reload all plugins.
     */
    public static void reloadAll() {
        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            reload(plugin);
        }
    }

    /**
     * Unload a plugin.
     *
     * @param plugin
     *     the plugin to unload
     * @return the message to send to the user.
     */
    @SuppressWarnings("unchecked")
    public static void unload(Plugin plugin) {
        String name = plugin.getName();
        PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = null;
        List<Plugin> plugins = null;
        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;

        boolean reloadlisteners = true;

        if(pluginManager != null) {
            pluginManager.disablePlugin(plugin);

            try {

                Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
                pluginsField.setAccessible(true);
                plugins = (List<Plugin>) pluginsField.get(pluginManager);

                Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
                lookupNamesField.setAccessible(true);
                names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

                try {
                    Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                    listenersField.setAccessible(true);
                    listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
                } catch(Exception e) {
                    reloadlisteners = false;
                }

                Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                commands = (Map<String, Command>) knownCommandsField.get(commandMap);

            } catch(NoSuchFieldException e) {
                e.printStackTrace();
                return;
            } catch(IllegalAccessException e) {
                e.printStackTrace();
                return;
            }
        }

        pluginManager.disablePlugin(plugin);

        if(plugins != null) {
            plugins.remove(plugin);
        }

        if(names != null) {
            names.remove(name);
        }

        if(listeners != null && reloadlisteners) {
            for(SortedSet<RegisteredListener> set : listeners.values()) {
                for(Iterator<RegisteredListener> it = set.iterator(); it.hasNext(); ) {
                    RegisteredListener value = it.next();
                    if(value.getPlugin() == plugin) {
                        it.remove();
                    }
                }
            }
        }

        if(commandMap != null) {
            for(Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Command> entry = it.next();
                if(entry.getValue() instanceof PluginCommand c) {
                    if(c.getPlugin() == plugin) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                }
            }
        }

        // Attempt to close the classloader to unlock any handles on the
        // plugin's
        // jar file.
        ClassLoader cl = plugin.getClass().getClassLoader();

        if(cl instanceof URLClassLoader) {
            try {
                ((URLClassLoader) cl).close();
            } catch(IOException ex) {
                Logger.getLogger(PluginUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Will not work on processes started with the -XX:+DisableExplicitGC
        // flag,
        // but lets try it anyway. This tries to get around the issue where
        // Windows
        // refuses to unlock jar files that were previously loaded into the JVM.
        System.gc();
    }
}