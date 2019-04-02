/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import livedpsgraph.Tools.DataObject;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class FileReader {

    private static final FileReader instance = new FileReader();
    private final AppSettings settings = AppSettings.getInstance();
    private final String sep = File.separator;
    private final Pattern fileNamePattern = Pattern.compile("^\\d{8}[_]\\d+.txt$", Pattern.CASE_INSENSITIVE);
    private final Pattern dateTimePattern = Pattern.compile("\\d{4}.\\d{2}.\\d{2}[ ]*\\d{2}:\\d{2}:\\d{2}");
    private final Pattern simpleCombatPattern = Pattern.compile("\\(combat\\)");
    private final Pattern combatPattern = Pattern.compile("\\(combat\\)\\d+[A-Za-z]{2,4}");
    private final Pattern missesPattern = Pattern.compile("\\(combat\\).+misses.+completely");
    private final Pattern missesYouCompletelyPattern = Pattern.compile("\\(combat\\).+missesyoucompletely");
    private File currentLog;
    private String lastDateTime = "";
    private String calibrationDate = "";
    private int previousDateTimeOccurences = 0;
    private ReverseLineInputStream reader;
    private boolean sawData = false;
    
    private FileReader() {
        // Singleton-class
    }
    
    protected static final FileReader getInstance() {
        return instance;
    }

    private final FilenameFilter fFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            // Only accept files which names matches the '8 digits, underscore, multiple digits, .txt'-pattern
            return fileNamePattern.matcher(name).matches();
        }
    };

    protected final boolean defineMostRecentFile() {
        currentLog = null;
        boolean foundFile = false;
        // Get the directory that contains the logs
        File logDir = new File(settings.getLogPath());
        if (logDir.isDirectory()) {
            // From the log-directory, get all files matching a certain name-pattern
            String[] paths = logDir.list(fFilter);
            // Sort the paths in alphabetical order
            Arrays.sort(paths);
            // Get the most recent path with valuable information

            while (!foundFile) {
                int lastPathIndex = paths.length - 1;
                if (lastPathIndex >= 0) {
                    String mostRecent = paths[lastPathIndex];
                    String path = logDir.getAbsolutePath().concat(sep).concat(mostRecent);
                    File checkingFile = new File(path);
                    if (hasValuableInfo(checkingFile)) {
                        // Update the file we are reading
                        currentLog = new File(path);
                        foundFile = true;
                    } else {
                        // If the file doesn't have valuable info, remove this path from the array
                        paths = Arrays.copyOf(paths, lastPathIndex);
                    }
                } else {
                    break;
                }
            }
        }
        if (currentLog != null) {
            System.out.println("Most recent path: " + currentLog.getAbsolutePath());
        }
        return foundFile;
    }

    protected String[] getUsersFromFiles() {
        ArrayList<String> foundUsers = new ArrayList<>();
        // Get the directory that contains the logs
        File logDir = new File(settings.getLogPath());
        if (logDir.isDirectory()) {
            // From the log-directory, get all files matching a certain name-pattern
            String[] paths = logDir.list(fFilter);
            // Sort the paths in alphabetical order
            Arrays.sort(paths);
            // Make sure we search a max amount of files
            int searched = 0;
            while (searched < 1000) {
                // Only search if there are files left
                int lastPathIndex = paths.length - 1;
                if (lastPathIndex >= 0) {
                    searched += 1;
                    // Get the next most recent file
                    String mostRecent = paths[lastPathIndex];
                    String path = logDir.getAbsolutePath().concat(sep).concat(mostRecent);
                    File checkingFile = new File(path);
                    // Get the user
                    String foundUser = getFileListener(checkingFile);
                    if (foundUser != null && foundUser.length() > 0 && !foundUsers.contains(foundUser)) {
                        // If a user was found and we haven't added it yet, add it
                        foundUsers.add(foundUser);
                    }
                    // Update the paths array, removing the path we just searched
                    paths = Arrays.copyOf(paths, lastPathIndex);
                } else {
                    // Searched all files, stop the loop
                    break;
                }
            }
        }
        return foundUsers.toArray(new String[foundUsers.size()]);
    }

    private String getFileListener(File f) {
        FileInputStream in;
        try {
            in = new FileInputStream(f);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                br.readLine();
                br.readLine();
                String lineIWant = br.readLine();
                in.close();
                if (lineIWant.contains("Listener:")) {
                    return lineIWant.replace("  Listener: ", "");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private boolean hasValuableInfo(File f) {
        if (settings.getSelectedUser() != null && !settings.getSelectedUser().equals("")) {
            FileInputStream in;
            try {
                in = new FileInputStream(f);
                String lineIWant;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                    br.readLine();
                    br.readLine();
                    lineIWant = br.readLine();
                    in.close();
                    return lineIWant.contains("Listener: ".concat(settings.getSelectedUser()));
                }
            } catch (IOException ex) {
                Logger.getLogger(FileReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    protected boolean hasNewCombatData() {
        if (currentLog.exists()) {
            try {
                reader = new ReverseLineInputStream(currentLog);
                boolean hasNewLine;
                int lastDateTimeOccurences = 0;
                boolean foundLine = false;
                // Iterate each line while there are lines left
                do {
                    int bytesRead;
                    String read = "";
                    hasNewLine = reader.findPrevLine();
                    // Read the new line
                    do {
                        byte[] b = new byte[1];
                        bytesRead = reader.read(b);
                        read = read.concat(new String(b));
                    } while (bytesRead != -1 && read.length() <= 40); // To save time, only match on the 'line header', which is in the first 40 chars
                    Matcher dateTimeMatcher = dateTimePattern.matcher(read);
                    if (dateTimeMatcher.find()) {
                        if (!sawData) {
                            sawData = true;
                            lastDateTime = dateTimeMatcher.group(0).replace(" ", "");
                            calibrationDate = lastDateTime;
                            // Stop reading
                            break;
                        } else {
                            // After the first line, we are only interested in combat-data
                            Matcher combatMatcher = simpleCombatPattern.matcher(read);
                            if (combatMatcher.find()) {
                                foundLine = true;
                                String newLineDateTime = dateTimeMatcher.group(0).replace(" ", "");
                                int calibrationComparisson = newLineDateTime.compareTo(calibrationDate);
                                if (calibrationComparisson > 0) {
                                    int lastDateComparisson = newLineDateTime.compareTo(lastDateTime);
                                    if (lastDateComparisson == 0) { // Equal
                                        // Update occurences and continue reading
                                        lastDateTimeOccurences += 1;
                                    } else if (lastDateComparisson < 0) { // newLineDateTime < lastDateTime
                                        // Stop reading
                                        break;
                                    } else { // newLineDateTime > lastDateTime
                                        // There is new data, reset the previousDateTimeOccurences and update lastDateTime and occurences
                                        previousDateTimeOccurences = 0;
                                        lastDateTime = newLineDateTime;
                                        lastDateTimeOccurences += 1;
                                    }
                                } else {
                                    // new date equals the calibration date, we only want lines after that
                                    break;
                                }
                            }
                        }
                    }
                } while (hasNewLine);
                // If the last line's DateTime equals lastDateTime, there can still
                // be new data if there are more occurences of this last DateTime
                boolean returnVal = foundLine && lastDateTimeOccurences > previousDateTimeOccurences;
                if (foundLine) {
                    // Only update previousDateTimeOccurences if we've found a combat-line
                    previousDateTimeOccurences = lastDateTimeOccurences;
                }
                return returnVal;
            } catch (IOException ex) {
                Logger.getLogger(FileReader.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                reader.close();
            }
        }
        return false;
    }

    int prevLastDateTimeOccurences = 0;

    protected ArrayList<DataObject> getNewCombatLines(String prevDateTime) {
        ArrayList<DataObject> data = new ArrayList<>();
        int lastDateTimeOccurences = 0;
        boolean first = true;
        if (currentLog.exists()) {
            try {
                reader = new ReverseLineInputStream(currentLog);
                boolean hasNewLine;
                // Iterate each line while there are lines left
                do {
                    int bytesRead;
                    String read = "";
                    hasNewLine = reader.findPrevLine();
                    boolean insideElement = false;
                    // Read the new line
                    do {
                        byte[] b = new byte[1];
                        bytesRead = reader.read(b);
                        String readByte = new String(b);
                        if (insideElement) {
                            if (readByte.equals(">")) {
                                insideElement = false;
                            }
                        } else {
                            // Exclude spaces, carriage returns and line feeds
                            if (!Character.isWhitespace(readByte.charAt(0))) {
                                switch (readByte) {
                                    case " ": // 'Special' whitespace character
                                        break;
                                    case "<": // Exclude html-elements
                                        insideElement = true;
                                        break;
                                    default:
                                        read = read.concat(readByte);
                                        break;
                                }
                            }
                        }
                    } while (bytesRead != -1);
                    // If the length of read does not exceed 40, it's a useless line
                    if (read.length() >= 40) {
                        // Parse the new line
                        String lineHeader = read.substring(0, 40);
                        Matcher dateTimeMatcher = dateTimePattern.matcher(lineHeader);
                        if (dateTimeMatcher.find()) {
                            String newLineDateTime = dateTimeMatcher.group(0);
                            // We are only interested in data newer than calibration dateTime
                            String spacelessCalibration = calibrationDate.replace(" ", "");
                            int calibrationComparisson = newLineDateTime.compareTo(spacelessCalibration);
                            if (calibrationComparisson > 0) {
                                int prevDateTimeComparisson = newLineDateTime.compareTo(prevDateTime);
                                if (prevDateTimeComparisson >= 0) { // Equal or newLineDateTime > prevDateTime
                                    // We are only interested in combat-data
                                    Matcher combatMatcher = combatPattern.matcher(lineHeader);
                                    if (combatMatcher.find()) {
                                        // Add DataObject at pos 0 and continue reading
                                        DataObject obj = new DataObject(newLineDateTime, read.substring(read.indexOf(")", newLineDateTime.length()) + 1));
                                        data.add(0, obj);
                                        if (first) {
                                            lastDateTime = obj.getDateTime();
                                            first = false;
                                        }
                                        if (lastDateTime.equals(obj.getDateTime())) {
                                            lastDateTimeOccurences += 1;
                                        }
                                    } else {
                                        // No damage done, check if it's a miss instead
                                        Matcher missesMatcher = missesPattern.matcher(read);
                                        if (missesMatcher.find()) {
                                            DataObject obj;
                                            Matcher missesYouMatcher = missesYouCompletelyPattern.matcher(read);
                                            // Check if it's an incoming or outgoing miss
                                            if (missesYouMatcher.find()) {
                                                obj = new DataObject(newLineDateTime, "0from");
                                            } else {
                                                obj = new DataObject(newLineDateTime, "0to-".concat(read.substring(read.lastIndexOf("-") + 1)).concat("-"));
                                            }
                                            data.add(0, obj);
                                            if (first) {
                                                lastDateTime = obj.getDateTime();
                                                first = false;
                                            }
                                            if (lastDateTime.equals(obj.getDateTime())) {
                                                lastDateTimeOccurences += 1;
                                            }
                                        }
                                    }
                                } else if (prevDateTimeComparisson < 0) { // newLineDateTime < prevDateTime
                                    // Stop reading
                                    break;
                                }
                            } else {
                                // DateTime is older than calibration, stop reading
                                break;
                            }
                        }
                    }
                } while (hasNewLine);
            } catch (IOException ex) {
                Logger.getLogger(FileReader.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                reader.close();
            }
        }
        if (data.size() >= prevLastDateTimeOccurences) {
            int iterations = data.size();
            for (int i = 0; i < iterations && (prevLastDateTimeOccurences > 0); i++) {
                DataObject delete = data.get(i);
                int comparisson = delete.getDateTime().compareTo(prevDateTime);
                if (comparisson <= 0) {
                    if (data.remove(delete)) {
                        iterations -= 1;
                        i -= 1;
                        if (comparisson == 0) {
                            prevLastDateTimeOccurences -= 1;
                        }
                    }
                }
            }
        }
        prevLastDateTimeOccurences = lastDateTimeOccurences;
        return data;
    }
}
