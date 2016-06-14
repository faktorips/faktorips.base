/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class SearchFilter extends ViewerFilter {
    private String searchText;

    public void setSearchText(String s) {
        // ensure that the value can be used for matching
        this.searchText = s;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (searchText == null || searchText.length() == 0) {
            return true;
        }
        return element.toString().contains(searchText);
    }
}
