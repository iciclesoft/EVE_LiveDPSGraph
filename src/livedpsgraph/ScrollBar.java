/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public class ScrollBar extends BasicScrollBarUI {

    private final AppSettings settings = AppSettings.getInstance();
    private Color bgColor = Tools.calcPercentageColor(settings.getSecondaryColor(), settings.getPrimaryColor(), .3);
    private Color fgColor = Tools.calcPercentageColor(settings.getSecondaryColor(), settings.getPrimaryColor(), .52);
    private final JButton veryTinyBtn = new JButton();

    protected ScrollBar() {
        veryTinyBtn.setPreferredSize(new Dimension(0, 0));
        updateColors();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        g.setColor(fgColor);
        g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
        if (thumbBounds.height > 8) {
            // Draw the blend
            for (int i = 0; i < 4; i++) {
                Color color = Tools.calcPercentageColor(settings.getSecondaryColor(), fgColor, .6d / (i + 1));
                g.setColor(color);
                int topY = thumbBounds.y + i;
                g.drawLine(0, topY, thumbBounds.width, topY);
                int bottomY = thumbBounds.y + thumbBounds.height - i - 1;
                g.drawLine(0, bottomY, thumbBounds.width, bottomY);
            }
        }
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(bgColor);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return veryTinyBtn;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return veryTinyBtn;
    }    

    protected final void updateColors() {
        bgColor = Tools.calcPercentageColor(settings.getSecondaryColor(), settings.getPrimaryColor(), .3);
        fgColor = Tools.calcPercentageColor(settings.getSecondaryColor(), settings.getPrimaryColor(), .52);
    }
}
