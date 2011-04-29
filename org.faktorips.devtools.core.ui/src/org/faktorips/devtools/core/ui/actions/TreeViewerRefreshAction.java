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
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("Refresh.gif")); //$NON-NLS-1$)
    }

    /**
     * Returns the tooltiptext ("refresh") for this action. {@inheritDoc}
     */
    @Override
    public String getToolTipText() {
        return Messages.TreeViewerRefreshAction_TooltipText;
    }

}
