/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.tablecontents;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.value.ValueTypeMismatch;
import org.faktorips.devtools.core.ui.wizards.fixcontent.AssignContentAttributesPage;
import org.faktorips.devtools.core.ui.wizards.fixcontent.DeltaFixWizardStrategy;
import org.faktorips.devtools.core.ui.wizards.fixcontent.TabularContentStrategy;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Strategy implementation of {@link TabularContentStrategy}
 * 
 * @author PBui
 */
public class FixTableContentStrategy implements TabularContentStrategy<ITableStructure, IColumn> {

    private ITableContents tableContents;

    public FixTableContentStrategy(ITableContents tableContents) {
        this.tableContents = tableContents;
    }

    @Override
    public int getContentValuesCount() {
        return tableContents.getTableRows().getNumOfRows();
    }

    @Override
    public void setContentType(String tableStructure) throws CoreException {
        tableContents.setTableStructure(tableStructure);
    }

    @Override
    public void fixAllContentAttributeValues() {
        tableContents.fixColumnReferences();
    }

    @Override
    public IIpsProject getIpsProject() {
        return tableContents.getIpsProject();
    }

    private List<IRow> getContentValues() {
        return Arrays.asList(tableContents.getTableRows().getRows());
    }

    @Override
    public ITableStructure findContentType(IIpsProject ipsProject) {
        try {
            return tableContents.findTableStructure(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public void deleteObsoleteContentAttributeValues(
            AssignContentAttributesPage<ITableStructure, IColumn> assignEnumAttributesPage) {
        // Collect all obsolete columnValues to delete.
        List<Integer> notAssignedColumns = assignEnumAttributesPage.getCurrentlyNotAssignedColumns();
        // Needs a reversed List since deleting a Column will alter the column ordering
        for (Integer columnIndexToDelete : Lists.reverse(notAssignedColumns)) {
            tableContents.deleteColumn(columnIndexToDelete - 1);
        }
    }

    @Override
    public void createNewContentAttributeValues(
            AssignContentAttributesPage<ITableStructure, IColumn> assignEnumAttributesPage) {
        ITableStructure structure;
        try {
            structure = tableContents.findTableStructure(getIpsProject());
            String defaultValue = assignEnumAttributesPage.isFillNewColumnsWithNull() ? null : IpsStringUtils.EMPTY;
            if (structure != null) {
                int[] columnOrder = assignEnumAttributesPage.getColumnOrder();
                for (int currentPosition = 0; currentPosition < columnOrder.length; currentPosition++) {
                    if (columnOrder[currentPosition] == 0) {
                        String columnName = structure.getColumn(currentPosition).getName();
                        tableContents.newColumn(defaultValue, columnName);
                    }
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

    }

    @Override
    public void moveAttributeValues(int[] columnOrdering) {
        int[] currentColumnOrdering = new int[columnOrdering.length];
        // filling currentColumnOrdering with numbers
        for (int i = 0; i < currentColumnOrdering.length; i++) {
            currentColumnOrdering[i] = i + 1;
        }
        // Using a reverse Insertion Sort to bring the columns to the right position
        for (int i = 0; i < currentColumnOrdering.length; i++) {
            if (Arrays.equals(columnOrdering, currentColumnOrdering)) {
                break;
            }
            int indexDestination = Ints.indexOf(currentColumnOrdering, columnOrdering[i]);
            // no swapping needed when the column is already at the right position
            if (i == indexDestination) {
                continue;
            }
            for (IRow currentRow : getContentValues()) {
                currentRow.swapValue(i, indexDestination);
            }
            swapValues(currentColumnOrdering, i, indexDestination);

        }
    }

    private void swapValues(int[] order, int firstIndex, int secondIndex) {
        int temp = order[firstIndex];
        order[firstIndex] = order[secondIndex];
        order[secondIndex] = temp;
    }

    @Override
    public Map<String, ValueTypeMismatch> checkAllContentAttributeValueTypeMismatch() {
        return new ConcurrentHashMap<String, ValueTypeMismatch>();
    }

    @Override
    public String getImage() {
        return "wizards/BrokenTableWizard.png"; //$NON-NLS-1$
    }

    @Override
    public DeltaFixWizardStrategy<ITableStructure, IColumn> createContentPageStrategy() {
        return new FixTableWizardStrategy(tableContents);
    }

}
