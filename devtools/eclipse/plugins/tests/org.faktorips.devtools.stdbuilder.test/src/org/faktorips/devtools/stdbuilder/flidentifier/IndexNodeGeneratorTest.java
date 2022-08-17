/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IndexNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexNodeGeneratorTest {

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private CompilationResult<JavaCodeFragment> contextCompilationResult;

    @Mock
    IAssociation association;

    @Mock
    private IType target;

    private IndexNodeGenerator indexNodeGenerator;

    private IndexNode indexNode;

    private IdentifierNodeFactory nodeFactory;

    @Before
    public void createIndexBasedAssociationNodeGenerator() throws Exception {
        nodeFactory = new IdentifierNodeFactory(new TextRegion("IndexBasedAssociationNodeGeneratorTest", 0,
                "IndexBasedAssociationNodeGeneratorTest".length()), ipsProject);
        indexNodeGenerator = new IndexNodeGenerator(factory, builderSet);
    }

    private IndexNode createIndexNode(int index) throws Exception {
        when(association.findTarget(any(IIpsProject.class))).thenReturn(target);
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
