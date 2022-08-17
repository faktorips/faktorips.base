/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelstructure;

import java.util.Arrays;

import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;

public final class ModelStructureDecoratingStyledCellLabelProvider extends DecoratingStyledCellLabelProvider {

    private ModelStructureLabelProvider labelProvider;

    public ModelStructureDecoratingStyledCellLabelProvider(ModelStructureLabelProvider labelProvider,
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
