/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.tablecontents.Row;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.SearchFilter;

public class TableSearchFilter extends SearchFilter {
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

        TableContents tableContents = (TableContents)parentElement;

        Row row = (Row)element;
        IIpsProject project = tableContents.getIpsProject();
        for (int i = 0; i < row.getNoOfColumns(); i++) {
            String value = row.getValue(i);
            if (value != null) {
                value = IpsUIPlugin.getDefault().getInputFormat(getValueDatatype(tableContents, i, project), project)
                        .format(value);
            }
            if (StringUtils.containsIgnoreCase(value, searchText)) {
                return true;
            }
        }
        return false;
    }

    private ValueDatatype getValueDatatype(TableContents tableContents, int i, IIpsProject project) {
        try {
            return tableContents.findTableStructure(project).getColumn(i).findValueDatatype(project);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }
}
