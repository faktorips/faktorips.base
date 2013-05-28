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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;
import org.faktorips.util.ArgumentCheck;

/**
 * This class is an abstract implementation of a {@link FunctionResolver} for tables.
 * 
 * @author dicker
 */
public abstract class AbstractTableFunctionsResolver implements FunctionResolver {

    private final IIpsProject ipsProject;

    public AbstractTableFunctionsResolver(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);
        this.ipsProject = ipsProject;
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    public FlFunction[] getFunctions() {
        List<FlFunction> functions = new ArrayList<FlFunction>();
        List<TableData> tableDatas = createTableDatas();

        for (TableData tableData : tableDatas) {
            addTableAccessFunction(functions, tableData);
        }

        return functions.toArray(new FlFunction[functions.size()]);
    }

    /**
     * returns the {@link TableData}s for this resolver
     */
    protected abstract List<TableData> createTableDatas();

    private void addTableAccessFunction(List<FlFunction> functions, TableData tableData) {

        ITableAccessFunction[] fcts = tableData.getTableStructure().getAccessFunctions();
        for (int j = 0; j < fcts.length; j++) {
            try {
                if (!fcts[j].validate(tableData.getTableContents().getIpsProject()).containsErrorMsg()) {
                    functions
                            .add(createFlFunction(tableData.getTableContents(), fcts[j], tableData.getReferencedName()));
                }
            } catch (CoreException e) {
                // if an error occurs while search for the function, the functions are not
                // provided and an error is logged.Länderspiele
                IpsPlugin.log(e);
            }
        }
    }

    /**
     * returns a {@link FlFunction} representing a function to call the given
     * {@link ITableAccessFunction} of the given {@link ITableContents} and the referencedName of
     * the table contents.
     */
    protected abstract FlFunction createFlFunction(ITableContents tableContents,
            ITableAccessFunction iTableAccessFunction,
            String referencedName);

    /**
     * Immutual Parameter-Object to store a table content, its structure and the referencedName,
     * which will be used within a formula.
     * 
     * @author dicker
     */
    protected static final class TableData {
        private final ITableContents tableContents;
        private final ITableStructure tableStructure;
        private final String referencedName;

        protected TableData(ITableContents tableContents, ITableStructure tableStructure, String referencedName) {
            this.tableContents = tableContents;
            this.tableStructure = tableStructure;
            this.referencedName = referencedName;
        }

        protected String getReferencedName() {
            return referencedName;
        }

        protected ITableContents getTableContents() {
            return tableContents;
        }

        protected ITableStructure getTableStructure() {
            return tableStructure;
        }
    }
}