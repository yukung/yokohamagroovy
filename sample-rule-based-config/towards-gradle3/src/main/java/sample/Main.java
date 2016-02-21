package sample;

import java.io.IOException;

public class Main {
    public static void main(String... args) throws IOException {
        Res res = new Res();
        System.out.println(res.getMessage("message"));
        System.out.println(res.getMessage("name"));
        System.out.println(res.getMessage("my"));
    }
}
