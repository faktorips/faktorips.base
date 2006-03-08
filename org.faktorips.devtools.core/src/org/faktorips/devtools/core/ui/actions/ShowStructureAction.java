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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.OpenInStructureExplorerActionDelegate;

public class ShowStructureAction extends Action {
	
    private OpenInStructureExplorerActionDelegate action;
    private ISelectionProvider selectionProvider;
    
    public ShowStructureAction() {
        super();
        this.setDescription(Messages.ShowStructureAction_description);
        this.setText(Messages.ShowStructureAction_name);
        this.setToolTipText(this.getDescription());
        this.action = new OpenInStructureExplorerActionDelegate();
    }
    
    public ShowStructureAction(ISelectionProvider selectionProvider) {
        this();
        this.selectionProvider = selectionProvider;
    }
    
    public void run() {
        action.run(this);
    }
    
    public IIpsSrcFile getIpsSrcFileForSelection() {
        if (this.selectionProvider != null) {
        	ISelection selection = this.selectionProvider.getSelection();
        	if (selection instanceof IStructuredSelection) {
        		Object selected = ((IStructuredSelection)selection).getFirstElement();
        		if (selected instanceof IIpsElement) {
        			return ((IIpsObject)selected).getIpsSrcFile();
        		}
        	}
        }
        return null;
    }
}
