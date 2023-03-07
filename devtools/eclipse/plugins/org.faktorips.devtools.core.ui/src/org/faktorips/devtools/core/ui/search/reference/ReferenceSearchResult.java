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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class ReferenceSearchResult extends AbstractTextSearchResult {

    private ReferenceSearchQuery query;

    public ReferenceSearchResult(ReferenceSearchQuery query) {
        this.query = query;
    }

    @Override
    public String getLabel() {
        return "" + super.getMatchCount() + Messages.ReferenceSearchResult_label + query.getReferencedName(); //$NON-NLS-1$
    }

    @Override
    public String getTooltip() {
        return null;
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

    @Override
    public Object[] getElements() {
        Set<IIpsProject> projects = new HashSet<>();

        Object[] elements = super.getElements();
        for (Object object : elements) {
            if (object instanceof IIpsElement element) {
                projects.add(element.getIpsProject());
            } else if (object instanceof Object[] && ((Object[])object)[0] instanceof IIpsElement) {
                IIpsElement element = (IIpsElement)(((Object[])object)[0]);

                projects.add(element.getIpsProject());
            }
        }
        return projects.toArray();
    }

    public Object[] getAllElements() {
        return super.getElements();
    }

}
