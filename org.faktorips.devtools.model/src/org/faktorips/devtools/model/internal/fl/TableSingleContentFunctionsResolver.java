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
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.tablestructure.TableStructureType;
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
        List<TableData> tableDatas = new ArrayList<>();
        List<IIpsSrcFile> result = getIpsProject().findAllIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);

        for (IIpsSrcFile srcFile : result) {
            createTableData(srcFile, tableDatas);
        }
        return tableDatas;
    }

    private void createTableData(IIpsSrcFile srcFile, List<TableData> tableDatas) {
        ITableStructure structure = (ITableStructure)srcFile.getIpsObject();
        if (!isSingleContent(structure)) {
            return;
        }

        List<IIpsSrcFile> tableContentsSrcFiles = getIpsProject().findAllTableContentsSrcFiles(structure);
        if (tableContentsSrcFiles.size() != 1) {
            return;
        }
        String tableContentQName = tableContentsSrcFiles.get(0).getQualifiedNameType().getName();
        String referencedName = structure.getQualifiedName();

        tableDatas.add(new TableData(tableContentQName, structure, referencedName));
    }

    protected boolean isSingleContent(ITableStructure structure) {
        return TableStructureType.SINGLE_CONTENT == structure.getTableStructureType();
    }

    /**
     * Returns a new table function adapter.
     */
    @Override
    protected FlFunction<JavaCodeFragment> createFlFunction(ITableAccessFunction function, TableData tableData) {

        return new TableStructureReferenceFunctionFlFunctionAdapter(tableData.getTableContentQualifiedName(), function,
                tableData.getReferencedName(), getIpsProject());
    }
}
