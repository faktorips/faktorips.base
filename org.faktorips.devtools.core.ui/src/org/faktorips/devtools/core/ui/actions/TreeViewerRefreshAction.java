/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jdt.ui.actions.RefreshAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Action for refreshButton in the toolbar of a TreeViewer. Used in <code>ModelExplorer</code>,
 * <code>ProductDefinitionExplorer</code> and <code>ProductStructureExplorer</code>.
 * @author Stefan Widmaier
 */
public class TreeViewerRefreshAction extends RefreshAction {
    public TreeViewerRefreshAction(IWorkbenchPartSite site) {
        super(site);
    }
    
    /**
     * Returns the imagedescriptor for the standard refresh image.
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor() {
        return IpsUIPlugin.getDefault().getImageDescriptor("Refresh.gif"); //$NON-NLS-1$
    }
    
    /**
     * Returns the tooltiptext ("refresh") for this action.
     * {@inheritDoc}
     */
    public String getToolTipText() {
        return Messages.TreeViewerRefreshAction_TooltipText;
    }
}
