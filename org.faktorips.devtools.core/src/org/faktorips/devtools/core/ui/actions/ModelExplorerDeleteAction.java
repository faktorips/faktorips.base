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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.util.ArgumentCheck;

public class ModelExplorerDeleteAction extends AbstractSelectionChangedListenerAction {
    
    private DeleteResourceAction deleteAction;

    public ModelExplorerDeleteAction(ISelectionProvider provider, Shell shell) {
        super(provider);
        ArgumentCheck.notNull(shell, this);
        deleteAction = new DeleteResourceAction(shell);
        provider.addSelectionChangedListener(deleteAction);
        ISelection selection= provider.getSelection();
        if(selection instanceof IStructuredSelection){
            deleteAction.selectionChanged((IStructuredSelection) selection);
        }
    }

    protected void execute(IStructuredSelection selection) {
        if(canDelete(selection)){
            deleteAction.run();
        }
    }
    
    private boolean canDelete(IStructuredSelection selection){
        Object[] items= selection.toArray();
        boolean canDelete= true;
        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof IIpsObjectPart) {
                canDelete= false;
            }
        }
        return canDelete;
    }
    
    protected void disposeInternal(){
        getSelectionProvider().removeSelectionChangedListener(deleteAction);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isEnabled(ISelection selection) {
        if(selection instanceof IStructuredSelection){
            return canDelete((IStructuredSelection)selection);
        }
        return false;
    }
}
