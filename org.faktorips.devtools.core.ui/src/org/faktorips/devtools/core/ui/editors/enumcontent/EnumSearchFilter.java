/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.ui.editors.SearchFilter;

public class EnumSearchFilter extends SearchFilter {
    private String searchText;

    @Override
    public void setSearchText(String s) {
        // ensure that the value can be used for matching
        this.searchText = s;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {

        if (searchText == null || searchText.length() == 0) {
            return true;
        }
        TableViewer tabViewer = (TableViewer)viewer;
        int columnCount = tabViewer.getTable().getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            if (StringUtils.containsIgnoreCase(
                    ((ITableLabelProvider)tabViewer.getLabelProvider(i)).getColumnText(element, i), searchText)) {
                return true;
            }
        }
        return false;
    }
}
