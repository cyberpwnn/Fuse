package com.volmit.fuse.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class DummyPluginLoader implements PluginLoader {
    @NotNull
    @Override
    public Plugin loadPlugin(@NotNull File file) throws InvalidPluginException, UnknownDependencyException {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public PluginDescriptionFile getPluginDescription(@NotNull File file) throws InvalidDescriptionException {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Pattern[] getPluginFileFilters() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(@NotNull Listener listener, @NotNull Plugin plugin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enablePlugin(@NotNull Plugin plugin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        throw new UnsupportedOperationException();
    }
}
