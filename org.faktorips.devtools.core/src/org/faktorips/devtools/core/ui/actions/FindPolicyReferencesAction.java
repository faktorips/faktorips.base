package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.search.ReferencesToPolicySearchQuery;

/**
 * Action for finding references to a given PolicyComponentType.
 * @author widmaier
 */
public class FindPolicyReferencesAction extends IpsAction {

	/**
	 * Creates a new action to find references to a policy component type. 
	 * The given selection provider is used to retrieve the policy component 
	 * type to be searched for.
	 * <p>
	 * Note: Only <code>IStructuredSelection</code>s are supported.
	 */
    public FindPolicyReferencesAction(ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.setDescription(Messages.FindPolicyReferencesAction_description);
        this.setText(Messages.FindPolicyReferencesAction_name);
        this.setToolTipText(this.getDescription());
    }
    
    /**
     * Executes a query if the current selection provides a PolicyCmptType
     * to search for.
     */
    public void run(IStructuredSelection selection) {
		IIpsObject selected = getIpsObjectForSelection(selection);
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
