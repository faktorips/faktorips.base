package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.search.ReferencesToPolicySearchQuery;

/**
 * Action for finding references to a given PolicyComponentType.
 * @author widmaier
 */
public class FindPolicyReferencesAction extends Action {

	/**
	 * The selection provider to get the selection from if requested to run.
	 */
	private ISelectionProvider selectionProvider;
	
	/**
	 * Creates a new action to find references to a policy component type. 
	 * The given selection provider is used to retrieve the policy component 
	 * type to be searched for.
	 * <p>
	 * Note: Only <code>IStructuredSelection</code>s are supported.
	 */
    public FindPolicyReferencesAction(ISelectionProvider selectionProvider) {
        super();
        this.selectionProvider = selectionProvider;
        this.setDescription(Messages.FindPolicyReferencesAction_description);
        this.setText(Messages.FindPolicyReferencesAction_name);
        this.setToolTipText(this.getDescription());
    }
    
    /**
     * Executes a query if the current selection provides a PolicyCmptType
     * to search for.
     */
    public void run() {
        ISelection sel = selectionProvider.getSelection();
		if (!(sel instanceof IStructuredSelection)) {
			// we dont support simple selection
			return;
		}
		Object selected = ((IStructuredSelection) sel).getFirstElement();
		if(selected!=null){
			if(selected instanceof IPolicyCmptType){
				IPolicyCmptType referenced = (IPolicyCmptType) selected;
				NewSearchUI.activateSearchResultView();
				NewSearchUI.runQueryInBackground(new ReferencesToPolicySearchQuery(referenced));
			}
		}else{
			return;
		}
		
    }
}
