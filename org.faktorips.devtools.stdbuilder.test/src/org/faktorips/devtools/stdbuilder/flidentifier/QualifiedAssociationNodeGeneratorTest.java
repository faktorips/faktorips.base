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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.QualifiedAssociationNode;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
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
public class QualifiedAssociationNodeGeneratorTest {

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

    private QualifiedAssociationNodeGenerator qualifiedAssociationNodeGenerator;

    private QualifiedAssociationNode qualifiedAssociationNode;

    private IdentifierNodeFactory nodeFactory;

    @Before
    public void createIndexBasedAssociationNodeGenerator() throws Exception {
        nodeFactory = new IdentifierNodeFactory("QualifiedAssociationNodeGeneratorTest", ipsProject);
        qualifiedAssociationNodeGenerator = new QualifiedAssociationNodeGenerator(factory, builderSet);
    }

    private QualifiedAssociationNode createQualifiedAssociationNode(String qualifier, String runtimeID, IType policyType)
            throws Exception {
        when(association.findTarget(any(IIpsProject.class))).thenReturn(target);
        return (QualifiedAssociationNode)nodeFactory.createQualifiedAssociationNode(association, qualifier, runtimeID,
                policyType, false);
    }

    @Test
    public void testGetCompilationResult_SameTargetDatatype() throws Exception {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("vertrag");
        XPolicyAssociation xPolicyAssociation = mock(XPolicyAssociation.class);
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        when(builderSet.getModelNode(association, XPolicyAssociation.class)).thenReturn(xPolicyAssociation);
        when(builderSet.getJavaClassName(target)).thenReturn("PolicyCmptType");
        when(xPolicyAssociation.getMethodNameGetter()).thenReturn("getHausratZusatzdeckungen");
        qualifiedAssociationNode = createQualifiedAssociationNode("HRD-Fahrraddiebstahl 2012-03",
                "hausrat.HRD-Fahrraddiebstahl 2012-03", target);

        CompilationResult<JavaCodeFragment> compilationResult = qualifiedAssociationNodeGenerator
                .getCompilationResultForCurrentNode(qualifiedAssociationNode, contextCompilationResult);
        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult.getDatatype());
        assertEquals(target, compilationResult.getDatatype());
        assertTrue(compilationResult.getCodeFragment().getSourcecode().startsWith("FormulaEvaluatorUtil"));
        System.out.println(compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_DifferentTargetDatatype() throws Exception {
        IType type = mock(PolicyCmptType.class);
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("vertrag");
        XPolicyAssociation xPolicyAssociation = mock(XPolicyAssociation.class);
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        when(builderSet.getModelNode(association, XPolicyAssociation.class)).thenReturn(xPolicyAssociation);
        when(builderSet.getJavaClassName(type)).thenReturn("ProductCmptType");
        when(xPolicyAssociation.getMethodNameGetter()).thenReturn("getHausratZusatzdeckungen");
        qualifiedAssociationNode = createQualifiedAssociationNode("HRD-Fahrraddiebstahl 2012-03",
                "hausrat.HRD-Fahrraddiebstahl 2012-03", type);

        CompilationResult<JavaCodeFragment> compilationResult = qualifiedAssociationNodeGenerator
                .getCompilationResultForCurrentNode(qualifiedAssociationNode, contextCompilationResult);
        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult.getDatatype());
        assertEquals(type, compilationResult.getDatatype());
        assertTrue(compilationResult.getCodeFragment().getSourcecode().startsWith("(("));
        System.out.println(compilationResult.getCodeFragment().getSourcecode());
    }
}
