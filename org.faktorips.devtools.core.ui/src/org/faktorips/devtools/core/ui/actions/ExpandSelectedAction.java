/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.TypedSelection;
import org.faktorips.util.ArgumentCheck;

/**
 * Expands the selected elements in a tree viewer.
 */
public class ExpandSelectedAction extends Action {
    private static final String EXPAND_ALL_ICON = "ExpandAll.gif"; //$NON-NLS-1$

    private final AbstractTreeViewer treeViewer;

    /**
     * Creates a new instance of {@link ExpandSelectedAction}.
     * 
     * @param treeViewer the viewer to expand
     */
    public ExpandSelectedAction(final AbstractTreeViewer treeViewer) {
        super(Messages.ExpandAllAction_Selection_Description, IpsUIPlugin.getImageHandling().createImageDescriptor(
                EXPAND_ALL_ICON));

        ArgumentCheck.notNull(treeViewer);
        this.treeViewer = treeViewer;
    }

    @Override
    public void run() {
        TypedSelection<Object> selection = TypedSelection.create(Object.class, treeViewer.getSelection());
        if (selection.isValid()) {
            treeViewer.expandToLevel(selection.getElement(), AbstractTreeViewer.ALL_LEVELS);
        } else {
            treeViewer.expandAll();
        }
    }

}
