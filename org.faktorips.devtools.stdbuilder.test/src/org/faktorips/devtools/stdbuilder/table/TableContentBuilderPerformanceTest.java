/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.table;

import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Ignore;
import org.junit.Test;

public class TableContentBuilderPerformanceTest extends AbstractStdBuilderTest {

    private static final int REPETITIONS = 10;
    private static final int ROWS = 50000;
    private static final int COLUMNS = 20;

    @Ignore
    @Test
    public void testBuildBigTable() throws CoreRuntimeException {
        ITableStructure tableStructure = newTableStructure(ipsProject, "TS");
        for (int c = 0; c < COLUMNS; c++) {
            IColumn column = tableStructure.newColumn();
            column.setDatatype(Datatype.STRING.getQualifiedName());
            column.setName("C" + c);
        }
        tableStructure.getIpsSrcFile().save(true, null);
        ITableContents tableContents = newTableContents(tableStructure, "TC");
        for (int c = 0; c < COLUMNS; c++) {
            tableContents.newColumn("VD", "C" + c);
        }
        ITableRows tableRows = tableContents.newTableRows();
        for (int r = 0; r < ROWS; r++) {
            IRow row = tableRows.newRow();
            for (int c = 0; c < COLUMNS; c++) {
                row.setValue(c, "V" + r + "/" + c);
            }
        }

        long duration = 0;
        for (int i = 0; i < REPETITIONS; i++) {
            IRow row = tableRows.newRow();
            for (int c = 0; c < COLUMNS; c++) {
                row.setValue(c, "VV" + i + "/" + c);
            }
            long start = System.currentTimeMillis();
            tableContents.getIpsSrcFile().save(true, null);
            fullBuild();
            long end = System.currentTimeMillis();
            System.out.println("Build took " + (end - start) + "ms");
            duration += (end - start);
        }
        duration /= REPETITIONS;

        // The problem with performance tests on different environments: The average build should
        // take about 1.5-2 sec. But if the current performance of the build server is bad it may
        // take much longer. But it should definitely be faster than 5 sec.
        assertTrue(duration < 5000);

        long size = tableContents.getIpsSrcFile().getCorrespondingFile().getLocation().toFile().length();
        assertTrue(size < 2.9E7);
    }

}
