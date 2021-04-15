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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractIdentifierResolverTest {
    private static final String ANY_IDENTIFIER_XYZ = "anyIdentifier.xyz";

    @Mock
    private ExprCompiler<JavaCodeFragment> exprCompiler;

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> identifierNodeGeneratorFactory;

    @Mock
    private IdentifierParser identifierParser;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractIdentifierResolver<JavaCodeFragment> abstractIdentifierResolver;

    @Mock
    private IIpsProject ipsProject;

    @Test
    public void testParseIdentifier() throws Exception {
        when(abstractIdentifierResolver.getParser()).thenReturn(identifierParser);

        abstractIdentifierResolver.parseIdentifier(ANY_IDENTIFIER_XYZ);

        verify(identifierParser).parse(ANY_IDENTIFIER_XYZ);
    }

    @Test
    public void testCompile() throws Exception {
        final CompilationResultImpl expectedResult = new CompilationResultImpl();
        when(abstractIdentifierResolver.getParser()).thenReturn(identifierParser);
        InvalidIdentifierNode node = new IdentifierNodeFactory(new TextRegion(ANY_IDENTIFIER_XYZ, 0,
                ANY_IDENTIFIER_XYZ.length()), ipsProject).createInvalidIdentifier(Message.newError("code", "text"));
        when(identifierParser.parse(ANY_IDENTIFIER_XYZ)).thenReturn(node);
        doReturn(identifierNodeGeneratorFactory).when(abstractIdentifierResolver).getGeneratorFactory();
        doReturn(new CompilationResultImpl()).when(abstractIdentifierResolver).getStartingCompilationResult();
        when(identifierNodeGeneratorFactory.getGeneratorForInvalidNode()).thenReturn(
                new IdentifierNodeGenerator<>(identifierNodeGeneratorFactory) {

                    @Override
                    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(
                            IdentifierNode identifierNode,
                            CompilationResult<JavaCodeFragment> contextCompilationResult) {
                        return expectedResult;
                    }
                });

        CompilationResult<JavaCodeFragment> compilationResult = abstractIdentifierResolver.compile(ANY_IDENTIFIER_XYZ,
                exprCompiler, Locale.GERMAN);

        assertSame(expectedResult, compilationResult);
    }

}
