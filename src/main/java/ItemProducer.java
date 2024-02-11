import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

public class ItemProducer {
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private final DataManager dataManager = DataManager.getInstance();

    private static final int maxLengthLineItem = 24;

    private final Font boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    // make items from text-lines
    public ArrayList<MenuItem> getItemsFromStrings() {
        ArrayList<MenuItem> result = new ArrayList<>();

        dataManager.getHeadersAndBody()
                .forEach(str -> result.add(getNewItem(
                        str[0].isBlank() ? str[1] : str[0],
                        str[1]
                )));

        return result;
    }

    // New
    public MenuItem getNewItem(String head, String body) {
        StringBuilder trimmedHead = new StringBuilder();

        if (head.length() > maxLengthLineItem) {
            trimmedHead.append(head, 0, maxLengthLineItem - 3);
            trimmedHead.append("...");
        } else {
            trimmedHead.append(head);
        }

        MenuItem item = new MenuItem(trimmedHead.toString());

        item.addActionListener(listener -> {
            StringSelection selection = new StringSelection(body);
            clipboard.setContents(selection, null);

            Gui.setOtherIcon(Gui.IconName.OUT);
            Logger.log(Logger.Status.INFO, "Text-line set to clipboard => {" + body + "}");
        });

        if (!head.equals(body)) {
            item.setFont(boldFont);
        }

        return item;
    }

    // Add
    public MenuItem getAddItem() {
        MenuItem addItem = new MenuItem("Add Line");
        addItem.setFont(boldFont);

        addItem.addActionListener(l -> {
            //Logger.log(Logger.Status.INFO, "Pressed Add-button...");
            try {
                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    String incomingString = (String) clipboard.getData(DataFlavor.stringFlavor);
                    dataManager.setTrueAddedNew();
                    dataManager.writeNewLineInFile(incomingString);

                    Gui.setOtherIcon(Gui.IconName.ADD);
                } else {
                    Logger.log(Logger.Status.INFO, "Object into clipboard is not text.");

                    return;
                }

            } catch (UnsupportedFlavorException | IOException e) {
                Logger.log(Logger.Status.WARN, e.getMessage());
            }

            Gui.update();
        });

        return addItem;
    }

    // Edit
    public MenuItem getEditItem() {
        MenuItem editItem = new MenuItem("Edit");
        editItem.addActionListener(l -> {
            //Logger.log(Logger.Status.INFO, "Pressed Edit-button...");
            dataManager.openDataInOS();
        });

        return editItem;
    }

    // Update
    public MenuItem getUpdateItem() {
        MenuItem updateItem = new MenuItem("Update");

        updateItem.addActionListener(l -> {
            //Logger.log(Logger.Status.INFO, "Pressed Update-button...");
            Gui.update();
            Gui.setOtherIcon(Gui.IconName.UPDATE);
        });

        return updateItem;
    }

    // Quit
    public MenuItem getQuitItem() {
        MenuItem quitItem = new MenuItem("Quit");

        quitItem.addActionListener(l -> {
            //Logger.log(Logger.Status.INFO, "Pressed Quit-button...");
            Logger.log(Logger.Status.INFO, "\t---- Exit ----");
            System.exit(0);
        });

        return quitItem;
    }
}
