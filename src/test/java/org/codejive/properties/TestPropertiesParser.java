package org.codejive.properties;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
    void testCommentOnFirstLine() throws IOException {
        List<String> expectedComments = new ArrayList<>();
        expectedComments.add("! Hi");
        expectedComments.add("! Hello");

        Properties props = new Properties();
        props.load(new StringReader("! Hi\n! Hello\nfoo=bar\n"));

        List<String> comments = props.getComment("foo");
        assertThat(comments).isEqualTo(expectedComments);
    }
}
