package org.codejive.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Properties {
    private final Map<String, String> values = new TreeMap<>();

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public String setProperty(String key, String value) {
        return values.put(key, value);
    }

    public void setProperties(Properties props) {
        setProperties(props.values);
    }

    public void setProperties(Map<String, String> props) {
        values.putAll(props);
    }

    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public Set<String> keySet() {
        return values.keySet();
    }

    public String remove(String key) {
        return values.remove(key);
    }

    public void load(Path file) throws IOException {
        load(Files.newBufferedReader(file));
    }

    public void load(Reader reader) throws IOException {
        BufferedReader br =
                reader instanceof BufferedReader
                        ? (BufferedReader) reader
                        : new BufferedReader(reader);
    }

    public static Properties loadProperties(Path file) throws IOException {
        Properties props = new Properties();
        props.load(file);
        return props;
    }

    public static Properties loadProperties(Reader reader) throws IOException {
        Properties props = new Properties();
        props.load(reader);
        return props;
    }

    public void store(Path file) throws IOException {
        store(Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING));
    }

    public void store(Writer writer) throws IOException {}
}
