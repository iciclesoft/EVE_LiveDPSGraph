/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class LiveDPSGraph extends JDialog {

    // Static constants
    protected static final String VERSION = "1.1";
    protected static final String APPNAME = "EVE Online - Live DPS Graph";
    protected static final String RELEASE_DATE = "07-Jan-2017";

    private final AppSettings settings = AppSettings.getInstance();
    // Other dialogs
    private AppDialog settingsDialog;
    private InfoDialog infoDialog;
    private final TaskbarFrame taskbarFrame;
    // Title components
    private final JPanel titleBar = new JPanel();
    private final JLabel title = new JLabel();
    // Buttons
    private final JLabel btnClose = new JLabel();
    private final Tools.ButtonMouseAdapter btnCloseMouseAdapter;
    private final JLabel btnMinimize = new JLabel();
    private final Tools.ButtonMouseAdapter btnMinimizeMouseAdapter;
    private final JLabel btnPin = new JLabel();
    private final Tools.ButtonMouseAdapter btnPinMouseAdapter;
    private final JLabel btnAlwaysOnTop = new JLabel();
    private final Tools.ButtonMouseAdapter btnAlwaysOnTopMouseAdapter;
    private final JLabel btnSettings = new JLabel();
    private final Tools.ButtonMouseAdapter btnSettingsMouseAdapter;
    private final JLabel btnScroll = new JLabel();
    private final Tools.ButtonMouseAdapter btnScrollAdapter;
    // Content and its contents
    private final JPanel content = new JPanel();
    private final DataVisualizer visualizer;
    private final ComboBox userSelect = new ComboBox();
    private final JLabel btnInfo = new JLabel();
    private final Tools.ButtonMouseAdapter btnInfoAdapter;

    private final JPanel NEBorder = new JPanel();
    private final JPanel SEBorder = new JPanel();
    private final JPanel SWBorder = new JPanel();
    private final JPanel NWBorder = new JPanel();
    private final JPanel NEBorderVisual = new JPanel();
    private final JPanel SEBorderVisual = new JPanel();
    private final JPanel SWBorderVisual = new JPanel();
    private final JPanel NWBorderVisual = new JPanel();

    private final int padding = 5;
    private final int borderWH = 6;
    private final int borderVisualWH = 3;
    private final int titleHeight = 20;
    private final int buttonWH = 16;

    private boolean active = true;

    private LiveDPSGraph() throws IOException {
        // Load the icon images
        BufferedImage btnCloseDefault = ImageIO.read(getClass().getResource("UIComponents/btnCloseDefault.png"));
        BufferedImage btnCloseHover = ImageIO.read(getClass().getResource("UIComponents/btnCloseHover.png"));
        BufferedImage btnCloseActive = ImageIO.read(getClass().getResource("UIComponents/btnCloseActive.png"));
        BufferedImage btnMinimizeDefault = ImageIO.read(getClass().getResource("UIComponents/btnMinimizeDefault.png"));
        BufferedImage btnMinimizeHover = ImageIO.read(getClass().getResource("UIComponents/btnMinimizeHover.png"));
        BufferedImage btnMinimizeActive = ImageIO.read(getClass().getResource("UIComponents/btnMinimizeActive.png"));
        BufferedImage btnPinDefault = ImageIO.read(getClass().getResource("UIComponents/btnPinDefault.png"));
        BufferedImage btnPinHover = ImageIO.read(getClass().getResource("UIComponents/btnPinHover.png"));
        BufferedImage btnPinActive = ImageIO.read(getClass().getResource("UIComponents/btnPinActive.png"));
        BufferedImage btnAlwaysOnTopDefault = ImageIO.read(getClass().getResource("UIComponents/btnAoTDefault.png"));
        BufferedImage btnAlwaysOnTopHover = ImageIO.read(getClass().getResource("UIComponents/btnAoTHover.png"));
        BufferedImage btnAlwaysOnTopActive = ImageIO.read(getClass().getResource("UIComponents/btnAoTActive.png"));
        BufferedImage btnSettingsDefault = ImageIO.read(getClass().getResource("UIComponents/btnSettingsDefault.png"));
        BufferedImage btnSettingsHover = ImageIO.read(getClass().getResource("UIComponents/btnSettingsHover.png"));
        BufferedImage btnSettingsActive = ImageIO.read(getClass().getResource("UIComponents/btnSettingsActive.png"));
        BufferedImage btnInfoDefault = ImageIO.read(getClass().getResource("UIComponents/btnInfoDefault.png"));
        BufferedImage btnInfoHover = ImageIO.read(getClass().getResource("UIComponents/btnInfoHover.png"));
        BufferedImage btnInfoActive = ImageIO.read(getClass().getResource("UIComponents/btnInfoActive.png"));
        BufferedImage btnScrollDefault = ImageIO.read(getClass().getResource("UIComponents/btnScrollDefault.png"));
        BufferedImage btnScrollHover = ImageIO.read(getClass().getResource("UIComponents/btnScrollHover.png"));
        BufferedImage btnScrollActive = ImageIO.read(getClass().getResource("UIComponents/btnScrollActive.png"));
        this.setTitle(APPNAME);
        // Define the static settings of the dialog
        this.setBackground(Color.BLACK);
        this.setUndecorated(true);
        this.setFocusable(false);
        this.setLayout(null);
        this.setFocusableWindowState(false);
        this.setAutoRequestFocus(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.getRootPane().setOpaque(true);
        this.setAlwaysOnTop(settings.isAlwaysOnTop());
        // Init the taskbar frame
        taskbarFrame = new TaskbarFrame(this);
        taskbarFrame.setVisible(!settings.isAlwaysOnTop());
        // Add the north-east border
        NEBorder.setBackground(new Color(0, 0, 0, 0));
        NEBorder.setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
        NEBorder.setLayout(null);
        NEBorder.add(NEBorderVisual);
        NEBorderVisual.setBounds(borderWH - borderVisualWH, 0, borderVisualWH, borderVisualWH);
        this.getContentPane().add(NEBorder);
        MouseInputAdapter NEListener = new MouseInputAdapter() {
            private boolean allowedToMove = false;
            private int xOffset;
            private int yOffset;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    Rectangle rect = LiveDPSGraph.this.getBounds();
                    rect.y += mouseLoc.y - yOffset;
                    rect.width += mouseLoc.x - xOffset;
                    rect.height -= mouseLoc.y - yOffset;
                    xOffset = mouseLoc.x;
                    yOffset = mouseLoc.y;

                    settings.resizeApp(rect);
                    LiveDPSGraph.this.setBounds(settings.getAppRectangle());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                allowedToMove = e.getButton() == MouseEvent.BUTTON1;
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    xOffset = mouseLoc.x;
                    yOffset = mouseLoc.y;
                    Color rootColor = LiveDPSGraph.this.getRootPane().getBackground();
                    Color borderColor = Tools.calcPercentageColor(rootColor, Tools.calcBestBlackWhiteColor(rootColor), .34);
                    LiveDPSGraph.this.getRootPane().setBorder(BorderFactory.createLineBorder(borderColor));
                    LiveDPSGraph.this.getContentPane().setVisible(false);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    allowedToMove = false;
                    LiveDPSGraph.this.updateResizeVisuals();
                    LiveDPSGraph.this.getContentPane().setVisible(true);
                    LiveDPSGraph.this.getRootPane().setBorder(null);
                }
            }
        };
        NEBorder.addMouseListener(NEListener);
        NEBorder.addMouseMotionListener(NEListener);

        // Add the south-east border
        SEBorder.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
        SEBorder.setBackground(new Color(0, 0, 0, 0));
        SEBorder.setLayout(null);
        SEBorder.add(SEBorderVisual);
        SEBorderVisual.setBounds(borderWH - borderVisualWH, borderWH - borderVisualWH, borderVisualWH, borderVisualWH);
        this.getContentPane().add(SEBorder);
        MouseInputAdapter SEListener = new MouseInputAdapter() {
            private boolean allowedToMove = false;
            private int xOffset;
            private int yOffset;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    Rectangle rect = LiveDPSGraph.this.getBounds();
                    rect.width += mouseLoc.x - xOffset;
                    rect.height += mouseLoc.y - yOffset;
                    xOffset = mouseLoc.x;
                    yOffset = mouseLoc.y;

                    settings.resizeApp(rect);
                    LiveDPSGraph.this.setBounds(settings.getAppRectangle());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                allowedToMove = e.getButton() == MouseEvent.BUTTON1;
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    xOffset = mouseLoc.x;
                    yOffset = mouseLoc.y;
                    Color rootColor = LiveDPSGraph.this.getRootPane().getBackground();
                    Color borderColor = Tools.calcPercentageColor(rootColor, Tools.calcBestBlackWhiteColor(rootColor), .34);
                    LiveDPSGraph.this.getRootPane().setBorder(BorderFactory.createLineBorder(borderColor));
                    LiveDPSGraph.this.getContentPane().setVisible(false);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    allowedToMove = false;
                    LiveDPSGraph.this.updateResizeVisuals();
                    LiveDPSGraph.this.getContentPane().setVisible(true);
                    LiveDPSGraph.this.getRootPane().setBorder(null);
                }
            }
        };
        SEBorder.addMouseListener(SEListener);
        SEBorder.addMouseMotionListener(SEListener);

        // Add the sout-west border
        SWBorder.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
        SWBorder.setBackground(new Color(0, 0, 0, 0));
        SWBorder.setLayout(null);
        SWBorder.add(SWBorderVisual);
        SWBorderVisual.setBounds(0, borderWH - borderVisualWH, borderVisualWH, borderVisualWH);
        this.getContentPane().add(SWBorder);
        MouseInputAdapter SWListener = new MouseInputAdapter() {
            private boolean allowedToMove = false;
            private int xOffset;
            private int yOffset;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    Rectangle rect = LiveDPSGraph.this.getBounds();
                    rect.x += mouseLoc.x - xOffset;
                    rect.width -= mouseLoc.x - xOffset;
                    rect.height += mouseLoc.y - yOffset;
                    xOffset = mouseLoc.x;
                    yOffset = mouseLoc.y;

                    settings.resizeApp(rect);
                    LiveDPSGraph.this.setBounds(settings.getAppRectangle());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                allowedToMove = e.getButton() == MouseEvent.BUTTON1;
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    xOffset = mouseLoc.x;
                    yOffset = mouseLoc.y;
                    Color rootColor = LiveDPSGraph.this.getRootPane().getBackground();
                    Color borderColor = Tools.calcPercentageColor(rootColor, Tools.calcBestBlackWhiteColor(rootColor), .34);
                    LiveDPSGraph.this.getRootPane().setBorder(BorderFactory.createLineBorder(borderColor));
                    LiveDPSGraph.this.getContentPane().setVisible(false);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    allowedToMove = false;
                    LiveDPSGraph.this.updateResizeVisuals();
                    LiveDPSGraph.this.getContentPane().setVisible(true);
                    LiveDPSGraph.this.getRootPane().setBorder(null);
                }
            }
        };
        SWBorder.addMouseListener(SWListener);
        SWBorder.addMouseMotionListener(SWListener);

        // Add the north-west border
        NWBorder.setBounds(0, 0, borderWH, borderWH);
        NWBorder.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
        NWBorder.setBackground(new Color(0, 0, 0, 0));
        NWBorder.setLayout(null);
        NWBorder.add(NWBorderVisual);
        NWBorderVisual.setBounds(0, 0, borderVisualWH, borderVisualWH);
        this.getContentPane().add(NWBorder);
        MouseInputAdapter NWListener = new MouseInputAdapter() {
            private boolean allowedToMove = false;
            private int xOffset;
            private int yOffset;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    Rectangle rect = LiveDPSGraph.this.getBounds();
                    rect.x += mouseLoc.x - xOffset;
                    rect.y += mouseLoc.y - yOffset;
                    rect.width -= mouseLoc.x - xOffset;
                    rect.height -= mouseLoc.y - yOffset;
                    xOffset = mouseLoc.x;
                    yOffset = mouseLoc.y;

                    settings.resizeApp(rect);
                    LiveDPSGraph.this.setBounds(settings.getAppRectangle());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                allowedToMove = e.getButton() == MouseEvent.BUTTON1;
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();
                    xOffset = mouseLoc.x;
                    yOffset = mouseLoc.y;
                    Color rootColor = LiveDPSGraph.this.getRootPane().getBackground();
                    Color borderColor = Tools.calcPercentageColor(rootColor, Tools.calcBestBlackWhiteColor(rootColor), .34);
                    LiveDPSGraph.this.getRootPane().setBorder(BorderFactory.createLineBorder(borderColor));
                    LiveDPSGraph.this.getContentPane().setVisible(false);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    allowedToMove = false;
                    LiveDPSGraph.this.updateResizeVisuals();
                    LiveDPSGraph.this.getContentPane().setVisible(true);
                    LiveDPSGraph.this.getRootPane().setBorder(null);
                }
            }
        };
        NWBorder.addMouseListener(NWListener);
        NWBorder.addMouseMotionListener(NWListener);
        // Init wether the border is visual or not
        setIsResizable(!settings.isPinned());

        // Set the title bar
        this.getContentPane().add(titleBar);
        titleBar.setLayout(null);
        // Add the title bar's components
        titleBar.add(title);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 10.0f));
        title.setText(APPNAME);
        title.setBounds(padding, 0, 200, titleHeight);
        // Add the close button
        btnCloseMouseAdapter = new Tools.ButtonMouseAdapter(btnClose, btnCloseDefault, btnCloseHover, btnCloseActive) {
            @Override
            protected void clickEvent() {
                LiveDPSGraph.this.exit();
            }
        };
        btnClose.addMouseListener(btnCloseMouseAdapter);
        btnClose.setVisible(false);
        titleBar.add(btnClose);
        // Add the minimize button
        btnMinimizeMouseAdapter = new Tools.ButtonMouseAdapter(btnMinimize, btnMinimizeDefault, btnMinimizeHover, btnMinimizeActive) {
            @Override
            protected void clickEvent() {
                toggleActive();
            }
        };
        btnMinimize.addMouseListener(btnMinimizeMouseAdapter);
        btnMinimize.setVisible(false);
        titleBar.add(btnMinimize);
        // Add the pin button
        btnPinMouseAdapter = new Tools.ButtonMouseAdapter(btnPin, btnPinDefault, btnPinHover, btnPinActive) {
            @Override
            protected void clickEvent() {
                settings.setPinned(!settings.isPinned());
                setIsResizable(!settings.isPinned());
            }
        };
        btnPin.addMouseListener(btnPinMouseAdapter);
        btnPin.setVisible(false);
        titleBar.add(btnPin);
        // Add the always-on-top button
        btnAlwaysOnTopMouseAdapter = new Tools.ButtonMouseAdapter(btnAlwaysOnTop, btnAlwaysOnTopDefault, btnAlwaysOnTopHover, btnAlwaysOnTopActive) {
            @Override
            protected void clickEvent() {
                settings.setAlwaysOnTop(!settings.isAlwaysOnTop());
                LiveDPSGraph.this.setAlwaysOnTop(settings.isAlwaysOnTop());
                taskbarFrame.setVisible(!settings.isAlwaysOnTop());
            }
        };
        btnAlwaysOnTop.addMouseListener(btnAlwaysOnTopMouseAdapter);
        btnAlwaysOnTop.setVisible(false);
        titleBar.add(btnAlwaysOnTop);

        // Add the content-panel
        content.setLayout(null);
        this.getContentPane().add(content);
        content.add(userSelect);
        userSelect.addActionListener(new ActionListener() {
            private long prevWhen = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                long when = e.getWhen();
                if (when != prevWhen && e.getActionCommand().equals("comboBoxEdited")) {
                    String selectedUsername = userSelect.getSelectedItem().toString();
                    settings.setSelectedUser(selectedUsername);
                    prevWhen = when;
                }
            }
        });
        settings.addAddedUserListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                userSelect.updateUsers();
            }
        });
        userSelect.updateUsers();

        // Add the settings button
        btnSettingsMouseAdapter = new Tools.ButtonMouseAdapter(btnSettings, btnSettingsDefault, btnSettingsHover, btnSettingsActive) {
            @Override
            protected void clickEvent() {
                try {
                    settingsDialog = SettingsDialog.getInstance(LiveDPSGraph.this);
                    settingsDialog.updateColors();
                } catch (IOException ex) {
                    Logger.getLogger(LiveDPSGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        btnSettings.addMouseListener(btnSettingsMouseAdapter);
        content.add(btnSettings);

        // Add the info button
        btnInfoAdapter = new Tools.ButtonMouseAdapter(btnInfo, btnInfoDefault, btnInfoHover, btnInfoActive) {
            @Override
            protected void clickEvent() {
                if (infoDialog == null) {
                    try {
                        infoDialog = new InfoDialog(LiveDPSGraph.this);
                        infoDialog.setVisible(true);
                        infoDialog.updateBounds();
                        infoDialog.updateColors();
                    } catch (IOException ex) {
                        Logger.getLogger(LiveDPSGraph.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    infoDialog.setVisible(true);
                }
                infoDialog.setLocationRelativeTo(LiveDPSGraph.this);
            }
        };
        btnInfo.addMouseListener(btnInfoAdapter);
        content.add(btnInfo);

        // Add the scroll button
        btnScrollAdapter = new Tools.ButtonMouseAdapter(btnScroll, btnScrollDefault, btnScrollHover, btnScrollActive) {
            @Override
            protected void clickEvent() {
                try {
                    String msg = "<html>You are about to open EVE Online After Action Report. This will be opened in your default browser.<br />"
                            + "<br />"
                            + "Are you sure you want to continue?</html>";
                    OptionDialog dialog = new OptionDialog(LiveDPSGraph.this, "EVE Online After Action Report", msg, null);
                    dialog.setBtnOkEvent(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            final String url = "http://www.iciclesoft.com/eveonline/aar/";
                            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                            if (desktop != null) {
                                try {
                                    desktop.browse(new URI(url));
                                    return;
                                } catch (URISyntaxException | IOException ex) {
                                    Logger.getLogger(LiveDPSGraph.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            // Unable to open the browser, notify user and copy the url instead
                            String copyMsg = "<html>We are unable to open the browser for you. To view an after action report, please visit<br />"
                                    + "<br />"
                                    + url + "</html>";
                            try {
                                OptionDialog copyDialog = new OptionDialog(LiveDPSGraph.this, "EVE Online After Action Report", copyMsg, "Copy url");
                                copyDialog.setBtnOkEvent(new ActionListener() {

                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        StringSelection selection = new StringSelection(url);
                                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                        clipboard.setContents(selection, selection);
                                    }
                                });
                                copyDialog.setVisible(true);
                                copyDialog.setLocationRelativeTo(LiveDPSGraph.this);
                            } catch (IOException ex) {
                                Logger.getLogger(LiveDPSGraph.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    dialog.setVisible(true);
                    dialog.setLocationRelativeTo(LiveDPSGraph.this);
                } catch (IOException ex) {
                    Logger.getLogger(LiveDPSGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        btnScroll.addMouseListener(btnScrollAdapter);
        content.add(btnScroll);

        MouseInputAdapter rootPaneListener = new MouseInputAdapter() {
            private boolean allowedToMove = false;
            private int xOffset;
            private int yOffset;

            @Override
            public void mouseExited(MouseEvent e) {
                if (!LiveDPSGraph.this.getRootPane().contains(e.getPoint())) {
                    btnClose.setVisible(false);
                    btnMinimize.setVisible(false);
                    btnPin.setVisible(false);
                    btnAlwaysOnTop.setVisible(false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setVisible(true);
                btnMinimize.setVisible(true);
                btnPin.setVisible(true);
                btnAlwaysOnTop.setVisible(true);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (allowedToMove) {
                    Point mouseLoc = e.getLocationOnScreen();

                    settings.setAppX(mouseLoc.x - xOffset);
                    settings.setAppY(mouseLoc.y - yOffset);
                    LiveDPSGraph.this.setLocation(settings.getAppX(), settings.getAppY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                allowedToMove = !settings.isPinned() && e.getButton() == MouseEvent.BUTTON1;
                if (allowedToMove) {
                    Point appLoc = LiveDPSGraph.this.getLocation();
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
        visualizer = new DataVisualizer(content);
        this.updateColors();
        this.updateResizeVisuals();
    }

    protected final void toggleActive() {
        active = !active;
        if (active) {
            this.updateResizeVisuals();
        } else {
            this.setSize(this.getSize().width, titleHeight + 2);
        }
        setIsResizable(active);
    }

    private void setIsResizable(boolean aFlag) {
        NEBorder.setVisible(!settings.isPinned() && aFlag);
        SEBorder.setVisible(!settings.isPinned() && aFlag);
        SWBorder.setVisible(!settings.isPinned() && aFlag);
        NWBorder.setVisible(!settings.isPinned() && aFlag);
    }

    protected final void updateColors() {
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
        btnMinimizeMouseAdapter.updateButtonColors();
        btnSettingsMouseAdapter.updateButtonColors();
        btnPinMouseAdapter.updateButtonColors();
        btnAlwaysOnTopMouseAdapter.updateButtonColors();
        btnInfoAdapter.updateButtonColors();
        btnScrollAdapter.updateButtonColors();
        visualizer.updateColors();
        userSelect.updateColors();
        // Update other dialogs
        if (settingsDialog != null) {
            settingsDialog.updateColors();
        }
        if (infoDialog != null) {
            infoDialog.updateColors();
        }
    }

    protected final void updateResizeVisuals() {
        // Define the width/height
        int width = settings.getAppWidth();
        int height = settings.getAppHeight();
        // Define the x/y
        int x = settings.getAppX();
        int y = settings.getAppY();
        // Apply the width/height and x/y
        this.setBounds(x, y, width, height);
        // Set the bounds of the borders
        NEBorder.setBounds(width - borderWH, 0, borderWH, borderWH);
        SEBorder.setBounds(width - borderWH, height - borderWH, borderWH, borderWH);
        SWBorder.setBounds(0, height - borderWH, borderWH, borderWH);
        NWBorder.setBounds(0, 0, borderWH, borderWH);
        // Set the bounds of the title bar
        titleBar.setBounds(1, 1, width - 2, titleHeight);
        // And its children
        int buttonY = (int) ((titleHeight - buttonWH) * .5);
        btnClose.setBounds(width - buttonWH - 1, buttonY, buttonWH, buttonWH);
        btnMinimize.setBounds(width - buttonWH - buttonWH - 1, buttonY, buttonWH, buttonWH);
        btnPin.setBounds(width - buttonWH - buttonWH - buttonWH - 1, buttonY, buttonWH, buttonWH);
        btnAlwaysOnTop.setBounds(width - buttonWH - buttonWH - buttonWH - buttonWH - 1, buttonY, buttonWH, buttonWH);
        // Set the bounds of the content-panel
        content.setBounds(1, 2 + titleHeight, width - 2, height - titleHeight - 3);
        // And its children
        visualizer.updateResizeVisuals();
        userSelect.setBounds(padding, padding, 150, 18);
        btnSettings.setBounds(padding + padding + 150 + padding, padding, 18, 18);
        btnInfo.setBounds(content.getWidth() - 18 - padding, padding, 18, 18);
        btnScroll.setBounds(content.getWidth() - 18 - padding, content.getHeight() - 18 - padding, 18, 18);
    }

    protected void exit() {
        try {
            settings.saveSettings();
        } catch (IOException ex) {
            Logger.getLogger(LiveDPSGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        final LiveDPSGraph program = new LiveDPSGraph();
        program.setVisible(true);
        Timer setAppOnTop = null;

        try {
            SwingUtilities.invokeAndWait(Thread.currentThread());
            setAppOnTop = new Timer(30,
                    new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Bring the app back to the front
                    if (AppSettings.getInstance().isAlwaysOnTop()) {
                        program.toFront();
                    }
                }
            });
            setAppOnTop.start();
        } catch (InterruptedException | InvocationTargetException ex) {
            if (setAppOnTop != null) {
                setAppOnTop.stop();
            }
        }
    }

    class TaskbarFrame extends JFrame {

        private final LiveDPSGraph app;

        TaskbarFrame(LiveDPSGraph app) {
            super(LiveDPSGraph.APPNAME);
            setUndecorated(true);
            setLocationRelativeTo(null);
            ArrayList<Image> icons = new ArrayList();
            icons.add(new ImageIcon(getClass().getResource("UIComponents/16x16.png")).getImage());
            icons.add(new ImageIcon(getClass().getResource("UIComponents/32x32.png")).getImage());
            icons.add(new ImageIcon(getClass().getResource("UIComponents/64x64.png")).getImage());
            setIconImages(icons);
            this.app = app;

            this.addWindowListener(new WindowAdapter() {

                @Override
                public void windowActivated(WindowEvent e) {
                    super.windowActivated(e); //To change body of generated methods, choose Tools | Templates.
                    TaskbarFrame.this.app.toFront();
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                    TaskbarFrame.this.app.exit();
                }
            });
        }
    }
}
