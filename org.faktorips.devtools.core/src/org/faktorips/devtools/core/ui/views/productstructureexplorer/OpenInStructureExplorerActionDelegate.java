/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.ui.actions.ShowStructureAction;
import org.faktorips.devtools.core.ui.views.productdefinitionexplorer.ProductExplorer;

public class OpenInStructureExplorerActionDelegate implements IWorkbenchWindowActionDelegate {

    public void dispose() {
        
    }

    public void init(IWorkbenchWindow window) {
        
    }

    public void run(IAction action) {
        IIpsSrcFile file = null;
        if (action instanceof ShowStructureAction) {
            file = ((ShowStructureAction)action).getIpsSrcFileForSelection();
        }
        
        try {
            if (file == null) {
                IViewReference[] views = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
                IViewPart pe = null;
                for (int i = 0; i < views.length; i++) {
                    if (views[i].getId().equals(ProductExplorer.EXTENSION_ID)) {
                        pe = views[i].getView(true);
                        break;
                    }
                }
                
                if (pe == null) {
                    pe = IpsPlugin.getDefault().getWorkbench().getViewRegistry().find(ProductExplorer.EXTENSION_ID).createView();
                }
                file = ((ProductExplorer)pe).getIpsSrcFileForSelection();
            }        
            
            if (file != null) {
                IViewPart pse = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ProductStructureExplorer.EXTENSION_ID);
                ((ProductStructureExplorer)pse).showStructure(file);
            }
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
     }

    public void selectionChanged(IAction action, ISelection selection) {
        
    }

 }
