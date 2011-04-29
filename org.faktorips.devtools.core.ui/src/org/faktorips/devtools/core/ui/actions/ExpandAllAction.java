/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * Expands all elements in a tree viewer.
 */
public class ExpandAllAction extends Action {

    private static final String EXPAND_ALL_ICON = "ExpandAll.gif"; //$NON-NLS-1$

    private final AbstractTreeViewer treeViewer;

    /**
     * Creates a new instance of {@link ExpandAllAction}.
     * 
     * @param treeViewer the viewer to expand
     */
    public ExpandAllAction(final AbstractTreeViewer treeViewer) {
        super(null, IpsUIPlugin.getImageHandling().createImageDescriptor(EXPAND_ALL_ICON));

        ArgumentCheck.notNull(treeViewer);
        this.treeViewer = treeViewer;

        setToolTipText(Messages.ExpandAllAction_Description);
    }

    @Override
    public void run() {
        treeViewer.expandAll();
    }

}
