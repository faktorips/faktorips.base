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
