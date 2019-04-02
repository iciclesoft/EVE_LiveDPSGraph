/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class InfoDialog extends AppDialog {

    private final AppSettings settings = AppSettings.getInstance();
    private final JPanel content;
    private final JLabel imgGrauthThorner = new JLabel();
    private final JLabel versionText = new JLabel();
    private final JLabel CCPNotice = new JLabel();
    // Constants
    private final int padding = 5;

    protected InfoDialog(LiveDPSGraph owner) throws IOException {
        super(owner, LiveDPSGraph.APPNAME + " info", 500, 310);
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        content = this.getContentPanel();
        Font lblFont = versionText.getFont().deriveFont(Font.PLAIN);
        // Add the image of Grauth Thorner
        content.add(imgGrauthThorner);
        BufferedImage image = ImageIO.read(getClass().getResource("UIComponents/GrauthThorner.jpg"));
        imgGrauthThorner.setHorizontalAlignment(JLabel.CENTER);
        imgGrauthThorner.setIcon(new ImageIcon(image));
        // Add the version text
        versionText.setText("<html>" + LiveDPSGraph.APPNAME + " version " + LiveDPSGraph.VERSION + "<br />"
                + "Released " + LiveDPSGraph.RELEASE_DATE + ".<br />"
                + "<br />"
                + "This third-party application has been created by Grauth Thorner.<br />\n"
                + "Grauth Thorner is a fictitious name, as this is a character's name.<br />\n"
                + "I am in no way related to CCP, as I am a third-party developer.<br />\n"
                + "<br />\n"
                + "For more information, check out http://www.iciclesoft.com/"
                + "</html>");
        versionText.setFont(lblFont);
        content.add(versionText);
        // Add CCP's notice
        CCPNotice.setText("<html>© 2014 CCP hf. All rights reserved. "
                + "\"EVE\", \"EVE Online\", \"CCP\", and all related logos<br />and "
                + "images are trademarks or registered trademarks of CCP hf.</html>");
        CCPNotice.setFont(lblFont);
        content.add(CCPNotice);
    }

    @Override
    protected final void updateBounds() {
        super.updateBounds();
        int widthMinusPadding = content.getWidth() - padding - padding;
        int versionTextX = (int) (content.getWidth() * .5) + padding;
        int versionTextY = 0;
        imgGrauthThorner.setBounds(padding, padding, 240, 240);
        versionText.setBounds(versionTextX, versionTextY, widthMinusPadding - versionTextX, content.getHeight() - 40);
        CCPNotice.setBounds(padding, content.getHeight() - 40, widthMinusPadding, 40);
    }

    @Override
    protected void updateColors() {
        super.updateColors();
        Color fontColor = Tools.calcBestBlackWhiteColor(settings.getPrimaryColor());
        Color noticeColor = Tools.calcPercentageColor(fontColor, settings.getPrimaryColor(), .5);
        versionText.setForeground(fontColor);
        CCPNotice.setForeground(noticeColor);
    }
}
