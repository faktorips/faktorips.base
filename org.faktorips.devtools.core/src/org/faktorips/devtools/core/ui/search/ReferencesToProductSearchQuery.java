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
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

public class ReferencesToProductSearchQuery implements ISearchQuery {

    private ReferencesToProductSearchResult result;
    private IProductCmpt referenced;
    
    public ReferencesToProductSearchQuery(IProductCmpt referenced) {
        this.referenced = referenced;
        this.result = new ReferencesToProductSearchResult(this);
    }
    
    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
        monitor.beginTask(this.getLabel(), 2);
        result.removeAll();
        try {
            IProductCmptGeneration[] found = referenced.getIpsProject().findReferencingProductCmptGenerations(referenced.getQualifiedName());
            
            monitor.worked(1);
            Match[] resultMatches = new Match[found.length];
            for (int i = 0; i < found.length; i++) {
                Object[] combined = {found[i].getProductCmpt(), found[i]};
                resultMatches[i] = new Match(combined, 0, 0);
            }
            result.addMatches(resultMatches);
        } catch (PartInitException e) {
            return new IpsStatus(e);
        } catch (CoreException e) {
            return new IpsStatus(e);
        }
        monitor.done();
        return new IpsStatus(IStatus.OK, 0, Messages.ReferencesToProductSearchQuery_ok, null);
    }

    public String getLabel() {
        return Messages.ReferencesToProductSearchQuery_labelPrefix + this.referenced.getName(); 
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
