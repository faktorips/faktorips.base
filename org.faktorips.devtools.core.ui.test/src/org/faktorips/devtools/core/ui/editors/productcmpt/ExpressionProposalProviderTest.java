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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

public class ExpressionProposalProviderTest extends AbstractIpsPluginTest {

    private ExpressionProposalProvider proposalProvider;
    private IpsProject ipsProject;
    private PolicyCmptType cmptType;
    private IProductCmptType productCmptType;
    private IProductCmptTypeMethod formulaSignature;
    private IProductCmptGeneration productCmptGen;
    private IFormula configElement;
    private EnumDatatype enumDatatype;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)newIpsProject();
        cmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
        enumDatatype = (EnumDatatype)ipsProject.findDatatype("TestEnumType");

        productCmptType = cmptType.findProductCmptType(ipsProject);
        formulaSignature = productCmptType.newProductCmptTypeMethod();
        formulaSignature.setFormulaSignatureDefinition(true);
        formulaSignature.setFormulaName("CalcPremium");

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "TestProduct");
        productCmptGen = productCmpt.getProductCmptGeneration(0);
        configElement = productCmptGen.newFormula();
        configElement.setFormulaSignature(formulaSignature.getFormulaName());
        proposalProvider = new ExpressionProposalProvider(configElement);
    }

    @Test
    public void testDoComputeCompletionProposals() throws Exception {
        IContentProposal[] results = proposalProvider.getProposals("Test", 4);
        assertEquals(0, results.length);

        formulaSignature.setDatatype(enumDatatype.getQualifiedName());
        results = proposalProvider.getProposals("Test", 4);
        IContentProposal proposal = results[0];
        assertEquals(StringUtil.unqualifiedName(TestEnumType.class.getName()), proposal.getLabel());
        results = proposalProvider.getProposals("TestEnumType.", 13);
        assertEquals(3, results.length);
        ArrayList<String> expectedValues = new ArrayList<String>();
        for (IContentProposal result : results) {
            expectedValues.add(result.getLabel());
        }
        assertTrue(expectedValues.contains("1(first)"));
        assertTrue(expectedValues.contains("2(second)"));
        assertTrue(expectedValues.contains("3(third)"));
    }

    @Test
    public void testDoComputeCompletionProposalsForMultipleTableContentsWithDateFormatName() throws Exception {
        ITableStructure table = (ITableStructure)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0],
                IpsObjectType.TABLE_STRUCTURE, "Testtable");
        IColumn column = table.newColumn();
        table.setTableStructureType(TableStructureType.MULTIPLE_CONTENTS);
        column.setName("first");
        column.setDatatype("String");
        column = table.newColumn();
        column.setName("second");
        column.setDatatype("String");
        IIndex tableKey = table.newIndex();
        tableKey.addKeyItem("second");

        ITableContents tableContents = (ITableContents)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0],
                IpsObjectType.TABLE_CONTENTS, "TestTable_2006-07-19");
        tableContents.setTableStructure(table.getQualifiedName());
        tableContents.newGeneration((GregorianCalendar)Calendar.getInstance());

        // create the table usage which will be used to resolve the available formula table access
        // functions
        ITableContentUsage tableContentUsage = productCmptGen.newTableContentUsage();
        tableContentUsage.setTableContentName(tableContents.getQualifiedName());
        tableContentUsage.setStructureUsage("ratePlan");

        IContentProposal[] results = proposalProvider.getProposals("TestTable_", 10);
        assertEquals(0, results.length);

        results = proposalProvider.getProposals("ra", 2);
        assertEquals(1, results.length);
    }

    @Test
    public void testDoComputeCompletionProposalsForSingleTableContents() throws Exception {
        ITableStructure table = (ITableStructure)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0],
                IpsObjectType.TABLE_STRUCTURE, "Testtable");
        IColumn column = table.newColumn();
        table.setTableStructureType(TableStructureType.SINGLE_CONTENT);
        column.setName("first");
        column.setDatatype("String");
        column = table.newColumn();
        column.setName("second");
        column.setDatatype("String");
        IIndex tableKey = table.newIndex();
        tableKey.addKeyItem("second");

        ITableContents tableContents = (ITableContents)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0],
                IpsObjectType.TABLE_CONTENTS, "Testtable");
        tableContents.setTableStructure(table.getQualifiedName());
        tableContents.newGeneration((GregorianCalendar)Calendar.getInstance());

        // create the table usage which will be used to resolve the available formula table access
        // functions
        ITableContentUsage tableContentUsage = productCmptGen.newTableContentUsage();
        tableContentUsage.setTableContentName(tableContents.getQualifiedName());
        tableContentUsage.setStructureUsage("ratePlan");

        // there needs to be a table content available for the structure otherwise no completion is
        // proposed
        IContentProposal[] results = proposalProvider.getProposals("TestAnyTable", 12);
        assertEquals(0, results.length);

        results = proposalProvider.getProposals("TestT", 5);
        assertEquals(1, results.length);

        results = proposalProvider.getProposals("ratePlan", 8);
        assertEquals(1, results.length);
    }

    @Test
    public void testDoComputeCompletionProposalsForParam() throws Exception {
        formulaSignature.newParameter(Datatype.DECIMAL.getQualifiedName(), "abcparam");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("a", 1);
        IContentProposal proposal = results[0];
        assertEquals("abcparam", proposal.getContent());

        results = proposalProvider.getProposals("", 0);
        proposal = results[0];
        assertEquals("abcparam", proposal.getContent());
    }

    @Test
    public void testGiveAplhabeticalOrderForCompletionProposalForParam() {
        formulaSignature.newParameter(Datatype.DECIMAL.getQualifiedName(), "bcdparam");
        formulaSignature.newParameter(Datatype.DECIMAL.getQualifiedName(), "abcparam");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("", 0);
        IContentProposal proposal_1 = results[0];
        IContentProposal proposal_2 = results[1];

        assertEquals("abcparam", proposal_1.getContent());
        assertEquals("bcdparam", proposal_2.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForParamWithCaseInsensitiv() {
        formulaSignature.newParameter(Datatype.DECIMAL.getQualifiedName(), "abcparam");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("AB", 2);
        IContentProposal proposal = results[0];
        assertEquals("abcparam", proposal.getContent());

        formulaSignature.newParameter(Datatype.DECIMAL.getQualifiedName(), "Abcparam");
        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("a", 1);
        proposal = results[1];
        assertEquals("abcparam", proposal.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForProductCmptTypeAttributes() throws Exception {
        IAttribute firstAttr = productCmptType.newAttribute();
        firstAttr.setName("firstAttr");
        firstAttr.setDatatype(Datatype.STRING.getQualifiedName());
        MultiLanguageSupport multiLanguageSupport = IpsPlugin.getMultiLanguageSupport();
        firstAttr.setLabelValue(multiLanguageSupport.getLocalizationLocale(), "FirstAttrLabel");
        firstAttr.setDescriptionText(multiLanguageSupport.getLocalizationLocale(), "FirstAttrDescription");

        IAttribute secondAttr = productCmptType.newAttribute();
        secondAttr.setName("secondAttr");
        secondAttr.setDatatype(Datatype.STRING.getQualifiedName());

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("f", 1);
        IContentProposal proposal = results[0];
        assertEquals("firstAttr", proposal.getContent());
        assertEquals("FirstAttrLabel - FirstAttrDescription", proposal.getDescription());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("se", 2);
        proposal = results[0];
        assertEquals("secondAttr", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("k", 1);
        assertEquals(0, results.length);
    }

    @Test
    public void testGiveAlphabeticalOrderForCompletionProposalsForProductCmptTypeAttributes() {
        IAttribute firstAttr = productCmptType.newAttribute();
        firstAttr.setName("Attr_BCD");
        firstAttr.setDatatype(Datatype.STRING.getQualifiedName());

        IAttribute secondAttr = productCmptType.newAttribute();
        secondAttr.setName("Attr_ABC");
        secondAttr.setDatatype(Datatype.STRING.getQualifiedName());

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("Attr_", 5);
        IContentProposal proposal_1 = results[0];
        IContentProposal proposal_2 = results[1];
        assertEquals("Attr_ABC", proposal_1.getContent());
        assertEquals("Attr_BCD", proposal_2.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForProductCmptTypeAttributesWithCaseInsensitiv() {
        IAttribute firstAttr = productCmptType.newAttribute();
        firstAttr.setName("FirstAttr");
        firstAttr.setDatatype(Datatype.STRING.getQualifiedName());

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("f", 1);
        assertEquals(1, results.length);
        IContentProposal proposal = results[0];
        assertEquals("FirstAttr", proposal.getContent());

        firstAttr.setName("firstAttr");
        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("F", 1);
        assertEquals(1, results.length);
        proposal = results[0];
        assertEquals("firstAttr", proposal.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAttributes() throws Exception {
        IAttribute firstAttr = cmptType.newAttribute();
        firstAttr.setName("firstAttr");
        firstAttr.setDatatype(Datatype.STRING.getQualifiedName());
        MultiLanguageSupport multiLanguageSupport = IpsPlugin.getMultiLanguageSupport();
        firstAttr.setLabelValue(multiLanguageSupport.getLocalizationLocale(), "FirstAttrLabel");
        firstAttr.setDescriptionText(multiLanguageSupport.getLocalizationLocale(), "FirstAttrDescription");

        IAttribute secondAttr = cmptType.newAttribute();
        secondAttr.setName("secondAttr");
        secondAttr.setDatatype(Datatype.STRING.getQualifiedName());

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.f", 8);
        IContentProposal proposal = results[0];
        assertEquals("firstAttr", proposal.getContent());
        assertEquals("FirstAttrLabel - FirstAttrDescription", proposal.getDescription());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.s", 8);
        proposal = results[0];
        assertEquals("secondAttr", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.k", 8);
        assertEquals(0, results.length);

        IPolicyCmptTypeAttribute thirdAttr = cmptType.newPolicyCmptTypeAttribute("thirdAttr");
        thirdAttr.setDatatype(Datatype.STRING.getQualifiedName());
        thirdAttr.setProductRelevant(true);
        results = proposalProvider.getProposals("policy.t", 8);
        proposal = results[0];
        assertEquals("thirdAttr", proposal.getContent());
        proposal = results[1];
        assertEquals("thirdAttr@default", proposal.getContent());
    }

    @Test
    public void testGiveAlphabeticalOrderForCompletionProposalForPolicyCmptTypeAttributes() {
        IAttribute firstAttr = cmptType.newAttribute();
        firstAttr.setName("abcde");
        firstAttr.setDatatype(Datatype.STRING.getQualifiedName());

        IAttribute secondAttr = cmptType.newAttribute();
        secondAttr.setName("aabcde");
        secondAttr.setDatatype(Datatype.STRING.getQualifiedName());

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.", 7);
        IContentProposal proposal = results[0];
        assertEquals("aabcde", proposal.getContent());

        proposal = results[1];
        assertEquals("abcde", proposal.getContent());

        results = proposalProvider.getProposals("policy.a", 8);
        proposal = results[0];
        assertEquals("aabcde", proposal.getContent());

        proposal = results[1];
        assertEquals("abcde", proposal.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAttributesWithCaseInsensitiv() {
        IAttribute firstAttr = cmptType.newAttribute();
        firstAttr.setName("firstAttr");
        firstAttr.setDatatype(Datatype.STRING.getQualifiedName());

        IAttribute secondAttr = cmptType.newAttribute();
        secondAttr.setName("SecondAttr");
        secondAttr.setDatatype(Datatype.STRING.getQualifiedName());

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.F", 8);
        IContentProposal proposal = results[0];
        assertEquals("firstAttr", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.s", 8);
        proposal = results[0];
        assertEquals("SecondAttr", proposal.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAssociations() throws Exception {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setAssociationsInFormulas(true);
        ipsProject.setProperties(properties);

        PolicyCmptType targetType = newPolicyAndProductCmptType(ipsProject, "TestTarget", "TestTargetType");

        IAssociation to1Association = cmptType.newAssociation();
        to1Association.setTargetRoleSingular("mainTarget");
        to1Association.setTarget(targetType.getQualifiedName());
        to1Association.setMaxCardinality(1);

        IAssociation toManyAssociation = cmptType.newAssociation();
        toManyAssociation.setTargetRoleSingular("additionalTarget");
        toManyAssociation.setTarget(targetType.getQualifiedName());
        toManyAssociation.setMaxCardinality(Integer.MAX_VALUE);

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.m", 8);
        assertEquals(1, results.length);
        IContentProposal proposal = results[0];
        assertEquals("mainTarget", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.a", 8);
        assertEquals(2, results.length);
        proposal = results[0];
        assertEquals("additionalTarget", proposal.getContent());
        proposal = results[1];
        assertEquals("additionalTarget[0]", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.x", 8);
        assertEquals(0, results.length);
        properties = ipsProject.getProperties();
        properties.setAssociationsInFormulas(false);
        ipsProject.setProperties(properties);

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.a", 8);
        assertEquals(0, results.length);
    }

    @Test
    public void testGetAlphabeticalOrderForCompletionProposalForPolicyCmptTypeAssociation() throws Exception {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setAssociationsInFormulas(true);
        ipsProject.setProperties(properties);

        PolicyCmptType targetType = newPolicyAndProductCmptType(ipsProject, "TestTarget", "TestTargetType");

        IAssociation to1Association = cmptType.newAssociation();
        to1Association.setTargetRoleSingular("targetABCDE");
        to1Association.setTarget(targetType.getQualifiedName());
        to1Association.setMaxCardinality(1);

        IAssociation toManyAssociation = cmptType.newAssociation();
        toManyAssociation.setTargetRoleSingular("targetAABCDE");
        toManyAssociation.setTarget(targetType.getQualifiedName());
        toManyAssociation.setMaxCardinality(1);

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.", 7);
        IContentProposal proposal = results[0];
        assertEquals("targetAABCDE", proposal.getContent());

        proposal = results[1];
        assertEquals("targetABCDE", proposal.getContent());

        results = proposalProvider.getProposals("policy.targetA", 14);
        proposal = results[0];
        assertEquals("targetAABCDE", proposal.getContent());

        proposal = results[1];
        assertEquals("targetABCDE", proposal.getContent());

    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAssociationsWithCaseInsensitiv() throws Exception {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setAssociationsInFormulas(true);
        ipsProject.setProperties(properties);

        PolicyCmptType targetType = newPolicyAndProductCmptType(ipsProject, "TestTarget", "TestTargetType");

        IAssociation to1Association = cmptType.newAssociation();
        to1Association.setTargetRoleSingular("mainTarget");
        to1Association.setTarget(targetType.getQualifiedName());
        to1Association.setMaxCardinality(1);

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.M", 8);
        assertEquals(1, results.length);
        IContentProposal proposal = results[0];
        assertEquals("mainTarget", proposal.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAssociationChains() throws Exception {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setAssociationsInFormulas(true);
        ipsProject.setProperties(properties);
        PolicyCmptType cmptType1 = newPolicyAndProductCmptType(ipsProject, "TestPart1", "TestPartType1");
        IAssociation association1 = cmptType.newAssociation();
        association1.setTargetRoleSingular("target1");
        association1.setMaxCardinality(1);
        association1.setTarget(cmptType1.getQualifiedName());

        PolicyCmptType cmptType2 = newPolicyAndProductCmptType(ipsProject, "TestPart2", "TestPartType2");
        IAssociation association2 = cmptType1.newAssociation();
        association2.setTargetRoleSingular("target2");
        association2.setMaxCardinality(Integer.MAX_VALUE);
        association2.setTarget(cmptType2.getQualifiedName());

        PolicyCmptType cmptType3 = newPolicyAndProductCmptType(ipsProject, "TestPart3", "TestPartType3");
        IAssociation association3 = cmptType2.newAssociation();
        association3.setTargetRoleSingular("target3");
        association3.setMaxCardinality(1);
        association3.setTarget(cmptType3.getQualifiedName());

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.t", 8);
        assertEquals(1, results.length);
        IContentProposal proposal = results[0];
        assertEquals("target1", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.target1.t", 16);
        assertEquals(2, results.length);
        proposal = results[0];
        assertEquals("target2", proposal.getContent());
        proposal = results[1];
        assertEquals("target2[0]", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.target1.target2.t", 24);
        assertEquals(2, results.length);
        proposal = results[0];
        assertEquals("target3", proposal.getContent());
        proposal = results[1];
        assertEquals("target3[0]", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.target1.target2[0].t", 27);
        assertEquals(1, results.length);
        proposal = results[0];
        assertEquals("target3", proposal.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAssociationWithQualifier() throws Exception {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setAssociationsInFormulas(true);
        ipsProject.setProperties(properties);
        PolicyCmptType cmptType1 = newPolicyAndProductCmptType(ipsProject, "TestPart1", "TestPartType1");
        IProductCmptType productCmptType1 = cmptType1.findProductCmptType(ipsProject);
        IAssociation association1 = cmptType.newAssociation();
        association1.setTargetRoleSingular("target");
        association1.setMaxCardinality(Integer.MAX_VALUE);
        association1.setTarget(cmptType1.getQualifiedName());

        newProductCmpt(productCmptType1, "pack.MyProduct1");

        PolicyCmptType cmptType2 = newPolicyAndProductCmptType(ipsProject, "TestPart2", "TestPartType2");
        cmptType2.setSupertype(cmptType1.getQualifiedName());
        IProductCmptType productCmptType2 = cmptType2.findProductCmptType(ipsProject);
        productCmptType2.setSupertype(productCmptType1.getQualifiedName());
        IPolicyCmptTypeAttribute attribute = cmptType2.newPolicyCmptTypeAttribute("myAttr");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());

        newProductCmpt(productCmptType2, "pack2.MyProduct2");

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.t", 8);
        assertEquals(4, results.length);
        IContentProposal proposal = results[0];
        assertEquals("target", proposal.getContent());
        proposal = results[1];
        assertEquals("target[0]", proposal.getContent());
        proposal = results[2];
        assertEquals("target[\"MyProduct1\"]", proposal.getContent());
        proposal = results[3];
        assertEquals("target[\"MyProduct2\"]", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.target[\"MyProduct1\"].m", 29);
        assertEquals(0, results.length);

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.target[\"MyProduct2\"].m", 29);
        assertEquals(1, results.length);
        proposal = results[0];
        assertEquals("myAttr", proposal.getContent());

        properties = ipsProject.getProperties();
        properties.setAssociationsInFormulas(false);
        ipsProject.setProperties(properties);
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAndProductCmptTypeParams() throws Exception {
        PolicyCmptType a = newPolicyAndProductCmptType(ipsProject, "a", "aConfigtype");
        ProductCmptType aConfig = (ProductCmptType)a.findProductCmptType(ipsProject);
        formulaSignature.newParameter(a.getQualifiedName(), "aParam");
        formulaSignature.newParameter(aConfig.getQualifiedName(), "aConfigParam");
        MultiLanguageSupport multiLanguageSupport = IpsPlugin.getMultiLanguageSupport();
        a.setLabelValue(multiLanguageSupport.getLocalizationLocale(), "PolicyCmptTypeLabel");
        a.setDescriptionText(multiLanguageSupport.getLocalizationLocale(), "PolicyCmptTypeDescription");
        aConfig.setLabelValue(multiLanguageSupport.getLocalizationLocale(), "ProductCmptTypeLabel");
        aConfig.setDescriptionText(multiLanguageSupport.getLocalizationLocale(), "ProductCmptTypeDescription");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("", 0);
        // Array index changed (from results[0] to results[1]), because of the now the list is
        // sorted
        IContentProposal proposal = results[1];
        assertEquals("aParam - a", proposal.getLabel());
        assertEquals("PolicyCmptTypeLabel - PolicyCmptTypeDescription", proposal.getDescription());

        // Array index changed (from results[1] fo results[0]), because of the now the list is
        // sorted
        proposal = results[0];
        assertEquals("aConfigParam - aConfigtype", proposal.getLabel());
        assertEquals("ProductCmptTypeLabel - ProductCmptTypeDescription", proposal.getDescription());
    }

    @Test
    public void testDoComputeCompletionProposalsForFunctionsLanguageDependant() throws Exception {
        proposalProvider = new ExpressionProposalProvider(configElement);
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
    public void testCheckMatchingNameWithCaseInsensitive() {
        proposalProvider = new ExpressionProposalProvider(configElement);
        String functionName = "produkte.hr_kompakt.FormLib.computeFormnula";

        assertTrue(proposalProvider.checkMatchingNameWithCaseInsensitive(functionName, "Form"));
        assertFalse(proposalProvider.checkMatchingNameWithCaseInsensitive(functionName, "Lib"));
        assertTrue(proposalProvider.checkMatchingNameWithCaseInsensitive(functionName, "produk"));
        assertTrue(proposalProvider.checkMatchingNameWithCaseInsensitive(functionName, "hr_kompakt"));
        assertFalse(proposalProvider.checkMatchingNameWithCaseInsensitive(functionName, ".FormLib"));
        assertTrue(proposalProvider.checkMatchingNameWithCaseInsensitive(functionName, "compute"));
        assertFalse(proposalProvider.checkMatchingNameWithCaseInsensitive(functionName, "Test"));
    }
}
