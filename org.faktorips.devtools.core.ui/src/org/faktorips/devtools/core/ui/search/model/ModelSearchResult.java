/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class ModelSearchResult extends AbstractTextSearchResult {

    private Set<IIpsElement> matchingIpsElements = new HashSet<IIpsElement>();

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

    private final ModelSearchQuery query;

    protected ModelSearchResult(ModelSearchQuery query) {
        this.query = query;
    }

    @Override
    public Match[] getMatches(Object element) {

        return super.getMatches(element);
    }

    @Override
    public String getLabel() {
        return getMatchCount() + "Model Search: " + query.getLabel();
    }

    @Override
    public String getTooltip() {
        return "TOOLTIP";
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
    public void addMatch(Match match) {
        matchingIpsElements.add((IIpsElement)match.getElement());
        super.addMatch(match);
    }

    @Override
    public void removeMatch(Match match) {
        matchingIpsElements.remove(match);
        super.removeMatch(match);
    }

    protected Set<IIpsElement> getMatchingIpsElements() {
        return matchingIpsElements;
    }
}
