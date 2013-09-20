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

package org.faktorips.devtools.core.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.CompilationResult;
import org.faktorips.util.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdentifierNodeGeneratorTest {

    @Mock
    IdentifierNodeGenerator<JavaCodeFragment> generator;

    @Mock
    IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IdentifierNode identifierNode;

    private InvalidIdentifierNode invalidNode;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        IdentifierNodeFactory nodeFactory = new IdentifierNodeFactory("anyIdentifierPart", ipsProject);
        invalidNode = nodeFactory.createInvalidIdentifier(new Message("Code", "text", 0));
        when(generator.generateNode(any(IdentifierNode.class), any(CompilationResult.class))).thenCallRealMethod();
        when(generator.getGeneratorFor(any(IdentifierNode.class))).thenCallRealMethod();

    }

    @Test
    public void testGenerateNode_InvalidNode() {
        generator.generateNode(invalidNode, null);

        verify(generator).getErrorCompilationResult(invalidNode);
    }

    @Test
    public void testGenerateNode_AttributeNode() {
        @SuppressWarnings("unchecked")
        CompilationResult<JavaCodeFragment> compilationResult = mock(CompilationResult.class);
        generator.generateNode(identifierNode, compilationResult);

        verify(generator).getCompilationResultForCurrentNode(identifierNode, compilationResult);
    }

    @Test
    public void testGetGeneratorFor() {
        when(generator.getNodeGeneratorFactory()).thenReturn(factory);
        @SuppressWarnings("unchecked")
        IdentifierNodeGenerator<JavaCodeFragment> identtifierNodeBuilder = mock(IdentifierNodeGenerator.class);
        when(factory.getGeneratorForInvalidNode()).thenReturn(identtifierNodeBuilder);

        IdentifierNodeGenerator<JavaCodeFragment> builderFor = generator.getGeneratorFor(invalidNode);
        assertEquals(identtifierNodeBuilder, builderFor);
    }
}
