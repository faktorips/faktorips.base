/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.SearchPattern;

public class TypeSelectionFilter extends ViewerFilter {

    private String searchText = StringUtils.EMPTY;

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if ((searchText == null || searchText.isEmpty())) {
            return true;
        } else {
            StructuredViewer structuredViewer = (StructuredViewer)viewer;
            String labelText = ((ILabelProvider)structuredViewer.getLabelProvider()).getText(element);
            return inputMatches(labelText);
        }
    }

    private boolean inputMatches(String labelText) {
        SearchPattern searchPattern = new SearchPattern(SearchPattern.RULE_CAMELCASE_MATCH
                | SearchPattern.RULE_BLANK_MATCH | SearchPattern.RULE_PREFIX_MATCH | SearchPattern.RULE_PATTERN_MATCH);
        searchPattern.setPattern(searchText);
        return searchPattern.matches(labelText);
    }

}