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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.message.MessageList;

public class ErrorCellLabelProvider extends CellLabelProvider {

    private final IEditTabelModel tableModel;

    public ErrorCellLabelProvider(IEditTabelModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public void update(ViewerCell cell) {
        MessageList list = tableModel.validate(cell.getElement());
        Image image = IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(list.getSeverity()),
                false);
        cell.setImage(image);
    }

}
