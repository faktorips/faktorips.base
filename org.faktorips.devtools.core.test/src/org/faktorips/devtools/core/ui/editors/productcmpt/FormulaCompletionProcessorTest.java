/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.TestEnumType;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.StringUtil;

public class FormulaCompletionProcessorTest extends AbstractIpsPluginTest {

	private FormulaCompletionProcessor processor;
	private IpsProject ipsProject;
	private PolicyCmptType cmptType;
    private IAttribute attr;
    private IProductCmptGeneration productCmptGen;
    private IConfigElement configElement;
    private EnumDatatype enumDatatype;
    
	public void setUp() throws Exception{
		super.setUp();
		ipsProject = (IpsProject)newIpsProject("TestProject");
		cmptType = newPolicyCmptType(ipsProject.getIpsPackageFragmentRoots()[0], "TestPolicy");
		newDefinedEnumDatatype(ipsProject, new Class[]{TestEnumType.class});
        enumDatatype = ipsProject.findEnumDatatype("TestEnumType");
        
		attr = cmptType.newAttribute();
		attr.setAttributeType(AttributeType.CHANGEABLE);
		attr.setDatatype("String");
		attr.setModifier(Modifier.PUBLISHED);
		attr.setName("a");
		
        IProductCmpt productCmpt = newProductCmpt(ipsProject, "TestProduct");
        productCmpt.setPolicyCmptType(cmptType.getQualifiedName());
        productCmptGen = (IProductCmptGeneration)productCmpt.newGeneration();
        configElement = productCmptGen.newConfigElement();
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setPcTypeAttribute(attr.getName());
		processor = new FormulaCompletionProcessor(configElement);
	}
	
	/*
	 * Test method for 'org.faktorips.devtools.core.ui.editors.productcmpt.FormulaCompletionProcessor.doComputeCompletionProposals(String, int, List)'
	 */
	public void testDoComputeCompletionProposals() throws Exception {
		ArrayList results = new ArrayList();
		processor.doComputeCompletionProposals("Test", 0, results);
		assertEquals(0, results.size());
        
        attr.setDatatype(enumDatatype.getQualifiedName());
        processor.doComputeCompletionProposals("Test", 0, results);
		CompletionProposal proposal = (CompletionProposal)results.get(0);
		assertEquals(StringUtil.unqualifiedName(TestEnumType.class.getName()), proposal.getDisplayString());
		results = new ArrayList();
		processor.doComputeCompletionProposals("TestEnumType.", 0, results);
		assertEquals(3, results.size());
		ArrayList expectedValues = new ArrayList();
		for (Iterator iter = results.iterator(); iter.hasNext();) {
			proposal = (CompletionProposal) iter.next();
			expectedValues.add(proposal.getDisplayString());
		}
		assertTrue(expectedValues.contains("1"));
		assertTrue(expectedValues.contains("2"));
		assertTrue(expectedValues.contains("3"));
	}

    public void testDoComputeCompletionProposalsForMultipleTableContentsWithDateFormatName() throws Exception{
        
        ITableStructure table = (ITableStructure)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TABLE_STRUCTURE, "Testtable");
        IColumn column =table.newColumn();
        table.setTableStructureType(TableStructureType.MULTIPLE_CONTENTS);
        column.setName("first");
        column.setDatatype("String");
        column =table.newColumn();
        column.setName("second");
        column.setDatatype("String");
        IUniqueKey tableKey = table.newUniqueKey();
        tableKey.addKeyItem("second");
        
        ITableContents tableContents = (ITableContents)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TABLE_CONTENTS, "TestTable_2006-07-19");
        tableContents.setTableStructure(table.getQualifiedName());
        tableContents.newGeneration((GregorianCalendar)GregorianCalendar.getInstance());
        
        // create the table usage which will be used to resolve the available formula table access functions
        ITableContentUsage tableContentUsage = productCmptGen.newTableContentUsage();
        tableContentUsage.setTableContentName(tableContents.getQualifiedName());
        tableContentUsage.setStructureUsage("ratePlan");
        
        ArrayList results = new ArrayList();
        processor.doComputeCompletionProposals("TestTable_", 0, results);
        assertEquals(0, results.size());

        processor.doComputeCompletionProposals("ra", 0, results);
        assertEquals(1, results.size());
    }
    
    public void testDoComputeCompletionProposalsForSingleTableContents() throws Exception{
        
        ITableStructure table = (ITableStructure)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TABLE_STRUCTURE, "Testtable");
        IColumn column =table.newColumn();
        table.setTableStructureType(TableStructureType.SINGLE_CONTENT);
        column.setName("first");
        column.setDatatype("String");
        column =table.newColumn();
        column.setName("second");
        column.setDatatype("String");
        IUniqueKey tableKey = table.newUniqueKey();
        tableKey.addKeyItem("second");
        
        ITableContents tableContents = (ITableContents)newIpsObject(ipsProject.getIpsPackageFragmentRoots()[0], IpsObjectType.TABLE_CONTENTS, "Testtable");
        tableContents.setTableStructure(table.getQualifiedName());
        tableContents.newGeneration((GregorianCalendar)GregorianCalendar.getInstance());
        
        // create the table usage which will be used to resolve the available formula table access functions
        ITableContentUsage tableContentUsage = productCmptGen.newTableContentUsage();
        tableContentUsage.setTableContentName(tableContents.getQualifiedName());
        tableContentUsage.setStructureUsage("ratePlan");
        
        //there needs to be a table content available for the structure otherwise no completion is proposed
        ArrayList results = new ArrayList();
        processor.doComputeCompletionProposals("TestT", 0, results);
        assertEquals(0, results.size());
        
        results.clear();
        processor.doComputeCompletionProposals("ratePlan", 0, results);
        assertEquals(1, results.size());
    }
    
    public void testDoComputeCompletionProposalsForParam() throws Exception {
        
        IAttribute attr = cmptType.newAttribute();
        attr.setName("newAttribute");
        attr.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        Parameter param = new Parameter(0, "abcparam", Datatype.DECIMAL.getQualifiedName());
        attr.setFormulaParameters(new Parameter[] {param});
        configElement.setPcTypeAttribute(attr.getName());
        
        ArrayList results = new ArrayList();
        processor = new FormulaCompletionProcessor(configElement);
        processor.doComputeCompletionProposals("a", 1, results);
        CompletionProposal proposal = (CompletionProposal)results.get(0);
        Document document = new Document("a");
        proposal.apply(document);
        assertEquals("abcparam", document.get());
    }

}
