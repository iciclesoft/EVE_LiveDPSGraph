/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class DataVisualizer {

    private final AppSettings settings = AppSettings.getInstance();
    private final JPanel contentPanel;
    private final DataAnalyzer analyzer;
    private final Font defaultFont;
    private Color fontColorPrimary;
    private Color fontColorSecondary;
    private final Graph graph = new Graph();
    private final JLabel combatOutText = new JLabel();
    private final JLabel combatInText = new JLabel();
    private final int padding = 5;
    private final int combatTextWidth = (int) (settings.getAppWidth() * .5);
    private final int repaintInterval = 30;

    protected DataVisualizer(JPanel contentPanel) {
        defaultFont = Font.decode(null).deriveFont(12.0f);
        this.contentPanel = contentPanel;
        analyzer = new DataAnalyzer();
        // Add the graph
        contentPanel.add(graph);
        // Add the combat texts
        combatOutText.setFont(defaultFont);
        contentPanel.add(combatOutText);
        combatInText.setFont(defaultFont);
        contentPanel.add(combatInText);

        final Runnable updateRunnable = new Runnable() {

            @Override
            public void run() {
                graph.updateData();
            }
        };

        Timer timer = new Timer(repaintInterval, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {

                    @Override
                    public void run() {
                        try {
                            SwingUtilities.invokeAndWait(updateRunnable);
                        } catch (InterruptedException | InvocationTargetException ex) {
                            Logger.getLogger(DataVisualizer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }.start();
                combatOutText.setText("Out: " + analyzer.getOutgoingDps() + " DPS");
                combatInText.setText("In: " + analyzer.getIncomingDps() + " DPS");
            }
        });

        timer.start();
    }

    protected final void updateColors() {
        fontColorPrimary = Tools.calcBestBlackWhiteColor(settings.getPrimaryColor());
        fontColorSecondary = Tools.calcPercentageColor(fontColorPrimary, settings.getPrimaryColor(), .5);
        combatOutText.setForeground(fontColorPrimary);
        combatInText.setForeground(fontColorSecondary);
        graph.updateColors();
    }

    protected void updateResizeVisuals() {
        int width = contentPanel.getWidth();
        int height = contentPanel.getHeight();
        combatOutText.setBounds(padding, height - 20, combatTextWidth, 20);
        int combatInX = (int) (padding + width * .4);
        combatInText.setBounds(combatInX, height - 20, combatTextWidth, 20);
        graph.setBounds(padding, padding + 20 + padding, width - padding - padding, height - padding - 20 - padding - 20 - padding - padding);
    }

    private final class Graph extends JPanel {

        // Constants
        private final Font txtFont;
        private final Font graphFont;
        private final int maxDpsInterval = 20;
        private final int minMaxDps = 40;
        private final float txtFontSize = 16.0f;
        private final float graphFontSize = 10.0f;
        private final int axisSize = 3;
        // Color variables
        private Color bgColor;
        private Color borderColor;
        private Color lineColor;
        private String message = "";
        private boolean hasMessage = false;
        // Graph variables
        private int currentMaxDps = minMaxDps;
        private int currentMinDps = 0;
        private long currMs = System.currentTimeMillis();
        private long prevMs = currMs;
        private boolean lastOutZero = false;
        private boolean lastInZero = false;
//        private final ArrayList<DoublePoint> outPoints = new ArrayList<>();
        private final ArrayList<ArrayList<DoublePoint>> outPointsLists = new ArrayList<>();
//        private final ArrayList<DoublePoint> inPoints = new ArrayList<>();
        private final ArrayList<ArrayList<DoublePoint>> inPointsLists = new ArrayList<>();

        private Graph() {
            txtFont = this.getFont().deriveFont(Font.BOLD, txtFontSize);
            graphFont = this.getFont().deriveFont(Font.PLAIN, graphFontSize);
            outPointsLists.add(new ArrayList<DoublePoint>());
            inPointsLists.add(new ArrayList<DoublePoint>());
        }

        private void setText(String text) {
            message = text;
            hasMessage = text != null && !text.equals("");
        }

        protected final void updateColors() {
            bgColor = Tools.calcPercentageColor(settings.getSecondaryColor(), settings.getPrimaryColor(), .87);
            borderColor = Tools.calcPercentageColor(bgColor, Tools.calcBestBlackWhiteColor(bgColor), .40);
            lineColor = Tools.calcPercentageColor(bgColor, borderColor, .60);
            this.setBorder(BorderFactory.createLineBorder(borderColor, 1));
            this.setBackground(bgColor);
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            // Get the current values prior to updating the bounds
            int oldWidth = this.getWidth();
            int oldHeight = this.getHeight();
            super.setBounds(x, y, width, height);
            // Only useful if the previous dimensions aren't zero
            if (oldWidth != 0 && oldHeight != 0) {
                // Update the out points
                for (ArrayList<DoublePoint> outPoints : outPointsLists) {
                    for (DoublePoint p : outPoints) {
                        p.x = (p.x / oldWidth) * width;
                        p.y = (p.y / oldHeight) * height;
                    }
                }
                // Update the in points
                for (ArrayList<DoublePoint> inPoints : inPointsLists) {
                    for (DoublePoint p : inPoints) {
                        p.x = (p.x / oldWidth) * width;
                        p.y = (p.y / oldHeight) * height;
                    }
                }
            }
        }

        protected final void updateData() {
            // If the user isn't in-combat
            if (!analyzer.getIsInCombat()) {
                if (settings.hasUsers()) {
                    if (settings.getSelectedUser().equals("")) {
                        setText("Please select a character from the drop-down list.");
                    } else {
                        setText(null);
                    }
                } else {
                    // No users available
                    setText("No characters found, please make sure the log path is pointed to the directory where EVE Online stores its gamelogs.");
                }
            }
            currMs = System.currentTimeMillis();
            double offsetFromPrev = ((double) (currMs - prevMs) * .001 / 40) * this.getWidth();
            prevMs = currMs;
            // Update the current points
            for (int i = 0; i < outPointsLists.size(); i++) {
                ArrayList<DoublePoint> outPoints = outPointsLists.get(i);
                DoublePoint prevPoint = null;
                for (int j = 0; j < outPoints.size(); j++) {
                    DoublePoint p = outPoints.get(j);
                    if (prevPoint != null && p.y == prevPoint.y) {
                        outPoints.remove(j);
                        j--;
                    } else {
                        p.x -= offsetFromPrev;
                    }
                    prevPoint = p;
                }
            }
            for (int i = 0; i < inPointsLists.size(); i++) {
                ArrayList<DoublePoint> inPoints = inPointsLists.get(i);
                DoublePoint prevPoint = null;
                for (int j = 0; j < inPoints.size(); j++) {
                    DoublePoint p = inPoints.get(j);
                    if (prevPoint != null && p.y == prevPoint.y) {
                        inPoints.remove(j);
                        j--;
                    } else {
                        p.x -= offsetFromPrev;
                    }
                    prevPoint = p;
                }
            }
            // Add the new out points
            int currentDpsDiff = currentMaxDps - currentMinDps;
            if (analyzer.getOutgoingDps() > 0) {
                lastOutZero = false;
                int newOutY = this.getHeight() - (int) (((double) (analyzer.getOutgoingDps() - currentMinDps) / currentDpsDiff) * this.getHeight());
                outPointsLists.get(outPointsLists.size() - 1).add(new DoublePoint(this.getWidth(), newOutY));
            } else if (!lastOutZero) {
                lastOutZero = true;
                outPointsLists.add(new ArrayList<DoublePoint>());
            }
            // Add the new in points
            if (analyzer.getIncomingDps() > 0) {
                lastInZero = false;
                int newInY = this.getHeight() - (int) (((double) (analyzer.getIncomingDps() - currentMinDps) / currentDpsDiff) * this.getHeight());
                inPointsLists.get(inPointsLists.size() - 1).add(new DoublePoint(this.getWidth(), newInY));
            } else if (!lastInZero) {
                lastInZero = true;
                inPointsLists.add(new ArrayList<DoublePoint>());
            }
            // Remove all out and in points below 0 on the x-axis, except for the first point
            for (ArrayList<DoublePoint> outPoints : outPointsLists) {
                while (outPoints.size() > 1 && outPoints.get(1).x <= 0) {
                    outPoints.remove(0);
                }
                if (outPoints.size() == 1 && outPoints.get(0).x <= 0) {
                    outPoints.remove(0);
                }
            }
            // Remove the first list if it is empty
            ArrayList<DoublePoint> firstOut = outPointsLists.get(0);
            if (firstOut.isEmpty() && outPointsLists.size() > 1) {
                outPointsLists.remove(0);
            }
            for (ArrayList<DoublePoint> inPoints : inPointsLists) {
                while (inPoints.size() > 1 && inPoints.get(1).x <= 0) {
                    inPoints.remove(0);
                }
                if (inPoints.size() == 1 && inPoints.get(0).x <= 0) {
                    inPoints.remove(0);
                }
            }
            // Remove the first list if it is empty
            ArrayList<DoublePoint> firstIn = inPointsLists.get(0);
            if (firstIn.isEmpty() && inPointsLists.size() > 1) {
                inPointsLists.remove(0);
            }
            // Update the max-dps if needed
            updateMaxDps();
            // Repaint the data
            repaint();
        }

        private void updateMaxDps() {
            double lowest = Integer.MAX_VALUE;
            double highest = 0.0;
            int oldDpsDiff = currentMaxDps - currentMinDps;
            for (ArrayList<DoublePoint> outPoints : outPointsLists) {
                for (DoublePoint p : outPoints) {
                    double pointDps = currentMinDps + (((this.getHeight() - p.y) / this.getHeight()) * oldDpsDiff);
                    if (pointDps > highest) {
                        highest = pointDps;
                    }
                    if (pointDps < lowest) {
                        lowest = pointDps;
                    }
                }
            }
            for (ArrayList<DoublePoint> inPoints : inPointsLists) {
                for (DoublePoint p : inPoints) {
                    double pointDps = currentMinDps + (((this.getHeight() - p.y) / this.getHeight()) * oldDpsDiff);
                    if (pointDps > highest) {
                        highest = pointDps;
                    }
                    if (pointDps < lowest) {
                        lowest = pointDps;
                    }
                }
            }
            if (lowest != Integer.MAX_VALUE) {
                int oldMinDps = currentMinDps;
                currentMinDps = (int) (Math.floor(lowest / maxDpsInterval) * maxDpsInterval);
                if (lowest - currentMinDps < (int) (maxDpsInterval * .5)) {
                    currentMinDps -= maxDpsInterval;
                }
                if (currentMinDps < 0) {
                    currentMinDps = 0;
                }
                int oldMaxDps = currentMaxDps;
                currentMaxDps = (int) (Math.ceil(highest / maxDpsInterval) * maxDpsInterval);
                if (currentMaxDps - highest < (int) (maxDpsInterval * .5)) {
                    currentMaxDps += maxDpsInterval;
                }
                if (currentMaxDps - currentMinDps < minMaxDps) {
                    currentMinDps = currentMaxDps - minMaxDps;
                    if (currentMinDps < 0) {
                        currentMinDps = 0;
                        currentMaxDps = minMaxDps;
                    }
                }
                if (oldMinDps != currentMinDps || oldMaxDps != currentMaxDps) {
                    int currentDpsDiff = currentMaxDps - currentMinDps;
                    for (ArrayList<DoublePoint> outPoints : outPointsLists) {
                        for (DoublePoint p : outPoints) {
                            double pointDps = oldMinDps + (((this.getHeight() - p.y) / this.getHeight()) * oldDpsDiff);
                            p.y = this.getHeight() - ((pointDps - currentMinDps) / currentDpsDiff) * this.getHeight();
                        }
                    }
                    for (ArrayList<DoublePoint> inPoints : inPointsLists) {
                        for (DoublePoint p : inPoints) {
                            double pointDps = oldMinDps + (((this.getHeight() - p.y) / this.getHeight()) * oldDpsDiff);
                            p.y = this.getHeight() - ((pointDps - currentMinDps) / currentDpsDiff) * this.getHeight();
                        }
                    }
                }
            }
        }

        private void paintYAxis(Graphics g) {
            int lineCount = this.getHeight() <= 300 ? 5 : this.getHeight() <= 600 ? 10 : 20;
            double lineInterval = (double) this.getHeight() / lineCount;
            int currentDpsDiff = currentMaxDps - currentMinDps;
            double dpsInterval = (double) currentDpsDiff / lineCount;
            int textX = axisSize + padding;
            int halfFontSize = (int) (graphFontSize * .5);
            g.setFont(graphFont);
            // Paint the lines, excluding the bottom- and top line
            for (int i = 1; i < lineCount; i++) {
                int y = (int) (i * lineInterval);
                // Draw the small lines
                g.setColor(borderColor);
                g.drawLine(0, y, axisSize, y);
                // Draw the text
                int textY = y + halfFontSize;
                String dpsValue = "" + (int) (currentMinDps + currentDpsDiff - (dpsInterval * i));
                g.drawString(dpsValue, textX, textY);
                // Draw the rest of the lines
                int wideLineX = textX + (int) (graphFontSize * dpsValue.length());
                g.setColor(lineColor);
                g.drawLine(wideLineX, y, this.getWidth(), y);
            }
            // Paint the min and max texts
            g.setColor(borderColor);
            g.drawString("" + currentMinDps, 2, this.getHeight() - 2);
            g.drawString("" + currentMaxDps, 2, (int) graphFontSize);
        }

        private void paintGraphLines(Graphics2D g) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color outColor = Tools.calcBestBlackWhiteColor(bgColor);
            // Draw the outgoing-lines
            g.setColor(outColor);
            for (ArrayList<DoublePoint> outPoints : outPointsLists) {
                int outSize = outPoints.size();
                int[] outX = new int[outSize];
                int[] outY = new int[outSize];
                for (int i = 0; i < outSize; i++) {
                    outX[i] = (int) outPoints.get(i).x;
                    outY[i] = (int) outPoints.get(i).y;
                }
                g.drawPolyline(outX, outY, outSize);
            }
            // Draw the incoming-line
            g.setColor(Tools.calcPercentageColor(bgColor, outColor, .5));
            for (ArrayList<DoublePoint> inPoints : inPointsLists) {
                int inSize = inPoints.size();
                int[] inX = new int[inSize];
                int[] inY = new int[inSize];
                for (int i = 0; i < inSize; i++) {
                    inX[i] = (int) inPoints.get(i).x;
                    inY[i] = (int) inPoints.get(i).y;
                }
                g.drawPolyline(inX, inY, inSize);
            }
            g.dispose();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (hasMessage) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(borderColor);
                g2.setFont(txtFont);
                FontMetrics metrics = g2.getFontMetrics(txtFont);
                int spaceWidth = metrics.charWidth(' ');
                int paddinglessWidth = this.getWidth() - padding - padding;
                ArrayList<String> lines = new ArrayList<>();
                String[] words = message.split(" ");
                for (String word : words) {
                    if (lines.isEmpty()) {
                        lines.add(word);
                        continue;
                    }
                    int lineNr = lines.size() - 1;
                    String line = lines.get(lineNr);
                    int lineWidth = metrics.stringWidth(line);
                    int wordWidth = metrics.stringWidth(word);
                    if (lineWidth + spaceWidth + wordWidth <= paddinglessWidth) {
                        line += " " + word;
                        // Strings are immutable, re-add the line
                        lines.remove(lineNr);
                        lines.add(line);
                    } else {
                        lines.add(word);
                    }
                }
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    g2.drawString(line, padding, (i + 1) * metrics.getHeight());
                }
            } else {
                paintYAxis(g);
                paintGraphLines((Graphics2D) g);
            }
        }
    }

    private class DoublePoint {

        double x;
        double y;

        private DoublePoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
