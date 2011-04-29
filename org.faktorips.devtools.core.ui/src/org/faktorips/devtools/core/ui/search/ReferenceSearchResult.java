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
    IpsElementsSearchViewPage page;

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

    public void setPage(IpsElementsSearchViewPage page) {
        this.page = page;
    }

    public void setActiveMatchedFilterFor(boolean testCaseMatchFilter, boolean productCmptMatchFilter) {
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
            } else {
                System.out.println(object.getClass() + ":" + object);
            }
        }
        return projects.toArray();
    }

    // TODO wieder rausnehmen, wenn der tree richtig gefuellt wird
    public Object[] getMatchingElements() {
        return super.getElements();
    }
}
