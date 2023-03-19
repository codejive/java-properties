package org.codejive.properties;

import static org.codejive.properties.PropertiesParser.unescape;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class is a replacement for <code>java.util.Properties</code>, with the difference that it
 * properly supports comments both for reading and writing. It also maintains an exact
 * representation of the input, meaning that when an input is read and later written out again the
 * output will match the input exactly. Methods exist for obtaining and setting comments on
 * properties.
 */
public class Properties extends AbstractMap<String, String> {
    private final LinkedHashMap<String, String> values;
    private final List<PropertiesParser.Token> tokens;
    private final Properties defaults;

    public Properties() {
        this((Properties) null);
    }

    public Properties(Properties defaults) {
        this.defaults = defaults;
        values = new LinkedHashMap<>();
        tokens = new ArrayList<>();
    }

    private Properties(Properties defaults, List<PropertiesParser.Token> tokens) {
        this.defaults = defaults;
        values = new LinkedHashMap<>();
        this.tokens = tokens;
        rawEntrySet()
                .forEach(
                        e -> {
                            values.put(unescape(e.getKey()), unescape(e.getValue()));
                        });
    }

    /**
     * Searches for the property with the specified key in this property list. If the key is not
     * found in this property list, the default property list, and its defaults, recursively, are
     * then checked. The method returns null if the property is not found.
     *
     * @param key the key to look up.
     * @return the value in this property list with the specified key value or <code>null</code>.
     */
    public String getProperty(String key) {
        return getProperty(key, null);
    }

    /**
     * Searches for the property with the specified key in this property list. If the key is not
     * found in this property list, the default property list, and its defaults, recursively, are
     * then checked. The method returns the default value argument if the property is not found.
     *
     * @param key the key to look up.
     * @param defaultValue the value to return if no mapping was found for the key.
     * @return the value in this property list with the specified key value or the value of <code>
     *     defaultValue</code>.
     */
    public String getProperty(String key, String defaultValue) {
        if (containsKey(key)) {
            return get(key);
        } else if (defaults != null) {
            return defaults.getProperty(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Searches for the property with the specified key in this property list. If the key is not
     * found in this property list, the default property list, and its defaults, recursively, are
     * then checked. The method returns the property's comments or an empty list if the property is
     * not found.
     *
     * @param key the key to look up.
     * @return the comments for the indicated property or an empty list.
     */
    public List<String> getPropertyComment(String key) {
        if (containsKey(key)) {
            return getComment(key);
        } else if (defaults != null) {
            return defaults.getPropertyComment(key);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Associates the specified value with the specified key in this properties table. If the
     * properties previously contained a mapping for the key, the old value is replaced. If any
     * comment lines are supplied they will be prepended to the property.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param comment comment lines to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public String setProperty(String key, String value, String... comment) {
        return putCommented(key, value, comment);
    }

    /**
     * Returns an enumeration of keys from this property list where the key and its corresponding
     * value are strings, including distinct keys in the default property list if a key of the same
     * name has not already been found from the main properties table.
     *
     * @return an enumeration of keys in this property list where the key and its corresponding
     *     value are strings, including the keys in the default property list.
     */
    public Enumeration<String> propertyNames() {
        return Collections.enumeration(stringPropertyNames());
    }

    /**
     * Returns an unmodifiable set of keys from this property list where the key and its
     * corresponding value are strings, including distinct keys in the default property list if a
     * key of the same name has not already been found from the main properties table.
     *
     * @return an unmodifiable set of keys in this property list where the key and its corresponding
     *     value are strings, including the keys in the default property list.
     */
    public Set<String> stringPropertyNames() {
        return Collections.unmodifiableSet(flatten().keySet());
    }

    /**
     * Prints this property list out to the specified output stream.
     *
     * @param out a <code>PrintStream</code> object
     */
    public void list(PrintStream out) {
        try {
            flatten().store(out);
        } catch (IOException e) {
            // Ignore any errors
        }
    }

    /**
     * Prints this property list out to the specified writer.
     *
     * @param out a <code>PrintWriter</code> object
     */
    public void list(PrintWriter out) {
        try {
            flatten().store(out);
        } catch (IOException e) {
            // Ignore any errors
        }
    }

    /**
     * Loads all the properties represented by the XML document on the specified input stream into
     * this properties table. NB: comments are not supported by this format.
     *
     * @param in the input stream from which to read the XML document.
     * @throws IOException Thrown when any IO error occurs during loading
     */
    public void loadFromXML(InputStream in) throws IOException {
        java.util.Properties p = new java.util.Properties();
        p.loadFromXML(in);
        p.forEach((key, value) -> put(Objects.toString(key), Objects.toString(value)));
    }

    /**
     * Emits an XML document representing all the properties contained in this table.
     *
     * @param os the output stream on which to emit the XML document.
     * @param comment a description of the property list, or null if no comment is desired.
     */
    public void storeToXML(OutputStream os, String comment) throws IOException {
        asJUProperties().storeToXML(os, comment);
    }

    /**
     * Emits an XML document representing all the properties contained in this table.
     *
     * @param os the output stream on which to emit the XML document.
     * @param comment a description of the property list, or null if no comment is desired.
     * @param encoding the name of a supported character encoding
     */
    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        asJUProperties().storeToXML(os, comment, encoding);
    }

    /**
     * Returns the current properties table with all its defaults as a single flattened properties
     * table. NB: Result will have no formatting or comments!
     *
     * @return a <code>Properties</code> object
     * @deprecated Use <code>flattened()</code>
     */
    @Deprecated
    public Properties flatten() {
        return flattened();
    }

    /**
     * Returns the current properties table with all its defaults as a single flattened properties
     * table. NB: Result will have no formatting or comments!
     *
     * @return a <code>Properties</code> object
     */
    public Properties flattened() {
        Properties result = new Properties();
        flatten(result);
        return result;
    }

    private void flatten(Properties target) {
        if (defaults != null) {
            defaults.flatten(target);
        }
        target.putAll(this);
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return new AbstractSet<Entry<String, String>>() {
            @Override
            public Iterator<Entry<String, String>> iterator() {
                return new Iterator<Entry<String, String>>() {
                    private final Iterator<Entry<String, String>> iter =
                            values.entrySet().iterator();
                    private Entry<String, String> currentEntry;

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @Override
                    public Entry<String, String> next() {
                        return (currentEntry = iter.next());
                    }

                    @Override
                    public void remove() {
                        if (currentEntry != null) {
                            removeItem(currentEntry.getKey());
                        }
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

    /**
     * Works like <code>keySet()</code> but returning the keys' raw values. Meaning that the keys
     * haven't been unescaped before being returned.
     *
     * @return A set of raw key values
     */
    public Set<String> rawKeySet() {
        return tokens.stream()
                .filter(t -> t.type == PropertiesParser.Type.KEY)
                .map(PropertiesParser.Token::getRaw)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Works like <code>values()</code> but returning the raw values. Meaning that the values have
     * not been unescaped before being returned.
     *
     * @return a collection of raw values.
     */
    public Collection<String> rawValues() {
        return combined(tokens)
                .filter(ts -> ts.get(0).type == PropertiesParser.Type.KEY)
                .map(ts -> ts.get(2).getRaw())
                .collect(Collectors.toList());
    }

    /**
     * Works like <code>entrySet()</code> but returning the raw values. Meaning that the values have
     * not been unescaped before being returned.
     *
     * @return A set of raw key-value entries
     */
    public Set<Entry<String, String>> rawEntrySet() {
        return combined(tokens)
                .filter(ts -> ts.get(0).type == PropertiesParser.Type.KEY)
                .map(ts -> new SimpleEntry<>(ts.get(0).getRaw(), ts.get(2).getRaw()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String get(Object key) {
        return values.get(key);
    }

    /**
     * Works like <code>get()</code> but returns the raw value associated with the given raw key.
     * This means that the value won't be unescaped before being returned.
     *
     * @param rawKey The key, in raw format, to look up
     * @return A raw value or <code>null</code> if the key wasn't found
     */
    public String getRaw(String rawKey) {
        Cursor pos = indexOf(unescape(rawKey));
        if (pos.hasToken()) {
            validate(pos.nextIf(PropertiesParser.Type.KEY), pos);
            validate(pos.nextIf(PropertiesParser.Type.SEPARATOR), pos);
            validate(pos.isType(PropertiesParser.Type.VALUE), pos);
            return pos.raw();
        } else {
            return null;
        }
    }

    @Override
    public String put(String key, String value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        String rawValue = escapeValue(value);
        if (values.containsKey(key)) {
            replaceValue(key, rawValue, value);
        } else {
            String rawKey = escapeKey(key);
            addNewKeyValue(rawKey, key, rawValue, value);
        }
        return values.put(key, value);
    }

    /**
     * Associates the specified value with the specified key in this properties table. If the
     * properties previously contained a mapping for the key, the old value is replaced. If any
     * comment lines are supplied they will be prepended to the property.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param comment comment lines to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public String putCommented(String key, String value, String... comment) {
        String old = put(key, value);
        setComment(key, comment);
        return old;
    }

    /**
     * Works like <code>put()</code> but uses raw values for keys and values. This means these keys
     * and values will not be escaped before being stored.
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
        Cursor pos = indexOf(key);
        validate(pos.nextIf(PropertiesParser.Type.KEY), pos);
        validate(pos.nextIf(PropertiesParser.Type.SEPARATOR), pos);
        validate(pos.isType(PropertiesParser.Type.VALUE), pos);
        pos.replace(new PropertiesParser.Token(PropertiesParser.Type.VALUE, rawValue, value));
    }

    // Add new tokens to the end of the list of tokens
    private Cursor addNewKeyValue(String rawKey, String key, String rawValue, String value) {
        // Track back from end until we encounter the last VALUE token (if any)
        Cursor pos = last();
        while (pos.isType(PropertiesParser.Type.WHITESPACE, PropertiesParser.Type.COMMENT)) {
            pos.prev();
        }
        // Make sure we're either at the start or we've found a VALUE
        validate(pos.atStart() || pos.isType(PropertiesParser.Type.VALUE), pos);
        // Add a newline whitespace token if necessary
        if (pos.hasToken()) {
            pos.next();
            if (pos.isEol()) {
                pos.next().addEol().prev();
            } else {
                pos.addEol();
            }
        } else {
            // We're at the start, meaning there are no properties yet,
            // but there might be comments, so we move forward again,
            // skipping any header comments
            pos = skipHeaderCommentLines();
            if (pos.position() > 0) {
                // We have to make sure there are at least 2 EOLs after the last comment
                int eols = pos.prevCount(t -> t.isEol());
                for (int i = 0; i < 2 - eols; i++) {
                    pos.addEol();
                }
            }
        }
        // Add tokens for key, separator and value
        pos.add(new PropertiesParser.Token(PropertiesParser.Type.KEY, rawKey, key));
        pos.add(new PropertiesParser.Token(PropertiesParser.Type.SEPARATOR, "="));
        pos.add(new PropertiesParser.Token(PropertiesParser.Type.VALUE, rawValue, value));
        return pos;
    }

    @Override
    public String remove(Object key) {
        String skey = key.toString();
        if (containsKey(key)) {
            removeItem(skey);
            return values.remove(skey);
        } else {
            return null;
        }
    }

    // Calling code MUST make sure skey exists!
    private void removeItem(String skey) {
        setComment(skey, Collections.emptyList());
        Cursor pos = indexOf(skey);
        validate(pos.isType(PropertiesParser.Type.KEY), pos);
        pos.remove();
        validate(pos.isType(PropertiesParser.Type.SEPARATOR), pos);
        pos.remove();
        validate(pos.isType(PropertiesParser.Type.VALUE), pos);
        pos.remove();
        if (pos.isEol()) {
            pos.remove();
        }
    }

    @Override
    public void clear() {
        tokens.clear();
        values.clear();
    }

    /**
     * Gather all the comments directly before the given key and return them as a list. The list
     * will only contain those lines that immediately follow one another, once a non-comment line is
     * encountered gathering will stop. The returned values will include the comment character that
     * the line started with in the original input.
     *
     * @param key The key to look for
     * @return A list of comment strings or an empty list if no comments lines were found or the key
     *     doesn't exist.
     */
    public List<String> getComment(String key) {
        return getComment(findPropertyCommentLines(key));
    }

    private List<String> getComment(List<Integer> indices) {
        return Collections.unmodifiableList(
                indices.stream()
                        .map(idx -> tokens.get(idx).getText())
                        .collect(Collectors.toList()));
    }

    /**
     * Adds the given comments to the item indicated by the given key. Each comment will be put on a
     * separate line. Each comment should start with one of the valid comment symbols <code>#</code>
     * or <code>!</code>, but if none is encountered the code will select one for you (it will look
     * at any existing comments, or at symbols found on previous items and as a last result will use
     * <code># </code>).
     *
     * @param key The key to look for
     * @param comments The comments to add to the item
     * @return The previous list of comments, if any
     * @throws NoSuchElementException Thrown when they key couldn't be found
     */
    public List<String> setComment(String key, String... comments) {
        return setComment(key, Arrays.asList(comments));
    }

    /**
     * Adds the list of comments to the item indicated by the given key. Each comment will be put on
     * a separate line. Each comment should start with one of the valid comment symbols <code>#
     * </code> or <code>!</code>, but if none is encountered the code will select one for you (it
     * will look at any existing comments, or at symbols found on previous items and as a last
     * result will use <code># </code>).
     *
     * @param key The key to look for
     * @param comments The list of comments to add to the item
     * @return The previous list of comments, if any
     * @throws NoSuchElementException Thrown when they key couldn't be found
     */
    public List<String> setComment(String key, List<String> comments) {
        Cursor pos = indexOf(key);
        if (!pos.hasToken()) {
            throw new NoSuchElementException("Key not found: " + key);
        }
        List<Integer> indices = findPropertyCommentLines(pos);
        List<String> oldcs = getComment(indices);
        setComment(indices, pos, comments);
        return oldcs;
    }

    private Cursor setComment(List<Integer> indices, Cursor pos, List<String> comments) {
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
            Cursor del = index(indices.get(i));
            int delcnt = pos.position() - del.position();
            for (int j = 0; j < delcnt; j++) {
                del.remove();
            }
        }

        // Add any additional lines (when there are more new lines than old ones)
        for (int j = i; j < newcs.size(); j++) {
            pos.add(new PropertiesParser.Token(PropertiesParser.Type.COMMENT, newcs.get(j)));
            pos.addEol();
        }

        return pos;
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

    private List<Integer> findPropertyCommentLines(String key) {
        Cursor pos = indexOf(key);
        if (pos.hasToken()) {
            return findPropertyCommentLines(pos);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns a list of token indices pointing to all the comment lines in a comment block. A list
     * of comments is considered a block when they are consecutive lines, without any empty lines in
     * between, using the same comment symbol (so they are either all `!` comments or all `#` ones).
     */
    private List<Integer> findPropertyCommentLines(Cursor pos) {
        List<Integer> result = new ArrayList<>();
        Cursor fpos = pos.copy();
        validate(fpos.isType(PropertiesParser.Type.KEY), pos);
        fpos.prev();
        // Skip a single preceding whitespace if it is NOT an EOL token
        fpos.prevIf(PropertiesParser.Token::isWs);
        // Skip a single preceding whitespace if it IS an EOL token
        fpos.prevIf(PropertiesParser.Token::isEol);
        // Now find the first line of the comment block
        while (fpos.prevIf(PropertiesParser.Type.COMMENT)) {
            result.add(0, fpos.position() + 1);
            // Skip a single preceding whitespace if it is NOT an EOL token
            fpos.prevIf(PropertiesParser.Token::isWs);
            // Skip a single preceding whitespace if it IS an EOL token
            fpos.prevIf(PropertiesParser.Token::isEol);
        }
        return Collections.unmodifiableList(result);
    }

    private Cursor indexOf(String key) {
        return index(
                tokens.indexOf(
                        new PropertiesParser.Token(
                                PropertiesParser.Type.KEY, escapeKey(key), key)));
    }

    private static String escapeValue(String value) {
        return value.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\f", "\\f");
    }

    private static String escapeKey(String key) {
        return escapeValue(key).replace(" ", "\\ ");
    }

    private static String escapeUnicode(String text) {
        return replace(
                text,
                "[^\\x{0000}-\\x{00FF}]",
                m -> "\\\\u" + String.format("%04x", (int) m.group(0).charAt(0)));
    }

    private static String unescapeUnicode(String escape) {
        StringBuilder txt = new StringBuilder();
        for (int i = 0; i < escape.length(); i++) {
            char ch = escape.charAt(i);
            if (ch == '\\') {
                ch = escape.charAt(++i);
                if (ch == 'u') {
                    String num = escape.substring(i + 1, i + 5);
                    txt.append((char) Integer.parseInt(num, 16));
                    i += 4;
                } else {
                    txt.append('\\');
                    txt.append(ch);
                }
            } else {
                txt.append(ch);
            }
        }
        return txt.toString();
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

    /**
     * Returns a copy of the object where all characters, in keys and values that are not in the
     * Unicode range of 0x0000-0x00FF, have been escaped. This is useful when using <code>store()
     * </code> to write to an output that does not support UTF8.
     *
     * @return A <code>Properties</code> with encoded keys and values
     */
    public Properties escaped() {
        return new Properties(defaults != null ? defaults.escaped() : null, escapeTokens(tokens));
    }

    private static List<PropertiesParser.Token> escapeTokens(List<PropertiesParser.Token> tokens) {
        return mapKeyValues(
                tokens,
                ts -> Arrays.asList(escapeToken(ts.get(0)), ts.get(1), escapeToken(ts.get(2))));
    }

    private static PropertiesParser.Token escapeToken(PropertiesParser.Token token) {
        String raw = escapeUnicode(token.raw);
        if (!raw.equals(token.raw)) {
            token = new PropertiesParser.Token(token.type, raw, token.text);
        }
        return token;
    }

    /**
     * Returns a copy of the object where all Unicode escape sequences, in keys and values, have
     * been decoded into their actual Unicode characters. This is useful when using <code>store()
     * </code> to write to an output that supports UTF8.
     *
     * @return A <code>Properties</code> without Unicode escape sequences in its keys and values
     */
    public Properties unescaped() {
        return new Properties(
                defaults != null ? defaults.unescaped() : null, unescapeTokens(tokens));
    }

    private static List<PropertiesParser.Token> unescapeTokens(
            List<PropertiesParser.Token> tokens) {
        return mapKeyValues(
                tokens,
                ts -> Arrays.asList(unescapeToken(ts.get(0)), ts.get(1), unescapeToken(ts.get(2))));
    }

    private static PropertiesParser.Token unescapeToken(PropertiesParser.Token token) {
        String raw = unescapeUnicode(token.raw);
        if (!raw.equals(token.raw)) {
            token = new PropertiesParser.Token(token.type, raw, token.text);
        }
        return token;
    }

    private static List<PropertiesParser.Token> mapKeyValues(
            List<PropertiesParser.Token> tokens,
            Function<List<PropertiesParser.Token>, List<PropertiesParser.Token>> mapper) {
        return combined(tokens)
                .map(
                        ts -> {
                            if (ts.get(0).type == PropertiesParser.Type.KEY) {
                                return mapper.apply(ts);
                            } else {
                                return ts;
                            }
                        })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static Stream<List<PropertiesParser.Token>> combined(
            List<PropertiesParser.Token> tokens) {
        Iterator<List<PropertiesParser.Token>> iter =
                new Iterator<List<PropertiesParser.Token>>() {
                    Iterator<PropertiesParser.Token> i = tokens.iterator();

                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public List<PropertiesParser.Token> next() {
                        PropertiesParser.Token t = i.next();
                        if (t.type == PropertiesParser.Type.KEY) {
                            return Arrays.asList(t, i.next(), i.next());
                        } else {
                            return Collections.singletonList(t);
                        }
                    }
                };

        return StreamSupport.stream(
                Spliterators.spliterator(iter, tokens.size(), Spliterator.SORTED), false);
    }

    /**
     * Returns a <code>java.util.Properties</code> with the same contents as this object. The
     * information is a copy, changes to one Properties object will not affect the other.
     *
     * @return a <code>java.util.Properties</code> object
     */
    public java.util.Properties asJUProperties() {
        java.util.Properties def = defaults != null ? defaults.asJUProperties() : null;
        java.util.Properties p = new java.util.Properties(def);
        p.putAll(this);
        return p;
    }

    /**
     * Loads the contents from the given file and stores it in this object. This includes not only
     * properties but also all whitespace and any comments that are encountered.
     *
     * @param file a path to the file to load
     * @throws IOException Thrown when any IO error occurs during loading
     */
    public void load(Path file) throws IOException {
        try (Reader br = Files.newBufferedReader(file)) {
            load(br);
        }
    }

    /**
     * Loads the contents from the input and stores it in this object. This includes not only
     * properties but also all whitespace and any comments that are encountered.
     *
     * @param in an <code>InputStream</code> object
     * @throws IOException Thrown when any IO error occurs during loading
     */
    public void load(InputStream in) throws IOException {
        load(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
    }

    /**
     * Loads the contents from the reader and stores it in this object. This includes not only
     * properties but also all whitespace and any comments that are encountered.
     *
     * @param reader a <code>Reader</code> object
     * @throws IOException Thrown when any IO error occurs during loading
     */
    public void load(Reader reader) throws IOException {
        tokens.clear();
        BufferedReader br =
                reader instanceof BufferedReader
                        ? (BufferedReader) reader
                        : new BufferedReader(reader);
        List<PropertiesParser.Token> ts = PropertiesParser.tokens(br).collect(Collectors.toList());
        load(ts);
    }

    private Properties load(List<PropertiesParser.Token> ts) {
        tokens.addAll(ts);
        String key = null;
        for (PropertiesParser.Token token : tokens) {
            if (token.type == PropertiesParser.Type.KEY) {
                key = token.getText();
            } else if (token.type == PropertiesParser.Type.VALUE) {
                values.put(key, token.getText());
            }
        }
        return this;
    }

    /**
     * Returns a <code>Properties</code> with the contents read from the given file. This includes
     * not only properties but also all whitespace and any comments that are encountered.
     *
     * @param file a path to the file to load
     * @throws IOException Thrown when any IO error occurs during loading
     */
    public static Properties loadProperties(Path file) throws IOException {
        Properties props = new Properties();
        props.load(file);
        return props;
    }

    /**
     * Returns a <code>Properties</code> with the contents read from the given stream. This includes
     * not only properties but also all whitespace and any comments that are encountered.
     *
     * @param in an <code>InputStream</code> object
     * @throws IOException Thrown when any IO error occurs during loading
     */
    public static Properties loadProperties(InputStream in) throws IOException {
        return loadProperties(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
    }

    /**
     * Returns a <code>Properties</code> with the contents read from the given reader. This includes
     * not only properties but also all whitespace and any comments that are encountered.
     *
     * @param reader a <code>Reader</code> object
     * @throws IOException Thrown when any IO error occurs during loading
     */
    public static Properties loadProperties(Reader reader) throws IOException {
        Properties props = new Properties();
        props.load(reader);
        return props;
    }

    /**
     * Stores the contents of this object to the given file.
     *
     * @param file a path to the file to write
     * @param comment comment lines to be written at the start of the output
     * @throws IOException Thrown when any IO error occurs during operation
     */
    public void store(Path file, String... comment) throws IOException {
        try (Writer bw = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING)) {
            store(bw, comment);
        }
    }

    /**
     * Stores the contents of this object to the given file.
     *
     * @param out an <code>OutputStream</code> object
     * @param comment comment lines to be written at the start of the output
     * @throws IOException Thrown when any IO error occurs during operation
     */
    public void store(OutputStream out, String... comment) throws IOException {
        store(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1), comment);
    }

    /**
     * Stores the contents of this object to the given file.
     *
     * @param writer a <code>Writer</code> object
     * @param comment comment lines to be written at the start of the output
     * @throws IOException Thrown when any IO error occurs during operation
     */
    public void store(Writer writer, String... comment) throws IOException {
        Cursor pos = first();
        if (comment.length > 0) {
            pos = skipHeaderCommentLines();
            String nl = determineNewline();
            List<String> newcs = normalizeComments(Arrays.asList(comment), "# ");
            for (String c : newcs) {
                writer.write(new PropertiesParser.Token(PropertiesParser.Type.COMMENT, c).getRaw());
                writer.write(nl);
            }
            // We write an extra empty line so this comment won't be taken as part of the first
            // property
            writer.write(nl);
        }
        while (pos.hasToken()) {
            writer.write(pos.raw());
            pos.next();
        }
    }

    /**
     * This method determines the newline string to use when generating line terminators. It looks
     * at all existing line terminators and will use those for any new lines. In case of ambiguity
     * (a file contains both LF and CRLF terminators) it will return the system's default line
     * ending.
     *
     * @return A string containing the line ending to use
     */
    private String determineNewline() {
        boolean lf = false;
        boolean crlf = false;
        for (PropertiesParser.Token token : tokens) {
            if (token.isWs()) {
                if (token.raw.endsWith("/r/n")) {
                    crlf = true;
                } else if (token.raw.endsWith("/n")) {
                    lf = true;
                }
            }
        }
        if (lf && crlf) {
            return System.lineSeparator();
        } else if (crlf) {
            return "/r/n";
        } else {
            return "\n";
        }
    }

    private Cursor skipHeaderCommentLines() {
        Cursor pos = first();
        // Skip a single following whitespace if it is NOT an EOL token
        pos.nextIf(PropertiesParser.Token::isWs);
        // Skip all consecutive comments
        while (pos.nextIf(PropertiesParser.Type.COMMENT)) {
            // Skip a single following whitespace if it IS an EOL token
            pos.nextIf(PropertiesParser.Token::isEol);
            // Skip a single following whitespace if it is NOT an EOL token
            pos.nextIf(PropertiesParser.Token::isWs);
        }
        if (pos.isType(PropertiesParser.Type.KEY)) {
            // We found a comment attached to a property, not a header comment
            return first();
        } else {
            // Skip any following empty lines
            pos.nextWhile(PropertiesParser.Token::isEol);
            return pos;
        }
    }

    Cursor index(int index) {
        return Cursor.index(tokens, index);
    }

    Cursor first() {
        return Cursor.first(tokens);
    }

    Cursor last() {
        return Cursor.last(tokens);
    }

    private void validate(boolean ok, Cursor cursor) {
        if (!ok) {
            throw new IllegalStateException("Unexpected state detected at " + cursor);
        }
    }
}
