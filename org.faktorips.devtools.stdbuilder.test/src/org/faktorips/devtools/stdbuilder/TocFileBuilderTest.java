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

package org.faktorips.devtools.stdbuilder;

import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.runtime.internal.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.TocEntryGeneration;
import org.faktorips.runtime.internal.TocEntryObject;
import org.w3c.dom.Document;

/**
 * CVS-trigger
 * @author Jan Ortmann 
 */
public class TocFileBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private StandardBuilderSet builderSet;
    private TableImplBuilder tableImplBuilder;
    private TocFileBuilder tocFileBuilder;
    
    /*
     * @see IpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        IIpsProjectProperties props = project.getProperties();
        props.setJavaSrcLanguage(Locale.GERMAN);
        project.setProperties(props);
        builderSet = (StandardBuilderSet)project.getIpsArtefactBuilderSet();
        tableImplBuilder = (TableImplBuilder)builderSet.getBuilder(TableImplBuilder.class);
        tocFileBuilder = (TocFileBuilder)builderSet.getBuilder(TocFileBuilder.class);
    }

    public void testGetToc() throws CoreException {
        
        IPolicyCmptType type = (IPolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "motor.MotorPolicy");
        IProductCmpt motorProduct = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "motor.MotorProduct");
        motorProduct.setPolicyCmptType(type.getQualifiedName());
        GregorianCalendar validFrom = new GregorianCalendar(2006, 0, 1);
        motorProduct.newGeneration().setValidFrom(validFrom);
        type.getIpsSrcFile().save(true, null);
        
        // toc should be empty as long as the project hasn't been builder
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        MutableClRuntimeRepositoryToc toc = tocFileBuilder.getToc(root);
        assertEquals(0, toc.getProductCmptTocEntries().length);
        
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        toc = tocFileBuilder.getToc(root);
        assertEquals(1, toc.getProductCmptTocEntries().length);
    }
    
    public void testCreateTocEntryTable() throws CoreException {
        GregorianCalendar validFrom = new GregorianCalendar(2006, 0, 1);
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

        // test that enum type tables are not generated as toc entry
        structure.setTableStructureType(TableStructureType.ENUMTYPE_MODEL);
        structure.getIpsSrcFile().save(true, null);

        entry = tocFileBuilder.createTocEntry(table);
        assertNull(entry);
    }
    
    public void test() throws Exception {
        
        // create a product component
        IPolicyCmptType type = (IPolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "motor.MotorPolicy");
        IProductCmpt motorProduct = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "motor.MotorProduct");
        motorProduct.setPolicyCmptType(type.getQualifiedName());
        GregorianCalendar validFrom = new GregorianCalendar(2006, 0, 1);
        motorProduct.newGeneration().setValidFrom(validFrom);
        type.getIpsSrcFile().save(true, null);
        motorProduct.getIpsSrcFile().save(true, null);
        
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
        IProductCmpt pcFromula = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "formulatests.PremiumCalcFormulaTest");
        pcFromula.setPolicyCmptType(type.getQualifiedName());
        IProductCmptGeneration pcdgFormula = (IProductCmptGeneration) pcFromula.newGeneration();
        pcdgFormula.setValidFrom(new GregorianCalendar(2006, 0, 1));
        IConfigElement ceFormula = pcdgFormula.newConfigElement();
        ceFormula.setType(ConfigElementType.FORMULA);
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
        TocEntryObject[] entries = toc.getProductCmptTocEntries();
        assertEquals(2, entries.length);
        
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
        assertEquals(1, toc2.getProductCmptTocEntries().length);

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
        assertEquals(0, toc2.getProductCmptTocEntries().length);
        
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
        
        // change table type
        structure.setTableStructureType(TableStructureType.ENUMTYPE_MODEL);
        structure.newColumn().setName("col1");
        structure.newColumn().setName("col2");
        IUniqueKey key = structure.newUniqueKey();
        IUniqueKey key2 = structure.newUniqueKey();
        key.addKeyItem("col1");
        key2.addKeyItem("col2");
        structure.getIpsSrcFile().save(true, null);
        
        // build
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        toc = tocFileBuilder.getToc(root);
        
        // table content based on enum type and therefore no toc entry should be exists
        assertNull(toc.getTableTocEntryByQualifiedTableName("motor.RateTableEnum"));
    }
    
    public void testIfIdenticalTocFileIsNotWrittenAfterFullBuild() throws CoreException {
        // create a product component
        IPolicyCmptType type = (IPolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "motor.MotorPolicy");
        IProductCmpt motorProduct = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "motor.MotorProduct");
        motorProduct.setPolicyCmptType(type.getQualifiedName());
        GregorianCalendar validFrom = new GregorianCalendar(2006, 0, 1);
        motorProduct.newGeneration().setValidFrom(validFrom);
        type.getIpsSrcFile().save(true, null);
        motorProduct.getIpsSrcFile().save(true, null);
        
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
        IPolicyCmptType type = (IPolicyCmptType)newIpsObject(project, IpsObjectType.POLICY_CMPT_TYPE, "motor.MotorPolicy");
        IProductCmpt motorProduct = (IProductCmpt)newIpsObject(project, IpsObjectType.PRODUCT_CMPT, "motor.MotorProduct");
        motorProduct.setPolicyCmptType(type.getQualifiedName());
        GregorianCalendar validFrom = new GregorianCalendar(2006, 0, 1);
        IProductCmptGeneration generation = (IProductCmptGeneration) motorProduct.newGeneration();
        generation.setValidFrom(validFrom);
        type.getIpsSrcFile().save(true, null);
        motorProduct.getIpsSrcFile().save(true, null);
        
        // now make a full build
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        MutableClRuntimeRepositoryToc toc = tocFileBuilder.getToc(root);
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        assertEquals(0, toc.getTestCaseTocEntries().length);
        
        IConfigElement ce = generation.newConfigElement();
        ce.setType(ConfigElementType.FORMULA);
        IFormulaTestCase ftc = ce.newFormulaTestCase();
        
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        toc = tocFileBuilder.getToc(root);
        
        assertEquals(1, toc.getTestCaseTocEntries().length);
        
        // delete formula test of product cmpt, this triggers the deleting of the formula toc file entry
        ftc.delete();
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        toc = tocFileBuilder.getToc(root);
        
        assertEquals(0, toc.getTestCaseTocEntries().length);
    }
}
