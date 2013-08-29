/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
