/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.fl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
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
        try {
            List<IIpsSrcFile> result = getIpsProject().findAllIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);

            for (IIpsSrcFile srcFile : result) {
                createTableData(srcFile, tableDatas);
            }
            return tableDatas;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void createTableData(IIpsSrcFile srcFile, List<TableData> tableDatas) throws CoreException {
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
