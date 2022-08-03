/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.reference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * Abstract class for ReferenceSearches in the Faktor-IPS object model. Subclasses are used with the
 * eclipse search ui:
 * 
 * <pre>
 * <code>
 * NewSearchUI.activateSearchResultView();
 * NewSearchUI.runQueryInBackground(ISearchQuery query);
 * </code>
 * </pre>
 * 
 * Subclasses must implement the abstract methods <code>findReferences()</code> and
 * <code>getDataForResult(IIpsElement object)</code>.
 * 
 * @author Stefan Widmaier
 */
public abstract class ReferenceSearchQuery implements ISearchQuery {

    protected ReferenceSearchResult result;
    protected IIpsObject referenced;

    public ReferenceSearchQuery(IIpsObject referenced) {
        this.referenced = referenced;
        result = new ReferenceSearchResult(this);
    }

    @Override
    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
        monitor.beginTask(getLabel(), 2);
        result.removeAll();
        try {
            IIpsElement[] found = findReferences();

            monitor.worked(1);
            addFoundMatches(found);
        } catch (IpsException e) {
            return new IpsStatus(e);
        }
        monitor.done();
        return new IpsStatus(IStatus.OK, 0, Messages.ReferenceSearchQuery_ok, null);
    }

    /**
     * Adds all given elements as result match to the result list. Subclasses can overwrite this
     * method to perform special operations before adding the found element to the result list (e.g.
     * combine the found elements to one match with multiple children)
     */
    protected void addFoundMatches(IIpsElement[] found) {
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
     * search in their object model, exception handling, progress monitors etc. are handled by the
     * abstract superclass. If no objects are found an empty array is returned. This method throws a
     * CoreException which is handled by the calling run() method.
     */
    protected abstract IIpsElement[] findReferences() throws IpsException;

    /**
     * Template method to be implemented by subclasses. This method is called for each IIpsElement
     * found by this query. It creates an object-array to be used as data/input for the
     * corresponding search-result for the found IIpsElement. By convention the returned object[]
     * contains at least one element, which is the IIpsElement itself. All following objects
     * represent its children. This method throws a CoreException which is handled by the calling
     * run() method.
     */
    protected abstract Object[] getDataForResult(IIpsElement object) throws IpsException;

    @Override
    public String getLabel() {
        return NLS.bind(Messages.ReferenceSearchQuery_labelPrefix, referenced.getName());
    }

    public String getReferencedName() {
        return referenced.getName();
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
        return result;
    }

}
