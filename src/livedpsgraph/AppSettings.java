/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class AppSettings {

    private final JFileChooser fc = new JFileChooser();
    private final String sep = File.separator;
    private static AppSettings instance;
    // Settings files root path and extension
    private final String settingsRootPath;
    private final String settingsExtension = ".json";
    // Settings file names
    private final String uiSettingsFileName = "ui_settings";
    private final String userNamesFileName = "user_names";
    // Finals
    private final int minWidth = 300;
    private final int minHeight = 200;
    private final ArrayList<ActionListener> userAddedListeners = new ArrayList<>();
    private final ArrayList<ActionListener> userChangedListeners = new ArrayList<>();
    // Variables
    private int appWidth;
    private int appHeight;
    private int appX;
    private int appY;
    private boolean pinned = false;
    private boolean alwaysOnTop = true;
    private Color primaryColor;
    private Color secondaryColor;
    private final ArrayList<String> userNames = new ArrayList<>();
    private String selectedUser = "";
    private String logPath;

    private AppSettings() {
        // Singleton
        // Init the filepaths
        settingsRootPath = new File("").getAbsolutePath() + sep + "settings" + sep;
        // Get the default path - there are multiple ways to do so and not all work on all OSs
        String tempPath = System.getProperty("user.home");
        // If tempPath is empty, try another way to get the default path
        if (tempPath == null || tempPath.length() < 1) {
            FileSystemView fw = fc.getFileSystemView();
            tempPath = fw.getDefaultDirectory().getAbsolutePath();
        } else {
            // Add \Documents to tempPath if System.getProperty works - this bypasses localization
            tempPath += sep + "Documents";
        }
        // Add the default EVE\Overview path to defaultPath, this is where the overview settings are exported and imported in EVE
        logPath = tempPath + sep + "EVE" + sep + "logs" + sep + "Gamelogs" + sep;
    }

    protected static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
            if (!instance.loadUISettings()) {
                instance.primaryColor = new Color(7, 7, 7);
                instance.secondaryColor = new Color(56, 56, 56);
                instance.setAppWidth(0);
                instance.setAppHeight(0);
            }
            instance.addToUsers("");
            if (!instance.loadUsers()) {
            }
        }
        return instance;
    }

    private boolean loadUISettings() {
        File file = new File(settingsRootPath + uiSettingsFileName + settingsExtension);
        if (file.exists()) {
            try {
                JsonObject parser = null;
                try (java.io.FileReader reader = new java.io.FileReader(file)) {
                    JsonElement element = new JsonParser().parse(reader);
                    if (element.isJsonObject()) {
                        parser = element.getAsJsonObject();
                    }
                }
                if (parser != null) {
                    appX = parser.get("posX").getAsInt();
                    appY = parser.get("posY").getAsInt();
                    appWidth = parser.get("width").getAsInt();
                    appHeight = parser.get("height").getAsInt();
                    pinned = parser.get("pinned").getAsBoolean();
                    // Version 1.1 try/catch
                    try {
                        alwaysOnTop = parser.get("alwaysOnTop").getAsBoolean();
                    } catch (NullPointerException ex) {
                        Logger.getLogger(AppSettings.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    primaryColor = new Color(parser.get("primaryColor").getAsInt());
                    secondaryColor = new Color(parser.get("secondaryColor").getAsInt());
                    return true;
                }
            } catch (Exception ex) {
                Logger.getLogger(AppSettings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }

    private boolean loadUsers() {
        File file = new File(settingsRootPath + userNamesFileName + settingsExtension);
        if (file.exists()) {
            try {
                JsonObject parser = null;
                try (java.io.FileReader reader = new java.io.FileReader(file)) {
                    JsonElement element = new JsonParser().parse(reader);
                    if (element.isJsonObject()) {
                        parser = element.getAsJsonObject();
                    }
                }
                if (parser != null) {
                    JsonElement arrElement = parser.get("userNames");
                    if (arrElement.isJsonArray()) {
                        JsonElement selectedElement = parser.get("selected");
                        this.selectedUser = selectedElement.getAsString();
                        JsonElement logPathElement = parser.get("logPath");
                        this.logPath = logPathElement.getAsString();
                        JsonArray array = arrElement.getAsJsonArray();
                        for (JsonElement ele : array) {
                            this.addToUsers(ele.getAsString());
                        }
                        return true;
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(AppSettings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }

    protected void saveSettings() throws IOException {
        File rootFile = new File(settingsRootPath);
        if (!rootFile.isDirectory()) {
            rootFile.mkdir();
        }
        File uiSettingsFile = new File(settingsRootPath + uiSettingsFileName + settingsExtension);
        if (!uiSettingsFile.exists()) {
            uiSettingsFile.createNewFile();
        }
        try (FileWriter writer = new FileWriter(uiSettingsFile)) {
            Gson gson = new Gson();
            gson.toJson(new UIObject(appX, appY, appWidth, appHeight, pinned, alwaysOnTop, instance.primaryColor.getRGB(), instance.secondaryColor.getRGB()), writer);
        }
        File userNamesFile = new File(settingsRootPath + userNamesFileName + settingsExtension);
        if (!userNamesFile.exists()) {
            userNamesFile.createNewFile();
        }
        try (FileWriter writer = new FileWriter(userNamesFile)) {
            Gson gson = new Gson();
            userNames.remove("");
            String[] userNamesArray = new String[userNames.size()];
            userNames.toArray(userNamesArray);
            gson.toJson(new UsersObject(userNamesArray, selectedUser, logPath), writer);
            userNames.add(0, "");
        }
    }

    protected boolean hasUsers() {
        for (String user : userNames) {
            if (!user.equals("")) {
                return true;
            }
        }
        return false;
    }

    protected void addAddedUserListener(ActionListener l) {
        userAddedListeners.add(l);
    }

    protected void addToUsers(String userName) {
        if (!userNames.contains(userName)) {
            userNames.add(userName);
            for (ActionListener l : userAddedListeners) {
                l.actionPerformed(new ActionEvent(userNames, 0, "Added user"));
            }
        }
    }

    protected void addToUsers(String[] userNames) {
        boolean addedUser = false;
        for (String userName : userNames) {
            if (!this.userNames.contains(userName)) {
                this.userNames.add(userName);
                addedUser = true;
            }
        }
        if (addedUser) {
            for (ActionListener l : userAddedListeners) {
                l.actionPerformed(new ActionEvent(userNames, 0, "Added users"));
            }
        }
    }

    protected String[] getUsers() {
        return userNames.toArray(new String[userNames.size()]);
    }
    
    private void emptyUsers() {
        userNames.clear();
        addToUsers("");
    }

    protected String getSelectedUser() {
        return selectedUser;
    }

    protected void setSelectedUser(String user) {
        selectedUser = user;
        for (ActionListener l : userChangedListeners) {
            l.actionPerformed(new ActionEvent(this, 0, "User changed"));
        }
    }

    protected String getLogPath() {
        return logPath;
    }

    protected void setLogPath(String logPath) {
        this.logPath = logPath;
        synchronizeUsers();
    }
    
    protected void synchronizeUsers() {
        emptyUsers();
        addToUsers(FileReader.getInstance().getUsersFromFiles());
    }

    protected final void addUserChangedListener(ActionListener l) {
        userChangedListeners.add(l);
    }

    protected Rectangle getAppRectangle() {
        return new Rectangle(instance.getAppX(), instance.getAppY(), instance.getAppWidth(), instance.getAppHeight());
    }

    protected void setAppRectangle(Rectangle rect) {
        instance.setAppX(rect.x);
        instance.setAppY(rect.y);
        instance.setAppWidth(rect.width);
        instance.setAppHeight(rect.height);
    }

    protected void resizeApp(Rectangle rect) {
        instance.setAppWidth(rect.width);
        instance.setAppHeight(rect.height);
        if (rect.width == instance.getAppWidth()) {
            instance.setAppX(rect.x);
        }
        if (rect.height == instance.getAppHeight()) {
            instance.setAppY(rect.y);
        }
    }

    protected int getAppWidth() {
        return appWidth;
    }

    protected void setAppWidth(int width) {
        this.appWidth = width > minWidth ? width : minWidth;
    }

    protected int getAppHeight() {
        return appHeight;
    }

    protected void setAppHeight(int height) {
        this.appHeight = height > minHeight ? height : minHeight;
    }

    protected boolean isPinned() {
        return pinned;
    }

    protected void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
    
    protected boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }
    
    protected void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
    }

    protected int getAppX() {
        return appX;
    }

    protected void setAppX(int appX) {
        this.appX = appX;
    }

    protected int getAppY() {
        return appY;
    }

    protected void setAppY(int appY) {
        this.appY = appY;
    }

    protected Color getPrimaryColor() {
        return primaryColor;
    }

    protected void setPrimaryColor(Color color) {
        primaryColor = color;
    }

    protected Color getSecondaryColor() {
        return secondaryColor;
    }

    protected void setSecondaryColor(Color color) {
        secondaryColor = color;
    }

    protected class UIObject {

        int posX;
        int posY;
        int width;
        int height;
        boolean pinned;
        boolean alwaysOnTop;
        int primaryColor;
        int secondaryColor;

        protected UIObject(int posX, int posY, int width, int height, boolean pinned, boolean alwaysOnTop, int primaryColor, int secondaryColor) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.pinned = pinned;
            this.alwaysOnTop = alwaysOnTop;
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
        }
    }

    protected class UsersObject {

        String[] userNames;
        String selected;
        String logPath;

        protected UsersObject(String[] userNames, String selected, String logPath) {
            this.userNames = userNames;
            this.selected = selected;
            this.logPath = logPath;
        }
    }
}
