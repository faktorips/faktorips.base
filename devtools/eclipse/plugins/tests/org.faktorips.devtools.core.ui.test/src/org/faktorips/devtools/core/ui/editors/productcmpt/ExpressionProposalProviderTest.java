/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierProposal;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeType;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionProposalProviderTest extends AbstractIpsPluginTest {

    private static final String IDENTIFIER_PROPOSAL1 = "identifierProposal1";
    private static final String ANY_IDENTIFIER = "TestPolicy";
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
        List<IdentifierProposal> list = new ArrayList<>();
        IdentifierProposal identifierProposal1 = mock(IdentifierProposal.class);
        IdentifierProposal identifierProposal2 = mock(IdentifierProposal.class);
        when(identifierProposal1.getText()).thenReturn(IDENTIFIER_PROPOSAL1);
        when(identifierProposal2.getText()).thenReturn(IDENTIFIER_PROPOSAL1);
        when(identifierProposal1.getNodeType()).thenReturn(IdentifierNodeType.PARAMETER);
        when(identifierProposal2.getNodeType()).thenReturn(IdentifierNodeType.PARAMETER);
        list.add(identifierProposal1);
        list.add(identifierProposal2);
        doReturn(list).when(parser).getProposals(ANY_IDENTIFIER);

        IContentProposal[] proposals = proposalProvider.getProposals(ANY_IDENTIFIER, ANY_IDENTIFIER.length());

        assertEquals(identifierProposal1.getText(), proposals[0].getContent());
        assertEquals(identifierProposal2.getText(), proposals[1].getContent());
        assertEquals(2, proposals.length);
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
        assertEquals("WENN(Boolean; any; any) - any", proposal.getLabel());
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
        assertEquals("IF(Boolean; any; any) - any", proposal.getLabel());
    }

    @Test
    public void testGetProposals_ignorePreviousFunctionsInInputString() throws Exception {
        proposalProvider = new ExpressionProposalProvider(formula, new IdentifierParser(formula, ipsProject));

        IContentProposal[] proposals = proposalProvider.getProposals("WENN(ABS(x+testPolicy.p)+)", 23);
        assertProposals(proposals);
    }

    @Test
    public void testGetProposals_ignoreFunctionsInMultilineString_tab() throws Exception {
        proposalProvider = new ExpressionProposalProvider(formula, new IdentifierParser(formula, ipsProject));

        IContentProposal[] proposals = proposalProvider.getProposals("WENN(\r\n\tABS(\r\n\t\tx+testPolicy.p\r\t)+\n)",
                30);
        assertProposals(proposals);
    }

    @Test
    public void testGetProposals_ignoreFunctionsInMultilineString_blank() throws Exception {
        proposalProvider = new ExpressionProposalProvider(formula, new IdentifierParser(formula, ipsProject));

        IContentProposal[] proposals = proposalProvider.getProposals(
                "WENN(\r\n       ABS(\r\n             x+testPolicy.p\r    )+\n)", 47);
        assertProposals(proposals);
    }

    @Test
    public void testGetProposals_ignoreFollowingFunctionsInInputString() throws Exception {
        proposalProvider = new ExpressionProposalProvider(formula, new IdentifierParser(formula, ipsProject));

        IContentProposal[] proposals = proposalProvider.getProposals("WENN(testPolicy.p)+ABS(x)", 17);
        assertProposals(proposals);
    }

    @Test
    public void testGetProposals_ignoreFollowingFunctionsInInputString_lexicalError() throws Exception {
        proposalProvider = new ExpressionProposalProvider(formula, new IdentifierParser(formula, ipsProject));

        /*
         * An unclosed "[" produces a lexical error for some reason. Expect valid proposal
         * nonetheless.
         */
        IContentProposal[] proposals = proposalProvider.getProposals("WENN(testPolicy.part[", 21);
        assertEquals(1, proposals.length);
        assertEquals("0]", proposals[0].getContent());
    }

    private void assertProposals(IContentProposal[] proposals) {
        assertEquals(5, proposals.length);
        assertEquals("premium", proposals[0].getContent());
        assertEquals("premium@default", proposals[1].getContent());
        assertEquals("part", proposals[2].getContent());
        assertEquals("part[\"", proposals[3].getContent());
        assertEquals("part[0]", proposals[4].getContent());
    }

}
