/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.runtime.internal.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.TocEntryGeneration;
import org.faktorips.runtime.internal.TocEntryObject;
import org.w3c.dom.Document;

/**
 * 
 * @author Jan Ortmann 
 */
public class TocFileBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private StandardBuilderSet builderSet;
    private TableImplBuilder tableImplBuilder;
    private TocFileBuilder tocFileBuilder;
    private GregorianCalendar validFrom;
    
    /*
     * @see IpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        IIpsProjectProperties props = project.getProperties();
        project.setProperties(props);
        builderSet = (StandardBuilderSet)project.getIpsArtefactBuilderSet();
        tableImplBuilder = (TableImplBuilder)builderSet.getBuilder(TableImplBuilder.class);
        tocFileBuilder = (TocFileBuilder)builderSet.getBuilder(TocFileBuilder.class);
        validFrom = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
    }

    public void testGetToc() throws CoreException {
        
        IPolicyCmptType type = newPolicyAndProductCmptType(project, "motor.MotorPolicy", "motor.MotorProduct");
        IProductCmptType productCmptType = type.findProductCmptType(project);
        newProductCmpt(productCmptType, "motor.MotorProduct");
        
        // toc should be empty as long as the project hasn't been builder
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        MutableClRuntimeRepositoryToc toc = tocFileBuilder.getToc(root);
        assertEquals(0, toc.getProductCmptTocEntries().size());
        
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        toc = tocFileBuilder.getToc(root);
        assertEquals(1, toc.getProductCmptTocEntries().size());
    }
    
    public void testCreateTocEntryPolicyCmptType() throws CoreException {
    	IPolicyCmptType type = newPolicyCmptType(project, "test.Policy");
    	TocEntryObject entry = tocFileBuilder.createTocEntry(type);
    	assertEquals("test.Policy", entry.getIpsObjectQualifiedName());
    	assertEquals("test.Policy", entry.getIpsObjectId());
    	assertEquals("org.faktorips.sample.model.internal.test.Policy", entry.getImplementationClassName());
    	assertEquals("org/faktorips/sample/model/internal/test/Policy.xml", entry.getXmlResourceName());
    }
    
    public void testCreateTocEntryProductCmptType() throws CoreException {
    	IProductCmptType type = newProductCmptType(project, "test.Product");
    	TocEntryObject entry = tocFileBuilder.createTocEntry(type);
    	assertEquals("test.Product", entry.getIpsObjectQualifiedName());
    	assertEquals("test.Product", entry.getIpsObjectId());
    	assertEquals("org.faktorips.sample.model.internal.test.Product", entry.getImplementationClassName());
    	assertEquals("org/faktorips/sample/model/internal/test/Product.xml", entry.getXmlResourceName());
    }

    public void testCreateTocEntryTable() throws CoreException {
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "motor.RateTableStructure");
        ITableContents table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "motor.RateTable");
        ITableContentsGeneration tableGen = (ITableContentsGeneration)table.newGeneration();
        tableGen.setValidFrom(validFrom);
        table.setTableStructure(structure.getQualifiedName());
        structure.getIpsSrcFile().save(true, null);
        table.getIpsSrcFile().save(true, null);
        
        TocEntryObject entry = tocFileBuilder.createTocEntry(table);
        assertEquals("motor.RateTable", entry.getIpsObjectId());
        assertEquals(tableImplBuilder.getQualifiedClassName(structure), entry.getImplementationClassName());
        assertTrue(entry.isTableTocEntry());
    }
    
    public void testToc() throws Exception {
        
        // create a product component
        IPolicyCmptType type = newPolicyAndProductCmptType(project, "motor.MotorPolicy", "motor.MotorProduct");
        IProductCmptType productCmptType = type.findProductCmptType(project);
        IProductCmpt motorProduct = newProductCmpt(productCmptType, "motor.MotorProduct");
        
        // create a table content
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "motor.RateTableStructure");
        ITableContents table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "motor.RateTable");
        ITableContentsGeneration tableGen = (ITableContentsGeneration)table.newGeneration();
        tableGen.setValidFrom(validFrom);
        table.setTableStructure(structure.getQualifiedName());
        structure.getIpsSrcFile().save(true, null);
        table.getIpsSrcFile().save(true, null);
        
        // create another table content based on the same structure
        ITableContents table2 = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "motor.RateTable2");
        tableGen = (ITableContentsGeneration)table2.newGeneration();
        tableGen.setValidFrom(validFrom);
        table2.setTableStructure(structure.getQualifiedName());
        table2.getIpsSrcFile().save(true, null);
        
        // create a test case type and a test case
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "tests.PremiumCalcTest");
        ITestCase testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "tests.PremiumCalcTestA");
        testCase.setTestCaseType(testCaseType.getQualifiedName());
        testCase.getIpsSrcFile().save(true, null);
        
        // create a formula test case
        IProductCmpt pcFromula = newProductCmpt(productCmptType, "formulatests.PremiumCalcFormulaTest");
        IProductCmptGeneration pcdgFormula = pcFromula.getProductCmptGeneration(0);
        pcdgFormula.setValidFrom(validFrom);
        IFormula ceFormula = pcdgFormula.newFormula();
        // only if the formula contains tests then the toc entry will be created
        ceFormula.newFormulaTestCase();
        pcFromula.getIpsSrcFile().save(true, null);
        
        // now build
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IFile tocFile = project.getIpsArtefactBuilderSet().getRuntimeRepositoryTocFile(root);
        assertTrue(tocFile.exists());
        assertTrue(tocFile.isDerived());
        assertEquals(project.getXmlFileCharset(), tocFile.getCharset());
        
        Document doc = getDocumentBuilder().parse(tocFile.getContents());
        ReadonlyTableOfContents toc = new ReadonlyTableOfContents();
        toc.initFromXml(doc.getDocumentElement());

        // asserts for product cmpt entry
        List<TocEntryObject> entries = toc.getProductCmptTocEntries();
        assertEquals(2, entries.size());
        
        TocEntryObject entry0 = toc.getProductCmptTocEntry(motorProduct.getRuntimeId());
        assertNotNull(entry0);
        TocEntryGeneration genEntry0 = entry0.getGenerationEntry(validFrom);
        assertNotNull(genEntry0);
        
        // asserts for table entry
        TocEntryObject entry1 = toc.getTableTocEntryByQualifiedTableName("motor.RateTable");
        assertNotNull(entry1);
        
        // asserts for second table entry
        TocEntryObject entry2 = toc.getTableTocEntryByQualifiedTableName("motor.RateTable2");
        assertNotNull(entry2);
        
        // assert for test case entry
        TocEntryObject testCaseEntry = toc.getTestCaseTocEntryByQName("tests.PremiumCalcTestA");
        assertNotNull(testCaseEntry);
        
        // assert for formula test entry
        TocEntryObject formulaTestEntry = toc.getTestCaseTocEntryByQName(pcFromula.getQualifiedName());
        assertNotNull(formulaTestEntry);
        
        // no changes => toc file should remain untouched.
        long stamp = tocFile.getModificationStamp();
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertEquals(stamp, tocFile.getModificationStamp());
        
        // delete the product cmpt => should be removed from toc
        motorProduct.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        MutableClRuntimeRepositoryToc toc2 = tocFileBuilder.getToc(root);
        assertEquals(1, toc2.getProductCmptTocEntries().size());

        // delete the table => should be removed from toc
        table.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        toc2 = tocFileBuilder.getToc(root);
        assertNull(toc2.getTableTocEntryByQualifiedTableName("motor.RateTable"));

        // delete the second table => should be removed from toc
        table2.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        toc2 = tocFileBuilder.getToc(root);
        assertNull(toc2.getTableTocEntryByClassname("motor.RateTable2"));
        
        // delete test case => should be removed from toc
        testCase.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        toc2 = tocFileBuilder.getToc(root);
        assertNull(toc2.getTestCaseTocEntryByQName("tests.PremiumCalcTestA"));
        
        // delete the product cmpt with formulas => product cmpt and formula test should be removed from toc
        formulaTestEntry = toc2.getTestCaseTocEntryByQName(pcFromula.getQualifiedName());
        assertNotNull(formulaTestEntry);
        pcFromula.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        toc2 = tocFileBuilder.getToc(root);
        assertEquals(0, toc2.getProductCmptTocEntries().size());
        
        formulaTestEntry = toc2.getTestCaseTocEntryByQName(pcFromula.getQualifiedName());
        assertNull(formulaTestEntry);

        // check removing of table toc entries depending on the table structure type
        // create table content
        ITableContents tableEnum = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "motor.RateTableEnum");
        tableGen = (ITableContentsGeneration)tableEnum.newGeneration();
        tableGen.setValidFrom(validFrom);
        tableEnum.setTableStructure(structure.getQualifiedName());
        tableEnum.newColumn("");
        tableEnum.newColumn("");
        tableEnum.getIpsSrcFile().save(true, null);
        
        // build
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        toc = tocFileBuilder.getToc(root);
        assertNotNull(toc.getTableTocEntryByQualifiedTableName("motor.RateTableEnum"));
    }
    
    public void testIfIdenticalTocFileIsNotWrittenAfterFullBuild() throws CoreException {
        // create a product component
        IPolicyCmptType type = newPolicyAndProductCmptType(project, "motor.MotorPolicy", "motor.MotorProduct");
        IProductCmptType productCmptType = type.findProductCmptType(project);
        newProductCmpt(productCmptType, "motor.MotorProduct");
        
        // now make a full build
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IFile tocFile = project.getIpsArtefactBuilderSet().getRuntimeRepositoryTocFile(root);
        assertTrue(tocFile.exists());
        long modStamp = tocFile.getModificationStamp();
        
        // now make a seond full build
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertEquals(modStamp, tocFile.getModificationStamp());
    }
    
    public void testCreateDeleteTocEntryFormulaTest() throws Exception {
        // create a product component
        IPolicyCmptType type = newPolicyAndProductCmptType(project, "motor.MotorPolicy", "motor.MotorProduct");
        IProductCmptType productCmptType = type.findProductCmptType(project);
        IProductCmpt motorProduct = newProductCmpt(productCmptType, "motor.MotorProduct");
        IProductCmptGeneration generation = motorProduct.getProductCmptGeneration(0);
        
        // now make a full build
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        MutableClRuntimeRepositoryToc toc = tocFileBuilder.getToc(root);
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        assertEquals(0, toc.getTestCaseTocEntries().size());
        
        IFormula ce = generation.newFormula();
        IFormulaTestCase ftc = ce.newFormulaTestCase();
        
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        toc = tocFileBuilder.getToc(root);
        
        assertEquals(1, toc.getTestCaseTocEntries().size());
        
        // delete formula test of product cmpt, this triggers the deleting of the formula toc file entry
        ftc.delete();
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        toc = tocFileBuilder.getToc(root);
        
        assertEquals(0, toc.getTestCaseTocEntries().size());
    }
}
