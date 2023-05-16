/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.stdbuilder.xtend.enumtype.EnumTypeBuilder;
import org.faktorips.devtools.stdbuilder.xtend.table.TableBuilder;
import org.faktorips.runtime.internal.toc.EnumContentTocEntry;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 *
 * @author Jan Ortmann
 */
public class TocFileBuilderTest extends AbstractStdBuilderTest {

    private TableBuilder tableBuilder;
    private TocFileBuilder tocFileBuilder;
    private GregorianCalendar validFrom;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        tableBuilder = builderSet.getTableBuilder();
        tocFileBuilder = builderSet.getBuilderById(BuilderKindIds.TOC_FILE, TocFileBuilder.class);
        validFrom = new GregorianCalendar();
    }

    @Test
    public void testGetToc() {

        IPolicyCmptType type = newPolicyAndProductCmptType(ipsProject, "motor.MotorPolicy", "motor.MotorProduct");
        IProductCmptType productCmptType = type.findProductCmptType(ipsProject);
        newProductCmpt(productCmptType, "motor.MotorProduct 2021-03");

        // toc should be empty as long as the project hasn't been built
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        TableOfContent toc = tocFileBuilder.getToc(root);
        assertEquals(0, toc.getEntries().size());

        ipsProject.getProject().build(ABuildKind.FULL, null);
        toc = tocFileBuilder.getToc(root);
        assertEquals(3, toc.getEntries().size());
    }

    @Test
    public void testCreateTocEntryProductCmptWithGeneration() {
        IPolicyCmptType type = newPolicyAndProductCmptType(ipsProject, "test.PolicyType", "test.ProductCmpt");
        IProductCmptType productCmptType = type.findProductCmptType(ipsProject);
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "test.ProductCmpt");

        ProductCmptTocEntry entry = tocFileBuilder.createTocEntry(productCmpt);

        assertEquals("test.ProductCmpt", entry.getIpsObjectQualifiedName());
        assertEquals("ProductCmpt", entry.getIpsObjectId());
        assertEquals("org.faktorips.sample.model.internal.test.ProductCmpt", entry.getImplementationClassName());
        assertFalse(entry.getGenerationEntries().isEmpty());
        assertEquals("org.faktorips.sample.model.internal.test.ProductCmptGen", entry.getGenerationImplClassName());
        assertEquals("org/faktorips/sample/model/internal/test/ProductCmpt.ipsproduct", entry.getXmlResourceName());
    }

    @Test
    public void testCreateTocEntryProductCmptWithoutGeneration() {
        IPolicyCmptType type = newPolicyAndProductCmptType(ipsProject, "test.PolicyType", "test.ProductCmpt");
        IProductCmptType productCmptType = type.findProductCmptType(ipsProject);
        productCmptType.setChangingOverTime(false);
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "test.ProductCmpt");

        ProductCmptTocEntry entry = tocFileBuilder.createTocEntry(productCmpt);

        assertEquals("test.ProductCmpt", entry.getIpsObjectQualifiedName());
        assertEquals("ProductCmpt", entry.getIpsObjectId());
        assertEquals("org.faktorips.sample.model.internal.test.ProductCmpt", entry.getImplementationClassName());
        assertTrue(entry.getGenerationEntries().isEmpty());
        assertEquals("org/faktorips/sample/model/internal/test/ProductCmpt.ipsproduct", entry.getXmlResourceName());
    }

    @Test
    public void testCreateTocEntryPolicyCmptType() {
        IPolicyCmptType type = newPolicyCmptType(ipsProject, "test.Policy");
        TocEntryObject entry = tocFileBuilder.createTocEntry(type);
        assertEquals("test.Policy", entry.getIpsObjectQualifiedName());
        assertEquals("test.Policy", entry.getIpsObjectId());
        assertEquals("org.faktorips.sample.model.internal.test.Policy", entry.getImplementationClassName());
    }

    @Test
    public void testCreateTocEntryProductCmptType() {
        IProductCmptType type = newProductCmptType(ipsProject, "test.Product");

        TocEntryObject entry = tocFileBuilder.createTocEntry(type);

        assertEquals("test.Product", entry.getIpsObjectQualifiedName());
        assertEquals("test.Product", entry.getIpsObjectId());
        assertEquals("org.faktorips.sample.model.internal.test.Product", entry.getImplementationClassName());
    }

    @Test
    public void testCreateTocEntryTable() {
        ITableStructure structure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
                "motor.RateTableStructure");
        ITableContents table = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS,
                "motor.RateTable");
        table.newTableRows();
        table.setTableStructure(structure.getQualifiedName());
        structure.getIpsSrcFile().save(null);
        table.getIpsSrcFile().save(null);

        TocEntryObject entry = tocFileBuilder.createTocEntry(table);

        assertEquals("motor.RateTable", entry.getIpsObjectId());
        assertEquals(tableBuilder.getQualifiedClassName(structure), entry.getImplementationClassName());
        assertTrue(entry instanceof TableContentTocEntry);
    }

    @Test
    public void testCreateTocEntry_RealEnum() {
        IEnumType enumType = newEnumType(ipsProject, "test.JavaEnum");
        enumType.setExtensible(false);
        IEnumAttribute idAttribute = enumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        idAttribute.setIdentifier(true);
        idAttribute.setUnique(true);
        IEnumAttribute nameAttribute = enumType.newEnumAttribute();
        nameAttribute.setName("name");
        nameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        enumType.newEnumLiteralNameAttribute();
        IEnumValue enumValue = enumType.newEnumValue();
        enumValue.setEnumAttributeValue(idAttribute, ValueFactory.createStringValue("a"));
        enumValue.setEnumAttributeValue(nameAttribute, ValueFactory.createStringValue("A"));
        enumType.getIpsSrcFile().save(null);

        TocEntryObject entry = tocFileBuilder.createEmptyEnumContentTocEntry(enumType);

        assertEquals("productdef.test.JavaEnum", entry.getIpsObjectId());
        EnumTypeBuilder enumTypeBuilder = builderSet.getEnumTypeBuilder();
        assertEquals(enumTypeBuilder.getQualifiedClassName(enumType), entry.getImplementationClassName());
        assertTrue(entry instanceof EnumContentTocEntry);
    }

    @Test
    public void testCreateTocEntry_SeparatedEnum() {
        IEnumType enumType = newEnumType(ipsProject, "test.JavaEnum");
        enumType.setExtensible(true);
        IEnumAttribute idAttribute = enumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        idAttribute.setIdentifier(true);
        idAttribute.setUnique(true);
        IEnumAttribute nameAttribute = enumType.newEnumAttribute();
        nameAttribute.setName("name");
        nameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        enumType.newEnumLiteralNameAttribute();
        IEnumValue enumValue = enumType.newEnumValue();
        enumValue.setEnumAttributeValue(idAttribute, ValueFactory.createStringValue("a"));
        enumValue.setEnumAttributeValue(nameAttribute, ValueFactory.createStringValue("A"));
        enumType.getIpsSrcFile().save(null);

        TocEntryObject entry = tocFileBuilder.createEmptyEnumContentTocEntry(enumType);

        assertEquals("productdef.test.JavaEnum", entry.getIpsObjectId());
        EnumTypeBuilder enumTypeBuilder = builderSet.getEnumTypeBuilder();
        assertEquals(enumTypeBuilder.getQualifiedClassName(enumType), entry.getImplementationClassName());
        assertTrue(entry instanceof EnumContentTocEntry);
    }

    @Test
    public void testCreateTocEntry_AbstractEnum() {
        IEnumType enumType = newEnumType(ipsProject, "test.JavaEnum");
        enumType.setExtensible(true);
        enumType.setIdentifierBoundary("e");
        enumType.setAbstract(true);
        IEnumAttribute idAttribute = enumType.newEnumAttribute();
        idAttribute.setName("id");
        idAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        idAttribute.setIdentifier(true);
        idAttribute.setUnique(true);
        IEnumAttribute nameAttribute = enumType.newEnumAttribute();
        nameAttribute.setName("name");
        nameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        enumType.newEnumLiteralNameAttribute();
        IEnumValue enumValue = enumType.newEnumValue();
        enumValue.setEnumAttributeValue(idAttribute, ValueFactory.createStringValue("a"));
        enumValue.setEnumAttributeValue(nameAttribute, ValueFactory.createStringValue("A"));
        enumType.getIpsSrcFile().save(null);

        TocEntryObject entry = tocFileBuilder.createEmptyEnumContentTocEntry(enumType);

        assertNull(entry);
    }

    /*
     * The arrows with numbers ( => 7 ) counting the actual size of the TOC
     */
    @Test
    public void testToc() throws Exception {
        // create a product component: policyCmptType => 1 productCmptType => 2 productCmpt => 3
        IPolicyCmptType type = newPolicyAndProductCmptType(ipsProject, "motor.MotorPolicy", "motor.MotorProduct");
        IProductCmptType productCmptType = type.findProductCmptType(ipsProject);
        IProductCmpt motorProduct = newProductCmpt(productCmptType, "motor.MotorProduct 2021-03");

        // create a table content => 4
        ITableStructure structure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
                "motor.RateTableStructure");
        ITableContents table = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS,
                "motor.RateTable");
        table.newTableRows();
        table.setTableStructure(structure.getQualifiedName());
        structure.getIpsSrcFile().save(null);
        table.getIpsSrcFile().save(null);

        // create another table content based on the same structure => 5
        ITableStructure structure2 = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
                "motor.RateTableStructure2");
        ITableContents table2 = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS,
                "motor.RateTable2");
        table2.newTableRows();
        table2.setTableStructure(structure2.getQualifiedName());
        table2.getIpsSrcFile().save(null);

        // create a test case type and a test case => 6
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE,
                "tests.PremiumCalcTest");
        ITestCase testCase = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "tests.PremiumCalcTestA");
        testCase.setTestCaseType(testCaseType.getQualifiedName());
        testCase.getIpsSrcFile().save(null);

        // now build
        ipsProject.getProject().build(ABuildKind.FULL, null);
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        IFile tocFile = ipsProject.getIpsArtefactBuilderSet().getRuntimeRepositoryTocFile(root).unwrap();
        assertTrue(tocFile.exists());
        assertTrue(tocFile.isDerived());
        assertEquals(ipsProject.getXmlFileCharset(), tocFile.getCharset());

        // asserted objects in toc:
        // type, productCmptType, motorProduct, table, table2, testCase
        assertEquals(6, tocFileBuilder.getToc(root).getEntries().size());

        Document doc = getDocumentBuilder().parse(tocFile.getContents());
        ReadonlyTableOfContents toc = new ReadonlyTableOfContents();
        toc.initFromXml(doc.getDocumentElement());

        // asserts for product cmpt entry
        List<ProductCmptTocEntry> entries = toc.getProductCmptTocEntries();
        assertEquals(1, entries.size());

        ProductCmptTocEntry entry0 = toc.getProductCmptTocEntry(motorProduct.getRuntimeId());
        assertNotNull(entry0);
        GenerationTocEntry genEntry0 = entry0.getGenerationEntry(validFrom);
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

        // no changes => toc file should remain untouched.
        long stamp = tocFile.getModificationStamp();
        ipsProject.getProject().build(ABuildKind.INCREMENTAL, null);
        assertEquals(stamp, tocFile.getModificationStamp());
        assertEquals(6, tocFileBuilder.getToc(root).getEntries().size());

        // delete the product cmpt => should be removed from toc => 5
        motorProduct.getIpsSrcFile().getCorrespondingFile().delete(null);
        ipsProject.getProject().build(ABuildKind.INCREMENTAL, null);
        assertEquals(5, tocFileBuilder.getToc(root).getEntries().size());

        // delete the table => should be removed from toc => 4
        table.getIpsSrcFile().getCorrespondingFile().delete(null);
        ipsProject.getProject().build(ABuildKind.INCREMENTAL, null);
        assertNull(tocFileBuilder.getToc(root)
                .getEntry(new QualifiedNameType("motor.RateTable", IpsObjectType.TABLE_CONTENTS)));
        assertEquals(4, tocFileBuilder.getToc(root).getEntries().size());

        // delete the second table => should be removed from toc => 3
        table2.getIpsSrcFile().getCorrespondingFile().delete(null);
        ipsProject.getProject().build(ABuildKind.INCREMENTAL, null);
        assertNull(tocFileBuilder.getToc(root)
                .getEntry(new QualifiedNameType("motor.RateTable2", IpsObjectType.TABLE_CONTENTS)));
        assertEquals(3, tocFileBuilder.getToc(root).getEntries().size());

        // delete test case => should be removed from toc => 2
        testCase.getIpsSrcFile().getCorrespondingFile().delete(null);
        ipsProject.getProject().build(ABuildKind.INCREMENTAL, null);
        assertNull(tocFileBuilder.getToc(root)
                .getEntry(new QualifiedNameType("tests.PremiumCalcTestA", IpsObjectType.TABLE_CONTENTS)));
        assertEquals(2, tocFileBuilder.getToc(root).getEntries().size());

        // check removing of table toc entries depending on the table structure type
        // create table content
        ITableStructure tableStructure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
                "motor.RateTableStructure3");
        ITableContents tableContent = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS,
                "motor.RateTableContent");
        tableStructure.newColumn();
        tableStructure.newColumn();
        tableContent.newTableRows();
        tableContent.setTableStructure(tableStructure.getQualifiedName());
        tableContent.newColumn("", "");
        tableContent.newColumn("", "");
        tableContent.getIpsSrcFile().save(null);

        // build
        ipsProject.getProject().build(ABuildKind.INCREMENTAL, null);
        TableOfContent builderToc = tocFileBuilder.getToc(root);
        assertNotNull(
                builderToc.getEntry(new QualifiedNameType("motor.RateTableContent", IpsObjectType.TABLE_CONTENTS)));
    }

    @Ignore
    // not supported at the moment
    @Test
    public void testIfIdenticalTocFileIsNotWrittenAfterFullBuild() {
        // create a product component
        IPolicyCmptType type = newPolicyAndProductCmptType(ipsProject, "motor.MotorPolicy", "motor.MotorProduct");
        IProductCmptType productCmptType = type.findProductCmptType(ipsProject);
        newProductCmpt(productCmptType, "motor.MotorProduct");

        // now make a full build
        ipsProject.getProject().build(ABuildKind.FULL, null);
        IIpsPackageFragmentRoot root = ipsProject.getIpsPackageFragmentRoots()[0];
        AFile tocFile = ipsProject.getIpsArtefactBuilderSet().getRuntimeRepositoryTocFile(root);
        assertTrue(tocFile.exists());
        long modStamp = tocFile.getModificationStamp();

        // now make a seond full build
        ipsProject.getProject().build(ABuildKind.FULL, null);
        assertEquals(modStamp, tocFile.getModificationStamp());
    }

}
