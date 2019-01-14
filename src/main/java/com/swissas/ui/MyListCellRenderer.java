package com.swissas.ui;

import com.swissas.beans.LabelData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.swissas.ui.LabelWithIcon.REMOVE_ICON;

/**
 * A List cell renderer that will simulate an action if the user press the zone defined for the delete icon
 *
 * @author Tavan Alain
 */

public class MyListCellRenderer extends DefaultListCellRenderer {
    private LabelWithIcon labelWithIcon;
    
    public MyListCellRenderer(final JList list){
        super();
        this.labelWithIcon = new LabelWithIcon();
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if ( SwingUtilities.isLeftMouseButton ( e ) )
                {
                    int index = list.locationToIndex ( e.getPoint () );
                    if ( index != -1 && list.isSelectedIndex ( index ) )
                    {
                        Rectangle rect = list.getCellBounds ( index, index );
                        Point pointWithinCell = new Point ( e.getX () - rect.x, e.getY () - rect.y );
                        Rectangle crossRect = new Rectangle ( rect.width - 9 - 5 - REMOVE_ICON.getIconWidth () / 2,
                                rect.height / 2 - REMOVE_ICON.getIconHeight () / 2, REMOVE_ICON.getIconWidth (), REMOVE_ICON.getIconHeight () );
                        if ( crossRect.contains ( pointWithinCell ) )
                        {
                            DefaultListModel model = ( DefaultListModel ) list.getModel ();
                            model.remove ( index );
                        }
                    }
                }
            }
        });
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        this.labelWithIcon.setSelected(isSelected);
        this.labelWithIcon.setData((LabelData)value);
        return this.labelWithIcon;
    }
}
