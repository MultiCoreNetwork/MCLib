package it.multicoredev.mclib.yaml;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ConfigurationTest {

    @Test
    public void saveTest() throws IOException {
        Configuration config = new Configuration(new File("test.yml"), getClass().getResourceAsStream("test.yml"), false);
        config.save();
    }

    @Test
    public void loadTest() throws IOException {
        Configuration config = new Configuration(new File("test.yml"), getClass().getResourceAsStream("test.yml"), false);
        try {
            config.load();
        } finally {
            new File("test.yml").delete();
        }
    }
}
