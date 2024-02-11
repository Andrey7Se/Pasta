import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Gui {
    private static final String ICON_MAIN = "icon_main.png";
    private static final String ICON_ADD = "icon_add.png";
    private static final String ICON_UPDATE = "icon_update.png";
    private static final String ICON_OUT = "icon_out.png";

    private static final ItemProducer itemProducer = new ItemProducer();
    private static final DataManager dataManager = DataManager.getInstance();

    private static PopupMenu menu;
    private static TrayIcon trayIcon;

    private static Image mainIconImage;
    private static Image addIconImage;
    private static Image updateIconImage;
    private static Image outIconImage;

    private static final ArrayList<MenuItem> menuItemsList = new ArrayList<>();

    public static void init() {
        try {
            if (!SystemTray.isSupported()) {
                Logger.log(Logger.Status.WARN, "SystemTray don't support");

                return;
            }

            Logger.log(Logger.Status.INFO, "\t---- Start ----");

            mainIconImage = loadIconImage(ICON_MAIN);
            addIconImage = loadIconImage(ICON_ADD);
            updateIconImage = loadIconImage(ICON_UPDATE);
            outIconImage = loadIconImage(ICON_OUT);

            SystemTray systemTray = SystemTray.getSystemTray();

            trayIcon = new TrayIcon(mainIconImage);
            trayIcon.setImageAutoSize(true);

            menu = new PopupMenu();
            trayIcon.setPopupMenu(menu);
            systemTray.add(trayIcon);

            update();
            dataManager.startWatchAtChangesInDataFile();

        } catch (AWTException | IOException e) {
            Logger.log(Logger.Status.WARN, e.getMessage());
        }
    }

    private static Image loadIconImage(String pathToImage) throws IOException {
        return ImageIO.read(Objects.requireNonNull(Gui.class.getResource(pathToImage)));
    }

    private static void setAllItemsToTray() {
        if (!menuItemsList.isEmpty()) {
            menuItemsList.forEach(item -> menu.add(item));
        } else {
            MenuItem emptyItem = new MenuItem("nothing");
            emptyItem.setEnabled(false);
            menu.add(emptyItem);
        }
    }

    private static void setUtilityItems() {
        menu.addSeparator();

        Menu moreMenu = new Menu("more..");
        moreMenu.add(itemProducer.getEditItem());
        moreMenu.add(itemProducer.getUpdateItem());
        moreMenu.add(itemProducer.getQuitItem());

        menu.add(moreMenu);

        menu.add(itemProducer.getAddItem());
    }

    private static void reloadItemsList() {
        ArrayList<MenuItem> list = itemProducer.getItemsFromStrings();

        if (list != null) {
            menuItemsList.clear();
            menuItemsList.addAll(list);
        }
    }

    public static void update() {
        menu.removeAll();
        reloadItemsList();
        setAllItemsToTray();
        setUtilityItems();
        //Logger.log(Logger.Status.INFO, "Menu was updated.");
    }

    public static void setOtherIcon(IconName iconName) {
        Image iconImage = null;

        switch (iconName) {
            case ADD:
                iconImage = addIconImage;
                break;
            case UPDATE:
                iconImage = updateIconImage;
                break;
            case OUT:
                iconImage = outIconImage;
                break;
        }

        trayIcon.setImage(iconImage);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                trayIcon.setImage(mainIconImage);
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 2000);
    }

    public enum IconName {
        ADD,
        UPDATE,
        OUT
    }
}