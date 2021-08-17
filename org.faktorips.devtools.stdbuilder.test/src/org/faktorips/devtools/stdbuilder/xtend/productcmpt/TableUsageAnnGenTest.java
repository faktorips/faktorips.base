/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XTableUsage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableUsageAnnGenTest {

    @Mock
    private XTableUsage tableUsage;

    private TableUsageAnnGen generator = new TableUsageAnnGen();

    @Test
    public void testCreateTableUsageAnnotation() {
        when(tableUsage.getName()).thenReturn("TestTableName");

        JavaCodeFragment annotationCode = generator.createAnnotation(tableUsage);

        assertEquals("@IpsTableUsage(name = \"TestTableName\")" + System.lineSeparator(),
                annotationCode.getSourcecode());
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsTableUsage"));
    }
}
