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

package org.faktorips.devtools.stdbuilder.flidentifier.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeBuilderFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumValueNodeJavaBuilderTest {

    @Mock
    private IdentifierNodeBuilderFactory<JavaCodeFragment> factory;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    private EnumValueNodeJavaBuilder enumValueNodeJavaBuilder;

    private EnumValueNode enumValueNode;

    @Before
    public void createEnumValueNodeJavaBuilder() throws Exception {
        enumValueNodeJavaBuilder = new EnumValueNodeJavaBuilder(factory, builderSet);
    }

    private void setUpEnumValueNode() throws Exception {
        EnumDatatype enumDatatype = mock(EnumDatatype.class);
        when(enumDatatype.getValueName("id")).thenReturn("EnumValueName");
        enumValueNode = new EnumValueNode(enumDatatype.getValueName("id"), Datatype.STRING);
    }

    @Test
    public void testGetCompilationResult() throws Exception {
        setUpEnumValueNode();
        CompilationResult<JavaCodeFragment> compilationResult = enumValueNodeJavaBuilder.getCompilationResult(
                enumValueNode, null);
        assertNotNull(compilationResult);
        assertNotNull(compilationResult.getCodeFragment());
        // assertEquals("EnumValueName", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }
}
