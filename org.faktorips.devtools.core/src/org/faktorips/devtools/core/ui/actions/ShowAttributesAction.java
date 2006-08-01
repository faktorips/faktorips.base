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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.ui.views.attrtable.AttributesTable;

/**
 * 
 * @author Stefan Widmaier
 */
public class ShowAttributesAction extends IpsAction {

    public ShowAttributesAction(ISelectionProvider provider) {
    	super(provider);
        this.setDescription(Messages.ShowAttributesAction_description);
        this.setText(Messages.ShowAttributesAction_name);
        this.setToolTipText(this.getDescription());
    }
    
	public void run(IStructuredSelection selection) {
		IIpsObject selected= getIpsObjectForSelection(selection);
		if(selected == null){
			return;
		}
        IPolicyCmptType pcType= null;
        if(selected instanceof IProductCmpt){
        	try {
				pcType= ((IProductCmpt)selected).findPolicyCmptType();
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
        }else if(selected instanceof IPolicyCmptType){
        	pcType= (IPolicyCmptType)selected;
        }
        if(pcType!=null){
			try {
				IViewPart attrTable = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(AttributesTable.EXTENSION_ID);
		        ((AttributesTable)attrTable).setPolicyCmptType(pcType);
			} catch (PartInitException e) {
				IpsPlugin.log(e);
			}
        }
	}
}
