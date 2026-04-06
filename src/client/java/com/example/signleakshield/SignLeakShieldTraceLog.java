package com.example.signleakshield;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class SignLeakShieldTraceLog {
    private static final Path LOG_PATH = Paths.get("logs", "signleakshield-trace.log");

    private SignLeakShieldTraceLog() {
    }

    public static synchronized void reset() {
        try {
            Files.deleteIfExists(LOG_PATH);
        } catch (IOException ignored) {
        }
    }

    public static void info(String message) {
        SignLeakShieldClient.LOGGER.info(message);
        append(message);
    }

    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    private static synchronized void append(String message) {
        try {
            Path parent = LOG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String line = String.format(
                "%s %s%n",
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now()),
                message
            );

            Files.writeString(
                LOG_PATH,
                line,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
            );
        } catch (IOException ignored) {
        }
    }
}
