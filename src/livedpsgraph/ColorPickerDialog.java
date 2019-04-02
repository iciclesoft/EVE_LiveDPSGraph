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
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class ColorPickerDialog extends AppDialog {

    private final AppSettings settings = AppSettings.getInstance();
    private Color currentColor;
    private final JPanel content;
    private final Slider redSlider;
    private final JLabel redValue = new JLabel();
    private final Slider greenSlider;
    private final JLabel greenValue = new JLabel();
    private final Slider blueSlider;
    private final JLabel blueValue = new JLabel();
    private final JLabel btnOk = new JLabel();
    private final JLabel btnCancel = new JLabel();
    private final JLabel pickedColor = new JLabel();
    private Color defaultBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .9);
    private Color hoverBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .7);
    private Color activeBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .4);
    private Color defaultTextColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(defaultBtnColor), settings.getSecondaryColor(), .5);
    private Color hoverTextColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(hoverBtnColor), settings.getSecondaryColor(), .9);
    private Color activeTextColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(activeBtnColor), settings.getSecondaryColor(), .9);
    private Color borderColor = Tools.calcPercentageColor(settings.getPrimaryColor(), Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()), .87);
    private final ArrayList<ActionListener> colorChangedListeners = new ArrayList<>();

    protected ColorPickerDialog(JDialog owner, String title, Color color) throws IOException {
        super(owner, title, 350, 160);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setVisible(false);
        this.setLocationRelativeTo(owner);
        currentColor = color;
        Font lblFont = btnOk.getFont().deriveFont(Font.PLAIN);
        content = super.getContentPanel();
        // Add the sliders
        redSlider = new Slider(0, 255, color.getRed());
        content.add(redSlider);
        content.add(redValue);
        redValue.setFont(lblFont);
        greenSlider = new Slider(0, 255, color.getGreen());
        content.add(greenSlider);
        content.add(greenValue);
        greenValue.setFont(lblFont);
        blueSlider = new Slider(0, 255, color.getBlue());
        content.add(blueSlider);
        content.add(blueValue);
        blueValue.setFont(lblFont);
        content.add(pickedColor);
        pickedColor.setOpaque(true);
        // Add the sliders changed-listeners
        redSlider.addChangeListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
                pickedColor.setBackground(newColor);
                redSlider.setBackgroundColor(new Color(redSlider.getValue(), 0, 0));
                redSlider.setForegroundColor(new Color(255, redSlider.getValue(), redSlider.getValue()));
                redValue.setText(redSlider.getValue() + "");
                redSlider.updateColors();
                for (ActionListener l : colorChangedListeners) {
                    l.actionPerformed(new ActionEvent(newColor, 0, "Temp color"));
                }
            }
        });
        greenSlider.addChangeListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
                pickedColor.setBackground(newColor);
                greenSlider.setBackgroundColor(new Color(0, greenSlider.getValue(), 0));
                greenSlider.setForegroundColor(new Color(greenSlider.getValue(), 255, greenSlider.getValue()));
                greenValue.setText(greenSlider.getValue() + "");
                greenSlider.updateColors();
                for (ActionListener l : colorChangedListeners) {
                    l.actionPerformed(new ActionEvent(newColor, 0, "Temp color"));
                }
            }
        });
        blueSlider.addChangeListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
                pickedColor.setBackground(newColor);
                blueSlider.setBackgroundColor(new Color(0, 0, blueSlider.getValue()));
                blueSlider.setForegroundColor(new Color(blueSlider.getValue(), blueSlider.getValue(), 255));
                blueValue.setText(blueSlider.getValue() + "");
                blueSlider.updateColors();
                for (ActionListener l : colorChangedListeners) {
                    l.actionPerformed(new ActionEvent(newColor, 0, "Temp color"));
                }
            }
        });

        content.add(btnOk);
        btnOk.setText("Ok");
        btnOk.setFont(lblFont);
        btnOk.setHorizontalAlignment(JLabel.CENTER);
        btnOk.setOpaque(true);
        btnOk.addMouseListener(new MouseListener() {
            boolean mousePressed = false;

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePressed = e.getButton() == MouseEvent.BUTTON1;
                if (mousePressed) {
                    btnOk.setForeground(activeTextColor);
                    btnOk.setBackground(activeBtnColor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mousePressed = false;
                    if (btnOk.contains(e.getPoint())) {
                        currentColor = new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
                        ColorPickerDialog.this.dispose();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (mousePressed) {
                    btnOk.setForeground(activeTextColor);
                    btnOk.setBackground(activeBtnColor);
                } else {
                    btnOk.setForeground(hoverTextColor);
                    btnOk.setBackground(hoverBtnColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnOk.setForeground(defaultTextColor);
                btnOk.setBackground(defaultBtnColor);
            }
        });
        content.add(btnCancel);
        btnCancel.setText("Cancel");
        btnCancel.setFont(lblFont);
        btnCancel.setHorizontalAlignment(JLabel.CENTER);
        btnCancel.setOpaque(true);
        btnCancel.addMouseListener(new MouseListener() {
            boolean mousePressed = false;

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePressed = e.getButton() == MouseEvent.BUTTON1;
                if (mousePressed) {
                    btnCancel.setForeground(activeTextColor);
                    btnCancel.setBackground(activeBtnColor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    mousePressed = false;
                    if (btnOk.contains(e.getPoint())) {
                        ColorPickerDialog.this.dispose();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (mousePressed) {
                    btnCancel.setForeground(activeTextColor);
                    btnCancel.setBackground(activeBtnColor);
                } else {
                    btnCancel.setForeground(hoverTextColor);
                    btnCancel.setBackground(hoverBtnColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnCancel.setForeground(defaultTextColor);
                btnCancel.setBackground(defaultBtnColor);
            }
        });

        updateBounds();
        updateColors();
    }

    protected final void addColorChangedListener(ActionListener l) {
        colorChangedListeners.add(l);
    }

    protected final Color pickColor() {
        this.setModal(true);
        this.setVisible(true);
        return currentColor;
    }

    @Override
    protected final void updateBounds() {
        super.updateBounds();
        int padding = 5;
        int sliderWH = 10;
        int buttonWidth = 80;
        int buttonHeight = 20;
        int buttonY = content.getHeight() - padding - buttonHeight;
        int currentY = padding;
        redSlider.setBounds(padding, currentY, 255 + sliderWH, sliderWH);
        redValue.setBounds(padding + redSlider.getWidth() + padding, currentY, 50, buttonHeight);
        currentY += padding + buttonHeight;
        greenSlider.setBounds(padding, currentY, 255 + sliderWH, sliderWH);
        greenValue.setBounds(padding + greenSlider.getWidth() + padding, currentY, 50, buttonHeight);
        currentY += padding + buttonHeight;
        blueSlider.setBounds(padding, currentY, 255 + sliderWH, sliderWH);
        blueValue.setBounds(padding + blueSlider.getWidth() + padding, currentY, 50, buttonHeight);
        currentY += padding + buttonHeight;
        pickedColor.setBounds(padding, currentY, content.getWidth() - padding - padding, buttonHeight);
        btnCancel.setBounds(content.getWidth() - padding - buttonWidth, buttonY, buttonWidth, buttonHeight);
        btnOk.setBounds(content.getWidth() - padding - buttonWidth - padding - buttonWidth, buttonY, buttonWidth, buttonHeight);
    }

    @Override
    protected final void updateColors() {
        super.updateColors();
        defaultBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .4);
        hoverBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .2);
        activeBtnColor = settings.getSecondaryColor();
        defaultTextColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(defaultBtnColor), settings.getSecondaryColor(), .6);
        hoverTextColor = Tools.calcBestBlackWhiteColor(hoverBtnColor);
        activeTextColor = Tools.calcBestBlackWhiteColor(activeBtnColor);
        borderColor = Tools.calcPercentageColor(settings.getPrimaryColor(), Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()), .77);
        redSlider.updateColors();
        redValue.setForeground(Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()));
        greenSlider.updateColors();
        greenValue.setForeground(Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()));
        blueSlider.updateColors();
        blueValue.setForeground(Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()));
        pickedColor.setBackground(currentColor);
        pickedColor.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        btnOk.setForeground(defaultTextColor);
        btnOk.setBackground(defaultBtnColor);
        btnOk.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        btnCancel.setForeground(defaultTextColor);
        btnCancel.setBackground(defaultBtnColor);
        btnCancel.setBorder(BorderFactory.createLineBorder(borderColor, 1));
    }
}
