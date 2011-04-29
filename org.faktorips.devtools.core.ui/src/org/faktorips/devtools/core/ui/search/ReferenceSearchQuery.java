/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * Abstract class for ReferenceSearches in the FaktorIPS object model. Subclasses are used with the
 * eclipse search ui: <code> NewSearchUI.activateSearchResultView();
			NewSearchUI.runQueryInBackground(ISearchQuery query); </code> Subclasses must
 * implement the abstract methods <code>findReferences()</code> and
 * <code>getDataForResult(IIpsElement object)</code>.
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

    @Override
    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
        monitor.beginTask(this.getLabel(), 2);
        result.removeAll();
        try {
            IIpsElement[] found = findReferences();

            monitor.worked(1);
            addFoundMatches(found);
        } catch (PartInitException e) {
            return new IpsStatus(e);
        } catch (CoreException e) {
            return new IpsStatus(e);
        }
        monitor.done();
        return new IpsStatus(IStatus.OK, 0, Messages.ReferenceSearchQuery_ok, null);
    }

    /**
     * Adds all given elements as result match to the result list. Subclasses can overwrite this
     * method to perform special operations before adding the found element to the result list (e.g.
     * combine the found elements to one match with multiple childs)
     */
    protected void addFoundMatches(IIpsElement[] found) throws CoreException {
        Match[] resultMatches = new Match[found.length];
        for (int i = 0; i < found.length; i++) {
            Object[] combined = getDataForResult(found[i]);
            resultMatches[i] = new Match(combined, 0, 0);
        }
        result.addMatches(resultMatches);
    }

    /**
     * Template method to be implemented by subclasses. This method is called once for each search
     * query and returns all objects the query matches. In this method subclasses realize the actual
     * search in their object model, Exceptionhandling, progressmonitors etc. are handled by the
     * abstract superclass. If no objects are found an empty array is returned. This method throws a
     * CoreException which is handled by the calling run() method.
     */
    protected abstract IIpsElement[] findReferences() throws CoreException;

    /**
     * Template method to be implemented by subclasses. This method is called for each IIpsElement
     * found by this query. It creates an object-array to be used as data/input for the
     * corresponding search-result for the found IIpsElement. By convention the returned object[]
     * contains at least one element, which is the IIpsElement itself. All following objects
     * represent its children. This method throws a CoreException which is handled by the calling
     * run() method.
     */
    protected abstract Object[] getDataForResult(IIpsElement object) throws CoreException;

    @Override
    public String getLabel() {
        return Messages.ReferenceSearchQuery_labelPrefix + this.referenced.getName();
    }

    public String getReferencedName() {
        return this.referenced.getName();
    }

    @Override
    public boolean canRerun() {
        return true;
    }

    @Override
    public boolean canRunInBackground() {
        return true;
    }

    @Override
    public ISearchResult getSearchResult() {
        return this.result;
    }

}
