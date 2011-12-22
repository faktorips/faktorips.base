/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
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

        Set<IIpsProject> projects = new HashSet<IIpsProject>();

        for (int i = 0; i < elements.length; i++) {

            IIpsElement part = (IIpsElement)elements[i];

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
}
