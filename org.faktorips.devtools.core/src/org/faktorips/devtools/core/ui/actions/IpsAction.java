package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Abstract base action for global actions on the ips-model.
 * 
 * @author Thorsten Guenther
 */
public abstract class IpsAction extends Action {

    /**
     * The source of objects to modify by this action.
     */
    private ISelectionProvider selectionProvider;
    
    /**
     * Creates a new action. If the action is started, the given selection-provider is asked 
     * for its selection and the modifications are done to the selection.
     * 
     * @param selectionProvider
     */
    public IpsAction(ISelectionProvider selectionProvider) {
        this.selectionProvider = selectionProvider;
    }
    
    public void run() {
        ISelection selection = this.selectionProvider.getSelection();
        if (selection instanceof IStructuredSelection) {
            run((IStructuredSelection)selection);
        }
        else {
            throw new RuntimeException("Can not handle selections of type " + selection.getClass().getName());
        }
    }
    
    abstract public void run(IStructuredSelection selection);

    /**
     * Returns the apropriate Transfer for every item in the given lists in the same order 
     * as the data is returend in getDataArray.
     */
    protected Transfer[] getTypeArray(List stringItems, List resourceItems) {
    	List resultList = new ArrayList();
    	
    	if (resourceItems.size() > 0) {
    		resultList.add(ResourceTransfer.getInstance());
    	}
    	
    	for (int i = 0; i < stringItems.size(); i ++) {
    		resultList.add(TextTransfer.getInstance());
		}

        Transfer[] result = new Transfer[resultList.size()];
        return (Transfer[])resultList.toArray(result);
    }

    /**
     * Builds the data-array for clipboard operations (copy, drag,...).
     * 
     * @param stringItems The list of strings to put to the clipboard
     * @param resourceItems The list of resources to put to the clipboard
     */
    protected Object[] getDataArray(List stringItems, List resourceItems) {
    	List result = new ArrayList();
    	if (resourceItems.size() > 0) {
    		IResource[] res = new IResource[resourceItems.size()];
    		result.add((IResource[])resourceItems.toArray(res));
    	}
    	result.addAll(stringItems);
    	return  result.toArray();
    }

}
