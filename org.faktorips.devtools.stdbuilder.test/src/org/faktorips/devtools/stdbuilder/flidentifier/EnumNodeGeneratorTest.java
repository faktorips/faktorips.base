/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.TextRegion;
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

    private static final String ENUM_VALUE_NAME = "EnumValueName";

    private static final String ENUM_DATATYPE_NAME = "EnumDatatypeName";

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ExtendedExprCompiler exprCompiler;

    @Mock
    private DatatypeHelper helper;

    @Mock
    private EnumTypeDatatypeAdapter enumDatatype;

    @Mock
    private EnumDatatype enumDataType;

    @Mock
    private EnumTypeBuilder enumTypeBuilder;

    private EnumNodeGenerator enumNodeGenerator;

    private EnumValueNode enumValueNode;

    @Before
    public void createEnumValueNodeJavaBuilder() throws Exception {
        enumNodeGenerator = new EnumNodeGenerator(factory, builderSet, exprCompiler);
    }

    @Test
    public void testGetCompilationResultForEnumTypeDatatypeAdapter() throws Exception {
        when(enumDatatype.getName()).thenReturn(ENUM_DATATYPE_NAME);
        enumValueNode = new IdentifierNodeFactory(new TextRegion(enumDatatype.getName(), 0, enumDatatype.getName()
                .length()), ipsProject).createEnumValueNode(ENUM_VALUE_NAME, enumDatatype);
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment(enumValueNode.getEnumValueName());
        when(enumNodeGenerator.getEnumTypeBuilder()).thenReturn(enumTypeBuilder);
        when(enumTypeBuilder.getNewInstanceCodeFragement(enumDatatype, enumValueNode.getEnumValueName())).thenReturn(
                javaCodeFragment);

        CompilationResult<JavaCodeFragment> compilationResult = enumNodeGenerator.getCompilationResultForCurrentNode(
                enumValueNode, null);

        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult);
        assertNotNull(compilationResult.getCodeFragment());
        assertEquals(ENUM_VALUE_NAME, compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResultForEnumDatatype() throws Exception {
        when(enumDataType.getName()).thenReturn(ENUM_DATATYPE_NAME);
        enumValueNode = new IdentifierNodeFactory(new TextRegion(enumDataType.getName(), 0, enumDataType.getName()
                .length()), ipsProject).createEnumValueNode(ENUM_VALUE_NAME, enumDataType);
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment();
        when(enumNodeGenerator.getIpsProject()).thenReturn(ipsProject);
        when(enumNodeGenerator.getIpsProject().getDatatypeHelper(enumDataType)).thenReturn(helper);
        when(helper.newInstance(enumValueNode.getEnumValueName())).thenReturn(
                javaCodeFragment.append(enumValueNode.getEnumValueName()));

        CompilationResult<JavaCodeFragment> compilationResult = enumNodeGenerator.getCompilationResultForCurrentNode(
                enumValueNode, null);

        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult);
        assertNotNull(compilationResult.getCodeFragment());
        assertEquals(ENUM_VALUE_NAME, compilationResult.getCodeFragment().getSourcecode());
    }
}
