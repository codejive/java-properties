package org.codejive.properties;

import static org.codejive.properties.PropertiesParser.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class TestPropertiesParser {
    private String props =
            ""
                    + "#comment1\n"
                    + "#  comment2   \n"
                    + "\n"
                    + "! comment3\n"
                    + "one=simple\n"
                    + "two=value containing spaces\n\r"
                    + "# another comment\n"
                    + "three=and escapes\\n\\t\\r\\f\n"
                    + "  \\ with\\ spaces   =    everywhere  \n"
                    + "altsep:value\n"
                    + "multiline = one \\\n"
                    + "    two  \\\n\r"
                    + "\tthree\n"
                    + "key.4 = \u1234\n"
                    + "# final comment";

    @Test
    void testTokens() throws IOException {
        StringReader rdr = new StringReader(props);
        List<PropertiesParser.Token> tokens = PropertiesParser.tokens(rdr);
        assertThat(
                tokens,
                equalTo(
                        Arrays.asList(
                                new CommentToken("#comment1"),
                                new WhitespaceToken("\n"),
                                new CommentToken("#  comment2"),
                                new WhitespaceToken("   \n\n"),
                                new CommentToken("! comment3"),
                                new WhitespaceToken("\n"),
                                new KeyToken("one"),
                                new SeparatorToken("="),
                                new ValueToken("simple"),
                                new WhitespaceToken("\n"),
                                new KeyToken("two"),
                                new SeparatorToken("="),
                                new ValueToken("value containing spaces"),
                                new WhitespaceToken("\n\r"),
                                new CommentToken("# another comment"),
                                new WhitespaceToken("\n"),
                                new KeyToken("three"),
                                new SeparatorToken("="),
                                new ValueToken("and escapes\\n\\t\\r\\f"),
                                new WhitespaceToken("\n  "),
                                new KeyToken("\\ with\\ spaces"),
                                new SeparatorToken("   =    "),
                                new ValueToken("everywhere"),
                                new WhitespaceToken("  \n"),
                                new KeyToken("altsep"),
                                new SeparatorToken(":"),
                                new ValueToken("value"),
                                new WhitespaceToken("\n"),
                                new KeyToken("multiline"),
                                new SeparatorToken(" = "),
                                new ValueToken("one \\\n    two  \\\n\r\tthree"),
                                new WhitespaceToken("\n"),
                                new KeyToken("key.4"),
                                new SeparatorToken(" = "),
                                new ValueToken("\u1234"),
                                new WhitespaceToken("\n"),
                                new CommentToken("# final comment"))));
    }

    @Test
    void testStringify() throws IOException {
        StringReader rdr = new StringReader(props);
        List<PropertiesParser.Token> tokens = PropertiesParser.tokens(rdr);
        String props2 = tokens.stream().map(Token::getText).collect(Collectors.joining());
        assertThat(props2, equalTo(props));
    }
}
