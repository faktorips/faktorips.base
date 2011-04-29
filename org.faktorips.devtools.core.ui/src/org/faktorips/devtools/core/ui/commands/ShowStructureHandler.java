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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureExplorer;

/**
 * Handler for opening the StructureExplorer for a selected ProductCmpt. Other types of IpsObjects
 * are ignored.
 * 
 * @author Stefan Widmaier
 */
public class ShowStructureHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile) {
        if (IpsObjectType.PRODUCT_CMPT.equals(ipsSrcFile.getIpsObjectType())) {

            try {
                IViewPart pse = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .showView(ProductStructureExplorer.EXTENSION_ID);
                ((ProductStructureExplorer)pse).showStructure(ipsSrcFile);
            } catch (PartInitException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            } catch (CoreException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }

    }
}
