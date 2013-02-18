/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import javax.swing.text.TabableView;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.message.MessageList;

/**
 * This cell label provider that providing icons depending on the validation messages retrieved by a
 * table model. The generic type <code>T</code> indicates the type of the objects provided by the
 * {@link IEditTableModel}. However it cannot be really checked whether the content provider of the
 * {@link TabableView} really provides this kind of object or not.
 * 
 * @author dirmeier
 */
public class ErrorCellLabelProvider<T> extends CellLabelProvider {

    private final IEditTableModel<T> tableModel;

    public ErrorCellLabelProvider(IEditTableModel<T> tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public void update(ViewerCell cell) {
        @SuppressWarnings("unchecked")
        // cell is provided by the framework and we cannot really check the type
        MessageList list = tableModel.validate((T)cell.getElement());
        Image image = IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(list.getSeverity()),
                false);
        cell.setImage(image);
    }

    @Override
    public String getToolTipText(Object element) {
        @SuppressWarnings("unchecked")
        // element is provided by the framework and we cannot really check the type
        T castedElement = (T)element;
        MessageList list = tableModel.validate(castedElement);
        if (list.isEmpty()) {
            return super.getToolTipText(element);
        } else {
            return list.getMessageWithHighestSeverity().getText();
        }
    }

    @Override
    public Point getToolTipShift(Object object) {
        return new Point(5, 5);
    }

    @Override
    public int getToolTipDisplayDelayTime(Object object) {
        return 100;
    }

    @Override
    public int getToolTipTimeDisplayed(Object object) {
        return 5000;
    }

}
