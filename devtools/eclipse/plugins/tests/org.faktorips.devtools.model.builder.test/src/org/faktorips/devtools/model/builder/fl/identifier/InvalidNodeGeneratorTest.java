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

import static org.faktorips.abstracttest.MockUtil.createMocks;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class InvalidNodeGeneratorTest {


    private static final String TEXT = "text";

    private static final Message MESSAGE = new Message("Code", TEXT, Message.ERROR);

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private JavaBuilderSet builderSet;

    private MockitoSession mockito;

    private InvalidIdentifierNode invalidNode;

    private InvalidNodeGenerator generator;

    @BeforeEach
    public void setUp() throws Exception {
        mockito = createMocks(this);
        generator = new InvalidNodeGenerator(factory, builderSet);
        IdentifierNodeFactory nodeFactory = new IdentifierNodeFactory(new TextRegion("anyIdentifierPart", 0,
                "anyIdentifierPart".length()), ipsProject);
        invalidNode = nodeFactory.createInvalidIdentifier(MESSAGE);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testGenerateNode_InvalidNode() {
        CompilationResult<JavaCodeFragment> compilationResult = generator.generateNode(invalidNode, null);

        assertTrue(compilationResult.failed());
        assertEquals(MESSAGE, compilationResult.getMessages().getFirstMessage(Message.ERROR));
    }
}
