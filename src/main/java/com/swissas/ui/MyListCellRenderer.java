package com.swissas.ui;

import com.swissas.beans.LabelData;
import icons.SwissAsIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * A List cell renderer that will simulate an action if the user press the zone defined for the delete icon
 *
 * @author Tavan Alain
 */

public class MyListCellRenderer extends DefaultListCellRenderer {
    private final LabelWithIcon labelWithIcon;
    
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
                        Rectangle crossRect = new Rectangle ( rect.width - 9 - 5 - SwissAsIcons.DELETE.getIconWidth () / 2,
                                rect.height / 2 - SwissAsIcons.DELETE.getIconHeight () / 2, SwissAsIcons.DELETE.getIconWidth (), SwissAsIcons.DELETE.getIconHeight () );
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
