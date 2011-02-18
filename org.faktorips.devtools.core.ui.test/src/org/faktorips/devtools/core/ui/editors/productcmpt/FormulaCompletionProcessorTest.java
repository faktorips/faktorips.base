/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
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

public class FormulaCompletionProcessorTest extends AbstractIpsPluginTest {

    private FormulaCompletionProcessor processor;
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
        processor = new FormulaCompletionProcessor(configElement);
    }

    @Test
    public void testDoComputeCompletionProposals() throws Exception {
        ArrayList<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
        processor.doComputeCompletionProposals("Test", 0, results);
        assertEquals(0, results.size());

        formulaSignature.setDatatype(enumDatatype.getQualifiedName());
        processor.doComputeCompletionProposals("Test", 0, results);
        CompletionProposal proposal = (CompletionProposal)results.get(0);
        assertEquals(StringUtil.unqualifiedName(TestEnumType.class.getName()), proposal.getDisplayString());
        results = new ArrayList<ICompletionProposal>();
        processor.doComputeCompletionProposals("TestEnumType.", 0, results);
        assertEquals(3, results.size());
        ArrayList<String> expectedValues = new ArrayList<String>();
        for (Object name : results) {
            proposal = (CompletionProposal)name;
            expectedValues.add(proposal.getDisplayString());
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

        ArrayList<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
        processor.doComputeCompletionProposals("TestTable_", 0, results);
        assertEquals(0, results.size());

        processor.doComputeCompletionProposals("ra", 0, results);
        assertEquals(1, results.size());
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
        ArrayList<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
        processor.doComputeCompletionProposals("TestT", 0, results);
        assertEquals(0, results.size());

        results.clear();
        processor.doComputeCompletionProposals("ratePlan", 0, results);
        assertEquals(1, results.size());
    }

    @Test
    public void testDoComputeCompletionProposalsForParam() throws Exception {
        formulaSignature.newParameter(Datatype.DECIMAL.getQualifiedName(), "abcparam");

        ArrayList<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
        processor = new FormulaCompletionProcessor(configElement);
        processor.doComputeCompletionProposals("a", 1, results);
        CompletionProposal proposal = (CompletionProposal)results.get(0);
        Document document = new Document("a");
        proposal.apply(document);
        assertEquals("abcparam", document.get());
    }

    @Test
    public void testDoComputeCompletionProposalsForProductCmptTypeAttributes() throws Exception {
        IAttribute firstAttr = productCmptType.newAttribute();
        firstAttr.setName("firstAttr");
        firstAttr.setDatatype(Datatype.STRING.getQualifiedName());

        IAttribute secondAttr = productCmptType.newAttribute();
        secondAttr.setName("secondAttr");
        secondAttr.setDatatype(Datatype.STRING.getQualifiedName());

        ArrayList<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
        processor = new FormulaCompletionProcessor(configElement);
        processor.doComputeCompletionProposals("f", 1, results);
        CompletionProposal proposal = (CompletionProposal)results.get(0);
        Document document = new Document(" ");
        proposal.apply(document);
        assertEquals("firstAttr", document.get());

        results = new ArrayList<ICompletionProposal>();
        processor = new FormulaCompletionProcessor(configElement);
        processor.doComputeCompletionProposals("s", 1, results);
        proposal = (CompletionProposal)results.get(0);
        document = new Document(" ");
        proposal.apply(document);
        assertEquals("secondAttr", document.get());

        results = new ArrayList<ICompletionProposal>();
        processor = new FormulaCompletionProcessor(configElement);
        processor.doComputeCompletionProposals("k", 1, results);
        assertEquals(0, results.size());
    }

    @Test
    public void testDoComputeCompletionProposalsForPolicyCmptTypeAndProductCmptTypeParams() throws Exception {
        PolicyCmptType a = newPolicyAndProductCmptType(ipsProject, "a", "aConfigtype");
        ProductCmptType aConfig = (ProductCmptType)a.findProductCmptType(ipsProject);
        formulaSignature.newParameter(a.getQualifiedName(), "aParam");
        formulaSignature.newParameter(aConfig.getQualifiedName(), "aConfigParam");

        ArrayList<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
        processor = new FormulaCompletionProcessor(configElement);
        processor.doComputeCompletionProposals("", 1, results);
        CompletionProposal proposal = (CompletionProposal)results.get(0);
        assertEquals("aParam - a", proposal.getDisplayString());

        proposal = (CompletionProposal)results.get(1);
        assertEquals("aConfigParam - aConfigtype", proposal.getDisplayString());
    }
}
