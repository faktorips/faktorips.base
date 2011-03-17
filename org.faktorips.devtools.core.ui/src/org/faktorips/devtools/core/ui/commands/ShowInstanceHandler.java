/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.views.instanceexplorer.InstanceExplorer;

/**
 * An Handler to show a selected element in instance explorer
 * 
 * @author Cornelius Dirmeier
 */
public class ShowInstanceHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile) {
        try {
            IIpsObject ipsObject = ipsSrcFile.getIpsObject();

            if (ipsObject == null) {
                return;
            }
            if (InstanceExplorer.supports(ipsObject)) {
                try {
                    IViewPart instanceExplorer = activePage.showView(InstanceExplorer.EXTENSION_ID, null,
                            IWorkbenchPage.VIEW_ACTIVATE);
                    ((InstanceExplorer)instanceExplorer).showInstancesOf(ipsObject);
                } catch (PartInitException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        } catch (CoreException e1) {
            IpsPlugin.log(e1);
        }

    }

}
