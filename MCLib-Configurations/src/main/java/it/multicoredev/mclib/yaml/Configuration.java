package it.multicoredev.mclib.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Copyright © 2019-2020 by Lorenzo Magni
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
public final class Configuration {
    public static final SimpleDateFormat TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");

    private static final char SEPARATOR = '.';
    final Map<String, Object> self;
    private final Configuration defaults;

    private final File configFile;
    private final InputStream defaultIS;
    private boolean autosave;

    private final ThreadLocal<Yaml> yaml = ThreadLocal.withInitial(() -> {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setAllowUnicode(true);
        dumperOptions.setIndent(2);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer representer = new Representer(dumperOptions) {
            {
                representers.put(Configuration.class, data -> represent(((Configuration) data).self));
            }
        };

        LoaderOptions loaderOptions = new LoaderOptions();

        return new Yaml(new Constructor(loaderOptions), representer, dumperOptions);
    });

    Configuration(Map<?, ?> map, Configuration defaults, File configFile, InputStream defaultIS, boolean autosave) {
        self = new LinkedHashMap<>();
        this.defaults = defaults;
        this.configFile = configFile;
        this.defaultIS = defaultIS;
        this.autosave = autosave;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = (entry.getKey() == null) ? "null" : entry.getKey().toString();

            if (entry.getValue() instanceof Map) {
                self.put(key, new Configuration((Map<?, ?>) entry.getValue(), (defaults == null) ? null : defaults.getSection(key)));
            } else {
                self.put(key, entry.getValue());
            }
        }
    }

    Configuration(Map<?, ?> map, Configuration defaults) {
        this(map, defaults, null, null, false);
    }

    /**
     * Create a configuration object starting from a default one
     *
     * @param defaults A default version of the configuration
     */
    public Configuration(Configuration defaults) {
        this(new LinkedHashMap<String, Object>(), defaults);
    }

    /**
     * Create a configuration object with the corresponding file and default InputStream
     *
     * @param configFile The file that stores this configuration
     * @param defaultIS  The InputStream of the default configuration
     * @param autosave   Set this to true to automatically save this configuration to disk when it's modified
     */
    public Configuration(File configFile, InputStream defaultIS, boolean autosave) {
        this(new LinkedHashMap<String, Object>(), null, configFile, defaultIS, autosave);
    }

    /**
     * Create a configuration object with the corresponding file and default InputStream
     *
     * @param configFile The file that stores this configuration
     * @param defaultIS  The InputStream of the default configuration
     */
    public Configuration(File configFile, InputStream defaultIS) {
        this(configFile, defaultIS, false);
    }


    /**
     * Create a configuration object with the corresponding file
     *
     * @param configFile The file that stores this configuration
     */
    public Configuration(File configFile) {
        this(configFile, null, false);
    }

    /**
     * Creates the file that will store this configuration starting from a default one
     *
     * @throws IOException IOException when the file or the InputStream are null or file creation fails
     */
    public synchronized void create() throws IOException {
        if (configFile == null)
            throw new IOException("Configuration need to be initialized with a file to be able to use the create method");
        if (defaultIS == null)
            throw new IOException("Configuration need to be initialized with the input stream of a default configuration file to use the create method");

        Files.copy(defaultIS, configFile.toPath());
    }

    /**
     * Loads the configuration into this class
     *
     * @throws IOException IOException when the file is null or the file loading fails
     */
    @SuppressWarnings("unchecked")
    public synchronized void load() throws IOException {
        if (configFile == null)
            throw new IOException("Configuration need to be initialized with a file to be able to use the load method");

        Map<String, Object> map = yaml.get().loadAs(new FileInputStream(configFile), LinkedHashMap.class);
        if (map == null) map = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = (entry.getKey() == null) ? "null" : entry.getKey().toString();

            if (entry.getValue() instanceof Map) {
                self.put(key, new Configuration((Map<?, ?>) entry.getValue(), (defaults == null) ? null : defaults.getSection(key)));
            } else {
                self.put(key, entry.getValue());
            }
        }
    }

    /**
     * Loads the configuration into this class and creates it if missing
     *
     * @throws IOException IOException thrown by create and load methods
     */
    public synchronized void autoload() throws IOException {
        if (!configFile.exists() || !configFile.isFile()) create();
        load();
    }

    /**
     * Save this configuration to the corresponding file
     *
     * @throws IOException IOException when the file is null or the file saving fails
     */
    public synchronized void save() throws IOException {
        if (configFile == null)
            throw new IOException("Configuration need to be initialized with a file to be able to use the save method");

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            yaml.get().dump(self, writer);
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        }
    }

    /**
     * Gets the configuration file
     *
     * @return The configuration file
     */
    public File getFile() {
        return configFile;
    }

    /**
     * Turn on/off autosave to automatically save this configuration to disk when it's modified
     *
     * @param autosave Set this to true to automatically save this configuration to disk when it's modified
     */
    public void setAutosave(boolean autosave) {
        this.autosave = true;
    }

    /**
     * Return if autosave is enabled
     *
     * @return True if autosave is enabled
     */
    public boolean autosave() {
        return autosave;
    }

    private Configuration getSectionFor(String path) {
        int index = path.indexOf(SEPARATOR);
        if (index == -1) return this;

        String root = path.substring(0, index);
        Object section = self.get(root);
        if (section == null) {
            section = new Configuration((defaults == null) ? null : defaults.getSection(root));
            self.put(root, section);
        }

        return (Configuration) section;
    }

    private String getChild(String path) {
        int index = path.indexOf(SEPARATOR);
        return (index == -1) ? path : path.substring(index + 1);
    }

    private Date parseDate(Object date) {
        if (date instanceof String) {
            try {
                return TIMESTAMP.parse((String) date);
            } catch (ParseException ignored0) {
                try {
                    return DATE.parse((String) date);
                } catch (ParseException ignored1) {
                    try {
                        return TIME.parse((String) date);
                    } catch (ParseException ignored2) {
                        return null;
                    }
                }
            }
        } else if (date instanceof Long) {
            return new Date((Long) date);
        } else {
            return null;
        }
    }

    /**
     * Gets an object from the configuration
     *
     * @param path Path of the object
     * @param def  Default value for the object
     * @param <T>  Type of the object
     * @return The object stored in the configuration or the default if missing
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path, T def) {
        Configuration section = getSectionFor(path);
        Object val;
        if (section == this) {
            val = self.get(path);
        } else {
            val = section.get(getChild(path), def);
        }

        if (val == null && def instanceof Configuration) {
            self.put(path, def);
        }

        return (val != null) ? (T) val : def;
    }

    /**
     * Checks if the configuration contains a path
     *
     * @param path Path to check
     * @return True if the path exists in the configuration
     */
    public boolean contains(String path) {
        return get(path, null) != null;
    }

    /**
     * Gets an object from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public Object get(String path) {
        return get(path, getDefault(path));
    }

    /**
     * Gets the default value of an object from the configuration if exists
     *
     * @param path Path of the object
     * @return The default value of the object stored in the configuration
     */
    public Object getDefault(String path) {
        return (defaults == null) ? null : defaults.get(path);
    }

    /**
     * Sets an object to the given path.
     * Pay attention, if autosave is enabled if save fails throwing an exception, the exception is ignored
     *
     * @param path  Path of the object you want to set
     * @param value The value of the object to set
     */
    public void set(String path, Object value) {
        if (value instanceof Map) {
            value = new Configuration((Map<?, ?>) value, (defaults == null) ? null : defaults.getSection(path));
        } else if (value instanceof Date) {
            value = TIMESTAMP.format(value);
        }

        Configuration section = getSectionFor(path);
        if (section == this) {
            if (value == null) {
                self.remove(path);
            } else {
                self.put(path, value);
            }
        } else {
            section.set(getChild(path), value);
        }

        if (autosave) {
            try {
                save();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Gets a section of the configuration
     *
     * @param path Path of the section
     * @return A Configuration object that corresponds to the section
     */
    public Configuration getSection(String path) {
        Object def = getDefault(path);
        return (Configuration) get(path, (def instanceof Configuration) ? def : new Configuration((defaults == null) ? null : defaults.getSection(path)));
    }

    /**
     * Gets a list of top level keys for this configuration
     *
     * @return A list of top level keys
     */
    public Collection<String> getKeys() {
        return new LinkedHashSet<>(self.keySet());
    }

    /**
     * Gets a byte from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public byte getByte(String path) {
        Object def = getDefault(path);
        return getByte(path, (def instanceof Number) ? ((Number) def).byteValue() : 0);
    }

    /**
     * Gets a byte from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public byte getByte(String path, byte def) {
        Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).byteValue() : def;
    }

    /**
     * Gets a byte list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<Byte> getByteList(String path) {
        List<?> list = getList(path);
        List<Byte> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).byteValue());
            }
        }
        return result;
    }

    /**
     * Gets a short from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public short getShort(String path) {
        Object def = getDefault(path);
        return getShort(path, (def instanceof Number) ? ((Number) def).shortValue() : 0);
    }

    /**
     * Gets a short from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public short getShort(String path, short def) {
        Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).shortValue() : def;
    }

    /**
     * Gets a short list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<Short> getShortList(String path) {
        List<?> list = getList(path);
        List<Short> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).shortValue());
            }
        }
        return result;
    }

    /**
     * Gets a int from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public int getInt(String path) {
        Object def = getDefault(path);
        return getInt(path, (def instanceof Number) ? ((Number) def).intValue() : 0);
    }

    /**
     * Gets a int from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public int getInt(String path, int def) {
        Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).intValue() : def;
    }

    /**
     * Gets a int list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<Integer> getIntList(String path) {
        List<?> list = getList(path);
        List<Integer> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).intValue());
            }
        }
        return result;
    }

    /**
     * Gets a long from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public long getLong(String path) {
        Object def = getDefault(path);
        return getLong(path, (def instanceof Number) ? ((Number) def).longValue() : 0);
    }

    /**
     * Gets a long from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public long getLong(String path, long def) {
        Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).longValue() : def;
    }

    /**
     * Gets a long list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<Long> getLongList(String path) {
        List<?> list = getList(path);
        List<Long> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).longValue());
            }
        }
        return result;
    }

    /**
     * Gets a float from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public float getFloat(String path) {
        Object def = getDefault(path);
        return getFloat(path, (def instanceof Number) ? ((Number) def).floatValue() : 0);
    }

    /**
     * Gets a float from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public float getFloat(String path, float def) {
        Number val = get(path, def);
        return (val != null) ? val.floatValue() : def;
    }

    /**
     * Gets a float list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<Float> getFloatList(String path) {
        List<?> list = getList(path);
        List<Float> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).floatValue());
            }
        }
        return result;
    }

    /**
     * Gets a double from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public double getDouble(String path) {
        Object def = getDefault(path);
        return getDouble(path, (def instanceof Number) ? ((Number) def).doubleValue() : 0);
    }

    /**
     * Gets a double from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public double getDouble(String path, double def) {
        Number val = get(path, def);
        return (val != null) ? val.doubleValue() : def;
    }

    /**
     * Gets a double list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<Double> getDoubleList(String path) {
        List<?> list = getList(path);
        List<Double> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).doubleValue());
            }
        }
        return result;
    }

    /**
     * Gets a boolean from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public boolean getBoolean(String path) {
        Object def = getDefault(path);
        return getBoolean(path, (def instanceof Boolean) ? (Boolean) def : false);
    }

    /**
     * Gets a boolean from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public boolean getBoolean(String path, boolean def) {
        Boolean val = get(path, def);
        return (val != null) ? val : def;
    }

    /**
     * Gets a boolean list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<Boolean> getBooleanList(String path) {
        List<?> list = getList(path);
        List<Boolean> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Boolean) {
                result.add((Boolean) object);
            }
        }
        return result;
    }

    /**
     * Gets a char from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public char getChar(String path) {
        Object def = getDefault(path);
        return getChar(path, (def instanceof Character) ? (Character) def : '\u0000');
    }

    /**
     * Gets a char from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public char getChar(String path, char def) {
        Character val = get(path, def);
        return (val != null) ? val : def;
    }

    /**
     * Gets a char list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<Character> getCharList(String path) {
        List<?> list = getList(path);
        List<Character> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Character) {
                result.add((Character) object);
            }
        }
        return result;
    }

    /**
     * Gets a String from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public String getString(String path) {
        Object def = getDefault(path);
        return getString(path, (def instanceof String) ? (String) def : "");
    }

    /**
     * Gets a String from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public String getString(String path, String def) {
        String val = get(path, def);
        return (val != null) ? val : def;
    }

    /**
     * Gets a String list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<String> getStringList(String path) {
        List<?> list = getList(path);
        List<String> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof String) {
                result.add((String) object);
            }
        }
        return result;
    }

    /**
     * Gets a Date from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public Date getDate(String path) {
        Object obj = get(path);

        if (obj != null) {
            if (obj instanceof String) {
                Date defDate = parseDate(obj);
                return getDate(path, defDate);
            } else if (obj instanceof Number) {
                Date defDate = parseDate(((Number) obj).longValue());
                return getDate(path, defDate);
            }
        }

        return null;
    }

    /**
     * Gets a Date from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public Date getDate(String path, Date def) {
        Object val = getDefault(path);

        if (val != null) {
            if (val instanceof String) {
                Date date = parseDate((String) val);
                return date != null ? date : def;
            } else if (val instanceof Number) {
                Date date = parseDate(((Number) val).longValue());
                return date != null ? date : def;
            }
        }

        return def;
    }

    /**
     * Gets a Date list from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration
     */
    public List<Date> getDateList(String path) {
        List<?> list = getList(path);
        List<Date> result = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof String) {
                result.add(parseDate(object));
            } else if (object instanceof Number) {
                result.add(parseDate(object));
            }
        }
        return result;
    }

    /**
     * Gets a list of objects from the configuration
     *
     * @param path Path of the object
     * @return The object stored in the configuration or the default value if missing
     */
    public List<?> getList(String path) {
        Object def = getDefault(path);
        return getList(path, (def instanceof List<?>) ? (List<?>) def : Collections.EMPTY_LIST);
    }

    /**
     * Gets a list of objects from the configuration
     *
     * @param path Path of the object
     * @param def  Default value of the object if nothing is found
     * @return The object stored in the configuration or the default value if missing
     */
    public List<?> getList(String path, List<?> def) {
        Object val = get(path, def);
        return (val != null) ? (List<?>) val : def;
    }
}