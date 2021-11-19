package org.codejive.properties;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class TestProperties {

    @Test
    void testLoad() throws IOException, URISyntaxException {
        Path f = Paths.get(getClass().getResource("/test.properties").toURI());
        Properties p = Properties.loadProperties(f);
        assertThat(p, aMapWithSize(7));
        assertThat(p.keySet(), contains("one", "two", "three", " with spaces", "altsep", "multiline", "key.4"));
        assertThat(p.values(), contains("simple", "value containing spaces", "and escapes\n\t\r\f", "everywhere",
                "value", "one \n    two  \n\tthree", "\u1234"));
    }

    @Test
    void testStore() throws IOException, URISyntaxException {
        Path f = Paths.get(getClass().getResource("/test.properties").toURI());
        Properties p = Properties.loadProperties(f);
        StringWriter sw = new StringWriter();
        p.store(sw);
        assertThat(sw.toString(), equalTo(new String(Files.readAllBytes(f))));
    }
}
