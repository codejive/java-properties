package org.codejive.properties;

import static org.codejive.properties.PropertiesParser.unescape;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Properties extends AbstractMap<String, String> {
    private final LinkedHashMap<String, String> values = new LinkedHashMap<>();
    private final List<PropertiesParser.Token> tokens = new ArrayList<>();

    @Override
    public Set<Entry<String, String>> entrySet() {
        return new AbstractSet<Entry<String, String>>() {
            @Override
            public Iterator<Entry<String, String>> iterator() {
                return new Iterator<Entry<String, String>>() {
                    final Iterator<Entry<String, String>> iter = values.entrySet().iterator();

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

    public Set<String> rawKeySet() {
        return tokens.stream()
                .filter(t -> t.type == PropertiesParser.Type.KEY)
                .map(PropertiesParser.Token::getRaw)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Collection<String> rawValues() {
        return IntStream.range(0, tokens.size())
                .filter(idx -> tokens.get(idx).type == PropertiesParser.Type.KEY)
                .mapToObj(idx -> tokens.get(idx + 2).getRaw())
                .collect(Collectors.toList());
    }

    @Override
    public String get(Object key) {
        return values.get(key);
    }

    public String getRaw(String rawKey) {
        int idx = indexOf(unescape(rawKey));
        if (idx >= 0) {
            return tokens.get(idx + 2).getRaw();
        } else {
            return null;
        }
    }

    @Override
    public String put(String key, String value) {
        String rawValue = escape(value, false);
        if (values.containsKey(key)) {
            replaceValue(key, rawValue, value);
        } else {
            String rawKey = escape(key, true);
            addNewKeyValue(rawKey, key, rawValue, value);
        }
        return values.put(key, value);
    }

    /**
     * Works like `put()` but uses raw values for keys and values. This means these keys and values
     * will not be escaped before being serialized.
     *
     * @param rawKey key with which the specified value is to be associated
     * @param rawValue value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public String putRaw(String rawKey, String rawValue) {
        String key = unescape(rawKey);
        String value = unescape(rawValue);
        if (values.containsKey(key)) {
            replaceValue(key, rawValue, value);
        } else {
            addNewKeyValue(rawKey, key, rawValue, value);
        }
        return values.put(key, value);
    }

    private void replaceValue(String key, String rawValue, String value) {
        int idx = indexOf(key);
        tokens.remove(idx + 2);
        tokens.add(
                idx + 2, new PropertiesParser.Token(PropertiesParser.Type.VALUE, rawValue, value));
    }

    // Add new tokens to the end of the list of tokens
    private void addNewKeyValue(String rawKey, String key, String rawValue, String value) {
        // Add a newline whitespace token if necessary
        int idx = tokens.size();
        if (idx > 0) {
            PropertiesParser.Token token = tokens.get(idx - 1);
            if (token.getType() != PropertiesParser.Type.WHITESPACE) {
                tokens.add(new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n"));
            }
        }
        // Add tokens for key, separator and value
        tokens.add(new PropertiesParser.Token(PropertiesParser.Type.KEY, rawKey, key));
        tokens.add(new PropertiesParser.Token(PropertiesParser.Type.SEPARATOR, "="));
        tokens.add(new PropertiesParser.Token(PropertiesParser.Type.VALUE, rawValue, value));
    }

    @Override
    public String remove(Object key) {
        // TODO handle remove
        return values.remove(key);
    }

    /**
     * Gather all the comments directly before the given key and return them as a list. The list
     * will only contain those lines that immediately follow one another, once a non-comment line is
     * encountered gathering will stop.
     *
     * @param key The key to look for
     * @return A list of comment strings or an empty list if no comments lines were found or the key
     *     doesn't exist.
     */
    public List<String> getComment(String key) {
        return getComment(findCommentLines(key));
    }

    private List<String> getComment(List<Integer> indices) {
        return Collections.unmodifiableList(
                indices.stream()
                        .map(idx -> tokens.get(idx).getText())
                        .collect(Collectors.toList()));
    }

    public List<String> setComment(String key, String... comments) {
        return setComment(key, Arrays.asList(comments));
    }

    public List<String> setComment(String key, List<String> comments) {
        int idx = indexOf(key);
        if (idx < 0) {
            throw new NoSuchElementException("Key not found: " + key);
        }
        List<Integer> indices = findCommentLines(idx);
        List<String> oldcs = getComment(indices);
        String prefix = oldcs.isEmpty() ? "# " : getPrefix(oldcs.get(0));
        List<String> newcs = normalizeComments(comments, prefix);

        // Replace existing comments with new ones
        // (doing it like this respects existing whitespace)
        int i;
        for (i = 0; i < indices.size() && i < newcs.size(); i++) {
            int n = indices.get(i);
            tokens.set(n, new PropertiesParser.Token(PropertiesParser.Type.COMMENT, newcs.get(i)));
        }

        // Remove any excess lines (when there are fewer new lines than old ones)
        if (i < indices.size()) {
            int del = indices.get(i);
            int delcnt = idx - del;
            for (int j = 0; j < delcnt; j++) {
                tokens.remove(del);
            }
        }

        // Add any additional lines (when there are more new lines than old ones)
        int ins = idx;
        for (int j = i; j < newcs.size(); j++) {
            tokens.add(
                    ins++, new PropertiesParser.Token(PropertiesParser.Type.COMMENT, newcs.get(j)));
            tokens.add(ins++, new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n"));
        }

        return oldcs;
    }

    /**
     * Takes a list of comments and makes sure each of them starts with a valid comment character
     * (either '#' or '!'). If only some lines have missing comment prefixes it will use the ones
     * that were used on previous lines, if not the default will be the value passed as
     * `preferredPrefix`.
     *
     * @param comments list of comment lines
     * @param preferredPrefix the preferred prefix to use
     * @return list of comment lines
     */
    private List<String> normalizeComments(List<String> comments, String preferredPrefix) {
        ArrayList<String> res = new ArrayList<>(comments.size());
        for (String c : comments) {
            if (getPrefix(c).isEmpty()) {
                c = preferredPrefix + c;
            } else {
                preferredPrefix = getPrefix(c);
            }
            res.add(c);
        }
        return res;
    }

    private String getPrefix(String comment) {
        if (comment.startsWith("# ")) {
            return "# ";
        } else if (comment.startsWith("#")) {
            return "#";
        } else if (comment.startsWith("! ")) {
            return "! ";
        } else if (comment.startsWith("!")) {
            return "!";
        } else {
            return "";
        }
    }

    private List<Integer> findCommentLines(String key) {
        int idx = indexOf(key);
        return findCommentLines(idx);
    }

    /**
     * Returns a list of token indices pointing to all the comment lines in a comment block. A list
     * of comments is considered a block when they are consecutive lines, without any empty lines in
     * between, using the same comment symbol (so they are either all `!` comments or all `#` ones).
     */
    private List<Integer> findCommentLines(int idx) {
        List<Integer> result = new ArrayList<>();
        // Skip any preceding whitespace
        idx--;
        while (idx >= 0 && tokens.get(idx).getType() == PropertiesParser.Type.WHITESPACE) {
            idx--;
        }
        // Now find the first line of the comment block
        PropertiesParser.Token token;
        while (idx >= 0 && (token = tokens.get(idx)).getType() == PropertiesParser.Type.COMMENT) {
            result.add(0, idx);
            // Skip any preceding whitespace making sure to stop at EOL
            while (--idx >= 0 && !tokens.get(idx).isEol()) {}
            idx--;
        }
        return Collections.unmodifiableList(result);
    }

    private int indexOf(String key) {
        return tokens.indexOf(
                new PropertiesParser.Token(PropertiesParser.Type.KEY, escape(key, true), key));
    }

    private String escape(String raw, boolean forKey) {
        raw = raw.replace("\n", "\\n");
        raw = raw.replace("\r", "\\r");
        raw = raw.replace("\t", "\\t");
        raw = raw.replace("\f", "\\f");
        if (forKey) {
            raw = raw.replace(" ", "\\ ");
        }
        raw =
                replace(
                        raw,
                        "[^\\x{0000}-\\x{00FF}]",
                        m -> "\\\\u" + Integer.toString(m.group(0).charAt(0), 16));
        return raw;
    }

    private static String replace(String input, String regex, Function<Matcher, String> callback) {
        return replace(input, Pattern.compile(regex), callback);
    }

    private static String replace(String input, Pattern regex, Function<Matcher, String> callback) {
        StringBuffer resultString = new StringBuffer();
        Matcher regexMatcher = regex.matcher(input);
        while (regexMatcher.find()) {
            regexMatcher.appendReplacement(resultString, callback.apply(regexMatcher));
        }
        regexMatcher.appendTail(resultString);

        return resultString.toString();
    }

    public java.util.Properties asJUProperties() {
        return asJUProperties(null);
    }

    public java.util.Properties asJUProperties(java.util.Properties defaults) {
        java.util.Properties p = new java.util.Properties(defaults);
        p.putAll(this);
        return p;
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
        List<PropertiesParser.Token> ts = PropertiesParser.tokens(br).collect(Collectors.toList());
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
