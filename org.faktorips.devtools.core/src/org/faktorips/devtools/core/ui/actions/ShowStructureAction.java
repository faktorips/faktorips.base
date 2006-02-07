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
