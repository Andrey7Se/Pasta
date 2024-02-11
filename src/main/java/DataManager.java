import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class DataManager {
    private static DataManager dm;
    private static final String PATH_TO_DATA_FILE = "data.txt";
    private boolean isAddedNew = false;

    private DataManager() {
    }

    public static DataManager getInstance() {
        if (dm == null) {
            dm = new DataManager();
        }

        return dm;
    }

    private ArrayList<String> readLinesFromFile() {
        ArrayList<String> list = new ArrayList<>();
        Path path = Paths.get(PATH_TO_DATA_FILE);

        if (Files.notExists(path)) {
            try {
                Files.createFile(path);
                Logger.log(Logger.Status.INFO, "Data-file not found. Created new file: " + path.toAbsolutePath());

                return list;
            } catch (IOException e) {
                Logger.log(Logger.Status.WARN, e.getMessage());
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(PATH_TO_DATA_FILE))) {
            while (reader.ready()) {
                String line = reader.readLine().trim();

                if (line.startsWith("<") && line.contains(">")) {
                    list.add(line);
                }
            }
            //Logger.log(Logger.Status.INFO, "Data-file was read.");
        } catch (IOException e) {
            Logger.log(Logger.Status.WARN, e.getMessage());
        }

        return list;
    }

    public ArrayList<String[]> getHeadersAndBody() {
        ArrayList<String> linesFromFileList = readLinesFromFile();
        ArrayList<String[]> linesWithHead = new ArrayList<>();
        ArrayList<String[]> linesWithoutHead = new ArrayList<>();

        linesFromFileList.forEach(str -> {
            String head = str.substring(str.indexOf('<') + 1, str.indexOf('>'));
            String body = str.substring(str.indexOf('>') + 1);
            String[] arrLines = new String[]{head, body};

            if (!head.isEmpty()) {
                linesWithHead.add(arrLines);
            } else {
                linesWithoutHead.add(arrLines);
            }
        });

        Collections.reverse(linesWithoutHead);
        ArrayList<String[]> resultList = new ArrayList<>(linesWithoutHead);

        Collections.reverse(linesWithHead);
        resultList.addAll(linesWithHead);

        return resultList;
    }

    public void openDataInOS() {
        try {
            Desktop.getDesktop().open(new File(PATH_TO_DATA_FILE));
            //Logger.log(Logger.Status.INFO, "Data-file was opened into default text-editor.");
        } catch (IOException e) {
            Logger.log(Logger.Status.WARN, e.getMessage());
        }
    }

    public void writeNewLineInFile(String str) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATH_TO_DATA_FILE, true))) {
            writer.write("<>" + str);
            writer.write(System.lineSeparator());

            Logger.log(Logger.Status.INFO, "New line written in data-file <= {" + str + "}");
        } catch (IOException e) {
            Logger.log(Logger.Status.WARN, e.getMessage());
        }
    }

    public void startWatchAtChangesInDataFile() {
        Path dir = Paths.get("."); //root application folder

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            // ENTRY_CREATE need only in Linux for use in some text-editors
            dir.register(watchService, ENTRY_MODIFY, ENTRY_CREATE);

            WatchKey key;

            while (true) {
                key = watchService.take();

                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().toString().endsWith(PATH_TO_DATA_FILE)) {
                            //Logger.log(Logger.Status.INFO, "Data-file was changed in editor.");
                            if (isAddedNew) {
                                isAddedNew = false;
                            } else {
                                Gui.update();
                                Gui.setOtherIcon(Gui.IconName.UPDATE);
                            }
                        }
                    }

                    key.reset();
                }
            }
        } catch (IOException | InterruptedException e) {
            Logger.log(Logger.Status.WARN, e.getMessage());
        }
    }

    public void setTrueAddedNew() {
        isAddedNew = true;
    }
}
