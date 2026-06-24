package com.project.teya.ledger;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@UtilityClass
public class TestUtils {

    public static String loadFileAsString(String filename) throws IOException {
        String absolutePath = Paths.get("src", "test", "resources").toFile().getAbsolutePath();
        return new String(Files.readAllBytes(Paths.get(absolutePath, filename)));
    }

    public static String loadFileAndTemplate(String filename, Object... args) throws IOException {
        String absolutePath = Paths.get("src", "test", "resources").toFile().getAbsolutePath();
        return String.format(new String(Files.readAllBytes(Paths.get(absolutePath, filename))), args);
    }
}
