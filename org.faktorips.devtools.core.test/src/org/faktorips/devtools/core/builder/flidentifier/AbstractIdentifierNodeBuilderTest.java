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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.fl.CompilationResult;
import org.faktorips.util.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractIdentifierNodeBuilderTest {

    @Mock
    AbstractIdentifierNodeBuilder<JavaCodeFragment> builder;

    @Mock
    InvalidIdentifierNode invalidNode = new InvalidIdentifierNode(new Message("Code", "text", 0));

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        when(builder.buildNode(any(IdentifierNode.class), any(CompilationResult.class))).thenCallRealMethod();
    }

    @Test
    public void testBuildNode_InvalidNode() {
        builder.buildNode(invalidNode, null);

        verify(builder).getErrorCompilationResult(invalidNode);
    }
}
