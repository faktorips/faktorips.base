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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.fl.FlFunction;
import org.faktorips.fl.FunctionResolver;

/**
 * A {@link FunctionResolver} for {@link TableContents}, which based on a single content table
 * structure.
 * 
 * @author dicker
 */
public class TableSingleContentFunctionsResolver extends AbstractTableFunctionsResolver {

    public TableSingleContentFunctionsResolver(IIpsProject ipsProject) {
        super(ipsProject);
    }

    @Override
    protected List<TableData> createTableDatas() {
        List<TableData> tableDatas = new ArrayList<TableData>();

        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        try {
            getIpsProject().findAllIpsSrcFiles(result, new IpsObjectType[] { IpsObjectType.TABLE_STRUCTURE });

            for (IIpsSrcFile srcFile : result) {
                createTableData(srcFile, tableDatas);
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return tableDatas;
    }

    private void createTableData(IIpsSrcFile srcFile, List<TableData> tableDatas) throws CoreException {
        ITableStructure structure = (ITableStructure)srcFile.getIpsObject();
        if (TableStructureType.SINGLE_CONTENT != structure.getTableStructureType()) {
            return;
        }
        List<ITableContents> tableContents = new ArrayList<ITableContents>();

        getIpsProject().findTableContents(structure, tableContents);

        if (tableContents.isEmpty()) {
            return;
        }

        ITableContents content = tableContents.get(0);
        String referencedName = structure.getQualifiedName();

        tableDatas.add(new TableData(content, structure, referencedName));

    }

    /**
     * Returns a new table function adapter.
     */
    @Override
    protected FlFunction createFlFunction(ITableContents tableContents,
            ITableAccessFunction function,
            String referencedName) {

        return new TableStructureReferenceFunctionFlFunctionAdapter(tableContents, function, referencedName,
                getIpsProject());
    }
}
