/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.fl;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;
import org.faktorips.util.ArgumentCheck;

/**
 * This class is an abstract implementation of a {@link FunctionResolver} for tables. We use a cache
 * for the functions because there always the same. If there is a model-update then the hole
 * resolver is new.
 * 
 */
public abstract class AbstractTableFunctionsResolver implements FunctionResolver<JavaCodeFragment> {

    private final IIpsProject ipsProject;
    private List<FlFunction<JavaCodeFragment>> flfunctions;

    public AbstractTableFunctionsResolver(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        this.ipsProject = ipsProject;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    public FlFunction<JavaCodeFragment>[] getFunctions() {
        if (flfunctions == null) {
            List<TableData> tableDatas = createTableDatas();
            flfunctions = getFlFunctionsFor(tableDatas);
        }
        @SuppressWarnings("unchecked")
        FlFunction<JavaCodeFragment>[] functions = new FlFunction[flfunctions.size()];
        return flfunctions.toArray(functions);
    }

    private List<FlFunction<JavaCodeFragment>> getFlFunctionsFor(List<TableData> tableDatas) {
        List<FlFunction<JavaCodeFragment>> functions = new ArrayList<>();
        for (TableData tableData : tableDatas) {
            functions.addAll(getTableAccessFunctionsFor(tableData));
        }
        return functions;
    }

    /**
     * returns the {@link TableData}s for this resolver
     */
    protected abstract List<TableData> createTableDatas();

    private List<FlFunction<JavaCodeFragment>> getTableAccessFunctionsFor(TableData tableData) {
        ITableAccessFunction[] fcts = tableData.getTableStructure().getAccessFunctions();
        List<FlFunction<JavaCodeFragment>> functions = new ArrayList<>();
        for (ITableAccessFunction tableAccessFunction : fcts) {
            functions.add(createFlFunction(tableAccessFunction, tableData));
        }
        return functions;
    }

    /**
     * returns a {@link FlFunction} representing a function to call the given
     * {@link ITableAccessFunction} of the given {@link ITableContents} and the referencedName of
     * the table contents.
     */
    protected abstract FlFunction<JavaCodeFragment> createFlFunction(ITableAccessFunction iTableAccessFunction,
            TableData tableData);

    /**
     * Immutual Parameter-Object to store a table content, its structure and the referencedName,
     * which will be used within a formula.
     * 
     * @author dicker
     */
    protected static final class TableData {
        private final String tableContentQualifiedName;
        private final ITableStructure tableStructure;
        private final String referencedName;

        public TableData(String tableContentQualifiedName, ITableStructure tableStructure, String referencedName) {
            this.tableStructure = tableStructure;
            this.tableContentQualifiedName = tableContentQualifiedName;
            this.referencedName = referencedName;
        }

        public String getReferencedName() {
            return referencedName;
        }

        public String getTableContentQualifiedName() {
            return tableContentQualifiedName;
        }

        public ITableStructure getTableStructure() {
            return tableStructure;
        }
    }
}
