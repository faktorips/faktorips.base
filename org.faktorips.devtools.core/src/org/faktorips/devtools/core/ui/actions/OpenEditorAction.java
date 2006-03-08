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
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;

/**
 * Open selected product component in editor.
 * 
 * @author Thorsten Guenther
 */
public class OpenEditorAction extends Action {

	ISelectionProvider selectionProvider;
    
    public OpenEditorAction(ISelectionProvider selectionProvider) {
        super();
        super.setText(Messages.OpenEditorAction_name);
        super.setDescription(Messages.OpenEditorAction_description);
        super.setToolTipText(Messages.OpenEditorAction_tooltip);
        this.selectionProvider = selectionProvider;
    }
    
    public void run() {
        try {
            IIpsSrcFile file = getIpsSrcFileForSelection();

            if (file != null) {
                IpsPlugin.getDefault().openEditor(file);
            }
            
        } catch (PartInitException e2) {
            IpsPlugin.logAndShowErrorDialog(e2);
        }
    }
    
    private IIpsSrcFile getIpsSrcFileForSelection() {
        if (this.selectionProvider != null) {
        	ISelection selection = this.selectionProvider.getSelection();
        	if (selection instanceof IStructuredSelection) {
        		Object selected = ((IStructuredSelection)selection).getFirstElement();
        		if (selected instanceof IIpsObject) {
        			return ((IIpsObject)selected).getIpsSrcFile();
        		}
        	}
        }
        return null;
    }

}
