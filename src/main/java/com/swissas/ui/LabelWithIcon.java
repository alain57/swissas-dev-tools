package com.swissas.ui;

import com.intellij.ui.JBColor;
import com.swissas.beans.LabelData;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A JLabel improvement with some icons and actions
 * 
 * @author Tavan Alain
 */

public class LabelWithIcon extends JLabel {
    private static final Color SELECTION_COLOR = new Color(82, 158, 202);
    static final ImageIcon REMOVE_ICON = new ImageIcon(LabelWithIcon.class.getResource("/delete.png"/*NON-NLS*/));
    private static final int SIZE = 36;
    private static final int MARGIN = 5;
    
    private boolean selected;
    private LabelData data;
    private ImageIcon warningIcon;

    public LabelWithIcon() {
        super();
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, SIZE + MARGIN, 0, SIZE + MARGIN +3));
    }

    void setSelected(boolean selected) {
        this.selected = selected;
        setForeground(selected ? JBColor.WHITE : JBColor.BLACK);
    }

    void setData(LabelData data) {
        this.data = data;
        ImageIcon tmpIcon = new ImageIcon(LabelWithIcon.class.getResource("/" + this.data.getWarningType().name().toLowerCase() + ".png"/*NON-NLS*/));
        this.warningIcon = new ImageIcon(tmpIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        setText(data.getName());
    }
    

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int halfSize = SIZE /2;
        if (this.selected) {
            Area area = new Area(new Ellipse2D.Double(0, 0, SIZE, SIZE));
            area.add(new Area(new RoundRectangle2D.Double(halfSize, 3, getWidth() - halfSize, 29, 6, 6)));
            g2d.setPaint(SELECTION_COLOR);
            g2d.fill(area);

            g2d.setPaint(JBColor.WHITE);
            g2d.fill(new Ellipse2D.Double(2, 2, 30, 30));
        }
        
        g2d.drawImage(this.warningIcon.getImage(), MARGIN + 13 - this.warningIcon.getIconWidth() / 2, MARGIN + 13 - this.warningIcon.getIconHeight() / 2, null);

        int bubbleWith = this.data.getNewMessages() > 99 ? halfSize + 4 : halfSize;
        int bubbleXPos = this.data.getNewMessages() > 99 ? getWidth() - SIZE - MARGIN -2 : getWidth() - SIZE - MARGIN;
        if(!this.selected) {
            if (this.data.getNewMessages() > 0) {
                g2d.setPaint(SELECTION_COLOR);
                g2d.fill(new Ellipse2D.Double(bubbleXPos, getHeight() / 2 - 9, bubbleWith, halfSize));

                final String text = "" + this.data.getNewMessages();
                final Font oldFont = g2d.getFont();
                g2d.setFont(oldFont.deriveFont(oldFont.getSize() - 1f));
                final FontMetrics fm = g2d.getFontMetrics();
                g2d.setPaint(JBColor.WHITE);
                g2d.drawString(text, getWidth() - 9 - halfSize - MARGIN - fm.stringWidth(text) / 2,
                        getHeight() / 2 + (fm.getAscent() - fm.getLeading() - fm.getDescent()) / 2);
                g2d.setFont(oldFont);
            }
        }else{
            if (this.data.getNewMessages() > 0) {
                g2d.setPaint(JBColor.WHITE);
                g2d.fill(new Ellipse2D.Double(bubbleXPos, getHeight() / 2 - 9, bubbleWith , halfSize ));

                g2d.setPaint(SELECTION_COLOR);
                final String text = "" + this.data.getNewMessages();
                final Font oldFont = g2d.getFont();
                g2d.setFont(oldFont.deriveFont(oldFont.getSize() - 1f));
                final FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, getWidth() - 9 - halfSize - MARGIN - fm.stringWidth(text) / 2,
                        getHeight() / 2 + (fm.getAscent() - fm.getLeading() - fm.getDescent()) / 2);
                g2d.setFont(oldFont);
            }
            
            g2d.drawImage(REMOVE_ICON.getImage(), getWidth() - 9 - MARGIN - REMOVE_ICON.getIconWidth() / 2,
                    getHeight() / 2 - REMOVE_ICON.getIconHeight() / 2, null);
        }

        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        final Dimension ps = super.getPreferredSize();
        ps.height = SIZE;
        return ps;
    }
}
