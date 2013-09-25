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
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.IndexNode;
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
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
    Association association;

    @Mock
    private IType target;

    private IndexNodeGenerator indexNodeGenerator;

    private IndexNode indexNode;

    private IdentifierNodeFactory nodeFactory;

    @Before
    public void createIndexBasedAssociationNodeGenerator() throws Exception {
        nodeFactory = new IdentifierNodeFactory("IndexBasedAssociationNodeGeneratorTest", ipsProject);
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
