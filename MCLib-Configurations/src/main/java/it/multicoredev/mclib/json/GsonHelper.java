package it.multicoredev.mclib.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Copyright © 2022 by Lorenzo Magni
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
public class GsonHelper {
    private final Gson gson;

    /**
     * Creates a new GsonHelper with the given Gson.
     *
     * @param gson the Gson object to use
     */
    public GsonHelper(Gson gson) {
        this.gson = gson;
    }

    /**
     * Creates a new GsonHelper with the default Gson.
     */
    public GsonHelper() {
        this(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create());
    }

    /**
     * Creates a new GsonHelper with the default Gson and custom Type Adapters.
     *
     * @param typeAdapters a map of Type Adapters to use
     */
    public GsonHelper(Map<Type, Object> typeAdapters) {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
        typeAdapters.forEach(builder::registerTypeAdapter);
        this.gson = builder.create();
    }

    /**
     * Creates a new GsonHelper with the default Gson and custom Type Adapters.
     *
     * @param typeAdapters a list of {@link TypeAdapter}s to use
     */
    public GsonHelper(TypeAdapter... typeAdapters) {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
        for (TypeAdapter typeAdapter : typeAdapters) {
            builder.registerTypeAdapter(typeAdapter.getType(), typeAdapter.getAdapter());
        }
        this.gson = builder.create();
    }

    /**
     * Saves the given object to the given file.
     *
     * @param object the object to save
     * @param file   the file to save to
     * @throws IOException if an I/O error occurs
     */
    public void save(Object object, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(gson.toJson(object));
        }
    }

    /**
     * Saves asynchronously the given object to the given file.
     *
     * @param object the object to save
     * @param file   the file to save to
     */
    public void saveAsync(Object object, File file) {
        new Thread(() -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(gson.toJson(object));
            } catch (IOException ignored) {
            }
        }).start();
    }

    /**
     * Loads the given file and returns the object.
     *
     * @param file the file to load
     * @param type the type of the object
     * @param <T>  the type of the object
     * @return the object
     * @throws IOException         if an I/O error occurs
     * @throws JsonSyntaxException if the file is not valid
     */
    public <T> T load(File file, Type type) throws IOException, JsonSyntaxException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        }
    }

    /**
     * Loads the given file and returns the object after completing missing fields.
     *
     * @param file the file to load
     * @param type the type of the object
     * @param <T>  the type of the object
     * @return the object
     * @throws IOException         if an I/O error occurs
     * @throws JsonSyntaxException if the file is not valid
     */
    public <T extends JsonConfig> T loadAndCompleteMissing(File file, Type type) throws IOException, JsonSyntaxException {
        T obj = load(file, type);
        if (obj.completeMissing()) save(obj, file);
        return obj;
    }

    /**
     * Load the given file, if it exists, and return the object after completing missing fields, otherwise save the default value.
     *
     * @param file the file to load
     * @param def  the default value
     * @param type the type of the object
     * @param <T>  the type of the object
     * @return the object
     * @throws IOException         if an I/O error occurs
     * @throws JsonSyntaxException if the file is not valid
     */
    public <T extends JsonConfig> T autoload(File file, T def, Type type) throws IOException, JsonSyntaxException {
        if (!file.exists() || !file.isFile()) {
            save(def, file);
            return def;
        } else {
            return loadAndCompleteMissing(file, type);
        }
    }
}
