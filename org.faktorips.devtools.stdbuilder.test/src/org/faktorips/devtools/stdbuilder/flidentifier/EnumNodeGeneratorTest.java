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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.ipsproject.IClasspathContentsChangeListener;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumNodeGeneratorTest extends AbstractStdBuilderTest {

    private static final String ENUM_VALUE_NAME = "EnumValueName";
    private static final String ENUM_VALUE_NAME_LITERAL = ENUM_VALUE_NAME + "_LITERAL";
    private static final String ENUM_TYPE_NAME = "TextEnum";
    private static final String PACKAGE_NAME = "model";

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    private ExtendedExprCompiler exprCompiler;

    @Mock
    private DatatypeHelper helper;

    private IIpsProject project;

    private EnumDatatype enumDatatype;

    private EnumValueNode enumValueNode;
    private EnumNodeGenerator enumNodeGenerator;
    private IdentifierNodeFactory nodeFactory;

    @Before
    public void createEnumValueNodeJavaBuilder() throws Exception {
        project = newIpsProject();

        exprCompiler = project.newExpressionCompiler();

        enumNodeGenerator = new EnumNodeGenerator(factory, builderSet, exprCompiler);
    }

    @Test
    public void testGetCompilationResultForEnumDatatypeAdapter() throws Exception {
        IEnumType enumType = newEnumType(project, PACKAGE_NAME + '.' + ENUM_TYPE_NAME);
        IEnumAttribute identifierAttribute = newIdentifierAttribute(enumType, "id", Datatype.STRING);
        IEnumLiteralNameAttribute literalNameAttribute = enumType.newEnumLiteralNameAttribute();

        IEnumValue enumValue = enumType.newEnumValue();
        enumValue.setEnumAttributeValue(identifierAttribute, ValueFactory.createStringValue(ENUM_VALUE_NAME));
        enumValue.setEnumAttributeValue(literalNameAttribute, ValueFactory.createStringValue(ENUM_VALUE_NAME_LITERAL));

        enumDatatype = new EnumTypeDatatypeAdapter(enumType, null);
        nodeFactory = newIdentifierNodeFactory(enumDatatype.getName());
        enumValueNode = nodeFactory.createEnumValueNode(ENUM_VALUE_NAME, enumDatatype);

        CompilationResult<JavaCodeFragment> compilationResult = enumNodeGenerator.getCompilationResultForCurrentNode(
                enumValueNode, null);

        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult);
        assertNotNull(compilationResult.getCodeFragment());
        assertEquals(ENUM_TYPE_NAME + '.' + ENUM_VALUE_NAME_LITERAL, compilationResult.getCodeFragment()
                .getSourcecode());
    }

    private IdentifierNodeFactory newIdentifierNodeFactory(String name) {
        return new IdentifierNodeFactory(new TextRegion(name, 0, name.length()), ipsProject);
    }

    private IEnumAttribute newIdentifierAttribute(IEnumType enumType, String name, StringDatatype string)
            {
        IEnumAttribute identifierAttribute = enumType.newEnumAttribute();
        identifierAttribute.setName(name);
        identifierAttribute.setIdentifier(true);
        identifierAttribute.setDatatype(string.getName());
        return identifierAttribute;
    }

    @Test
    public void testGetCompilationResultForEnumDatatype() throws Exception {
        try (TestIpsModelExtensions testIpsModelExtensions = new TestIpsModelExtensions()) {
            testIpsModelExtensions.setClassLoaderProviderFactory(new TestClassLoaderProviderFactory());
            enumDatatype = newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class })[0];
            nodeFactory = newIdentifierNodeFactory(enumDatatype.getName());
            enumValueNode = nodeFactory.createEnumValueNode(ENUM_VALUE_NAME, enumDatatype);

            CompilationResult<JavaCodeFragment> compilationResult = enumNodeGenerator
                    .getCompilationResultForCurrentNode(enumValueNode, null);

            assertFalse(compilationResult.failed());
            assertNotNull(compilationResult);
            assertNotNull(compilationResult.getCodeFragment());
            assertEquals(TestEnumType.class.getSimpleName() + ".valueOf(\"" + ENUM_VALUE_NAME + "\")", compilationResult
                    .getCodeFragment().getSourcecode());
        }
    }

    private static final class TestClassLoaderProviderFactory implements IClassLoaderProviderFactory {
        @Override
        public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject, ClassLoader parent) {
            return getClassLoaderProvider(ipsProject);
        }

        @Override
        public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject) {
            return new TestClassLoaderProvider();
        }
    }

    private static final class TestClassLoaderProvider implements IClassLoaderProvider {
        @Override
        public void addClasspathChangeListener(IClasspathContentsChangeListener listener) {
            // don't care
        }

        @Override
        public void removeClasspathChangeListener(IClasspathContentsChangeListener listener) {
            // don't care
        }

        @Override
        public ClassLoader getClassLoader() {
            return EnumNodeGeneratorTest.class.getClassLoader();
        }
    }
}