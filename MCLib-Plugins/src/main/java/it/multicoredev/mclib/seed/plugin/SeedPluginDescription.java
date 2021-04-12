package it.multicoredev.mclib.seed.plugin;

import java.io.File;

/**
 * Copyright © 2020 by Lorenzo Magni
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
public class SeedPluginDescription {
    private final String name;
    private final String main;
    private final String version;
    private final String author;
    private final String description;
    private final File file;

    public SeedPluginDescription(String name, String main, String version, String author, String description, File file) {
        this.name = name;
        this.main = main;
        this.version = version;
        this.author = author;
        this.description = description;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public String getMain() {
        return main;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "SeedPluginDescription{" +
                "name='" + name + '\'' +
                ", main='" + main + '\'' +
                ", version='" + version + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", file=" + file +
                '}';
    }
}
