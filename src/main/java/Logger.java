import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final Path LOG_FILE = Paths.get("file.log");
    private static final String TIME_PATTERN = "yyyy.MM.dd HH:mm:ss";
    private static final String LINE_PATTERN = "[%-4s %s] %s";

    private static String getLogString(Status status, String string) {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
        String dateString = dateTime.format(formatter);

        return String.format(LINE_PATTERN, status.name(), dateString, string);
    }

    private static void print(String string) {
        System.out.println(string);
    }

    private static void writeToFile(String string) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE.toFile(), true))) {
            writer.write(string);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            log(Status.WARN, e.getMessage());
        }
    }

    public static void log(Status status, String message) {
        String logString = getLogString(status, message);
        writeToFile(logString);
        print(logString);
    }

    public enum Status {
        WARN,
        INFO
    }
}
