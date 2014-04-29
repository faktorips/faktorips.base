/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionProposalProviderTest extends AbstractIpsPluginTest {

    private ExpressionProposalProvider proposalProvider;
    private PolicyCmptType policyCmptType;
    private IProductCmptTypeMethod formulaSignature;
    private IProductCmptGeneration productCmptGen;
    private IFormula formula;
    private IpsProject ipsProject;
    private IProductCmptType productCmptType;
    private static final String FUNCTION_ABS = "Abs";

    @Mock
    private IdentifierParser parser;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        PolicyCmptType targetPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "Target", "TargetProduct");
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });

        IPolicyCmptTypeAssociation association = policyCmptType.newPolicyCmptTypeAssociation();
        association.setTargetRoleSingular("part");
        association.setTargetRolePlural("parts");
        association.setMinCardinality(0);
        association.setMaxCardinality(10);
        association.setTarget(targetPolicyCmptType.getQualifiedName());
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("premium");
        attribute.setDatatype("String");

        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        formulaSignature = productCmptType.newProductCmptTypeMethod();
        formulaSignature.setFormulaSignatureDefinition(true);
        formulaSignature.setFormulaName("CalcPremium");
        formulaSignature.newParameter("TestPolicy", "testPolicy");

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "TestProduct");
        productCmptGen = productCmpt.getProductCmptGeneration(0);
        formula = productCmptGen.newFormula();
        formula.setFormulaSignature(formulaSignature.getFormulaName());
        formula.setExpression("expresion");
    }

    @Test
    public void testGetProposalsWithoutFunctions() {
        proposalProvider = new ExpressionProposalProvider(formula, parser);

        List<IdentifierNode> list = new ArrayList<IdentifierNode>();
        AttributeNode attr1 = mock(AttributeNode.class);
        AttributeNode attr2 = mock(AttributeNode.class);
        AssociationNode assoc1 = mock(AssociationNode.class);
        list.add(attr1);
        list.add(attr2);
        list.add(assoc1);
        doReturn("attr1").when(attr1).getText();
        doReturn("attr2").when(attr2).getText();
        doReturn(list).when(parser).getProposals("TestPolicy");

        IContentProposal[] proposals = proposalProvider.getProposals("TestPolicy", 10);
        assertEquals(3, proposals.length);
        assertEquals(attr1.getText(), proposals[0].getContent());
        assertEquals(attr2.getText(), proposals[1].getContent());
    }

    @Test
    public void testGetProposalsWithFunctions() {
        proposalProvider = new ExpressionProposalProvider(formula, parser);
        doReturn(Collections.EMPTY_LIST).when(parser).getProposals(FUNCTION_ABS);

        IContentProposal[] proposals = proposalProvider.getProposals(FUNCTION_ABS, 3);
        assertEquals(1, proposals.length);
        assertEquals(FUNCTION_ABS.toUpperCase(), proposals[0].getContent());
    }

    @Test
    public void testGetProposals_considerPosition() {
        proposalProvider = new ExpressionProposalProvider(formula, parser);
        doReturn(Collections.EMPTY_LIST).when(parser).getProposals(FUNCTION_ABS);

        IContentProposal[] proposals = proposalProvider.getProposals("AbsXYZ_ILLEGAL_INPUT", 6);
        assertEquals(0, proposals.length);

        proposals = proposalProvider.getProposals("AbsXYZ_ILLEGAL_INPUT", 3);
        assertEquals(1, proposals.length);
        assertEquals(FUNCTION_ABS.toUpperCase(), proposals[0].getContent());
    }

    @Test
    public void testgetProposalCompletionForFunctions() throws Exception {
        proposalProvider = new ExpressionProposalProvider(formula, parser);
        IContentProposal[] results = proposalProvider.getProposals("WE", 2);
        IContentProposal proposal = results[0];
        assertEquals("WENN(boolean; any; any) - any", proposal.getLabel());
        assertEquals("WE", ((ContentProposal)proposal).getPrefix());
        assertEquals(2, ((ContentProposal)proposal).getPrefixLength());

        results = proposalProvider.getProposals("We", 2);
        proposal = results[0];
        assertEquals("WENN", proposal.getContent());

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setFormulaLanguageLocale(Locale.ENGLISH);
        ipsProject.setProperties(properties);

        results = proposalProvider.getProposals("WEN", 3);
        assertEquals(0, results.length);

        results = proposalProvider.getProposals("I", 1);
        proposal = results[0];
        assertEquals("IF(boolean; any; any) - any", proposal.getLabel());
    }

    @Test
    public void testGetProposals_ignorePreviousFunctionsInInputString() throws Exception {
        proposalProvider = new ExpressionProposalProvider(formula, new IdentifierParser(formula, ipsProject));

        IContentProposal[] proposals = proposalProvider.getProposals("WENN(ABS(x+testPolicy.p)+)", 23);
        assertProposals(proposals);
    }

    @Test
    public void testGetProposals_ignoreFunctionsInMultilineString() throws Exception {
        proposalProvider = new ExpressionProposalProvider(formula, new IdentifierParser(formula, ipsProject));

        IContentProposal[] proposals = proposalProvider.getProposals("WENN(\r\n\tABS(\r\n\t\tx+testPolicy.p\r\t)+\n)",
                30);
        assertProposals(proposals);
    }

    @Test
    public void testGetProposals_ignoreFollowingFunctionsInInputString() throws Exception {
        proposalProvider = new ExpressionProposalProvider(formula, new IdentifierParser(formula, ipsProject));

        IContentProposal[] proposals = proposalProvider.getProposals("WENN(testPolicy.p)+ABS(x)", 17);
        assertProposals(proposals);
    }

    private void assertProposals(IContentProposal[] proposals) {
        assertEquals(3, proposals.length);
        assertEquals("premium", proposals[0].getContent());
        assertEquals("premium@default", proposals[1].getContent());
        assertEquals("part", proposals[2].getContent());
    }

}
