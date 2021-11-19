package org.codejive.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Properties extends AbstractMap<String, String> {
    private final LinkedHashMap<String, String> values = new LinkedHashMap<>();
    private final List<PropertiesParser.Token> tokens = new ArrayList<>();

    @Override
    public Set<Entry<String, String>> entrySet() {
        return new AbstractSet<Entry<String, String>>() {
            @Override
            public Iterator<Entry<String, String>> iterator() {
                return new Iterator<Entry<String, String>>() {
                    Iterator<Entry<String, String>> iter = values.entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @Override
                    public Entry<String, String> next() {
                        return iter.next();
                    }

                    @Override
                    public void remove() {
                        // TODO handle remove
                        iter.remove();
                    }
                };
            }

            @Override
            public int size() {
                return values.entrySet().size();
            }
        };
    }

    @Override
    public String put(String key, String value) {
        // TODO handle adds and replaces
        return values.put(key, value);
    }

    @Override
    public String remove(Object key) {
        // TODO handle remove
        return values.remove(key);
    }

    public void load(Path file) throws IOException {
        try (Reader br = Files.newBufferedReader(file)) {
            load(br);
        }
    }

    public void load(Reader reader) throws IOException {
        tokens.clear();
        BufferedReader br =
                reader instanceof BufferedReader
                        ? (BufferedReader) reader
                        : new BufferedReader(reader);
        List<PropertiesParser.Token> ts = PropertiesParser.tokens(br)
                .collect(Collectors.toList());
        tokens.addAll(ts);
        String key = null;
        for (PropertiesParser.Token token : tokens) {
            if (token.type == PropertiesParser.Type.KEY) {
                key = token.getText();
            } else if (token.type == PropertiesParser.Type.VALUE) {
                values.put(key, token.getText());
            }
        }
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
        try (Writer bw = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING)) {
            store(bw);
        }
    }

    public void store(Writer writer) throws IOException {
        for (PropertiesParser.Token token : tokens) {
            writer.write(token.getRaw());
        }
    }
}
