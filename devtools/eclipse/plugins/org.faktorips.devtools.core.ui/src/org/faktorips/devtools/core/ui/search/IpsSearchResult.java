/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * The result of a model or product search.
 * <p>
 * The result is based on a {@link IIpsSearchQuery}.
 * 
 * 
 * @author dicker
 */
public class IpsSearchResult extends AbstractTextSearchResult {

    private final IIpsSearchQuery query;

    /**
     * @param query the {@link IIpsSearchQuery} the {@link IpsSearchResult} is based on.
     */
    protected IpsSearchResult(IIpsSearchQuery query) {
        this.query = query;
    }

    @Override
    public Object[] getElements() {

        Object[] elements = super.getElements();

        Set<IIpsProject> projects = new HashSet<>();

        for (Object element : elements) {

            IIpsElement part = (IIpsElement)element;

            projects.add(part.getIpsProject());
        }

        return projects.toArray();
    }

    @Override
    public String getLabel() {
        return query.getResultLabel(getMatchCount());
    }

    @Override
    public String getTooltip() {
        return query.getResultLabel(getMatchCount());
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public ISearchQuery getQuery() {
        return query;
    }

    @Override
    public IEditorMatchAdapter getEditorMatchAdapter() {
        return null;
    }

    @Override
    public IFileMatchAdapter getFileMatchAdapter() {
        return null;
    }
    
    public Object[] getIpsElements() {
        return super.getElements();
    }

}
