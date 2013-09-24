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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;
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
    private IdentifierParser parser;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractIdentifierResolver<JavaCodeFragment> abstractIdentifierResolver;

    @Mock
    private IIpsProject ipsProject;

    @Test
    public void testParseIdentifier() throws Exception {
        when(abstractIdentifierResolver.getParser()).thenReturn(parser);

        abstractIdentifierResolver.parseIdentifier(ANY_IDENTIFIER_XYZ);

        verify(parser).parse(ANY_IDENTIFIER_XYZ);
    }

    @Test
    public void testCompile() throws Exception {
        final CompilationResultImpl expectedResult = new CompilationResultImpl();
        when(abstractIdentifierResolver.getParser()).thenReturn(parser);
        InvalidIdentifierNode node = new IdentifierNodeFactory(ANY_IDENTIFIER_XYZ, ipsProject)
                .createInvalidIdentifier(Message.newError("code", "text"));
        when(parser.parse(ANY_IDENTIFIER_XYZ)).thenReturn(node);
        doReturn(identifierNodeGeneratorFactory).when(abstractIdentifierResolver).getGeneratorFactory();
        doReturn(new CompilationResultImpl()).when(abstractIdentifierResolver).getStartingCompilationResult();
        when(identifierNodeGeneratorFactory.getGeneratorForInvalidNode()).thenReturn(
                new IdentifierNodeGenerator<JavaCodeFragment>(identifierNodeGeneratorFactory) {

                    @Override
                    protected CompilationResult<JavaCodeFragment> getCompilationResultForCurrentNode(IdentifierNode identifierNode,
                            CompilationResult<JavaCodeFragment> contextCompilationResult) {
                        return expectedResult;
                    }
                });

        CompilationResult<JavaCodeFragment> compilationResult = abstractIdentifierResolver.compile(ANY_IDENTIFIER_XYZ,
                exprCompiler, Locale.GERMAN);

        assertSame(expectedResult, compilationResult);
    }
}
