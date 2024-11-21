/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.productcmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XTableUsage;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TableUsageAnnGenTest {

    @Mock
    private XTableUsage tableUsage;

    @Mock
    private ITableStructureUsage tableStructureUsage;

    private TableUsageAnnGen generator = new TableUsageAnnGen();

    @Test
    public void testCreateTableUsageAnnotation_withNameOnly_OptionalTable() {
        when(tableUsage.getName()).thenReturn("TestTableName");
        when(tableUsage.getTableStructureUsage()).thenReturn(tableStructureUsage);
        when(tableStructureUsage.isMandatoryTableContent()).thenReturn(false);

        JavaCodeFragment annotationCode = generator.createAnnotation(tableUsage);

        assertThat(annotationCode.getSourcecode(),
                is("@IpsTableUsage(name = \"TestTableName\")" + System.lineSeparator()));
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsTableUsage"));
    }

    @Test
    public void testCreateTableUsageAnnotation_withOneTableClass_RequiredTable() {
        when(tableUsage.getName()).thenReturn("TestTableName");
        when(tableUsage.getAllTableClassNames()).thenReturn(Collections.singletonList("TableClass1"));
        when(tableUsage.getTableStructureUsage()).thenReturn(tableStructureUsage);
        when(tableStructureUsage.isMandatoryTableContent()).thenReturn(true);

        JavaCodeFragment annotationCode = generator.createAnnotation(tableUsage);

        assertThat(annotationCode.getSourcecode(),
                is("@IpsTableUsage(name = \"TestTableName\", required = true, tableClasses = TableClass1.class)"
                        + System.lineSeparator()));
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsTableUsage"));
    }

    @Test
    public void testCreateTableUsageAnnotation_withMultipleTableClasses_RequiredTable() {
        when(tableUsage.getName()).thenReturn("TestTableName");
        when(tableUsage.getAllTableClassNames()).thenReturn(Arrays.asList("TableClass1", "TableClass2", "TableClass3"));
        when(tableUsage.getTableStructureUsage()).thenReturn(tableStructureUsage);
        when(tableStructureUsage.isMandatoryTableContent()).thenReturn(true);

        JavaCodeFragment annotationCode = generator.createAnnotation(tableUsage);

        assertThat(annotationCode.getSourcecode(), is(
                "@IpsTableUsage(name = \"TestTableName\", required = true, tableClasses = {TableClass1.class, TableClass2.class, TableClass3.class})"
                        + System.lineSeparator()));
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsTableUsage"));
    }
}
