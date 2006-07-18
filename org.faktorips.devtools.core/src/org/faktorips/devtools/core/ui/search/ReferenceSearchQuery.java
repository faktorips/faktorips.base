package org.faktorips.devtools.core.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
/**
 * Abstract class for ReferenceSearches in the FaktorIPS object model. 
 * Subclasses are used with the eclipse search ui:
 * <code> NewSearchUI.activateSearchResultView();
			NewSearchUI.runQueryInBackground(ISearchQuery query); </code>
 * Subclasses must implement the abstract methods <code>findReferences()</code> 
 * and <code>getDataForResult(IIpsElement object)</code>.
 * 
 * @author Stefan Widmaier
 */
public abstract class ReferenceSearchQuery implements ISearchQuery {

    protected ReferenceSearchResult result;
    protected IIpsObject referenced;
    
    public ReferenceSearchQuery(IIpsObject referenced) {
        this.referenced = referenced;
        this.result = new ReferenceSearchResult(this);
    }
    
    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
        monitor.beginTask(this.getLabel(), 2);
        result.removeAll();
        try {
        	IIpsElement[] found= findReferences();
            
            monitor.worked(1);
            Match[] resultMatches = new Match[found.length];
            for (int i = 0; i < found.length; i++) {
                Object[] combined = getDataForResult(found[i]);
                resultMatches[i] = new Match(combined, 0, 0);
            }
            result.addMatches(resultMatches);
        } catch (PartInitException e) {
            return new IpsStatus(e);
        } catch (CoreException e) {
            return new IpsStatus(e);
        }
        monitor.done();
        return new IpsStatus(IStatus.OK, 0, Messages.ReferenceSearchQuery_ok, null);
    }
    
    /**
     * Template method to be implemented by subclasses.
     * This method is called once for each search query and returns
     * all objects the query matches.
     * In this method subclasses realize the actual search in
     * their object model, Exceptionhandling, progressmonitors etc. 
     * are handled by the abstract superclass.
     * If no objects are found an empty array is returned. 
     * This method throws a CoreException which is handled by the 
     * calling run() method.
     * @throws CoreException
     */
    protected abstract IIpsElement[] findReferences() throws CoreException;
    
    /**
     * Template method to be implemented by subclasses. 
     * This method is called for each IIpsElement found by this query. 
     * It creates an object-array to be used as data/input for the corresponding 
     * search-result for the found IIpsElement.
     * By convention the returned object[] contains at least one element,
     * which is the IIpsElement itself. All following objects represent 
     * its children.
     * This method throws a CoreException which is handled by the 
     * calling run() method.
     * @throws CoreException
     */
    protected abstract Object[] getDataForResult(IIpsElement object) throws CoreException;
    

	public String getLabel() {
        return Messages.ReferenceSearchQuery_labelPrefix + this.referenced.getName(); 
    }

    public String getReferencedName() {
        return this.referenced.getName();
    }
    
    public boolean canRerun() {
        return true;
    }

    public boolean canRunInBackground() {
        return true;
    }

    public ISearchResult getSearchResult() {
        return this.result;
    }
	
}
