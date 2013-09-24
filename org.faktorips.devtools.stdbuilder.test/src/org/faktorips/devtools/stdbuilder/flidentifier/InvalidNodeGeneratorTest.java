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
import static org.junit.Assert.assertTrue;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.fl.CompilationResult;
import org.faktorips.util.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InvalidNodeGeneratorTest {

    private static final String TEXT = "text";

    private static final Message MESSAGE = new Message("Code", TEXT, Message.ERROR);

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private StandardBuilderSet builderSet;

    private InvalidIdentifierNode invalidNode;

    private InvalidNodeGenerator generator;

    @Before
    public void setUp() throws Exception {
        generator = new InvalidNodeGenerator(factory, builderSet);
        IdentifierNodeFactory nodeFactory = new IdentifierNodeFactory("anyIdentifierPart", ipsProject);
        invalidNode = nodeFactory.createInvalidIdentifier(MESSAGE);
    }

    @Test
    public void testGenerateNode_InvalidNode() {
        CompilationResult<JavaCodeFragment> compilationResult = generator.generateNode(invalidNode, null);

        assertTrue(compilationResult.failed());
        assertEquals(MESSAGE, compilationResult.getMessages().getFirstMessage(Message.ERROR));
    }
}
