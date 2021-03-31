/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.CompilationResult;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.Severity;
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

    @Before
    public void setUp() throws Exception {
        IdentifierNodeFactory nodeFactory = new IdentifierNodeFactory(new TextRegion("anyIdentifierPart", 0, 17),
                ipsProject);
        invalidNode = nodeFactory.createInvalidIdentifier(new Message("Code", "text", Severity.NONE));
        @SuppressWarnings("unchecked")
        CompilationResult<JavaCodeFragment> anyCompilationResult = any(CompilationResult.class);
        when(generator.generateNode(any(IdentifierNode.class), anyCompilationResult)).thenCallRealMethod();
        when(generator.getGeneratorFor(any(IdentifierNode.class))).thenCallRealMethod();
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
