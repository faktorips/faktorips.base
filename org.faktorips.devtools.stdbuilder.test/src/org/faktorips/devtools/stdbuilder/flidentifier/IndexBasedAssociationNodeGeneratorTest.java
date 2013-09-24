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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.IndexBasedAssociationNode;
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexBasedAssociationNodeGeneratorTest {

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

    private IndexBasedAssociationNodeGenerator indexBasedAssociationNodeGenerator;

    private IndexBasedAssociationNode indexBasedAssociationNode;

    private IdentifierNodeFactory nodeFactory;

    @Before
    public void createIndexBasedAssociationNodeGenerator() throws Exception {
        nodeFactory = new IdentifierNodeFactory("IndexBasedAssociationNodeGeneratorTest", ipsProject);
        indexBasedAssociationNodeGenerator = new IndexBasedAssociationNodeGenerator(factory, builderSet);
    }

    private IndexBasedAssociationNode createIndexBasedAssociationNode(int index) throws Exception {
        when(association.findTarget(any(IIpsProject.class))).thenReturn(target);
        return (IndexBasedAssociationNode)nodeFactory.createIndexBasedAssociationNode(association, index);
    }

    @Test
    public void testGetCompilationResult() throws Exception {
        JavaCodeFragment javaCodeFragment = spy(new JavaCodeFragment("vertrag"));
        XPolicyAssociation xPolicyAssociation = mock(XPolicyAssociation.class);
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        when(builderSet.getModelNode(association, XPolicyAssociation.class)).thenReturn(xPolicyAssociation);
        when(xPolicyAssociation.getMethodNameGetSingle()).thenReturn("getDeckung");
        indexBasedAssociationNode = createIndexBasedAssociationNode(1);

        CompilationResult<JavaCodeFragment> compilationResult = indexBasedAssociationNodeGenerator
                .getCompilationResultForCurrentNode(indexBasedAssociationNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals("vertrag.getDeckung(1)", compilationResult.getCodeFragment().getSourcecode());
        assertNotNull(compilationResult.getDatatype());
        verify(javaCodeFragment).getImportDeclaration();
        verifyZeroInteractions(javaCodeFragment);
    }

    @Test
    public void testGetCompilationResult_listContext() throws Exception {
        JavaCodeFragment javaCodeFragment = spy(new JavaCodeFragment("vertrag"));
        XPolicyAssociation xPolicyAssociation = mock(XPolicyAssociation.class);
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        ListOfTypeDatatype listOfTypeDatatype = new ListOfTypeDatatype(target);
        when(contextCompilationResult.getDatatype()).thenReturn(listOfTypeDatatype);
        when(builderSet.getModelNode(association, XPolicyAssociation.class)).thenReturn(xPolicyAssociation);
        when(xPolicyAssociation.getMethodNameGetter()).thenReturn("getDeckungen");
        when(builderSet.getJavaClassName(target, true)).thenReturn("TargetClassName");
        indexBasedAssociationNode = createIndexBasedAssociationNode(1);

        CompilationResult<JavaCodeFragment> compilationResult = indexBasedAssociationNodeGenerator
                .getCompilationResultForCurrentNode(indexBasedAssociationNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals(
                "new AssociationTo1Helper<TargetClassName, TargetClassName>(){@Override protected TargetClassName getTargetInternal(TargetClassName sourceObject){return sourceObject.getDeckungen();}}.getTargets(vertrag).get(1)",
                compilationResult.getCodeFragment().getSourcecode());
        assertNotNull(compilationResult.getDatatype());
        verify(javaCodeFragment).getImportDeclaration();
        verifyZeroInteractions(javaCodeFragment);
    }

}
