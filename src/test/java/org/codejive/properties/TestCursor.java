package org.codejive.properties;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestCursor {
    @Test
    void testNavigateToStartOfBlock() {
        List<List<PropertiesParser.Token>> blocks = Arrays.asList(
                Arrays.asList(
                        // first property, with comment
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  "),
                        new PropertiesParser.Token(PropertiesParser.Type.COMMENT, "comment"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  "),
                        new PropertiesParser.Token(PropertiesParser.Type.KEY, "key"),
                        new PropertiesParser.Token(PropertiesParser.Type.SEPARATOR, " = "),
                        new PropertiesParser.Token(PropertiesParser.Type.VALUE, "value"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n")
                ),
                Arrays.asList(
                        // second property, whitespace, no comment, no value
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  "),
                        new PropertiesParser.Token(PropertiesParser.Type.KEY, "key"),
                        new PropertiesParser.Token(PropertiesParser.Type.SEPARATOR, "="),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n")
                ),
                Arrays.asList(
                        // third property, whitespace, comment, no separator
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  "),
                        new PropertiesParser.Token(PropertiesParser.Type.COMMENT, "comment"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  "),
                        new PropertiesParser.Token(PropertiesParser.Type.KEY, "key"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n")
                ),
                Arrays.asList(
                        // fourth property, whitespace, multiple comments
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  "),
                        new PropertiesParser.Token(PropertiesParser.Type.COMMENT, "comment"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  "),
                        new PropertiesParser.Token(PropertiesParser.Type.COMMENT, "comment"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  "),
                        new PropertiesParser.Token(PropertiesParser.Type.KEY, "key"),
                        new PropertiesParser.Token(PropertiesParser.Type.SEPARATOR, " = "),
                        new PropertiesParser.Token(PropertiesParser.Type.VALUE, "value"),
                        new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n")
                ),
                // region of empty lines
                Collections.singletonList(new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n")),
                Collections.singletonList(new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "\n")),
                // region of empty lines with whitespace
                Collections.singletonList(new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  \n")),
                Collections.singletonList(new PropertiesParser.Token(PropertiesParser.Type.WHITESPACE, "  \n")),
                // fifth property, no whitespace, no comment, no value
                Collections.singletonList(new PropertiesParser.Token(PropertiesParser.Type.KEY, "key"))
        );

        List<PropertiesParser.Token> tokens = blocks.stream().flatMap(Collection::stream).collect(Collectors.toList());

        // 'atStart' remains unchanged.
        Cursor c = Cursor.index(tokens, -1);
        assertThat(c.navigateToStartOfBlock().getIndex()).isEqualTo(-1);
        // 'atEnd' remains unchanged.
        c.setIndex(tokens.size());
        assertThat(c.navigateToStartOfBlock().getIndex()).isEqualTo(tokens.size());

        // verify that for every index in each block, the cursor navigates to the first index of the block.
        int fromIdx = 0;
        int toIdx = 0;
        for (List<PropertiesParser.Token> block : blocks) {
            for (int i = 0; i < block.size(); i++) {
                c.setIndex(fromIdx);
                assertThat(c.navigateToStartOfBlock().getIndex()).isEqualTo(toIdx);
                fromIdx++;
            }

            toIdx += block.size();
        }
    }
}
