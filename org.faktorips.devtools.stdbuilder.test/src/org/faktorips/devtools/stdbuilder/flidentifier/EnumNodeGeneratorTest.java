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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.enumtype.EnumTypeBuilder;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumNodeGeneratorTest {

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    private EnumNodeGenerator enumNodeGenerator;

    private EnumClassNode enumClassNode;

    private EnumValueNode enumValueNode;

    @Before
    public void createEnumValueNodeJavaBuilder() throws Exception {
        enumNodeGenerator = new EnumNodeGenerator(factory, builderSet);
    }

    @Test
    public void testGetCompilationResultForEnumTypeDatatypeAdapter() throws Exception {
        EnumTypeDatatypeAdapter enumDatatype = mock(EnumTypeDatatypeAdapter.class);
        EnumClass enumClass = new EnumClass(enumDatatype);
        createEnumClassNodeAndEnumValueNode(enumDatatype, enumClass);
        mockForEnumTypeDatatypeAdapter(enumDatatype);
        CompilationResult<JavaCodeFragment> compilationResult = enumNodeGenerator.getCompilationResultForCurrentNode(
                enumClassNode, null);
        assertionTest(compilationResult);
    }

    @Test
    public void testGetCompilationResultForEnumDatatype() throws Exception {
        EnumDatatype enumDatatype = mock(EnumDatatype.class);
        EnumClass enumClass = new EnumClass(enumDatatype);
        createEnumClassNodeAndEnumValueNode(enumDatatype, enumClass);
        CompilationResult<JavaCodeFragment> compilationResult = enumNodeGenerator.getCompilationResultForCurrentNode(
                enumClassNode, null);
        assertionTest(compilationResult);
    }

    private void mockForEnumTypeDatatypeAdapter(EnumTypeDatatypeAdapter enumDatatype) throws CoreException {
        EnumTypeBuilder enumTypeBuilder = mock(EnumTypeBuilder.class);
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("EnumValueName");
        when(enumNodeGenerator.getEnumTypeBuilder()).thenReturn(enumTypeBuilder);
        when(enumTypeBuilder.getNewInstanceCodeFragement(enumDatatype, enumValueNode.getEnumValueName())).thenReturn(
                javaCodeFragment);
    }

    private void assertionTest(CompilationResult<JavaCodeFragment> compilationResult) {
        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult);
        assertNotNull(compilationResult.getCodeFragment());
        assertEquals("EnumValueName", compilationResult.getCodeFragment().getSourcecode());
    }

    private void createEnumClassNodeAndEnumValueNode(EnumDatatype enumDatatype, EnumClass enumClass) {
        enumClassNode = new IdentifierNodeFactory(enumDatatype.getName(), ipsProject).createEnumClassNode(enumClass);
        enumValueNode = new IdentifierNodeFactory(enumDatatype.getName(), ipsProject).createEnumValueNode(
                "EnumValueName", enumDatatype);
        enumClassNode.setSuccessor(enumValueNode);
    }
}
