package it.multicoredev.mclib.seed.plugin;

import com.google.common.base.Preconditions;
import it.multicoredev.mclib.seed.SeedSystem;
import it.multicoredev.mclib.seed.exceptions.PluginDisableException;
import it.multicoredev.mclib.seed.exceptions.PluginEnableException;
import it.multicoredev.mclib.seed.exceptions.PluginLoadException;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Copyright © 2020 - 2022 by Lorenzo Magni
 * This file is part of MCLib.
 * MCLib is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public final class SeedPluginManager {
    private final SeedSystem seedSystem;
    private final Yaml yaml;
    private final Map<String, SeedPlugin> plugins = new HashMap<>();
    private final Map<String, SeedPlugin> toEnable = new HashMap<>();
    private final Map<String, SeedPluginDescription> toLoad = new HashMap<>();

    public SeedPluginManager(SeedSystem seedSystem) {
        this.seedSystem = seedSystem;

        Constructor yamlConstructor = new Constructor();
        PropertyUtils propertyUtils = yamlConstructor.getPropertyUtils();
        propertyUtils.setSkipMissingProperties(true);
        yamlConstructor.setPropertyUtils(propertyUtils);
        yaml = new Yaml(yamlConstructor);
    }

    /**
     * Get a list of enabled plugins
     *
     * @return Collection of enabled plugins
     */
    public Collection<SeedPlugin> getPlugins() {
        return plugins.values();
    }

    /**
     * Get a list of loaded plugins not enabled
     *
     * @return Collection of loaded plugins
     */
    public Collection<SeedPlugin> getLoadedPlugins() {
        return toEnable.values();
    }

    /**
     * Get a list of detected plugins to load and enable
     *
     * @return Collection of detected plugins
     */
    public Collection<SeedPluginDescription> getPluginsToLoad() {
        return toLoad.values();
    }

    /**
     * Detect all the plugins in the {@link SeedSystem} plugin folder
     */
    public void detectPlugins() {
        detectPlugins(null);
    }

    /**
     * Load all the plugins detected
     */
    public void loadPlugins() {
        for (Map.Entry<String, SeedPluginDescription> plugin : toLoad.entrySet()) {
            loadPlugin(plugin.getValue());
        }
    }

    /**
     * Enable all the plugins loaded
     */
    public void enablePlugins() {
        for (SeedPlugin plugin : toEnable.values()) {
            try {
                plugin.onEnable();
                System.out.println("Enabled plugin " + plugin.getPluginName() + " version " + plugin.getPluginVersion() + " by " + plugin.getPluginAuthor());
            } catch (PluginEnableException e) {
                System.out.println("Exception encountered when enabling plugin: " + plugin.getPluginName() + " version " + plugin.getPluginVersion() + " by " + plugin.getPluginAuthor());
                e.printStackTrace();
            }
        }
    }

    /**
     * Disable all the plugins enabled
     */
    public void disablePlugins() {
        for (SeedPlugin plugin : plugins.values()) {
            try {
                disablePlugin(plugin);
            } catch (PluginDisableException e) {
                System.out.println("Exception encountered when disabling plugin: " + plugin.getPluginName() + " version " + plugin.getPluginVersion() + " by " + plugin.getPluginAuthor());
                e.printStackTrace();
            }
        }
    }

    /**
     * Detect and load a plugin
     *
     * @param pluginName The name contained in the plugin.yml descriptor of the plugin
     * @return True if the plugin is detected and loaded correctly
     */
    public boolean loadPlugin(@NotNull String pluginName) {
        if (pluginName.trim().isEmpty()) throw new IllegalArgumentException("Plugin name cannot be empty");

        if (detectPlugins(pluginName)) {
            SeedPluginDescription description = toLoad.get(pluginName.toLowerCase());
            if (description == null) return false;
            return loadPlugin(description);
        }

        return false;
    }

    /**
     * Enable a plugin
     *
     * @param pluginName The name contained in the plugin.yml descriptor of the plugin
     * @throws PluginEnableException PluginEnableException if thrown by the developer in the {@link SeedPlugin#onEnable()}
     */
    public void enablePlugin(@NotNull String pluginName) throws PluginEnableException {
        if (pluginName.trim().isEmpty()) throw new IllegalArgumentException("Plugin name cannot be empty");

        if (!toEnable.containsKey(pluginName.toLowerCase())) throw new PluginEnableException("Plugin " + pluginName + " not loaded");

        SeedPlugin plugin = toEnable.get(pluginName.toLowerCase());
        enablePlugin(plugin);
    }

    /**
     * Disable a plugin
     *
     * @param pluginName The name contained in the plugin.yml descriptor of the plugin
     * @throws PluginDisableException PluginDisableException if thrown by the developer in the {@link SeedPlugin#onDisable()}
     */
    public void disablePlugin(@NotNull String pluginName) throws PluginDisableException {
        if (pluginName.trim().isEmpty()) throw new IllegalArgumentException("Plugin name cannot be empty");

        if(!plugins.containsKey(pluginName.toLowerCase())) throw new PluginDisableException("Plugin " + pluginName + " not enabled");

        SeedPlugin plugin = plugins.get(pluginName.toLowerCase());
        disablePlugin(plugin);
    }

    private boolean detectPlugins(String pluginName) {
        Preconditions.checkNotNull(seedSystem.getPluginsDir(), "Directory cannot be null");
        Preconditions.checkArgument(seedSystem.getPluginsDir().exists(), "Plugin directory must exists");
        Preconditions.checkArgument(seedSystem.getPluginsDir().isDirectory(), "Plugin directory must be a directory");

        File[] files = seedSystem.getPluginsDir().listFiles();

        if (files == null || files.length == 0) return false;

        for (File file : files) {
            if (!file.isFile() || !file.getName().toLowerCase().endsWith(".jar")) continue;

            try (JarFile jar = new JarFile(file)) {
                JarEntry pdf = jar.getJarEntry("plugin.yml");

                Preconditions.checkNotNull(pdf, "Plugin must have a plugin.yml as a descriptor");

                try (InputStream is = jar.getInputStream(pdf)) {
                    Map<String, Object> data = yaml.load(is);

                    SeedPluginDescription description = new SeedPluginDescription(
                            (String) data.get("name"),
                            (String) data.get("main"),
                            (String) data.get("version"),
                            (String) data.get("author"),
                            (String) data.get("description"),
                            file
                    );

                    Preconditions.checkNotNull(description.getName(), "Plugin from file %file has no name", file);
                    Preconditions.checkNotNull(description.getMain(), "Plugin from file %file has no main", file);
                    Preconditions.checkNotNull(description.getAuthor(), "Plugin from file %file has no author", file);
                    Preconditions.checkNotNull(description.getVersion(), "Plugin from file %file has no version", file);

                    if (pluginName == null) {
                        toLoad.put(description.getName().toLowerCase(), description);
                    } else {
                        if (pluginName.equalsIgnoreCase(description.getName())) {
                            toLoad.put(description.getName().toLowerCase(), description);
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not load plugin from file " + file);
                e.printStackTrace();
            }
        }

        return false;
    }

    private boolean loadPlugin(SeedPluginDescription description) {
        if (plugins.containsKey(description.getName().toLowerCase())) return false;

        try {
            PluginLoader loader = new PluginLoader(new URL[]{description.getFile().toURI().toURL()});
            Class<?> main = loader.loadClass(description.getMain());
            SeedPlugin plugin = (SeedPlugin) main.getDeclaredConstructor().newInstance();
            plugin.init(description, seedSystem);

            try {
                loadPlugin(plugin);
            } catch (PluginLoadException e) {
                System.out.println("Exception encountered when loading plugin: " + plugin.getPluginName() + " version " + plugin.getPluginVersion() + " by " + plugin.getPluginAuthor());
                e.printStackTrace();
                return false;
            }
        } catch (Throwable t) {
            System.out.println("Exception encountered when loading plugin: " + description.getName() + " version " + description.getVersion() + " by " + description.getAuthor());
            t.printStackTrace();
            return false;
        }

        return true;
    }

    private void loadPlugin(SeedPlugin plugin) throws PluginLoadException {
        plugin.onLoad();
        toLoad.remove(plugin.getPluginName().toLowerCase());
        toEnable.put(plugin.getPluginName().toLowerCase(), plugin);

        System.out.println("Loaded plugin " + plugin.getPluginName() + " version " + plugin.getPluginVersion() + " by " + plugin.getPluginAuthor());
    }

    private void enablePlugin(SeedPlugin plugin) throws PluginEnableException {
        plugin.onEnable();
        toEnable.remove(plugin.getPluginName().toLowerCase());
        plugins.put(plugin.getPluginName().toLowerCase(), plugin);

        System.out.println("Enabled plugin " + plugin.getPluginName() + " version " + plugin.getPluginVersion() + " by " + plugin.getPluginAuthor());
    }

    private void disablePlugin(SeedPlugin plugin) throws PluginDisableException {
        plugin.onDisable();
        plugins.remove(plugin.getPluginName().toLowerCase());

        System.out.println("Disabled plugin " + plugin.getPluginName() + " version " + plugin.getPluginVersion() + " by " + plugin.getPluginAuthor());
    }
}
