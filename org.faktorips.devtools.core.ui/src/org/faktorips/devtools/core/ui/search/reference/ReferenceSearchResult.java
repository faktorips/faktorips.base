/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.reference;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class ReferenceSearchResult extends AbstractTextSearchResult {

    private ReferenceSearchQuery query;

    public ReferenceSearchResult(ReferenceSearchQuery query) {
        this.query = query;
    }

    @Override
    public String getLabel() {
        return "" + super.getMatchCount() + Messages.ReferenceSearchResult_label + this.query.getReferencedName(); //$NON-NLS-1$
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
        return this.query;
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
        Set<IIpsProject> projects = new HashSet<IIpsProject>();

        Object[] elements = super.getElements();
        for (Object object : elements) {
            if (object instanceof IIpsElement) {
                IIpsElement element = (IIpsElement)object;

                projects.add(element.getIpsProject());
            } else if (object instanceof Object[] && ((Object[])object)[0] instanceof IIpsElement) {
                IIpsElement element = (IIpsElement)(((Object[])object)[0]);

                projects.add(element.getIpsProject());
            }
        }
        return projects.toArray();
    }
}
