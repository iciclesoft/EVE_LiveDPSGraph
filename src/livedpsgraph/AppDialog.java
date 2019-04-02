/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public class AppDialog extends JDialog {

    private final AppSettings settings = AppSettings.getInstance();
    // Constants
    private final int width;
    private final int height;
    private final int padding = 5;
    private final int titleHeight = 20;
    private final int buttonWH = 16;
    private final int borderVisualWH = 3;
    // Components
    private final JPanel titleBar = new JPanel();
    private final JLabel title = new JLabel();
    // Buttons
    private final JLabel btnClose = new JLabel();
    private final Tools.ButtonMouseAdapter btnCloseMouseAdapter;
    private final BufferedImage btnCloseDefault;
    private final BufferedImage btnCloseHover;
    private final BufferedImage btnCloseActive;
    // Content and its contents
    private final JPanel content = new JPanel();
    
    private final JPanel NEBorderVisual = new JPanel();
    private final JPanel SEBorderVisual = new JPanel();
    private final JPanel SWBorderVisual = new JPanel();
    private final JPanel NWBorderVisual = new JPanel();

    protected AppDialog(JDialog owner, String dialogTitle, int width, int height) throws IOException {
        super(owner);
        this.width = width;
        this.height = height;
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setTitle(dialogTitle);
        // Define the static settings of the dialog
        this.setBackground(Color.BLACK);
        this.setUndecorated(true);
        this.setFocusable(false);
        this.setLayout(null);
        this.setFocusableWindowState(false);
        this.setAutoRequestFocus(false);
        this.getRootPane().setOpaque(true);
        this.setSize(width, height);
        // Load the icon images
        btnCloseDefault = ImageIO.read(getClass().getResource("UIComponents/btnCloseDefault.png"));
        btnCloseHover = ImageIO.read(getClass().getResource("UIComponents/btnCloseHover.png"));
        btnCloseActive = ImageIO.read(getClass().getResource("UIComponents/btnCloseActive.png"));
        // Add the borders
        this.getContentPane().add(NEBorderVisual);
        this.getContentPane().add(SEBorderVisual);
        this.getContentPane().add(SWBorderVisual);
        this.getContentPane().add(NWBorderVisual);
        // Set the title bar
        this.getContentPane().add(titleBar);
        titleBar.setLayout(null);
        // Add the title bar's components
        titleBar.add(title);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 10.0f));
        title.setText(dialogTitle);
        // Add the close button
        btnCloseMouseAdapter = new Tools.ButtonMouseAdapter(btnClose, btnCloseDefault, btnCloseHover, btnCloseActive) {
            @Override
            protected void clickEvent() {
                AppDialog.this.dispose();
            }
        };
        btnClose.addMouseListener(btnCloseMouseAdapter);
        btnClose.setVisible(false);
        
        titleBar.add(btnClose);
        // Add the 'move'-listener
        MouseInputAdapter rootPaneListener = new MouseInputAdapter() {
            private boolean allowedToMove = false;
            private int xOffset;
            private int yOffset;

            @Override
            public void mouseExited(MouseEvent e) {
                if (!AppDialog.this.getRootPane().contains(e.getPoint())) {
                    btnClose.setVisible(false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setVisible(true);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();

                    int newX = mouseLoc.x - xOffset;
                    int newY = mouseLoc.y - yOffset;
                    AppDialog.this.setLocation(newX, newY);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                allowedToMove = e.getButton() == MouseEvent.BUTTON1;
                if (allowedToMove) {
                    Point appLoc = AppDialog.this.getLocation();
                    Point mouseLoc = e.getLocationOnScreen();
                    xOffset = mouseLoc.x - appLoc.x;
                    yOffset = mouseLoc.y - appLoc.y;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                allowedToMove = false;
            }
        };
        this.getRootPane().addMouseListener(rootPaneListener);
        this.getRootPane().addMouseMotionListener(rootPaneListener);
        // Add the content-panel
        content.setLayout(null);
        this.getContentPane().add(content);
    }
    
    protected final JPanel getTitlePanel() {
        return this.titleBar;
    }
    
    protected final JPanel getContentPanel() {
        return this.content;
    }
    
    protected void updateBounds() {
        NEBorderVisual.setBounds(width - borderVisualWH, 0, borderVisualWH, borderVisualWH);
        SEBorderVisual.setBounds(width - borderVisualWH, height - borderVisualWH, borderVisualWH, borderVisualWH);
        SWBorderVisual.setBounds(0, height - borderVisualWH, borderVisualWH, borderVisualWH);
        NWBorderVisual.setBounds(0, 0, borderVisualWH, borderVisualWH);
        titleBar.setBounds(1, 1, width - 2, titleHeight);
        title.setBounds(padding, 0, 200, titleHeight);
        int buttonY = (int) ((titleHeight - buttonWH) * .5);
        btnClose.setBounds(width - buttonWH - 1, buttonY, buttonWH, buttonWH);
        content.setBounds(1, 2 + titleHeight, width - 2, height - titleHeight - 3);
    }
    
    protected void updateColors() {
        Color borderColor = Tools.calcPercentageColor(settings.getPrimaryColor(), Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()), .87);
        Color defaultTextColor = Tools.calcBestBlackWhiteColor(settings.getPrimaryColor());
        // Apply the app's colors
        this.getContentPane().setBackground(borderColor);
        this.getRootPane().setBackground(borderColor);
        // Set the content colors
        content.setBackground(settings.getPrimaryColor());
        // Set the title colors
        titleBar.setBackground(settings.getPrimaryColor());
        title.setForeground(Tools.calcPercentageColor(settings.getPrimaryColor(), defaultTextColor, .25));
        // Set the border colors
        Color resizeBorderColor = Tools.calcPercentageColor(settings.getPrimaryColor(), Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()), .34);
        NEBorderVisual.setBackground(settings.getPrimaryColor());
        NEBorderVisual.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 1, resizeBorderColor));
        SEBorderVisual.setBackground(settings.getPrimaryColor());
        SEBorderVisual.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, resizeBorderColor));
        SWBorderVisual.setBackground(settings.getPrimaryColor());
        SWBorderVisual.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, resizeBorderColor));
        NWBorderVisual.setBackground(settings.getPrimaryColor());
        NWBorderVisual.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, resizeBorderColor));
        // Update the button colors
        btnCloseMouseAdapter.updateButtonColors();
    }
}
