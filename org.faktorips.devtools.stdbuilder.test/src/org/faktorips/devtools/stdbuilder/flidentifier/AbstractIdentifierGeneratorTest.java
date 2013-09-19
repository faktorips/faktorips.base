/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

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
public class AbstractIdentifierGeneratorTest {
    @Mock
    AbstractIdentifierGenerator generator;
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
