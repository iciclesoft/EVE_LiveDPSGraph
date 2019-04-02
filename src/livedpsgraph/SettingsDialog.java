/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import livedpsgraph.Tools.ButtonMouseAdapter;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class SettingsDialog extends AppDialog {

    private static SettingsDialog instance;
    private final LiveDPSGraph parent;
    private final AppSettings settings = AppSettings.getInstance();
    private final JPanel content;
    // Add primary color content
    private final JLabel lblPrimaryColor = new JLabel();
    private final JLabel lblPrimaryColorValue = new JLabel();
    private final JLabel primaryColorIcon = new JLabel();
    private final ButtonMouseAdapter bmaPrimaryColor;
    // Add secondary color content
    private final JLabel lblSecondaryColor = new JLabel();
    private final JLabel lblSecondaryColorValue = new JLabel();
    private final JLabel secondaryColorIcon = new JLabel();
    private final ButtonMouseAdapter bmaSecondaryColor;
    // Add log path content
    private final JLabel lblLogPath = new JLabel();
    private final JTextArea txtLogPath = new JTextArea();
    private final JLabel logPathIcon = new JLabel();
    private final ButtonMouseAdapter bmaLogPath;
    // Add synchronize users content
    private final JLabel btnSync = new JLabel();
    // Add button close
    private final JLabel btnClose = new JLabel();
    // Colors...
    private Color defaultBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .7);
    private Color hoverBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .4);
    private Color activeBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .2);
    private Color defaultTextColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(defaultBtnColor), settings.getSecondaryColor(), .5);
    private Color hoverTextColor = Tools.calcBestBlackWhiteColor(hoverBtnColor);
    private Color activeTextColor = Tools.calcBestBlackWhiteColor(activeBtnColor);
    private Color borderColor = Tools.calcPercentageColor(settings.getPrimaryColor(), Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()), .87);
    // Constants
    private final int width;

    private SettingsDialog(LiveDPSGraph parent) throws IOException {
        super(parent, "Settings", 500, 240);
        this.parent = parent;
        width = 500;
        content = super.getContentPanel();
        // Load the icon images
        BufferedImage btnBrowseDefault = ImageIO.read(getClass().getResource("UIComponents/btnBrowseDefault.png"));
        BufferedImage btnBrowseHover = ImageIO.read(getClass().getResource("UIComponents/btnBrowseHover.png"));
        BufferedImage btnBrowseActive = ImageIO.read(getClass().getResource("UIComponents/btnBrowseActive.png"));
        BufferedImage btnPickColorDefault = ImageIO.read(getClass().getResource("UIComponents/btnPickColorDefault.png"));
        BufferedImage btnPickColorHover = ImageIO.read(getClass().getResource("UIComponents/btnPickColorHover.png"));
        BufferedImage btnPickColorActive = ImageIO.read(getClass().getResource("UIComponents/btnPickColorActive.png"));
        Font lblFont = lblLogPath.getFont().deriveFont(Font.PLAIN);
        // Add the primary color setting
        content.add(lblPrimaryColor);
        lblPrimaryColor.setText("Primary color:");
        lblPrimaryColor.setFont(lblFont);
        content.add(lblPrimaryColorValue);
        lblPrimaryColorValue.setFont(lblFont);
        content.add(primaryColorIcon);
        bmaPrimaryColor = new ButtonMouseAdapter(primaryColorIcon, btnPickColorDefault, btnPickColorHover, btnPickColorActive) {

            @Override
            protected void clickEvent() {
                try {
                    ColorPickerDialog colorPicker = new ColorPickerDialog(SettingsDialog.this, "Pick primary color", settings.getPrimaryColor());
                    colorPicker.addColorChangedListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            settings.setPrimaryColor((Color) e.getSource());
                            SettingsDialog.this.parent.updateColors();
                        }
                    });
                    Color c = colorPicker.pickColor();
                    settings.setPrimaryColor(c);
                    SettingsDialog.this.parent.updateColors();
                } catch (IOException ex) {
                    Logger.getLogger(SettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        primaryColorIcon.addMouseListener(bmaPrimaryColor);
        // Add the secondary color setting
        content.add(lblSecondaryColor);
        lblSecondaryColor.setText("Secondary color:");
        lblSecondaryColor.setFont(lblFont);
        content.add(lblSecondaryColorValue);
        lblSecondaryColorValue.setFont(lblFont);
        content.add(secondaryColorIcon);
        bmaSecondaryColor = new ButtonMouseAdapter(secondaryColorIcon, btnPickColorDefault, btnPickColorHover, btnPickColorActive) {

            @Override
            protected void clickEvent() {
                try {
                    ColorPickerDialog colorPicker = new ColorPickerDialog(SettingsDialog.this, "Pick secondary color", settings.getSecondaryColor());
                    colorPicker.addColorChangedListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            settings.setSecondaryColor((Color) e.getSource());
                            SettingsDialog.this.parent.updateColors();
                        }
                    });
                    Color c = colorPicker.pickColor();
                    settings.setSecondaryColor(c);
                    SettingsDialog.this.parent.updateColors();
                } catch (IOException ex) {
                    Logger.getLogger(SettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        secondaryColorIcon.addMouseListener(bmaSecondaryColor);
        // Add the LogPath setting
        content.add(lblLogPath);
        lblLogPath.setText("Log path:");
        lblLogPath.setFont(lblFont);
        content.add(txtLogPath);
        txtLogPath.setText(settings.getLogPath());
        txtLogPath.setEditable(false);
        txtLogPath.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (txtLogPath.contains(e.getPoint())) {
                    browseLogFile();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        content.add(logPathIcon);
        bmaLogPath = new ButtonMouseAdapter(logPathIcon, btnBrowseDefault, btnBrowseHover, btnBrowseActive) {

            @Override
            protected void clickEvent() {
                browseLogFile();
            }
        };
        logPathIcon.addMouseListener(bmaLogPath);
        // Add the sync setting
        content.add(btnSync);
        btnSync.setText("Synchronize users");
        btnSync.setFont(lblFont);
        btnSync.setOpaque(true);
        btnSync.setHorizontalAlignment(JLabel.CENTER);
        btnSync.addMouseListener(new MouseListener() {
            boolean mousePressed = false;

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePressed = e.getButton() == MouseEvent.BUTTON1;
                if (mousePressed) {
                    btnSync.setForeground(activeTextColor);
                    btnSync.setBackground(activeBtnColor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mousePressed = false;
                    if (btnSync.contains(e.getPoint())) {
                        btnSync.setForeground(hoverTextColor);
                        btnSync.setBackground(hoverBtnColor);
                        settings.synchronizeUsers();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (mousePressed) {
                    btnSync.setForeground(activeTextColor);
                    btnSync.setBackground(activeBtnColor);
                } else {
                    btnSync.setForeground(hoverTextColor);
                    btnSync.setBackground(hoverBtnColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSync.setForeground(defaultTextColor);
                btnSync.setBackground(defaultBtnColor);
            }
        });
        // Add the close setting
        content.add(btnClose);
        btnClose.setText("Close");
        btnClose.setFont(lblFont);
        btnClose.setOpaque(true);
        btnClose.setHorizontalAlignment(JLabel.CENTER);
        btnClose.addMouseListener(new MouseListener() {
            boolean mousePressed = false;

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePressed = e.getButton() == MouseEvent.BUTTON1;
                if (mousePressed) {
                    btnClose.setForeground(activeTextColor);
                    btnClose.setBackground(activeBtnColor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mousePressed = false;
                    if (btnClose.contains(e.getPoint())) {
                        SettingsDialog.this.dispose();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (mousePressed) {
                    btnClose.setForeground(activeTextColor);
                    btnClose.setBackground(activeBtnColor);
                } else {
                    btnClose.setForeground(hoverTextColor);
                    btnClose.setBackground(hoverBtnColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(defaultTextColor);
                btnClose.setBackground(defaultBtnColor);
            }
        });

        updateColors();
    }

    protected static final AppDialog getInstance(LiveDPSGraph parent) throws IOException {
        if (instance == null) {
            instance = new SettingsDialog(parent);
        }
        instance.setLocationRelativeTo(parent);
        instance.setVisible(true);
        return instance;
    }

    private void browseLogFile() {
        JFileChooser chooser = new JFileChooser();
        File curDir = new File(settings.getLogPath());
        if (!curDir.exists()) {
            curDir = new File(".");
        }
        chooser.setCurrentDirectory(curDir);
        chooser.setDialogTitle("Select gamelog directory...");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Select gamelog directory", "txt"));
        int returnVal = chooser.showOpenDialog(SettingsDialog.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            String path;
            if (selected.exists() && selected.isDirectory()) {
                path = selected.getAbsolutePath();
            } else {
                path = chooser.getCurrentDirectory().getAbsolutePath();
            }
            settings.setLogPath(path);
            txtLogPath.setText(settings.getLogPath());
        }
    }

    private String colorToString(Color color) {
        return "Red: " + color.getRed() + " Green: " + color.getGreen() + " Blue: " + color.getBlue();
    }

    @Override
    protected final void updateBounds() {
        super.updateBounds();
        int padding = 5;
        int heightPadding = 15;
        int buttonWH = 18;
        int labelHeight = 20;
        int currentY = heightPadding;
        // Update primary color
        int lblPrimaryColorWidth = (int) (lblPrimaryColor.getText().length() * 6);
        lblPrimaryColor.setBounds(padding, currentY, lblPrimaryColorWidth, labelHeight);
        int lblPrimaryColorValueWidth = (int) (lblPrimaryColorValue.getText().length() * 6);
        lblPrimaryColorValue.setBounds(padding + lblPrimaryColorWidth + padding, currentY, lblPrimaryColorValueWidth, labelHeight);
        primaryColorIcon.setBounds(padding + lblPrimaryColorWidth + padding + lblPrimaryColorValueWidth + padding, currentY, buttonWH, buttonWH);
        currentY += heightPadding + labelHeight;
        // Update secondary color
        int lblSecondaryColorWidth = (int) (lblSecondaryColor.getText().length() * 6);
        lblSecondaryColor.setBounds(padding, currentY, lblSecondaryColorWidth, labelHeight);
        int lblSecondaryColorValueWidth = (int) (lblSecondaryColorValue.getText().length() * 6);
        lblSecondaryColorValue.setBounds(padding + lblSecondaryColorWidth + padding, currentY, lblSecondaryColorValueWidth, labelHeight);
        secondaryColorIcon.setBounds(padding + lblSecondaryColorWidth + padding + lblSecondaryColorValueWidth + padding, currentY, buttonWH, buttonWH);
        currentY += heightPadding + labelHeight + heightPadding + labelHeight;
        // Update log path
        int lblLogPathWidth = (int) (lblLogPath.getText().length() * 6);
        lblLogPath.setBounds(padding, currentY, lblLogPathWidth, labelHeight);
        int txtLogPathWidth = width - (padding + lblLogPathWidth + padding + padding + buttonWH + padding);
        txtLogPath.setBounds(padding + lblLogPathWidth + padding, currentY, txtLogPathWidth, labelHeight);
        logPathIcon.setBounds(padding + lblLogPathWidth + padding + txtLogPathWidth + padding, currentY, buttonWH, buttonWH);
        currentY += heightPadding + labelHeight;
        // Update synchronize users
        int btnSyncWidth = (int) (btnSync.getText().length() * 6) + heightPadding;
        btnSync.setBounds((int) ((content.getWidth() - btnSyncWidth) * .5), currentY, btnSyncWidth, labelHeight);
        // Update close button
        int btnCloseWidth = (int) (btnClose.getText().length() * 6) + heightPadding;
        btnClose.setBounds(content.getWidth() - padding - btnCloseWidth, content.getHeight() - padding - labelHeight, btnCloseWidth, labelHeight);
    }

    @Override
    protected final void updateColors() {
        super.updateColors();
        // Update the colors
        defaultBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .4);
        hoverBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .2);
        activeBtnColor = settings.getSecondaryColor();
        defaultTextColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(defaultBtnColor), settings.getSecondaryColor(), .6);
        hoverTextColor = Tools.calcBestBlackWhiteColor(hoverBtnColor);
        activeTextColor = Tools.calcBestBlackWhiteColor(activeBtnColor);
        borderColor = Tools.calcPercentageColor(settings.getPrimaryColor(), Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()), .77);
        Color txtBackground = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .60);
        Color primaryBestBlackWhite = Tools.calcBestBlackWhiteColor(settings.getPrimaryColor());
        Color secondaryBestBlackWhite = Tools.calcBestBlackWhiteColor(settings.getSecondaryColor());
        // Update primary color
        lblPrimaryColor.setForeground(primaryBestBlackWhite);
        lblPrimaryColorValue.setForeground(primaryBestBlackWhite);
        bmaPrimaryColor.updateButtonColors();
        // Update secondary color
        lblSecondaryColor.setForeground(primaryBestBlackWhite);
        lblSecondaryColorValue.setForeground(primaryBestBlackWhite);
        bmaSecondaryColor.updateButtonColors();
        // Update log path
        lblLogPath.setForeground(primaryBestBlackWhite);
        txtLogPath.setBackground(txtBackground);
        txtLogPath.setForeground(secondaryBestBlackWhite);
        bmaLogPath.updateButtonColors();
        txtLogPath.setBorder(
                new CompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, borderColor),
                        BorderFactory.createMatteBorder(0, 5, 0, 0, txtBackground)));
        // Update synchronize users
        btnSync.setForeground(defaultTextColor);
        btnSync.setBackground(defaultBtnColor);
        btnSync.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        // Update close button
        btnClose.setForeground(defaultTextColor);
        btnClose.setBackground(defaultBtnColor);
        btnClose.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        // Update the text representing the colors
        lblPrimaryColorValue.setText(colorToString(settings.getPrimaryColor()));
        lblSecondaryColorValue.setText(colorToString(settings.getSecondaryColor()));
        updateBounds();
    }
}
