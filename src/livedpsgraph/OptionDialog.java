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
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public class OptionDialog extends AppDialog {

    private final AppSettings settings = AppSettings.getInstance();
    private final JPanel content;
    private final JLabel lblQuestion;
    private final JLabel btnOk = new JLabel();
    private final JLabel btnCancel = new JLabel("Cancel");
    // Button listeners
    private ActionListener btnOkListener;
    // Colors
    private Color defaultBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .9);
    private Color hoverBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .7);
    private Color activeBtnColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .4);
    private Color defaultTextColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(defaultBtnColor), settings.getSecondaryColor(), .5);
    private Color hoverTextColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(hoverBtnColor), settings.getSecondaryColor(), .9);
    private Color activeTextColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(activeBtnColor), settings.getSecondaryColor(), .9);
    private Color borderColor = Tools.calcPercentageColor(settings.getPrimaryColor(), Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()), .87);

    protected OptionDialog(LiveDPSGraph owner, String title, String questionHtml, String okTxt) throws IOException {
        super(owner, title, 400, 200);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        content = this.getContentPanel();
        lblQuestion = new JLabel(questionHtml);
        content.add(lblQuestion);
        Font lblFont = lblQuestion.getFont().deriveFont(Font.PLAIN);
        lblQuestion.setFont(lblFont);

        content.add(btnOk);
        btnOk.setFont(lblFont);
        if (okTxt == null || okTxt.length() <= 0)
            okTxt = "Ok";
        btnOk.setText(okTxt);
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
                        if (btnOkListener != null) {
                            btnOkListener.actionPerformed(new ActionEvent(e, 0, "Ok clicked"));
                        }
                        OptionDialog.this.dispose();
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
                    if (btnCancel.contains(e.getPoint())) {
                        OptionDialog.this.dispose();
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

    protected final void setBtnOkEvent(ActionListener listener) {
        btnOkListener = listener;
    }

    @Override
    protected final void updateBounds() {
        super.updateBounds();
        int padding = 5;
        int buttonWidth = 80;
        int buttonHeight = 20;
        int buttonY = content.getHeight() - padding - buttonHeight;
        lblQuestion.setBounds(padding, padding, content.getWidth() - padding - padding, buttonY - padding - padding);
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
        
        lblQuestion.setForeground(defaultTextColor);

        btnOk.setForeground(defaultTextColor);
        btnOk.setBackground(defaultBtnColor);
        btnOk.setBorder(BorderFactory.createLineBorder(borderColor, 1));

        btnCancel.setForeground(defaultTextColor);
        btnCancel.setBackground(defaultBtnColor);
        btnCancel.setBorder(BorderFactory.createLineBorder(borderColor, 1));
    }
}
