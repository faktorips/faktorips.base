/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.runtime.internal.IpsStringUtils;

public class TypeSelectionFilter extends ViewerFilter {

    private String searchText = IpsStringUtils.EMPTY;

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
