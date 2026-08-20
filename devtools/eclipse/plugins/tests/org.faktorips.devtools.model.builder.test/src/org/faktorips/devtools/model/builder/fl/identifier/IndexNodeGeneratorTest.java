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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IndexNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.CompilationResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class IndexNodeGeneratorTest {


    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private JavaBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private CompilationResult<JavaCodeFragment> contextCompilationResult;

    @Mock
    IAssociation association;

    @Mock
    private IType target;

    private MockitoSession mockito;

    private IndexNodeGenerator indexNodeGenerator;

    private IndexNode indexNode;

    private IdentifierNodeFactory nodeFactory;

    @BeforeEach
    public void createIndexBasedAssociationNodeGenerator() throws Exception {
        mockito = createMocks(this);
        nodeFactory = new IdentifierNodeFactory(new TextRegion("IndexBasedAssociationNodeGeneratorTest", 0,
                "IndexBasedAssociationNodeGeneratorTest".length()), ipsProject);
        indexNodeGenerator = new IndexNodeGenerator(factory, builderSet);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    private IndexNode createIndexNode(int index) throws Exception {
        return (IndexNode)nodeFactory.createIndexBasedAssociationNode(index, target);
    }

    @Test
    public void testGetCompilationResult() throws Exception {
        JavaCodeFragment javaCodeFragment = spy(new JavaCodeFragment("vertrag"));
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        indexNode = createIndexNode(1);

        CompilationResult<JavaCodeFragment> compilationResult = indexNodeGenerator.getCompilationResultForCurrentNode(
                indexNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals("vertrag.get(1)", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(target, compilationResult.getDatatype());
    }

}
