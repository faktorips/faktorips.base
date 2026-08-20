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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.xmodel.XMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class FormulaAnnGenTest {


    @Mock
    private XMethod method;

    private MockitoSession mockito;

    private FormulaAnnGen generator = new FormulaAnnGen();

    @BeforeEach
    void setUp() {
        mockito = createMocks(this);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testCreateFormulaMethodAnnotation_RequiredFormula() {
        when(method.getFormularName()).thenReturn("testFormula");
        when(method.isFormulaOptional()).thenReturn(false);

        JavaCodeFragment annotationCode = generator.createAnnotation(method);

        assertEquals("@IpsFormula(name = \"testFormula\", required = true)" + System.lineSeparator(),
                annotationCode.getSourcecode());
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsFormula"));
    }

    @Test
    public void testCreateFormulaMethodAnnotation_OptionalFormula() {
        when(method.getFormularName()).thenReturn("testFormula");
        when(method.isFormulaOptional()).thenReturn(true);

        JavaCodeFragment annotationCode = generator.createAnnotation(method);

        assertEquals("@IpsFormula(name = \"testFormula\")" + System.lineSeparator(),
                annotationCode.getSourcecode());
        assertThat(annotationCode.getImportDeclaration().getImports(),
                hasItem("org.faktorips.runtime.model.annotation.IpsFormula"));
    }
}
