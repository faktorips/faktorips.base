/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jdt.ui.actions.RefreshAction;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Action for refreshButton in the toolbar of a TreeViewer. Used in <code>ModelExplorer</code>,
 * <code>ProductDefinitionExplorer</code> and <code>ProductStructureExplorer</code>.
 * 
 * @author Stefan Widmaier
 */
public class TreeViewerRefreshAction extends RefreshAction {

    public TreeViewerRefreshAction(IWorkbenchPartSite site) {
        super(site);
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("Refresh.gif")); //$NON-NLS-1$ )
    }

    /**
     * Returns the tooltiptext ("refresh") for this action. {@inheritDoc}
     */
    @Override
    public String getToolTipText() {
        return Messages.TreeViewerRefreshAction_TooltipText;
    }

}
