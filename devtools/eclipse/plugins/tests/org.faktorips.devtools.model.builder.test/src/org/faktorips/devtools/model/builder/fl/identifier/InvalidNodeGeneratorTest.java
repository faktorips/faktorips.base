/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.fl.identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.InvalidIdentifierNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.CompilationResult;
import org.faktorips.runtime.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InvalidNodeGeneratorTest {

    private static final String TEXT = "text";

    private static final Message MESSAGE = new Message("Code", TEXT, Message.ERROR);

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private JavaBuilderSet builderSet;

    private InvalidIdentifierNode invalidNode;

    private InvalidNodeGenerator generator;

    @BeforeEach
    public void setUp() throws Exception {
        generator = new InvalidNodeGenerator(factory, builderSet);
        IdentifierNodeFactory nodeFactory = new IdentifierNodeFactory(new TextRegion("anyIdentifierPart", 0,
                "anyIdentifierPart".length()), ipsProject);
        invalidNode = nodeFactory.createInvalidIdentifier(MESSAGE);
    }

    @Test
    public void testGenerateNode_InvalidNode() {
        CompilationResult<JavaCodeFragment> compilationResult = generator.generateNode(invalidNode, null);

        assertTrue(compilationResult.failed());
        assertEquals(MESSAGE, compilationResult.getMessages().getFirstMessage(Message.ERROR));
    }
}
