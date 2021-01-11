/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.fl.AbstractTableFunctionsResolver;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * A {@link FunctionResolver} for {@link ITableContentUsage} within a product component.
 * 
 * @author dicker
 */
public class TableUsageFunctionsResolver extends AbstractTableFunctionsResolver {

    private ITableContentUsage[] tableContentUsages;

    public TableUsageFunctionsResolver(IIpsProject ipsProject, ITableContentUsage[] tableContentUsages) {
        super(ipsProject);
        ArgumentCheck.notNull(tableContentUsages);
        this.tableContentUsages = tableContentUsages;
    }

    @Override
    protected List<TableData> createTableDatas() {
        List<TableData> tableData = new ArrayList<TableData>();

        for (ITableContentUsage tableContentUsage : tableContentUsages) {
            try {
                String referencedName = tableContentUsage.getStructureUsage();
                ITableContents tableContents = tableContentUsage.findTableContents(getIpsProject());
                if (tableContents == null) {
                    // ignore if the table content wasn't found (validation error)
                    continue;
                }
                ITableStructure table = tableContents.findTableStructure(getIpsProject());
                if (table != null) {
                    // only add the access-function if the content has a structure...
                    tableData.add(new TableData(tableContents.getQualifiedName(), table, referencedName));
                }
            } catch (CoreException e) {
                // if an error occurs while search for the function, the functions are not
                // provided and an error is logged.Länderspiele
                IpsPlugin.log(e);
            }
        }
        return tableData;
    }

    /**
     * Returns a new table function adapter.
     */
    @Override
    protected FlFunction<JavaCodeFragment> createFlFunction(ITableAccessFunction function, TableData tableData) {

        return new TableAccessFunctionFlFunctionAdapter(tableData.getTableContentQualifiedName(), function,
                tableData.getReferencedName(), getIpsProject());
    }
}
