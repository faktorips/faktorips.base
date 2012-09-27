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

package org.faktorips.devtools.core.ui.views.modeloverview;

import java.util.Arrays;

import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;

public class ModelOverviewDecoratingStyledCellLabelProvider extends DecoratingStyledCellLabelProvider {

    private ModelOverviewLabelProvider labelProvider;

    public ModelOverviewDecoratingStyledCellLabelProvider(ModelOverviewLabelProvider labelProvider,
            ILabelDecorator decorator, IDecorationContext decorationContext) {
        super(labelProvider, decorator, decorationContext);
        this.labelProvider = labelProvider;
    }

    @Override
    public String getToolTipText(Object element) {
        return labelProvider.getToolTipText(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.StyledCellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell
     * )
     */
    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();

        StyledString styledString = getStyledText(element);
        String newText = styledString.toString();

        StyleRange[] oldStyleRanges = cell.getStyleRanges();
        StyleRange[] newStyleRanges = isOwnerDrawEnabled() ? styledString.getStyleRanges() : null;

        if (!Arrays.equals(oldStyleRanges, newStyleRanges)) {
            cell.setStyleRanges(newStyleRanges);
            if (cell.getText().equals(newText)) {
                // make sure there will be a refresh from a change
                cell.setText(""); //$NON-NLS-1$
            }
        }

        cell.setText(newText);
        cell.setImage(getImage(element));
        cell.setFont(getFont(element));
        cell.setForeground(getForeground(element));
        cell.setBackground(getBackground(element));

        // no super call required. changes on item will trigger the refresh.
    }

}
