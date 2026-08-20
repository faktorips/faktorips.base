/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.fl.identifier;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.fl.CompilationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class StdBuilderIdentifierNodeGeneratorTest {

    @Mock
    JavaBuilderIdentifierNodeGenerator generator;
    @Mock
    CompilationResult<JavaCodeFragment> compilationResult;

    private MockitoSession mockito;

    Datatype elementDatatype = new IntegerDatatype();
    Datatype listDatatype = new ListOfTypeDatatype(elementDatatype);

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        when(generator.isListDatatypeContext(any(CompilationResult.class))).thenCallRealMethod();
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testIsListDatatypeContext_List() {
        when(compilationResult.getDatatype()).thenReturn(listDatatype);

        assertTrue(generator.isListDatatypeContext(compilationResult));
    }

    @Test
    public void testIsListDatatypeContext_Element() {
        when(compilationResult.getDatatype()).thenReturn(elementDatatype);

        assertFalse(generator.isListDatatypeContext(compilationResult));
    }
}
