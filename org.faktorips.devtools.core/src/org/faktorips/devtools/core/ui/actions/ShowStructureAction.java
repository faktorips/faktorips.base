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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureExplorer;

/**
 * Action for opening the StructureExplorer for a selected ProductCmpt.
 * Other types of IpsObjects are ignored.
 * This Action is instanciated with a SelectionProvider that returns 
 * the selected objects when <code>run()</code> is called later in program execution. 
 * 
 * @author Stefan Widmaier
 */
public class ShowStructureAction extends IpsAction {
	
    public ShowStructureAction(ISelectionProvider selectionProvider) {
    	super(selectionProvider);
        this.setDescription(Messages.ShowStructureAction_description);
        this.setText(Messages.ShowStructureAction_name);
        this.setToolTipText(this.getDescription());
    }
    

	public void run(IStructuredSelection selection) {
		IIpsObject ipsObject= getIpsObjectForSelection(selection);
		if(!(ipsObject instanceof IProductCmpt) || ipsObject==null){
			return;
		}
		IIpsSrcFile file= ipsObject.getIpsSrcFile();
		if (file == null) {
        	return;
        }
        try {
	        IViewPart pse = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ProductStructureExplorer.EXTENSION_ID);
	        ((ProductStructureExplorer)pse).showStructure(file);
        } catch (PartInitException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
	}
}
