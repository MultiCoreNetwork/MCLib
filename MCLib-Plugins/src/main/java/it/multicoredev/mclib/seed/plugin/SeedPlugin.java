package it.multicoredev.mclib.seed.plugin;

import it.multicoredev.mclib.seed.SeedSystem;
import it.multicoredev.mclib.seed.exceptions.PluginDisableException;
import it.multicoredev.mclib.seed.exceptions.PluginEnableException;
import it.multicoredev.mclib.seed.exceptions.PluginLoadException;

import java.io.File;
import java.io.InputStream;

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
public class SeedPlugin {
    private SeedPluginDescription description;
    protected SeedSystem seedSystem;

    /**
     * Method called during plugin load phase
     *
     * @throws PluginLoadException PluginLoadException printed to System output by the {@link SeedPluginManager}
     */
    public void onLoad() throws PluginLoadException {
    }

    /**
     * Method called when the plugin is enabled
     *
     * @throws PluginEnableException PluginEnableException printed to System output by the {@link SeedPluginManager}
     */
    public void onEnable() throws PluginEnableException {
    }

    /**
     * Method called when the plugin is disabled
     *
     * @throws PluginDisableException PluginDisableException printed to System output by the {@link SeedPluginManager}
     */
    public void onDisable() throws PluginDisableException {
    }

    /**
     * Gets the folder that should contains the plugin data
     *
     * @return The data folder of the plugin
     */
    public final File getDataFolder() {
        return new File(seedSystem.getPluginsDir(), description.getName());
    }

    /**
     * Gets a file inside plugin jar as an InputStream
     *
     * @param resource The name of the file
     * @return The InputStream of the file
     */
    public final InputStream getResourceAsStream(String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }

    /**
     * Gets the description of the plugin
     *
     * @return The {@link SeedPluginDescription} of the plugin
     */
    public final SeedPluginDescription getPluginDescription() {
        return description;
    }

    /**
     * Gets the name of the plugin
     *
     * @return The name of the plugin
     */
    public final String getPluginName() {
        return description.getName();
    }


    /**
     * Gets the author of the plugin
     *
     * @return The author of the plugin
     */
    public final String getPluginAuthor() {
        return description.getAuthor();
    }

    /**
     * Gets the version of the plugin
     *
     * @return The plugin version
     */
    public final String getPluginVersion() {
        return description.getVersion();
    }

    /**
     * Gets the file of the plugin
     *
     * @return the file of the plugin
     */
    public final File getPluginFile() {
        return description.getFile();
    }

    /**
     * Initialize this plugin
     *
     * @param description The description of the plugin
     * @param seedSystem  The SeedSystem
     */
    final void init(SeedPluginDescription description, SeedSystem seedSystem) {
        this.description = description;
        this.seedSystem = seedSystem;
    }
}
