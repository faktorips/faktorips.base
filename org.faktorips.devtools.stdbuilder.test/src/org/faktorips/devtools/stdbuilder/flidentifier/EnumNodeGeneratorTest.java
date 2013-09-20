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

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumClassNode.EnumClass;
import org.faktorips.devtools.core.builder.flidentifier.ast.EnumValueNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
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

    @Mock
    private DatatypeHelper helper;

    private EnumNodeGenerator enumNodeJavaBuilder;

    private EnumClassNode enumClassNode;

    private EnumValueNode enumValueNode;

    @Before
    public void createEnumValueNodeJavaBuilder() throws Exception {
        enumNodeJavaBuilder = new EnumNodeGenerator(factory, builderSet);
    }

    private void setUpEnumValueNode() throws Exception {
        EnumDatatype enumDatatype = mock(EnumTypeDatatypeAdapter.class);
        EnumClass enumClass = new EnumClass(enumDatatype);
        enumClassNode = (EnumClassNode)new IdentifierNodeFactory(enumDatatype.getName(), ipsProject)
                .createEnumClassNode(enumClass);
        enumValueNode = (EnumValueNode)new IdentifierNodeFactory(enumDatatype.getName(), ipsProject)
                .createEnumValueNode("EnumValueName", Datatype.STRING);
        enumClassNode.setSuccessor(enumValueNode);
    }

    @Test
    public void testGetCompilationResult() throws Exception {
        setUpEnumValueNode();
        CompilationResult<JavaCodeFragment> compilationResult = enumNodeJavaBuilder.getCompilationResultForCurrentNode(
                enumClassNode, null);

        assertFalse(compilationResult.failed());
        // assertNotNull(compilationResult);
        // assertNotNull(compilationResult.getCodeFragment());
        // assertEquals("EnumValueName", compilationResult.getCodeFragment().getSourcecode());
        // assertEquals(EnumDatatype.STRING, compilationResult.getDatatype());
    }
}
