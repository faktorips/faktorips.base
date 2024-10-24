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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TableUsageAnnGenTest {

    @Mock
    private XTableUsage tableUsage;

    private TableUsageAnnGen generator = new TableUsageAnnGen();

    @Test
    public void testCreateTableUsageAnnotation_withNameOnly() {
        when(tableUsage.getName()).thenReturn("TestTableName");

        JavaCodeFragment annotationCode = generator.createAnnotation(tableUsage);

        assertThat(annotationCode.getSourcecode(),
                is("@IpsTableUsage(name = \"TestTableName\")" + System.lineSeparator()));
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsTableUsage"));
    }

    @Test
    public void testCreateTableUsageAnnotation_withOneTableClass() {
        when(tableUsage.getName()).thenReturn("TestTableName");
        when(tableUsage.getAllTableClassNames()).thenReturn(Collections.singletonList("TableClass1"));

        JavaCodeFragment annotationCode = generator.createAnnotation(tableUsage);

        assertThat(annotationCode.getSourcecode(),
                is("@IpsTableUsage(name = \"TestTableName\", tableClasses = TableClass1.class)"
                        + System.lineSeparator()));
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsTableUsage"));
    }

    @Test
    public void testCreateTableUsageAnnotation_withMultipleTableClasses() {
        when(tableUsage.getName()).thenReturn("TestTableName");
        when(tableUsage.getAllTableClassNames()).thenReturn(Arrays.asList("TableClass1", "TableClass2", "TableClass3"));

        JavaCodeFragment annotationCode = generator.createAnnotation(tableUsage);

        assertThat(annotationCode.getSourcecode(), is(
                "@IpsTableUsage(name = \"TestTableName\", tableClasses = {TableClass1.class, TableClass2.class, TableClass3.class})"
                        + System.lineSeparator()));
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsTableUsage"));
    }
}
