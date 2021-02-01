/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StdBuilderIdentifierNodeGeneratorTest {
    @Mock
    StdBuilderIdentifierNodeGenerator generator;
    @Mock
    CompilationResult<JavaCodeFragment> compilationResult;

    Datatype elementDatatype = new IntegerDatatype();
    Datatype listDatatype = new ListOfTypeDatatype(elementDatatype);

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        when(generator.isListDatatypeContext(any(CompilationResult.class))).thenCallRealMethod();
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
