/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.views.ipshierarchy.IpsHierarchyView;

/**
 * ShowHierarchyHandler is a defaultHandler for the command id:
 * org.faktorips.devtools.core.ui.actions.showHierarchy in plugin.xml Extensions
 * org.eclipse.ui.commands Opens or updates IpsHierarchyView
 * 
 * @author stoll
 */
public class ShowHierarchyHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile) {
        try {
            IIpsObject ipsObject = ipsSrcFile.getIpsObject();
            if (IpsHierarchyView.supports(ipsObject)) {
                try {
                    IViewPart hierarchyView = activePage.showView(IpsHierarchyView.EXTENSION_ID, null,
                            IWorkbenchPage.VIEW_ACTIVATE);
                    ((IpsHierarchyView)hierarchyView).showHierarchy(ipsObject);
                } catch (PartInitException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

    }
}
