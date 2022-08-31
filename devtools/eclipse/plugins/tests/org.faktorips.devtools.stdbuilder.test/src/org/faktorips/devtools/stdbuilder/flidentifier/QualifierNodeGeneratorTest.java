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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class QualifierNodeGeneratorTest {

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private CompilationResult<JavaCodeFragment> contextCompilationResult;

    @Mock
    private IAssociation association;

    @Mock
    private IPolicyCmptType target;

    private QualifierNodeGenerator qualifiedAssociationNodeGenerator;

    private QualifierNode qualifiedAssociationNode;

    private IdentifierNodeFactory nodeFactory;

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IProductCmpt productCmpt;

    @Before
    public void createIndexBasedAssociationNodeGenerator() throws Exception {
        nodeFactory = new IdentifierNodeFactory(new TextRegion("QualifiedAssociationNodeGeneratorTest", 0,
                "QualifiedAssociationNodeGeneratorTest".length()), ipsProject);
        qualifiedAssociationNodeGenerator = new QualifierNodeGenerator(factory, builderSet);
    }

    private QualifierNode createQualifiedAssociationNode(String qualifier,
            String packageName,
            IPolicyCmptType targetType,
            boolean isListOfDatatype) throws Exception {
        when(productCmpt.getRuntimeId()).thenReturn(packageName + "." + qualifier);
        when(productCmpt.findPolicyCmptType(ipsProject)).thenReturn(targetType);
        return (QualifierNode)nodeFactory.createQualifierNode(productCmpt, qualifier, isListOfDatatype);
    }

    @Test
    public void testGetCompilationResult_SingleSameTargetDatatype() throws Exception {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("vertrag");
        mock(XPolicyAssociation.class);
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        when(contextCompilationResult.getDatatype()).thenReturn(target);
        when(builderSet.getJavaClassName(target, true)).thenReturn("PolicyCmptType");
        qualifiedAssociationNode = createQualifiedAssociationNode("HRD-Fahrraddiebstahl 2012-03", "hausrat", target,
                false);

        CompilationResult<JavaCodeFragment> compilationResult = qualifiedAssociationNodeGenerator
                .getCompilationResultForCurrentNode(qualifiedAssociationNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult.getDatatype());
        assertEquals(target, compilationResult.getDatatype());
        assertEquals(
                "FormulaEvaluatorUtil.<PolicyCmptType, PolicyCmptType>getModelObjectById(vertrag, \"hausrat.HRD-Fahrraddiebstahl 2012-03\")",
                compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_SingleDifferentTargetDatatype() throws Exception {
        IPolicyCmptType type = mock(IPolicyCmptType.class);
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("vertrag");
        mock(XPolicyAssociation.class);
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        when(contextCompilationResult.getDatatype()).thenReturn(target);
        when(builderSet.getJavaClassName(type, true)).thenReturn("SubPolicyCmptType");
        when(builderSet.getJavaClassName(target, true)).thenReturn("PolicyCmptType");
        qualifiedAssociationNode = createQualifiedAssociationNode("HRD-Fahrraddiebstahl 2012-03", "hausrat", type,
                false);

        CompilationResult<JavaCodeFragment> compilationResult = qualifiedAssociationNodeGenerator
                .getCompilationResultForCurrentNode(qualifiedAssociationNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult.getDatatype());
        assertEquals(type, compilationResult.getDatatype());
        assertEquals(
                "FormulaEvaluatorUtil.<PolicyCmptType, SubPolicyCmptType>getModelObjectById(vertrag, \"hausrat.HRD-Fahrraddiebstahl 2012-03\")",
                compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_List() throws Exception {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("vertrag");
        mock(XPolicyAssociation.class);
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        when(contextCompilationResult.getDatatype()).thenReturn(target);
        when(builderSet.getJavaClassName(target, true)).thenReturn("PolicyCmptType");
        qualifiedAssociationNode = createQualifiedAssociationNode("HRD-Fahrraddiebstahl 2012-03", "hausrat", target,
                true);

        CompilationResult<JavaCodeFragment> compilationResult = qualifiedAssociationNodeGenerator
                .getCompilationResultForCurrentNode(qualifiedAssociationNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult.getDatatype());
        assertEquals(new ListOfTypeDatatype(target), compilationResult.getDatatype());
        assertEquals(
                "FormulaEvaluatorUtil.<PolicyCmptType, PolicyCmptType>getListModelObjectById(vertrag, \"hausrat.HRD-Fahrraddiebstahl 2012-03\")",
                compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_ListAndContextList() throws Exception {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("vertrag");
        mock(XPolicyAssociation.class);
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        ListOfTypeDatatype listOfTypeDatatype = new ListOfTypeDatatype(target);
        when(contextCompilationResult.getDatatype()).thenReturn(listOfTypeDatatype);
        when(builderSet.getJavaClassName(target, true)).thenReturn("PolicyCmptType");
        qualifiedAssociationNode = createQualifiedAssociationNode("HRD-Fahrraddiebstahl 2012-03", "hausrat", target,
                true);

        CompilationResult<JavaCodeFragment> compilationResult = qualifiedAssociationNodeGenerator
                .getCompilationResultForCurrentNode(qualifiedAssociationNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult.getDatatype());
        assertEquals(listOfTypeDatatype, compilationResult.getDatatype());
        assertEquals(
                "FormulaEvaluatorUtil.<PolicyCmptType, PolicyCmptType>getListModelObjectById(vertrag, \"hausrat.HRD-Fahrraddiebstahl 2012-03\")",
                compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_ListAndContextListDifferentTarget() throws Exception {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("vertrag");
        mock(XPolicyAssociation.class);
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        ListOfTypeDatatype listOfTypeDatatype = new ListOfTypeDatatype(target);
        when(contextCompilationResult.getDatatype()).thenReturn(listOfTypeDatatype);
        when(builderSet.getJavaClassName(target, true)).thenReturn("PolicyCmptType");
        qualifiedAssociationNode = createQualifiedAssociationNode("HRD-Fahrraddiebstahl 2012-03", "hausrat", target,
                true);

        CompilationResult<JavaCodeFragment> compilationResult = qualifiedAssociationNodeGenerator
                .getCompilationResultForCurrentNode(qualifiedAssociationNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertNotNull(compilationResult.getDatatype());
        assertEquals(listOfTypeDatatype, compilationResult.getDatatype());
        assertEquals(
                "FormulaEvaluatorUtil.<PolicyCmptType, PolicyCmptType>getListModelObjectById(vertrag, \"hausrat.HRD-Fahrraddiebstahl 2012-03\")",
                compilationResult.getCodeFragment().getSourcecode());
    }
}
