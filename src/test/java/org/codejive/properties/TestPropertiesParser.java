package org.codejive.properties;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codejive.properties.PropertiesParser.Token;
import org.codejive.properties.PropertiesParser.Type;
import org.junit.jupiter.api.Test;

public class TestPropertiesParser {
    private final String props =
            ""
                    + "#comment1\n"
                    + "#  comment2   \n"
                    + "\n"
                    + "! comment3\n"
                    + "one=simple\n"
                    + "two=value containing spaces\r\n"
                    + "# another comment\n"
                    + "! and a comment\n"
                    + "! block\n"
                    + "three=and escapes\\n\\t\\r\\f\n"
                    + "  \\ with\\ spaces   =    everywhere  \n"
                    + "altsep:value\n"
                    + "multiline = one \\\n"
                    + "    two  \\\r\n"
                    + "\tthree\n"
                    + "key.4 = \\u1234\r\n"
                    + "line-with-missing-value  =  \n"
                    + "line-with-missing-delim  \n"
                    + "multidelim===value\n"
                    + "  # final comment";

    @Test
    void testTokens() throws IOException {
        StringReader rdr = new StringReader(props);
        List<PropertiesParser.Token> tokens =
                PropertiesParser.tokens(rdr).collect(Collectors.toList());
        assertThat(tokens)
                .containsExactly(
                        new Token(Type.COMMENT, "#comment1"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.COMMENT, "#  comment2   "),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.COMMENT, "! comment3"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.KEY, "one"),
                        new Token(Type.SEPARATOR, "="),
                        new Token(Type.VALUE, "simple"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.KEY, "two"),
                        new Token(Type.SEPARATOR, "="),
                        new Token(Type.VALUE, "value containing spaces"),
                        new Token(Type.WHITESPACE, "\r\n"),
                        new Token(Type.COMMENT, "# another comment"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.COMMENT, "! and a comment"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.COMMENT, "! block"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.KEY, "three"),
                        new Token(Type.SEPARATOR, "="),
                        new Token(Type.VALUE, "and escapes\\n\\t\\r\\f", "and escapes\n\t\r\f"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.WHITESPACE, "  "),
                        new Token(Type.KEY, "\\ with\\ spaces", " with spaces"),
                        new Token(Type.SEPARATOR, "   =    "),
                        new Token(Type.VALUE, "everywhere  "),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.KEY, "altsep"),
                        new Token(Type.SEPARATOR, ":"),
                        new Token(Type.VALUE, "value"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.KEY, "multiline"),
                        new Token(Type.SEPARATOR, " = "),
                        new Token(Type.VALUE, "one \\\n    two  \\\r\n\tthree", "one two  three"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.KEY, "key.4"),
                        new Token(Type.SEPARATOR, " = "),
                        new Token(Type.VALUE, "\\u1234", "\u1234"),
                        new Token(Type.WHITESPACE, "\r\n"),
                        new Token(Type.KEY, "line-with-missing-value"),
                        new Token(Type.SEPARATOR, "  =  "),
                        new Token(Type.VALUE, ""),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.KEY, "line-with-missing-delim"),
                        new Token(Type.SEPARATOR, "  "),
                        new Token(Type.VALUE, ""),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.KEY, "multidelim"),
                        new Token(Type.SEPARATOR, "="),
                        new Token(Type.VALUE, "==value"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.WHITESPACE, "  "),
                        new Token(Type.COMMENT, "# final comment"));
    }

    @Test
    void testStringify() throws IOException {
        StringReader rdr = new StringReader(props);
        Stream<Token> tokens = PropertiesParser.tokens(rdr);
        String props2 = tokens.map(Token::getRaw).collect(Collectors.joining());
        assertThat(props2).isEqualTo(props);
    }

    @Test
    void testBackslashesInComment() throws IOException {
        List<Token> tokens =
                PropertiesParser.tokens(new StringReader("# C:\\users\\\nfoo=bar"))
                        .collect(Collectors.toList());

        assertThat(tokens)
                .containsExactly(
                        new Token(Type.COMMENT, "# C:\\users\\"),
                        new Token(Type.WHITESPACE, "\n"),
                        new Token(Type.KEY, "foo"),
                        new Token(Type.SEPARATOR, "="),
                        new Token(Type.VALUE, "bar"));
    }

    @Test
    void testLineContinuationInKey() throws IOException {
        List<Token> tokens =
                PropertiesParser.tokens(new StringReader("key\\\n  part=value"))
                        .collect(Collectors.toList());

        assertThat(tokens)
                .containsExactly(
                        new Token(Type.KEY, "key\\\n  part", "keypart"),
                        new Token(Type.SEPARATOR, "="),
                        new Token(Type.VALUE, "value"));
    }

    @Test
    void testEmptySeparatorAndValueAtEof() throws IOException {
        List<Token> tokens =
                PropertiesParser.tokens(new StringReader("foo=")).collect(Collectors.toList());

        assertThat(tokens)
                .containsExactly(
                        new Token(Type.KEY, "foo"),
                        new Token(Type.SEPARATOR, "="),
                        new Token(Type.VALUE, ""));
    }

    @Test
    void testMissingSeparatorAndValueAtEof() throws IOException {
        List<Token> tokens =
                PropertiesParser.tokens(new StringReader("foo")).collect(Collectors.toList());

        assertThat(tokens)
                .containsExactly(
                        new Token(Type.KEY, "foo"),
                        new Token(Type.SEPARATOR, ""),
                        new Token(Type.VALUE, ""));
    }

    @Test
    void testBackslashAtEof() throws IOException {
        List<Token> tokens =
                PropertiesParser.tokens(new StringReader("foo=bar\\")).collect(Collectors.toList());

        assertThat(tokens)
                .containsExactly(
                        new Token(Type.KEY, "foo"),
                        new Token(Type.SEPARATOR, "="),
                        new Token(Type.VALUE, "bar\\", "bar"));
    }

    @Test
    void testPendingBackslashBelongsToValue() throws IOException {
        for (String separator : new String[] {" ", "=", " : ", " \\\r\n = "}) {
            for (String rawValue :
                    new String[] {"\\", "\\t", "\\=value", "\\ value", "\\\\tail", "\\u1234"}) {
                String input = "key" + separator + rawValue;
                PropertiesParser parser = new PropertiesParser(new StringReader(input));
                assertThat(parser.nextToken()).isEqualTo(new Token(Type.KEY, "key"));
                Token separatorToken = parser.nextToken();
                assertThat(separatorToken.getType()).isEqualTo(Type.SEPARATOR);
                assertThat(separatorToken.getRaw()).isEqualTo(separator);
                Token valueToken = parser.nextToken();
                assertThat(valueToken.getType()).isEqualTo(Type.VALUE);
                assertThat(valueToken.getRaw()).isEqualTo(rawValue);
                assertThat(valueToken.getText()).isEqualTo(PropertiesParser.unescape(rawValue));
                assertThat(parser.nextToken()).isNull();
                assertThat(parser.nextToken()).isNull();
                assertLoadsLikeJava(input);
            }
        }
    }

    @Test
    void testContinuedSeparators() throws IOException {
        for (String eol : new String[] {"\n", "\r", "\r\n"}) {
            for (String separator : new String[] {"=", ":"}) {
                assertLoadsLikeJava("key \\" + eol + " \t\f" + separator + " value");
                assertLoadsLikeJava("key \\" + eol + " \\" + eol + separator + " value");
                assertLoadsLikeJava("key=" + "\\" + eol + " :value");
                assertLoadsLikeJava("key" + separator + "\\" + eol + "\\" + eol + " :value");
                assertLoadsLikeJava("key" + separator + "\\" + eol);
            }
        }
    }

    @Test
    void testContinuedUnicodeEscapes() throws IOException {
        for (String eol : new String[] {"\n", "\r", "\r\n"}) {
            for (int split = 2; split < 6; split++) {
                String escape = "\\u1234";
                String continued =
                        escape.substring(0, split) + "\\" + eol + " \t\f" + escape.substring(split);
                assertLoadsLikeJava(continued + "=" + continued);
                assertThat(PropertiesParser.unescape(continued)).isEqualTo("\u1234");
            }
        }
    }

    @Test
    void testInvalidUnicodeEscapes() {
        for (String escape :
                new String[] {"\\u１２３４", "\\u١٢٣٤", "\\uu1234", "\\u123", "\\u12x4", "\\u+123"}) {
            for (String input : new String[] {"key=" + escape, escape + "=value"}) {
                assertThatThrownBy(() -> new Properties().load(new StringReader(input)))
                        .isInstanceOf(IllegalArgumentException.class);
            }
            assertThatThrownBy(() -> PropertiesParser.unescape(escape))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void testEmptyContinuationPrefixes() throws IOException {
        for (String eol : new String[] {"\n", "\r", "\r\n"}) {
            for (String tail :
                    new String[] {"#comment", "!comment", "", " \t\f", "key=value", "=value"}) {
                assertLoadsLikeJava("\\" + eol + tail + eol + "k=v");
                assertLoadsLikeJava(" \\" + eol + "\t\\" + eol + tail + eol + "k=v");
            }
            assertLoadsLikeJava("\\" + eol);
            assertLoadsLikeJava("\\" + eol + " \t\f");
            assertLoadsLikeJava("\\" + eol + "\\\n");
            assertLoadsLikeJava("key=one\\" + eol + " #two");
        }
        assertLoadsLikeJava("\\");
    }

    @Test
    void testBackslashParity() throws IOException {
        for (String eol : new String[] {"\n", "\r", "\r\n"}) {
            for (int count = 0; count < 8; count++) {
                String slashes = new String(new char[count]).replace('\0', '\\');
                for (String prefix : new String[] {"key", "key ", "key=", "key=x", "#comment"}) {
                    for (String tail :
                            new String[] {" value", " = value", "#comment", "", "\nnext=v"}) {
                        assertLoadsLikeJava(prefix + slashes + eol + tail);
                    }
                }
            }
        }
    }

    private void assertLoadsLikeJava(String input) throws IOException {
        java.util.Properties expected = new java.util.Properties();
        expected.load(new StringReader(input));
        Properties actual = new Properties();
        actual.load(new StringReader(input));
        assertThat(actual).as("Input: %s", input).isEqualTo(expected);
        String raw =
                PropertiesParser.tokens(new StringReader(input))
                        .map(Token::getRaw)
                        .collect(Collectors.joining());
        assertThat(raw).isEqualTo(input);
        java.io.StringWriter writer = new java.io.StringWriter();
        actual.store(writer);
        assertThat(writer.toString()).isEqualTo(input);
    }
}
