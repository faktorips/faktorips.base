/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
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
        assertTrue(expectedValues.contains("1"));
        assertTrue(expectedValues.contains("2"));
        assertTrue(expectedValues.contains("3"));
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
        IUniqueKey tableKey = table.newUniqueKey();
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
        IUniqueKey tableKey = table.newUniqueKey();
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
        IContentProposal[] results = proposalProvider.getProposals("TestT", 5);
        assertEquals(0, results.length);

        results = proposalProvider.getProposals("ratePlan", 8);
        assertEquals(1, results.length);
    }

    @Test
    public void testDoComputeCompletionProposalsForParam() throws Exception {
        formulaSignature.newParameter(Datatype.DECIMAL.getQualifiedName(), "abcparam");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("a", 1);
        IContentProposal proposal = results[0];
        assertEquals("bcparam", proposal.getContent());

        results = proposalProvider.getProposals("", 0);
        proposal = results[0];
        assertEquals("abcparam", proposal.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForProductCmptTypeAttributes() throws Exception {
        IAttribute firstAttr = productCmptType.newAttribute();
        firstAttr.setName("firstAttr");
        firstAttr.setDatatype(Datatype.STRING.getQualifiedName());

        IAttribute secondAttr = productCmptType.newAttribute();
        secondAttr.setName("secondAttr");
        secondAttr.setDatatype(Datatype.STRING.getQualifiedName());

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("f", 1);
        IContentProposal proposal = results[0];
        assertEquals("irstAttr", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("s", 1);
        proposal = results[0];
        assertEquals("econdAttr", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("k", 1);
        assertEquals(0, results.length);
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAttributes() throws Exception {
        IAttribute firstAttr = cmptType.newAttribute();
        firstAttr.setName("firstAttr");
        firstAttr.setDatatype(Datatype.STRING.getQualifiedName());

        IAttribute secondAttr = cmptType.newAttribute();
        secondAttr.setName("secondAttr");
        secondAttr.setDatatype(Datatype.STRING.getQualifiedName());

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.f", 8);
        IContentProposal proposal = results[0];
        assertEquals("irstAttr", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.s", 8);
        proposal = results[0];
        assertEquals("econdAttr", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.k", 8);
        assertEquals(0, results.length);

        IPolicyCmptTypeAttribute thirdAttr = cmptType.newPolicyCmptTypeAttribute("thirdAttr");
        thirdAttr.setDatatype(Datatype.STRING.getQualifiedName());
        thirdAttr.setProductRelevant(true);
        results = proposalProvider.getProposals("policy.t", 8);
        proposal = results[0];
        assertEquals("hirdAttr", proposal.getContent());
        proposal = results[1];
        assertEquals("hirdAttr@default", proposal.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAssociations() throws Exception {
        IAssociation to1Association = cmptType.newAssociation();
        to1Association.setTargetRoleSingular("mainTarget");
        to1Association.setMaxCardinality(1);

        IAssociation toManyAssociation = cmptType.newAssociation();
        toManyAssociation.setTargetRoleSingular("additionalTarget");
        toManyAssociation.setMaxCardinality(Integer.MAX_VALUE);

        formulaSignature.newParameter(cmptType.getQualifiedName(), "policy");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("policy.m", 8);
        assertEquals(1, results.length);
        IContentProposal proposal = results[0];
        assertEquals("ainTarget", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.a", 8);
        assertEquals(2, results.length);
        proposal = results[0];
        assertEquals("dditionalTarget", proposal.getContent());
        proposal = results[1];
        assertEquals("dditionalTarget[0]", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.x", 8);
        assertEquals(0, results.length);
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAssociationChains() throws Exception {
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
        assertEquals("arget1", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.target1.t", 16);
        assertEquals(2, results.length);
        proposal = results[0];
        assertEquals("arget2", proposal.getContent());
        proposal = results[1];
        assertEquals("arget2[0]", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.target1.target2.t", 24);
        assertEquals(2, results.length);
        proposal = results[0];
        assertEquals("arget3", proposal.getContent());
        proposal = results[1];
        assertEquals("arget3[0]", proposal.getContent());

        proposalProvider = new ExpressionProposalProvider(configElement);
        results = proposalProvider.getProposals("policy.target1.target2[0].t", 27);
        assertEquals(1, results.length);
        proposal = results[0];
        assertEquals("arget3", proposal.getContent());
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAndProductCmptTypeParams() throws Exception {
        PolicyCmptType a = newPolicyAndProductCmptType(ipsProject, "a", "aConfigtype");
        ProductCmptType aConfig = (ProductCmptType)a.findProductCmptType(ipsProject);
        formulaSignature.newParameter(a.getQualifiedName(), "aParam");
        formulaSignature.newParameter(aConfig.getQualifiedName(), "aConfigParam");

        proposalProvider = new ExpressionProposalProvider(configElement);
        IContentProposal[] results = proposalProvider.getProposals("", 0);
        IContentProposal proposal = results[0];
        assertEquals("aParam - a", proposal.getLabel());

        proposal = results[1];
        assertEquals("aConfigParam - aConfigtype", proposal.getLabel());
    }
}
