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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.runtime.ReadonlyTableOfContentsImpl;
import org.faktorips.runtime.TocEntryGeneration;
import org.faktorips.runtime.TocEntryObject;
import org.w3c.dom.Document;

/**
 * 
 * @author Jan Ortmann
 */
public class TocFileBuilderTest extends IpsPluginTest {

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
        builderSet = (StandardBuilderSet)project.getCurrentArtefactBuilderSet();
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
        assertEquals("motor.RateTable", entry.getIpsObjectName());
        assertEquals(tableImplBuilder.getQualifiedClassName(structure), entry.getImplementationClassName());
        assertTrue(entry.isTableTocEntry());
        assertNull(entry.getPcTypeClassName());
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
        
        // now build
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IFile tocFile = project.getCurrentArtefactBuilderSet().getRuntimeRepositoryTocFile(root);
        assertTrue(tocFile.exists());
        assertEquals(project.getXmlFileCharset(), tocFile.getCharset());
        
        Document doc = getDocumentBuilder().parse(tocFile.getContents());
        ReadonlyTableOfContentsImpl toc = new ReadonlyTableOfContentsImpl();
        toc.initFromXml(doc.getDocumentElement());

        // asserts for product cmpt entry
        TocEntryObject[] entries = toc.getProductCmptTocEntries();
        assertEquals(1, entries.length);
        
        TocEntryObject entry0 = toc.getProductCmptTocEntry("motor.MotorProduct");
        assertNotNull(entry0);
        TocEntryGeneration genEntry0 = entry0.getGenerationEntry(validFrom);
        assertNotNull(genEntry0);
        
        // asserts for table entry
        TocEntryObject entry1 = toc.getTableTocEntryByClassname(tableImplBuilder.getQualifiedClassName(structure));
        assertNotNull(entry1);
        
        // no changes => toc file should remain untouched.
        long stamp = tocFile.getModificationStamp();
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        assertEquals(stamp, tocFile.getModificationStamp());
        
        // delete the product cmpt => should be removed from toc
        motorProduct.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        MutableClRuntimeRepositoryToc toc2 = tocFileBuilder.getToc(root);
        assertEquals(0, toc2.getProductCmptTocEntries().length);

        // delete the table => should be removed from toc
        table.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        project.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        toc2 = tocFileBuilder.getToc(root);
        assertNull(toc2.getTableTocEntryByClassname(tableImplBuilder.getQualifiedClassName(structure)));
    }
    


}
