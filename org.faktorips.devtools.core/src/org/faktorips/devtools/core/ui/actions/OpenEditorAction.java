package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
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
        this();
        this.selectionProvider = selectionProvider;
    }
    
    public OpenEditorAction() {
        super();
        super.setText(Messages.OpenEditorAction_name);
        super.setDescription(Messages.OpenEditorAction_description);
        super.setToolTipText(Messages.OpenEditorAction_tooltip);
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
        		if (selected instanceof IIpsElement) {
        			return ((IIpsObject)selected).getIpsSrcFile();
        		}
        	}
        }
        return null;
    }

}
