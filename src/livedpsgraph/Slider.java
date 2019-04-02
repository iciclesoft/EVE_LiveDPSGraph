/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class Slider extends JLabel {

    private final AppSettings settings = AppSettings.getInstance();
    private final ArrayList<ActionListener> releasedListeners = new ArrayList<>();
    private final ArrayList<ActionListener> changedListeners = new ArrayList<>();
    private Color borderColor;
    private Color backgroundColor = settings.getPrimaryColor();
    private Color foregroundColor = settings.getSecondaryColor();
    private final int min;
    private final int max;
    private int widthHeight;
    private int current;
    private final JLabel circle = new JLabel() {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Draw the circle
            g2.setColor(foregroundColor);
            g2.fillOval(1, 1, this.getWidth() - 2, this.getHeight() - 2);
            g2.dispose();
        }

    };

    protected Slider(int min, int max, int current) {
        this.setOpaque(true);
        this.min = min;
        this.max = max;
        this.current = current;
        this.add(circle);
        MouseInputAdapter listener = new MouseInputAdapter() {
            private boolean allowedToMove = false;
            private int xOffset;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    int newX = mouseLoc.x - xOffset;
                    if (newX < 0) {
                        newX = 0;
                    } else if (newX > Slider.this.getWidth() - circle.getWidth()) {
                        newX = Slider.this.getWidth() - circle.getWidth();
                    }
                    circle.setLocation(newX, 0);
                    Slider.this.current = (int) (((double) newX / (Slider.this.getWidth() - widthHeight)) * (Slider.this.max - Slider.this.min));
                    for (ActionListener l : changedListeners) {
                        l.actionPerformed(new ActionEvent(Slider.this, 0, "Changed"));
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                allowedToMove = e.getButton() == MouseEvent.BUTTON1;
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    xOffset = mouseLoc.x - circle.getX();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                allowedToMove = false;
                for (ActionListener l : releasedListeners) {
                    l.actionPerformed(new ActionEvent(Slider.this, 0, "Released"));
                }
            }
        };
        circle.addMouseListener(listener);
        circle.addMouseMotionListener(listener);

        updateColors();
    }
    
    protected void setBackgroundColor(Color c) {
        this.backgroundColor = c;
    }
    
    protected void setForegroundColor(Color c) {
        this.foregroundColor = c;
    }

    protected void addMouseReleasedListener(ActionListener l) {
        releasedListeners.add(l);
    }
    
    protected void addChangeListener(ActionListener l) {
        changedListeners.add(l);
        l.actionPerformed(new ActionEvent(this, 0, "Initialized"));
    }

    protected int getValue() {
        return current;
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        widthHeight = height;
        updateBounds();
    }

    protected final void updateBounds() {
        int xPos = (int) (((double) current / (max - min)) * (this.getWidth() - widthHeight));
        circle.setBounds(xPos, 0, widthHeight, widthHeight);
    }

    protected final void updateColors() {
        borderColor = Tools.calcPercentageColor(backgroundColor, Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()), .77);
        this.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        this.setBackground(backgroundColor);
        circle.repaint();
    }
}
