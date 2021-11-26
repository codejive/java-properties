package org.codejive.properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.codejive.properties.PropertiesParser.Token;
import org.codejive.properties.PropertiesParser.Type;

public class TestPropertiesParser {
    private final String props =
            ""
                    + "#comment1\n"
                    + "#  comment2   \n"
                    + "\n"
                    + "! comment3\n"
                    + "one=simple\n"
                    + "two=value containing spaces\n\r"
                    + "# another comment\n"
                    + "! and a comment\n"
                    + "! block\n"
                    + "three=and escapes\\n\\t\\r\\f\n"
                    + "  \\ with\\ spaces   =    everywhere  \n"
                    + "altsep:value\n"
                    + "multiline = one \\\n"
                    + "    two  \\\n\r"
                    + "\tthree\n"
                    + "key.4 = \\u1234\n\r"
                    + "  # final comment";

    @Test
    void testTokens() throws IOException {
        StringReader rdr = new StringReader(props);
        List<PropertiesParser.Token> tokens = PropertiesParser.tokens(rdr).collect(Collectors.toList());
        assertThat(
                tokens,
                equalTo(
                        Arrays.asList(
                                new Token(Type.COMMENT, "#comment1"),
                                new Token(Type.WHITESPACE, "\n"),
                                new Token(Type.COMMENT, "#  comment2"),
                                new Token(Type.WHITESPACE, "   \n"),
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
                                new Token(Type.WHITESPACE, "\n\r"),
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
                                new Token(Type.VALUE, "everywhere"),
                                new Token(Type.WHITESPACE, "  \n"),
                                new Token(Type.KEY, "altsep"),
                                new Token(Type.SEPARATOR, ":"),
                                new Token(Type.VALUE, "value"),
                                new Token(Type.WHITESPACE, "\n"),
                                new Token(Type.KEY, "multiline"),
                                new Token(Type.SEPARATOR, " = "),
                                new Token(Type.VALUE, "one \\\n    two  \\\n\r\tthree", "one \n    two  \n\r\tthree"),
                                new Token(Type.WHITESPACE, "\n"),
                                new Token(Type.KEY, "key.4"),
                                new Token(Type.SEPARATOR, " = "),
                                new Token(Type.VALUE, "\\u1234", "\u1234"),
                                new Token(Type.WHITESPACE, "\n\r"),
                                new Token(Type.WHITESPACE, "  "),
                                new Token(Type.COMMENT, "# final comment"))));
    }

    @Test
    void testStringify() throws IOException {
        StringReader rdr = new StringReader(props);
        Stream<Token> tokens = PropertiesParser.tokens(rdr);
        String props2 = tokens.map(Token::getRaw).collect(Collectors.joining());
        assertThat(props2, equalTo(props));
    }
}
