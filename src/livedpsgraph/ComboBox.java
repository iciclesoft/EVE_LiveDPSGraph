/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package livedpsgraph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.LEFT;
import javax.swing.border.CompoundBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 *
 * @author Marco de Zeeuw <iciclesoft.com>
 */
public final class ComboBox extends JComboBox<String> {

    private final AppSettings settings = AppSettings.getInstance();
    private String[] userNames;
    private final ComboBoxCellRenderer comboBoxCellRenderer;
    private final CombBoxUI UI = new CombBoxUI();
    private Color darkSecondary = Tools.calcPercentageColor(settings.getSecondaryColor(), settings.getPrimaryColor(), .5);
    private Color borderColor = Tools.calcPercentageColor(darkSecondary, Tools.calcBestBlackWhiteColor(darkSecondary), .95);
    private Color bgColor;
    private double toBWColorPercentage = .53;
    private double bgBWColorPercentage = .0;
    private final BasicComboPopup popup;
    private final ScrollBar customScrollBar = new ScrollBar();

    protected ComboBox() {
        userNames = settings.getUsers();
        bgColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(darkSecondary), darkSecondary, bgBWColorPercentage);
        this.setModel(new DefaultComboBoxModel<>(settings.getUsers()));
        this.setUI(UI);
        this.setEditable(true);
        comboBoxCellRenderer = new ComboBoxCellRenderer();
        this.setRenderer(comboBoxCellRenderer);
        Object child = this.getAccessibleContext().getAccessibleChild(0);
        popup = (BasicComboPopup) child;
        Component c = ((Container) popup).getComponent(0);
        if (c instanceof JScrollPane) {            
            JScrollPane scrollPane = (JScrollPane) c;
            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
            scrollBar.setUI(customScrollBar);
            Dimension scrollBarDim = new Dimension(7, scrollBar
                    .getPreferredSize().height);
            scrollBar.setPreferredSize(scrollBarDim);
        }
        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                toBWColorPercentage = .8;
                bgBWColorPercentage = .05;
                updateColors();
                UI.comboBoxEditor.getEditorComponent().repaint();
                UI.button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                toBWColorPercentage = .53;
                bgBWColorPercentage = .0;
                updateColors();
                UI.comboBoxEditor.getEditorComponent().repaint();
                UI.button.repaint();
            }
        });

        updateColors();
        // Ugly way to init the combobox with the right selected user at startup
        this.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                UI.comboBoxEditor.getEditorComponent();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    protected final void updateColors() {
        comboBoxCellRenderer.updateColors();
        CompoundBorder border = new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, settings.getPrimaryColor()),
                BorderFactory.createMatteBorder(0, 1, 1, 1, borderColor));
        popup.setBorder(border);
        UI.updateColors();
        customScrollBar.updateColors();
    }

    protected final void updateUsers() {
        userNames = settings.getUsers();
        String selected = settings.getSelectedUser();
        this.setModel(new DefaultComboBoxModel<>(settings.getUsers()));
        for (String user : userNames) {
            if (this.getSelectedItem().toString().equals(selected)) {
                break;
            } else if (selected != null && selected.equals(user)) {
                this.setSelectedItem(selected);
                break;
            }
        }
    }

    protected final String[] getUsers() {
        return userNames;
    }

    private class ComboBoxCellRenderer extends JLabel implements ListCellRenderer {

        private Color defaultColor;
        private Color defaultFontColor;
        private Color selectedColor;
        private Color selectedFontColor;
        private Color hoverColor;
        private Color hoverFontColor;

        public ComboBoxCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        protected final void updateColors() {
            darkSecondary = Tools.calcPercentageColor(settings.getSecondaryColor(), settings.getPrimaryColor(), .5);
            defaultColor = Tools.calcPercentageColor(Tools.calcPercentageColor(settings.getPrimaryColor(),
                    settings.getSecondaryColor(), .87), Tools.calcBestBlackWhiteColor(settings.getPrimaryColor()), .95);
            defaultFontColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(defaultColor), defaultColor, .75);
            selectedColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .20);
            selectedFontColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(selectedColor), selectedColor, .75);
            hoverColor = Tools.calcPercentageColor(settings.getPrimaryColor(), settings.getSecondaryColor(), .38);
            hoverFontColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(hoverColor), hoverColor, .75);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isHovered, boolean cellHasFocus) {
            String user = "";
            if (index >= 0) {
                user = userNames[index];
            }
            int selected = getSelectedIndex();
            if (isHovered) {
                setBackground(hoverColor);
                setForeground(hoverFontColor);
            } else {
                setBackground(defaultColor);
                setForeground(defaultFontColor);
            }
            if (selected == index) {
                setBackground(selectedColor);
                setForeground(selectedFontColor);
            }

            setFont(list.getFont().deriveFont(Font.PLAIN));
            if (user.equals("")) {
                setText("");
                setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                setPreferredSize(new Dimension(150, 1));
            } else {
                setText(" " + user);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, defaultColor));
                setPreferredSize(new Dimension(150, 21));
            }

            return this;
        }
    }

    protected class CombBoxUI extends BasicComboBoxUI {

        private ComboBoxEditor comboBoxEditor;
        private ComboBoxButton button;
        protected final JPanel panel = new JPanel();
        protected final JLabel label = new JLabel();

        @Override
        protected ComboBoxEditor createEditor() {
            comboBoxEditor = new ComboBoxEditor() {
                private final ArrayList<ActionListener> listeners = new ArrayList<>();
                private String selected = null;
                private String prevSelected = null;

                @Override
                public Component getEditorComponent() {
                    label.setFont(label.getFont().deriveFont(Font.PLAIN));
                    label.setVerticalAlignment(JLabel.TOP);
                    panel.add(label);
                    panel.setLayout(null);
                    Rectangle rect = panel.getBounds();
                    label.setBounds(rect.x + 5, rect.y, rect.width, rect.height);
                    return panel;
                }

                @Override
                public void setItem(Object anObject) {
                    selected = anObject.toString();
                    if (selected != null && !selected.equals(prevSelected)) {
                        label.setText(selected);
                        for (ActionListener listener : listeners) {
                            listener.actionPerformed(new ActionEvent(this, 0, "Changed item"));
                        }
                        prevSelected = selected;
                    }
                }

                @Override
                public Object getItem() {
                    return selected;
                }

                @Override
                public void selectAll() {

                }

                @Override
                public void addActionListener(ActionListener l) {
                    listeners.add(l);
                }

                @Override
                public void removeActionListener(ActionListener l) {
                    listeners.remove(l);
                }
            };
            return comboBoxEditor;
        }

        @Override
        protected JButton createArrowButton() {
            button = new ComboBoxButton();
            button.addMouseListener(button);
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    comboBoxEditor.getEditorComponent().repaint();
                }
            });
            return button;
        }

        protected void updateColors() {
            Color fontColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(darkSecondary), darkSecondary, toBWColorPercentage);
            bgColor = Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(darkSecondary), darkSecondary, bgBWColorPercentage);
            borderColor = Tools.calcPercentageColor(darkSecondary, Tools.calcBestBlackWhiteColor(darkSecondary), .95);
            panel.setBackground(bgColor);
            panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, borderColor));
            label.setForeground(fontColor);
            button.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, borderColor));
        }
    }

    private class ComboBoxButton extends JButton implements MouseListener {

        private final ArrayList<ActionListener> listeners = new ArrayList<>();

        @Override
        public void addActionListener(ActionListener l) {
            listeners.add(l);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw rectangle
            g.setColor(bgColor);
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            // Draw arrow
            g.setColor(Tools.calcPercentageColor(Tools.calcBestBlackWhiteColor(darkSecondary), darkSecondary, toBWColorPercentage));
            g.fillPolygon(new int[]{5, 12, 8}, new int[]{7, 7, 11}, 3);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            toBWColorPercentage = .8;
            bgBWColorPercentage = .05;
            updateColors();
            for (ActionListener listener : listeners) {
                listener.actionPerformed(new ActionEvent(this, 0, "Entered"));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            toBWColorPercentage = .53;
            bgBWColorPercentage = .0;
            updateColors();
            for (ActionListener listener : listeners) {
                listener.actionPerformed(new ActionEvent(this, 0, "Exited"));
            }
        }
    }
}
