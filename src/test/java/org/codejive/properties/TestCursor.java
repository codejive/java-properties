package org.codejive.properties;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

public class TestCursor {
    @Test
    void addTokenToEmptyDocument() throws IOException {
        Properties p = Properties.loadProperties(new StringReader(""));
        Cursor c = p.first();

        // expect no tokens to be in the document
        assertThat(c.hasToken()).isEqualTo(false);

        // expect that add should not throw
        PropertiesParser.Token addedToken = new PropertiesParser.Token(PropertiesParser.Type.KEY, "test", "test");
        c.add(addedToken);

        // expect that add skips the added token
        assertThat(c.hasToken()).isEqualTo(false);
        c.prev();
        assertThat(c.hasToken()).isEqualTo(true);
        assertThat(c.token()).isEqualTo(addedToken);
    }

    @Test
    void addTokensToDocument() throws IOException {
        Properties p = Properties.loadProperties(new StringReader("key=value"
                + "\nkey2=value2"));

        Cursor c = p.first();
        c.add(new PropertiesParser.Token(PropertiesParser.Type.COMMENT, "# beginning"));
        c.addEol();

        c = p.indexOf("key2");
        c.add(new PropertiesParser.Token(PropertiesParser.Type.COMMENT, "# middle"));
        c.addEol();

        c = p.last();
        c.next();
        c.addEol();
        c.add(new PropertiesParser.Token(PropertiesParser.Type.COMMENT, "# end"));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        p.store(os);
        assertThat(os.toString()).isEqualTo("# beginning" +
                "\nkey=value" +
                "\n# middle" +
                "\nkey2=value2" +
                "\n# end");
    }
}
