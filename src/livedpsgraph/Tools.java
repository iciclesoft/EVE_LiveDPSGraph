/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class Tools {
    
    private static long timeMillisOffset = 0;

    protected static long currentTimeMillis() {
        // Normalized timestamp compared to combat log
        return System.currentTimeMillis() - timeMillisOffset;
    }

    protected static void updateTimeMillisOffset(long time) {
        timeMillisOffset = System.currentTimeMillis() - time;
    }

    protected static Color calcBestBlackWhiteColor(Color color) {
        return (color.getRed() + color.getGreen() + color.getBlue()) >= 385 ? Color.BLACK : Color.WHITE;
    }
    
    protected static Color calcPercentageColor(Color c1, Color c2, double percentage) {
        return calcPercentageColor(c1, c2, percentage, true);
    }

    protected static Color calcPercentageColor(Color c1, Color c2, double percentage, boolean includeAlpha) {
        double r1 = c1.getRed();
        double g1 = c1.getGreen();
        double b1 = c1.getBlue();
        double a1 = c1.getAlpha();

        double r2 = c2.getRed();
        double g2 = c2.getGreen();
        double b2 = c2.getBlue();
        double a2 = c2.getAlpha();

        double negPercentage = 1 - percentage;
        int newR = (int) (r1 * percentage + negPercentage * r2);
        int newG = (int) (g1 * percentage + negPercentage * g2);
        int newB = (int) (b1 * percentage + negPercentage * b2);
        int newA = includeAlpha ? (int) (a1 * percentage + negPercentage * a2) : (int) a1;
        return new Color(newR, newG, newB, newA);
    }

    protected static Color multiply(Color c1, Color c2) {
        float[] c1Components = c1.getRGBComponents(null);
        float[] c2Components = c2.getRGBColorComponents(null);
        float[] newComponents = new float[3];

        for (int i = 0; i < 3; i++) {
            newComponents[i] = c1Components[i] * c2Components[i];
        }

        return new Color(newComponents[0], newComponents[1], newComponents[2],
                c1Components[3]);
    }

    protected static Image applyNewColors(BufferedImage original, Color background) {
        Color newColor = Tools.calcBestBlackWhiteColor(background);
        boolean multiply = newColor.equals(Color.WHITE);
        
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color imageColor = new Color(original.getRGB(x, y), true);
                if (multiply) {
                    image.setRGB(x, y, multiply(imageColor, newColor).getRGB());
                } else {
                    image.setRGB(x, y, calcPercentageColor(imageColor, newColor, .3, false).getRGB());
                }
            }
        }
        return image;
    }

    protected static class DataObject {

        private final String dateTime;
        private final String data;

        public DataObject(String dateTime, String data) {
            this.dateTime = dateTime;
            this.data = data;
        }

        public String getDateTime() {
            return dateTime;
        }

        public String getData() {
            return data;
        }
    }

    protected static class ButtonMouseAdapter implements MouseListener {

        private final JLabel parent;
        private final BufferedImage defaultIcon;
        private final BufferedImage hoverIcon;
        private final BufferedImage activeIcon;

        private ButtonState btnState = ButtonState.DEFAULT;
        private Image visualDefaultIcon;
        private Image visualHoverIcon;
        private Image visualActiveIcon;

        protected ButtonMouseAdapter(JLabel parent, BufferedImage defaultIcon, BufferedImage hoverIcon, BufferedImage activeIcon) {
            this.parent = parent;
            this.defaultIcon = defaultIcon;
            this.hoverIcon = hoverIcon;
            this.activeIcon = activeIcon;
            updateButtonColors();
            this.parent.setIcon(new ImageIcon(visualDefaultIcon));
            this.parent.addAncestorListener(new AncestorListener() {

                @Override
                public void ancestorAdded(AncestorEvent event) {
                    ButtonMouseAdapter.this.parent.setIcon(new ImageIcon(visualDefaultIcon));
                }

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                }
            });
        }

        protected final void updateButtonColors() {
            AppSettings settings = AppSettings.getInstance();
            visualDefaultIcon = Tools.applyNewColors(defaultIcon, settings.getPrimaryColor());
            visualHoverIcon = Tools.applyNewColors(hoverIcon, settings.getPrimaryColor());
            visualActiveIcon = Tools.applyNewColors(activeIcon, settings.getPrimaryColor());
            updateIcon();
        }
        
        private void updateIcon() {
            switch (btnState) {
                default:
                    parent.setIcon(new ImageIcon(visualDefaultIcon));
                    break;
                case HOVER:
                    parent.setIcon(new ImageIcon(visualHoverIcon));
                    break;
                case ACTIVE:
                    parent.setIcon(new ImageIcon(visualActiveIcon));
                    break;
            }
        }

        protected void clickEvent() {
            try {
                throw new Exception("Method clickEvent must be overridden on instantiation.");
            } catch (Exception ex) {
                Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            btnState = ButtonState.ACTIVE;
            updateIcon();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (parent.contains(e.getPoint())) {
                clickEvent();
                btnState = ButtonState.HOVER;
                updateIcon();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            btnState = ButtonState.HOVER;
            updateIcon();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            btnState = ButtonState.DEFAULT;
            updateIcon();
        }

        protected enum ButtonState {
            DEFAULT, HOVER, ACTIVE
        }
    }
}
