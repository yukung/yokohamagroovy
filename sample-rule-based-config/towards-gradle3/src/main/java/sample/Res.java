package sample;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Res {

    private final Properties prop;

    public Res() throws IOException {
        ClassLoader cl = getClass().getClassLoader();
        prop = new Properties();
        try(InputStream is = cl.getResourceAsStream("app.properties")) {
            InputStreamReader r = new InputStreamReader(is, StandardCharsets.UTF_8);
            prop.load(r);
        }
    }

    public String getMessage(String key) {
        return prop.getProperty(key, "<no-entry>");
    }
}
