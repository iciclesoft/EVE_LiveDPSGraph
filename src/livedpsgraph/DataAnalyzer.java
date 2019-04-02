/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.Timer;
import livedpsgraph.Tools.DataObject;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class DataAnalyzer {

    private final FileReader reader;
    private final AppSettings settings = AppSettings.getInstance();
    private String lastDateTime = "";
    // Timespan constants
    private final long threeSeconds = 1000 * 3;
    private final long thirtySeconds = 1000 * 30;
    private final long maxOutgoingCombatInterval = 1000 * 40; // 40 seconds
    // Other internally used variables
    private final GregorianCalendar calendar = new GregorianCalendar(Locale.UK);
    private long startTime = 0;
    private long lastSeenTimestamp = 0;
    private long lastMsgMillis = 0;
    private Timer analyzeTimer;
    // Incoming/outgoing arrays
    private final ArrayList<DamageDataObject> incoming = new ArrayList<>();
    private final ArrayList<DamageDataObject> outgoing = new ArrayList<>();
    // Patterns
    private final Pattern dmgScopeToPattern = Pattern.compile("\\d+[to]{1}.*");
    private final Pattern dmgScopeFromPattern = Pattern.compile("\\d+[from]{1}.*");
    // Variables for the DataVisualizer
    private boolean foundFile = false;
    private boolean isInCombat = false;
    private boolean isShooting = false;
    private boolean isGettingShot = false;
    private int outgoingDps = 0;
    private int incomingDps = 0;
    private long lastDataMs = 0;

    protected DataAnalyzer() {
        reader = FileReader.getInstance();
        if (!settings.hasUsers()) {
            settings.addToUsers(reader.getUsersFromFiles());
        }
        settings.addUserChangedListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stopAnalyzing();
                foundFile = reader.defineMostRecentFile();
                if (foundFile) {
                    startAnalyzing();
                }
            }
        });
        foundFile = reader.defineMostRecentFile();
        if (foundFile) {
            startAnalyzing();
        }
    }

    protected final boolean getFoundFile() {
        return foundFile;
    }

    protected final void startAnalyzing() {
        analyzeTimer = new Timer(500,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Only parse new data if there is new data
                        if (reader.hasNewCombatData()) {
                            lastDataMs = 0;
                            parseNewData(reader.getNewCombatLines(lastDateTime));
                            updateCurrentTimeMillis();
                        } else {
                            if (lastDataMs == 0) {
                                lastDataMs = System.currentTimeMillis();
                            } else {
                                if (System.currentTimeMillis() - lastDataMs > thirtySeconds) {
                                    lastDataMs = 0;
                                    reader.defineMostRecentFile();
                                }
                            }
                        }
                        boolean alreadyWasInCombat = isInCombat;
                        updateIsInCombat();
                        if (isInCombat) {
                            if (!alreadyWasInCombat) {
                                // Init a new combat session
                                initCombatSession();
                            }
                        } else if (alreadyWasInCombat) {
                            // Just got out of combat
                            resetCombatSession();
                        }
                        outgoingDps = updateData(outgoing);
                        incomingDps = updateData(incoming);
                    }
                });
        analyzeTimer.start();
    }

    protected final void stopAnalyzing() {
        if (analyzeTimer != null) {
            analyzeTimer.stop();
        }
    }

    protected boolean getIsInCombat() {
        return isInCombat;
    }

    protected boolean getIsShooting() {
        return isShooting;
    }

    protected boolean getIsGettingShot() {
        return isGettingShot;
    }

    protected int getOutgoingDps() {
        return outgoingDps;
    }

    protected int getIncomingDps() {
        return incomingDps;
    }

    private void updateIsInCombat() {
        long diffLastOutgoing = System.currentTimeMillis() - lastMsgMillis;
        if (diffLastOutgoing < maxOutgoingCombatInterval) {
            isInCombat = true;
            return;
        }
        isShooting = false;
        isGettingShot = false;
        isInCombat = false;
    }

    private void initCombatSession() {
        long firstStamp = 0;
        if (!outgoing.isEmpty()) {
            firstStamp = outgoing.get(outgoing.size() - 1).timestamp;
        }
        if (!incoming.isEmpty()) {
            long firstInStamp = incoming.get(incoming.size() - 1).timestamp;
            if (firstStamp == 0 || firstStamp < firstInStamp) {
                firstStamp = firstInStamp;
            }
        }
        startTime = firstStamp;
    }

    private void updateCurrentTimeMillis() {
        long firstStamp = 0;
        if (!outgoing.isEmpty()) {
            firstStamp = outgoing.get(outgoing.size() - 1).timestamp;
        }
        if (!incoming.isEmpty()) {
            long firstInStamp = incoming.get(incoming.size() - 1).timestamp;
            if (firstStamp == 0 || firstStamp < firstInStamp) {
                firstStamp = firstInStamp;
            }
        }
        lastSeenTimestamp = firstStamp;
        lastMsgMillis = System.currentTimeMillis();
        Tools.updateTimeMillisOffset(lastSeenTimestamp);
    }

    private int updateData(ArrayList<DamageDataObject> damageList) {
        if (startTime != 0) {
            long totalDamage = 0;
            for (DamageDataObject obj : damageList) {
                totalDamage += obj.damage;
            }

            long millisElapsed = Tools.currentTimeMillis() - startTime + threeSeconds;
            if (millisElapsed > 1000) {
                return (int) ((totalDamage * 1000) / millisElapsed);
            } else {
                return (int) totalDamage;
            }
        }
        return 0;
    }

    private void resetCombatSession() {
        Calendar startCal = Calendar.getInstance(Locale.UK);
        startCal.setTimeInMillis(startTime);

        Calendar endCal = Calendar.getInstance(Locale.UK);
        endCal.setTimeInMillis(lastSeenTimestamp);
        startTime = 0;
        outgoing.clear();
        incoming.clear();
        outgoingDps = 0;
        incomingDps = 0;
    }

    private void parseNewData(ArrayList<DataObject> newLines) {
        if (newLines.size() > 0) {
            lastDateTime = newLines.get(newLines.size() - 1).getDateTime();
            for (DataObject obj : newLines) {
                DmgScope dmgScope = fetchDamageScope(obj.getData());
                // We're only interested in damage
                if (dmgScope != DmgScope.NONE) {
                    long timestamp = fetchTimestamp(obj.getDateTime());
                    int damage = fetchDamage(obj.getData(), dmgScope);
                    if (dmgScope == DmgScope.OUTGOING) {
                        outgoing.add(new DamageDataObject(damage, dmgScope, timestamp));
                    } else {
                        incoming.add(new DamageDataObject(damage, dmgScope, timestamp));
                    }
                }
            }
        }
    }

    private long fetchTimestamp(String dateTime) {
        int year = Integer.parseInt(dateTime.substring(0, 4));
        int month = Integer.parseInt(dateTime.substring(5, 7)) - 1; // Months start at 0, hence the -1
        int day = Integer.parseInt(dateTime.substring(8, 10));
        int hours = Integer.parseInt(dateTime.substring(10, 12));
        int minutes = Integer.parseInt(dateTime.substring(13, 15));
        int seconds = Integer.parseInt(dateTime.substring(16, 18));

        calendar.set(year, month, day, hours, minutes, seconds);
        return calendar.getTimeInMillis();
    }

    private DmgScope fetchDamageScope(String data) {
        if (dmgScopeToPattern.matcher(data).matches()) {
            return DmgScope.OUTGOING;
        } else if (dmgScopeFromPattern.matcher(data).matches()) {
            return DmgScope.INCOMING;
        }
        return DmgScope.NONE;
    }

    private int fetchDamage(String data, DmgScope dmgScope) {
        switch (dmgScope) {
            case OUTGOING:
                return Integer.parseInt(data.substring(0, data.indexOf("to")));
            case INCOMING:
                return Integer.parseInt(data.substring(0, data.indexOf("from")));
            default:
                return -1;
        }
    }

    protected final class DamageDataObject {

        private final int damage;
        private final DmgScope scope;
        private final long timestamp;

        public DamageDataObject(int damage, DmgScope scope, long timestamp) {
            this.damage = damage;
            this.scope = scope;
            this.timestamp = timestamp;
        }

        public int getDamage() {
            return damage;
        }

        public DmgScope getScope() {
            return scope;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public enum DmgScope {

        INCOMING,
        OUTGOING,
        NONE
    }
}
