package org.codejive.properties;

import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestProperties {
    @Test
    void testLoad() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p).size().isEqualTo(10);
        assertThat(p.keySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p.rawKeySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        "\\ with\\ spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p.values())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "",
                        "",
                        "",
                        "\u1234\u1234");
        assertThat(p.rawValues())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\\n\\t\\r\\f",
                        "everywhere  ",
                        "value",
                        "one \\\n    two  \\\n\tthree",
                        "",
                        "",
                        "",
                        "\\u1234\u1234");
        assertThat(p.entrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\n\t\r\f"),
                        new AbstractMap.SimpleEntry<>(" with spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one two  three"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\u1234\u1234"));
        assertThat(p.rawEntrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\\n\\t\\r\\f"),
                        new AbstractMap.SimpleEntry<>("\\ with\\ spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one \\\n    two  \\\n\tthree"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\\u1234\u1234"));
    }

    @Test
    void testLoadCrLf() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/testcrlf.properties"));
        assertThat(p).size().isEqualTo(10);
        assertThat(p.keySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p.rawKeySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        "\\ with\\ spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p.values())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "",
                        "",
                        "",
                        "\u1234\u1234");
        assertThat(p.rawValues())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\\n\\t\\r\\f",
                        "everywhere  ",
                        "value",
                        "one \\\r\n    two  \\\r\n\tthree",
                        "",
                        "",
                        "",
                        "\\u1234\u1234");
        assertThat(p.entrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\n\t\r\f"),
                        new AbstractMap.SimpleEntry<>(" with spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one two  three"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\u1234\u1234"));
        assertThat(p.rawEntrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\\n\\t\\r\\f"),
                        new AbstractMap.SimpleEntry<>("\\ with\\ spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>(
                                "multiline", "one \\\r\n    two  \\\r\n\tthree"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\\u1234\u1234"));
    }

    @Test
    void testLoadKeyAtEnd() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test-keyatend.properties"));
        assertThat(p).size().isEqualTo(11);
        assertThat(p.keySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4",
                        "key.at.end");
        assertThat(p.rawKeySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        "\\ with\\ spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4",
                        "key.at.end");
        assertThat(p.values())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "",
                        "",
                        "",
                        "\u1234\u1234",
                        "");
        assertThat(p.rawValues())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\\n\\t\\r\\f",
                        "everywhere  ",
                        "value",
                        "one \\\n    two  \\\n\tthree",
                        "",
                        "",
                        "",
                        "\\u1234\u1234",
                        "");
        assertThat(p.entrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\n\t\r\f"),
                        new AbstractMap.SimpleEntry<>(" with spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one two  three"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\u1234\u1234"),
                        new AbstractMap.SimpleEntry<>("key.at.end", ""));
        assertThat(p.rawEntrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\\n\\t\\r\\f"),
                        new AbstractMap.SimpleEntry<>("\\ with\\ spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one \\\n    two  \\\n\tthree"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\\u1234\u1234"),
                        new AbstractMap.SimpleEntry<>("key.at.end", ""));
    }

    @Test
    void testLoadSeparatorAtEnd() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test-separatoratend.properties"));
        assertThat(p).size().isEqualTo(11);
        assertThat(p.keySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4",
                        "separator.at.end");
        assertThat(p.rawKeySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        "\\ with\\ spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4",
                        "separator.at.end");
        assertThat(p.values())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "",
                        "",
                        "",
                        "\u1234\u1234",
                        "");
        assertThat(p.rawValues())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\\n\\t\\r\\f",
                        "everywhere  ",
                        "value",
                        "one \\\n    two  \\\n\tthree",
                        "",
                        "",
                        "",
                        "\\u1234\u1234",
                        "");
        assertThat(p.entrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\n\t\r\f"),
                        new AbstractMap.SimpleEntry<>(" with spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one two  three"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\u1234\u1234"),
                        new AbstractMap.SimpleEntry<>("separator.at.end", ""));
        assertThat(p.rawEntrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\\n\\t\\r\\f"),
                        new AbstractMap.SimpleEntry<>("\\ with\\ spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one \\\n    two  \\\n\tthree"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\\u1234\u1234"),
                        new AbstractMap.SimpleEntry<>("separator.at.end", ""));
    }

    @Test
    void testStore() throws IOException, URISyntaxException {
        Path f = getResource("/test.properties");
        Properties p = Properties.loadProperties(f);
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(f));
    }

    @Test
    void testStoreOutputStream() throws IOException, URISyntaxException {
        Path f = getResource("/test-escaped.properties");
        Properties p = Properties.loadProperties(f);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        p.store(os);
        assertThat(os.toString()).isEqualTo(readAll(f));
    }

    @Test
    void testStoreCrLf() throws IOException, URISyntaxException {
        Path f = getResource("/testcrlf.properties");
        Properties p = Properties.loadProperties(f);
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(f));
    }

    @Test
    void testStoreHeader() throws IOException, URISyntaxException {
        Path f = getResource("/test.properties");
        Properties p = Properties.loadProperties(f);
        StringWriter sw = new StringWriter();
        p.store(sw, "A header line");
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-storeheader.properties")));
    }

    @Test
    void testStoreHeaderCrLf() throws IOException, URISyntaxException {
        Path f = getResource("/testcrlf.properties");
        Properties p = Properties.loadProperties(f);
        StringWriter sw = new StringWriter();
        p.store(sw, "A header line");
        assertThat(sw.toString())
                .isEqualTo(readAll(getResource("/testcrlf-storeheader.properties")));
    }

    @Test
    void testStoreTest2() throws IOException, URISyntaxException {
        Path f = getResource("/test2.properties");
        Properties p = Properties.loadProperties(f);
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(f));
    }

    @Test
    void testLf() throws IOException, URISyntaxException {
        Path f = getResource("/test.properties");
        Properties p = Properties.loadProperties(f);
        assertThat(p.determineEol()).isEqualTo(Cursor.EolType.LF);
    }

    @Test
    void testCrLf() throws IOException, URISyntaxException {
        Path f = getResource("/testcrlf.properties");
        Properties p = Properties.loadProperties(f);
        assertThat(p.determineEol()).isEqualTo(Cursor.EolType.CRLF);
    }

    @Test
    void testGet() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.get("one")).isEqualTo("simple");
        assertThat(p.get("two")).isEqualTo("value containing spaces");
        assertThat(p.get("three")).isEqualTo("and escapes\n\t\r\f");
        assertThat(p.get(" with spaces")).isEqualTo("everywhere  ");
        assertThat(p.get("altsep")).isEqualTo("value");
        assertThat(p.get("multiline")).isEqualTo("one two  three");
        assertThat(p.get("key.4")).isEqualTo("\u1234\u1234");
    }

    @Test
    void testGetProperty() throws IOException, URISyntaxException {
        Properties pdef = Properties.loadProperties(getResource("/test.properties"));
        Properties p = new Properties(pdef);
        p.setProperty("two", "a different two");
        p.setProperty("altsep", "");
        p.setProperty("five", "5", "a new comment");
        assertThat(p).size().isEqualTo(3);
        assertThat(p.keySet()).containsExactly("two", "altsep", "five");
        assertThat(p.stringPropertyNames()).size().isEqualTo(11);
        assertThat(p.stringPropertyNames())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4",
                        "five");
        assertThat(p.getProperty("one")).isEqualTo("simple");
        assertThat(p.getPropertyComment("one")).containsExactly("! comment3");
        assertThat(p.getProperty("two")).isEqualTo("a different two");
        assertThat(p.getPropertyComment("two")).isEmpty();
        assertThat(p.getProperty("three")).isEqualTo("and escapes\n\t\r\f");
        assertThat(p.getPropertyComment("three"))
                .containsExactly("# another comment", "! and a comment", "! block");
        assertThat(p.getProperty(" with spaces")).isEqualTo("everywhere  ");
        assertThat(p.getProperty("altsep")).isEqualTo("");
        assertThat(p.getProperty("multiline")).isEqualTo("one two  three");
        assertThat(p.getProperty("novalue")).isEmpty();
        assertThat(p.getProperty("keyonly")).isEmpty();
        assertThat(p.getProperty("")).isEmpty();
        assertThat(p.getProperty("key.4")).isEqualTo("\u1234\u1234");
        assertThat(p.getProperty("five")).isEqualTo("5");
        assertThat(p.getPropertyComment("five")).containsExactly("# a new comment");
        StringWriter sw = new StringWriter();
        p.list(new PrintWriter(sw));
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-getproperty.properties")));
    }

    @Test
    void testGetRaw() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.getRaw("one")).isEqualTo("simple");
        assertThat(p.getRaw("two")).isEqualTo("value containing spaces");
        assertThat(p.getRaw("three")).isEqualTo("and escapes\\n\\t\\r\\f");
        assertThat(p.getRaw(" with spaces")).isEqualTo("everywhere  ");
        assertThat(p.getRaw("altsep")).isEqualTo("value");
        assertThat(p.getRaw("multiline")).isEqualTo("one \\\n    two  \\\n\tthree");
        assertThat(p.getRaw("key.4")).isEqualTo("\\u1234\u1234");
    }

    @Test
    void testGetNonExistent() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.get("wrong")).isNull();
    }

    @Test
    void testGetPropertyNonExistent() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.getProperty("wrong")).isNull();
    }

    @Test
    void testGetPropertyDefault() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.getProperty("wrong", "right")).isEqualTo("right");
    }

    @Test
    void testGetComment() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.getComment("one")).containsExactly("! comment3");
        assertThat(p.getComment("two")).isEmpty();
        assertThat(p.getComment("three"))
                .containsExactly("# another comment", "! and a comment", "! block");
    }

    @Test
    void testGetCommentNonExistent() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.getComment("wrong")).isEmpty();
    }

    @Test
    void testCommentFirstLine() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test-commentfirstline.properties"));
        assertThat(p.getComment("one")).containsExactly("#comment1", "#  comment2");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString())
                .isEqualTo(readAll(getResource("/test-commentfirstline.properties")));
    }

    @Test
    void testSetComment() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.setComment("one", "new single comment");
        p.setComment("two", "new multi", "line", "comment");
        p.setComment("three", Collections.emptyList());
        assertThat(p.getComment("one")).containsExactly("! new single comment");
        assertThat(p.getComment("two")).containsExactly("# new multi", "# line", "# comment");
        assertThat(p.getComment("three")).isEmpty();
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-comment.properties")));
    }

    @Test
    void testSetCommentNonExistent() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThatThrownBy(() -> p.setComment("wrong", "dummy"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void testPut() throws IOException, URISyntaxException {
        Properties p = new Properties(Cursor.EolType.LF);
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put(" with spaces", "everywhere  ");
        p.put("altsep", "value");
        p.put("multiline", "one two  three");
        p.put("novalue", "");
        p.put("keyonly", "");
        p.put("", "");
        p.put("key.4", "\u1234\u1234");
        assertThat(p).size().isEqualTo(10);
        assertThat(p.keySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p.rawKeySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        "\\ with\\ spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p.values())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "",
                        "",
                        "",
                        "\u1234\u1234");
        assertThat(p.rawValues())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\\n\\t\\r\\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "",
                        "",
                        "",
                        "\u1234\u1234");
        assertThat(p.entrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\n\t\r\f"),
                        new AbstractMap.SimpleEntry<>(" with spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one two  three"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\u1234\u1234"));
        assertThat(p.rawEntrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\\n\\t\\r\\f"),
                        new AbstractMap.SimpleEntry<>("\\ with\\ spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one two  three"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\u1234\u1234"));
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-put.properties")));
    }

    @Test
    void testSetProperty() throws IOException, URISyntaxException {
        Properties p = new Properties(Cursor.EolType.LF);
        p.setProperty("one", "simple", "! comment3");
        p.setProperty("two", "value containing spaces");
        p.setProperty(
                "three", "and escapes\n\t\r\f", "# another comment", "! and a comment", "! block");
        p.setProperty(" with spaces", "everywhere  ");
        p.setProperty("altsep", "value");
        p.setProperty("multiline", "one two  three");
        p.put("novalue", "");
        p.put("keyonly", "");
        p.put("", "");
        p.setProperty("key.4", "\u1234\u1234");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-setproperty.properties")));
    }

    @Test
    void testPutRaw() throws IOException, URISyntaxException {
        Properties p = new Properties(Cursor.EolType.LF);
        p.putRaw("one", "simple");
        p.putRaw("two", "value containing spaces");
        p.putRaw("three", "and escapes\\n\\t\\r\\f");
        p.putRaw("\\ with\\ spaces", "everywhere  ");
        p.putRaw("altsep", "value");
        p.putRaw("multiline", "one \\\n    two  \\\n\tthree");
        p.putRaw("novalue", "");
        p.putRaw("keyonly", "");
        p.putRaw("", "");
        p.putRaw("key.4", "\\u1234\u1234");
        assertThat(p).size().isEqualTo(10);
        assertThat(p.keySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p.rawKeySet())
                .containsExactly(
                        "one",
                        "two",
                        "three",
                        "\\ with\\ spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p.values())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "",
                        "",
                        "",
                        "\u1234\u1234");
        assertThat(p.rawValues())
                .containsExactly(
                        "simple",
                        "value containing spaces",
                        "and escapes\\n\\t\\r\\f",
                        "everywhere  ",
                        "value",
                        "one \\\n    two  \\\n\tthree",
                        "",
                        "",
                        "",
                        "\\u1234\u1234");
        assertThat(p.entrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\n\t\r\f"),
                        new AbstractMap.SimpleEntry<>(" with spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one two  three"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\u1234\u1234"));
        assertThat(p.rawEntrySet())
                .containsExactly(
                        new AbstractMap.SimpleEntry<>("one", "simple"),
                        new AbstractMap.SimpleEntry<>("two", "value containing spaces"),
                        new AbstractMap.SimpleEntry<>("three", "and escapes\\n\\t\\r\\f"),
                        new AbstractMap.SimpleEntry<>("\\ with\\ spaces", "everywhere  "),
                        new AbstractMap.SimpleEntry<>("altsep", "value"),
                        new AbstractMap.SimpleEntry<>("multiline", "one \\\n    two  \\\n\tthree"),
                        new AbstractMap.SimpleEntry<>("novalue", ""),
                        new AbstractMap.SimpleEntry<>("keyonly", ""),
                        new AbstractMap.SimpleEntry<>("", ""),
                        new AbstractMap.SimpleEntry<>("key.4", "\\u1234\u1234"));
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-putraw.properties")));
    }

    @SuppressWarnings("OverwrittenKey") // assigning the same key twice is the point of this test
    @Test
    void testPutReplaceFirst() throws IOException, URISyntaxException {
        Properties p = new Properties(Cursor.EolType.LF);
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put("one", "replaced");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString())
                .isEqualTo(readAll(getResource("/test-putreplacefirst.properties")));
    }

    @SuppressWarnings("OverwrittenKey") // assigning the same key twice is the point of this test
    @Test
    void testPutReplaceMiddle() throws IOException, URISyntaxException {
        Properties p = new Properties(Cursor.EolType.LF);
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put("two", "replaced");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString())
                .isEqualTo(readAll(getResource("/test-putreplacemiddle.properties")));
    }

    @SuppressWarnings("OverwrittenKey") // assigning the same key twice is the point of this test
    @Test
    void testPutReplaceLast() throws IOException, URISyntaxException {
        Properties p = new Properties(Cursor.EolType.LF);
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put("three", "replaced");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString())
                .isEqualTo(readAll(getResource("/test-putreplacelast.properties")));
    }

    @Test
    void testPutNew() throws IOException, URISyntaxException {
        Path f = getResource("/test.properties");
        Properties p = Properties.loadProperties(f);
        p.put("five", "5");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-putnew.properties")));
    }

    @Test
    void testPutNewTest2() throws IOException, URISyntaxException {
        Path f = getResource("/test2.properties");
        Properties p = Properties.loadProperties(f);
        p.put("five", "5");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test2-putnew.properties")));
    }

    @Test
    void testPutNewTest3() throws IOException, URISyntaxException {
        Path f = getResource("/test3.properties");
        Properties p = Properties.loadProperties(f);
        p.put("five", "5");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test3-putnew.properties")));
    }

    @Test
    void testPutNewAfterKey() throws IOException, URISyntaxException {
        Path f = getResource("/test-keyatend.properties");
        Properties p = Properties.loadProperties(f);
        p.put("five", "5");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString())
                .isEqualTo(readAll(getResource("/test-keyatend-putnew.properties")));
    }

    @Test
    void testPutNewAfterSeparator() throws IOException, URISyntaxException {
        Path f = getResource("/test-separatoratend.properties");
        Properties p = Properties.loadProperties(f);
        p.put("five", "5");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString())
                .isEqualTo(readAll(getResource("/test-separatoratend-putnew.properties")));
    }

    @Test
    void testPutFirstWithHeader0Eol() throws IOException, URISyntaxException {
        try (StringReader sr = new StringReader("# A header comment")) {
            Properties p = Properties.loadProperties(sr);
            p.put("first", "dummy");
            StringWriter sw = new StringWriter();
            p.store(sw);
            assertThat(sw.toString())
                    .isEqualTo(readAll(getResource("/test-putfirstwithheader.properties")));
        }
    }

    @Test
    void testPutFirstWithHeader1Eol() throws IOException, URISyntaxException {
        try (StringReader sr = new StringReader("# A header comment\n")) {
            Properties p = Properties.loadProperties(sr);
            p.put("first", "dummy");
            StringWriter sw = new StringWriter();
            p.store(sw);
            assertThat(sw.toString())
                    .isEqualTo(readAll(getResource("/test-putfirstwithheader.properties")));
        }
    }

    @Test
    void testPutFirstWithHeader2Eol() throws IOException, URISyntaxException {
        try (StringReader sr = new StringReader("# A header comment\n\n")) {
            Properties p = Properties.loadProperties(sr);
            p.put("first", "dummy");
            StringWriter sw = new StringWriter();
            p.store(sw);
            assertThat(sw.toString())
                    .isEqualTo(readAll(getResource("/test-putfirstwithheader.properties")));
        }
    }

    @Test
    void testPutFirstWithHeader3Eol() throws IOException, URISyntaxException {
        try (StringReader sr = new StringReader("# A header comment\n\n\n")) {
            Properties p = Properties.loadProperties(sr);
            p.put("first", "dummy");
            StringWriter sw = new StringWriter();
            p.store(sw);
            assertThat(sw.toString())
                    .isEqualTo(readAll(getResource("/test-putfirstwithheader3.properties")));
        }
    }

    @Test
    void testPutNull() {
        Properties p = new Properties(Cursor.EolType.LF);
        assertThatThrownBy(() -> p.put("one", null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> p.setProperty("one", null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> p.put(null, "value")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> p.setProperty(null, "value"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testPutUnicode() throws IOException, URISyntaxException {
        Properties p = new Properties(Cursor.EolType.LF);
        p.putRaw("encoded", "\\u0627\\u0644\\u0623\\u0644\\u0628\\u0627\\u0646\\u064a\\u0629");
        p.put("text", "\u0627\u0644\u0623\u0644\u0628\u0627\u0646\u064a\u0629");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-putunicode.properties")));
    }

    @Test
    void testRemoveFirst() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.remove("one");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-removefirst.properties")));
    }

    @Test
    void testRemoveMiddle() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.remove("three");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-removemiddle.properties")));
    }

    @Test
    void testRemoveLast() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.remove("key.4");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-removelast.properties")));
    }

    @Test
    void testRemoveAll() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.remove("one");
        p.remove("two");
        p.remove("three");
        p.remove(" with spaces");
        p.remove("altsep");
        p.remove("multiline");
        p.remove("novalue");
        p.remove("keyonly");
        p.remove("");
        p.remove("key.4");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-removeall.properties")));
    }

    @Test
    void testRemoveNonExistent() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        assertThat(p.remove("wrong")).isNull();
    }

    @Test
    void testRemoveMiddleIterator() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        Iterator<String> iter = p.keySet().iterator();
        while (iter.hasNext()) {
            if (iter.next().equals("three")) {
                iter.remove();
                break;
            }
        }
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-removemiddle.properties")));
    }

    @Test
    void testClear() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.clear();
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-clear.properties")));
    }

    @Test
    void testRemoveComment() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        p.setComment("one");
        assertThat(p.getComment("one")).isEmpty();
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-removecomment.properties")));
    }

    @Test
    public void testInteropLoad() throws IOException, URISyntaxException {
        java.util.Properties p = new java.util.Properties();
        try (Reader br = Files.newBufferedReader(getResource("/test.properties"))) {
            p.load(br);
        }
        assertThat(p).size().isEqualTo(10);
        assertThat(p.keySet())
                .containsExactlyInAnyOrder(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p.values())
                .containsExactlyInAnyOrder(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "",
                        "",
                        "",
                        "\u1234\u1234");
    }

    @Test
    void testInteropStore() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        StringWriter sw = new StringWriter();
        p.asJUProperties().store(sw, null);
        assertThat(sw.toString()).contains("one=simple" + System.lineSeparator());
        assertThat(sw.toString()).contains("two=value containing spaces" + System.lineSeparator());
        assertThat(sw.toString())
                .contains("three=and escapes\\n\\t\\r\\f" + System.lineSeparator());
        assertThat(sw.toString())
                .contains("\\ with\\ spaces=everywhere  " + System.lineSeparator());
        assertThat(sw.toString()).contains("altsep=value" + System.lineSeparator());
        assertThat(sw.toString()).contains("multiline=one two  three" + System.lineSeparator());
        assertThat(sw.toString()).contains("key.4=\u1234\u1234" + System.lineSeparator());
    }

    @Test
    void testInteropPutLoad() throws IOException {
        java.util.Properties p = new java.util.Properties();
        p.put("one", "simple");
        p.put("two", "value containing spaces");
        p.put("three", "and escapes\n\t\r\f");
        p.put(" with spaces", "everywhere  ");
        p.put("altsep", "value");
        p.put("multiline", "one two  three");
        p.put("novalue", "");
        p.put("keyonly", "");
        p.put("", "");
        p.put("key.4", "\u1234");
        StringWriter sw = new StringWriter();
        p.store(sw, null);
        assertThat(sw.toString()).contains("one=simple" + System.lineSeparator());
        assertThat(sw.toString()).contains("two=value containing spaces" + System.lineSeparator());
        assertThat(sw.toString())
                .contains("three=and escapes\\n\\t\\r\\f" + System.lineSeparator());
        assertThat(sw.toString())
                .contains("\\ with\\ spaces=everywhere  " + System.lineSeparator());
        assertThat(sw.toString()).contains("altsep=value" + System.lineSeparator());
        assertThat(sw.toString()).contains("multiline=one two  three" + System.lineSeparator());
        assertThat(sw.toString()).contains("key.4=\u1234" + System.lineSeparator());
        java.util.Properties p2 = new java.util.Properties();
        p2.load(new StringReader(sw.toString()));
        assertThat(p2).size().isEqualTo(10);
        assertThat(p2.keySet())
                .containsExactlyInAnyOrder(
                        "one",
                        "two",
                        "three",
                        " with spaces",
                        "altsep",
                        "multiline",
                        "novalue",
                        "keyonly",
                        "",
                        "key.4");
        assertThat(p2.values())
                .containsExactlyInAnyOrder(
                        "simple",
                        "value containing spaces",
                        "and escapes\n\t\r\f",
                        "everywhere  ",
                        "value",
                        "one two  three",
                        "",
                        "",
                        "",
                        "\u1234");
    }

    @Test
    void testEscaped() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        StringWriter sw = new StringWriter();
        p.escaped().store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-escaped.properties")));
    }

    @Test
    void testUnescaped() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        StringWriter sw = new StringWriter();
        p.unescaped().store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-unescaped.properties")));
    }

    @Test
    void testCursor() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test.properties"));
        Cursor c = p.first();
        assertThat(c.nextCount(t -> true)).isEqualTo(p.last().position() + 1);
        c = p.last();
        assertThat(c.prevCount(t -> true)).isEqualTo(p.last().position() + 1);
    }

    @Test
    void testMissingDelim() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test-missingdelim.properties"));
        assertThat(p).containsOnlyKeys("A-string-without-delimiter");
        assertThat(p).containsEntry("A-string-without-delimiter", "");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-missingdelim.properties")));
    }

    @Test
    void testMultiDelim() throws IOException, URISyntaxException {
        Properties p = Properties.loadProperties(getResource("/test-multidelim.properties"));
        assertThat(p).containsOnlyKeys("key");
        assertThat(p).containsEntry("key", "==value");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(readAll(getResource("/test-multidelim.properties")));
    }

    @Test
    void testPutAll() {
        Properties p = new Properties(Cursor.EolType.LF);
        java.util.Properties ju = new java.util.Properties();
        ju.setProperty("foo", "bar");
        p.putAll(ju);
        assertThat(p.getProperty("foo")).isEqualTo("bar");
    }

    @Test
    void testPutCrLf() throws IOException {
        final String given = "first=line\r\n# trailer";
        final String expected = "first=line\r\nsecond=line\r\n# trailer";

        Properties p = Properties.loadProperties(new StringReader(given));
        p.put("second", "line");
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString()).isEqualTo(expected);
    }

    // A test that inserts a property before every comment,
    // by passing a Cursor to 'put()' that points at each comment.
    @Test
    void testPutTargetsComment() throws IOException {
        final String given = "  # comment 1\n"
                + "\n"
                + "# comment 2\n"
                + "  \n"
                + "  # comment 3\n"
                + "  key1 = value1\n"
                + "\n"
                + "  ! block- 4\n"
                + "  # comment 5\n"
                + "\n"
                + "# comment 6";
        final String expected = "put1=value1\n"
                + "  # comment 1\n"
                + "\n"
                + "put2=value2\n"
                + "# comment 2\n"
                + "  \n"
                + "put3=value3\n"
                + "  # comment 3\n"
                + "  key1 = value1\n"
                + "\n"
                + "put4=value4\n"
                + "put5=value5\n"
                + "  ! block- 4\n"
                + "  # comment 5\n"
                + "\n"
                + "put6=value6\n"
                + "# comment 6";

        Properties p = Properties.loadProperties(new StringReader(given));
        for (int commentIdx = 0; ; commentIdx++) {
            Cursor c = p.first();
            // skip n comments
            for (int i = 0; i < commentIdx; i++) {
                c.nextWhile(t -> t.getType() != PropertiesParser.Type.COMMENT);
                c.next();
            }
            // find the target comment
            c.nextWhile(t -> t.getType() != PropertiesParser.Type.COMMENT);

            if (c.atEnd()) {
                break;
            }

            assertThat(c.type()).isEqualTo(PropertiesParser.Type.COMMENT);
            int putNum = commentIdx + 1;
            p.put("put" + putNum, "value" + putNum, c);
        }

        expectStoreText(p, expected);
    }

    // A test that inserts a property before every whitespace,
    // by passing a Cursor to 'put()' that points at each whitespace.
    @Test
    void testPutTargetsWhitespace() throws IOException {
        final String given = "  \n" // ws at beginning of file
                + "  \n" // ws inbetween tokens
                + "  # comment\n" // ws before comment
                + "  key=value\n" // ws before property
                + "# comment\n"
                + "  \n" // ws after comment
                + "  "; // ws at end of file
        final String expected = "put1=value1\n"
                + "  \n"
                + "put2=value2\n"
                + "  \n"
                + "put3=value3\n" // added by the ws before the comment
                + "put4=value4\n" // added by the ws before the property
                + "  # comment\n"
                + "  key=value\n"
                // added by the ws below the comment.
                // (is inserted before the comment, because inserting after would attach the comment to the property)
                + "put5=value5\n"
                + "# comment\n"
                + "  \n"
                + "put6=value6\n"
                + "  ";

        Properties p = Properties.loadProperties(new StringReader(given));
        for (int wsIdx = 0; ; wsIdx++) {
            Cursor c = p.first();
            // skip n whitespaces
            for (int i = 0; i < wsIdx; i++) {
                c.nextWhile(t -> t.getType() != PropertiesParser.Type.WHITESPACE || !t.raw.startsWith(" "));
                c.next();
            }
            // find the target whitespace
            c.nextWhile(t -> t.getType() != PropertiesParser.Type.WHITESPACE || !t.raw.startsWith(" "));

            if (c.atEnd()) {
                break;
            }

            assertThat(c.type()).isEqualTo(PropertiesParser.Type.WHITESPACE);
            int putNum = wsIdx + 1;
            p.put("put" + putNum, "value" + putNum, c);
        }

        expectStoreText(p, expected);
    }

    static Stream<Arguments> testPutFirstLine() {
        return Stream.of(
                Arguments.arguments("", "put=val"),
                Arguments.arguments("key=value", "put=val\nkey=value"),
                Arguments.arguments("# file comment", "put=val\n# file comment"),
                Arguments.arguments("  ", "put=val\n  ")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testPutFirstLine(String given, String expected) throws IOException {
        Properties p = Properties.loadProperties(new StringReader(given));
        p.put("put", "val", p.first().prev());
        expectStoreText(p, expected);
    }

    static Stream<Arguments> testPutLastLine() {
        return Stream.of(
                Arguments.arguments("", "put=val"),
                Arguments.arguments("key=value", "key=value\nput=val"),
                Arguments.arguments("# file comment", "# file comment\n\nput=val"),
                Arguments.arguments("  ", "  \nput=val")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testPutLastLine(String given, String expected) throws IOException {
        Properties p = Properties.loadProperties(new StringReader(given));
        p.put("put", "val", p.last().next());
        expectStoreText(p, expected);
    }

    // A test that inserts a property before and after every property.
    @Test
    void testInsertBeforeAndAfterProperties() throws IOException {
        final String given = "key1=val1\n" // property at beginning of file
                + "\n"
                + "  # key2 comment\n"
                + "  key2=val2\n" // property with preceeding and trailing comment
                + "  # key2 trailing comment\n"
                + "\n"
                + "key\\\n3 = val\\\n3\n" // property with "multiline" key and value
                + "\n"
                + "key4=val4"; // property at end of file
        final String expected = "key1.before=val1.before\n"
                + "key1=val1\n"
                + "key1.after=val1.after\n"
                + "\n"
                + "key2.before=val2.before\n"
                + "  # key2 comment\n"
                + "  key2=val2\n"
                + "key2.after=val2.after\n"
                + "  # key2 trailing comment\n"
                + "\n"
                + "key3.before=val3.before\n"
                + "key\\\n3 = val\\\n3\n"
                + "key3.after=val3.after\n"
                + "\n"
                + "key4.before=val4.before\n"
                + "key4=val4\n"
                + "key4.after=val4.after\n"
                + "unknown.key.before=unknown.value.before\n"
                + "unknown.key.after=unknown.value.after";

        Properties p = Properties.loadProperties(new StringReader(given));
        Map<String, String> pCopy = new HashMap<>(p);
        for (Map.Entry<String, String> entry : pCopy.entrySet()) {
            p.put(entry.getKey() + ".before", entry.getValue() + ".before", p.before(entry.getKey()));
            p.put(entry.getKey() + ".after", entry.getValue() + ".after", p.after(entry.getKey()));
        }

        // also insert at an unknown key
        p.put("unknown.key.before", "unknown.value.before", p.before("unknown.key"));
        p.put("unknown.key.after", "unknown.value.after", p.after("unknown.key"));

        expectStoreText(p, expected);
    }

    // Test inserts with a user-specified cursor pointing at a VALUE token.
    @Test
    void testPutTargetsValue() throws IOException {
        final String given = "  key1=val1\n"
                + "  # key 2 comment\n"
                + "  key2=val2\n"
                + "  \n"
                + "  key3=val3";
        final String expected = "put1=val1\n"
                + "  key1=val1\n"
                + "put2=val2\n"
                + "  # key 2 comment\n"
                + "  key2=val2\n"
                + "  \n"
                + "put3=val3\n"
                + "  key3=val3";

        Properties p = Properties.loadProperties(new StringReader(given));
        for (int valIdx = 0; ; valIdx++) {
            Cursor c = p.first();
            // skip 2*n VALUE tokens
            for (int i = 0; i < (valIdx * 2); i++) {
                c.nextWhile(t -> t.getType() != PropertiesParser.Type.VALUE);
                c.next();
            }
            // find the target VALUE token
            c.nextWhile(t -> t.getType() != PropertiesParser.Type.VALUE);

            if (c.atEnd()) {
                break;
            }

            assertThat(c.type()).isEqualTo(PropertiesParser.Type.VALUE);
            int putNum = valIdx + 1;
            p.put("put" + putNum, "val" + putNum, c);
        }

        expectStoreText(p, expected);
    }

    // Test inserts with a user-specified cursor pointing at an EOL token after a VALUE token.
    @Test
    void testPutTargetsValueEol() throws IOException {
        final String given = "  key1=val1\n"
                + "  # key 2 comment\n"
                + "  key2=val2\n"
                + "  \n"
                + "  key3=val3\n";
        final String expected = "put1=val1\n"
                + "  key1=val1\n"
                + "put2=val2\n"
                + "  # key 2 comment\n"
                + "  key2=val2\n"
                + "  \n"
                + "put3=val3\n"
                + "  key3=val3\n";

        Properties p = Properties.loadProperties(new StringReader(given));
        for (int valIdx = 0; ; valIdx++) {
            Cursor c = p.first();
            // skip 2*n VALUE tokens
            for (int i = 0; i < (valIdx * 2); i++) {
                c.nextWhile(t -> t.getType() != PropertiesParser.Type.VALUE);
                c.next();
            }
            // find the target VALUE token
            c.nextWhile(t -> t.getType() != PropertiesParser.Type.VALUE);

            if (c.atEnd()) {
                break;
            }

            assertThat(c.type()).isEqualTo(PropertiesParser.Type.VALUE);
            c.next();
            int putNum = valIdx + 1;
            p.put("put" + putNum, "val" + putNum, c);
        }

        expectStoreText(p, expected);
    }

    @Test
    void testPutBeforeFirstProperty() throws IOException {
        final String given = "# header comment 1\n"
                + "! header comment 2\n"
                + "\n"
                + "\n"
                + "key=val\n"
                + "\n"
                + "# trailer comment 1\n"
                + "! trailer comment 2";
        final String expected = "# header comment 1\n"
                + "! header comment 2\n"
                + "\n"
                + "\n"
                + "put=putVal\n"
                + "key=val\n"
                + "\n"
                + "# trailer comment 1\n"
                + "! trailer comment 2";

        Properties p = Properties.loadProperties(new StringReader(given));
        p.put("put", "putVal", p.beforeFirstProperty());
        expectStoreText(p, expected);
    }

    @Test
    void testPutAfterHeaderComment() throws IOException {
        final String given = "# header comment 1\n"
                + "! header comment 2\n"
                + "\n"
                + "\n"
                + "key=val";
        final String expected = "# header comment 1\n"
                + "! header comment 2\n"
                + "\n"
                + "put=putVal\n"
                + "\n"
                + "key=val";

        Properties p = Properties.loadProperties(new StringReader(given));
        p.put("put", "putVal", p.afterHeaderComment());
        expectStoreText(p, expected);
    }

    @Test
    void testPutAfterLastProperty() throws IOException {
        final String given = "# header comment 1\n"
                + "! header comment 2\n"
                + "\n"
                + "\n"
                + "key=val\n"
                + "\n"
                + "# trailer comment 1\n"
                + "! trailer comment 2";
        final String expected = "# header comment 1\n"
                + "! header comment 2\n"
                + "\n"
                + "\n"
                + "key=val\n"
                + "put=putVal\n"
                + "\n"
                + "# trailer comment 1\n"
                + "! trailer comment 2";

        Properties p = Properties.loadProperties(new StringReader(given));
        p.put("put", "putVal", p.afterLastProperty());
        expectStoreText(p, expected);
    }

    private Path getResource(String name) throws URISyntaxException {
        URL resource = getClass().getResource(name);
        if (resource == null)
            throw new IllegalArgumentException("resource '" + name + "' does not exist.");

        return Paths.get(resource.toURI());
    }

    private String readAll(Path f) throws IOException {
        return new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
    }

    private void expectStoreText(Properties props, String expectedText) throws IOException {
        StringWriter sw = new StringWriter();
        props.store(sw);
        assertThat(sw.toString()).isEqualTo(expectedText);
    }
}
