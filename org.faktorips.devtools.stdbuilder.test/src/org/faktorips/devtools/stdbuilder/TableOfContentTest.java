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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.devtools.model.internal.DefaultVersion;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.MyDummyTocEntryFactory.MyDummyRuntimeObject;
import org.faktorips.devtools.stdbuilder.MyDummyTocEntryFactory.MyDummyTypedTocEntryObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.CustomTocEntryObject;
import org.faktorips.runtime.internal.toc.EnumXmlAdapterTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class TableOfContentTest extends XmlAbstractTestCase {

    private TableOfContent toc;

    @Before
    public void setUp() throws Exception {
        toc = new TableOfContent();
    }

    @Test
    public void testGetTocEntry() {
        assertNull(toc.getEntry(new QualifiedNameType("Unkown", IpsObjectType.POLICY_CMPT_TYPE)));

        TocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPk", new DateTime(2010, 1, 1));
        TocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPk", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        @SuppressWarnings("unchecked")
        CustomTocEntryObject<IProductComponent> entry2 = mock(CustomTocEntryObject.class);
        when(entry2.getIpsObjectTypeId()).thenReturn(IpsObjectType.PRODUCT_CMPT.getId());
        when(entry2.getIpsObjectQualifiedName()).thenReturn("Other");
        toc.addOrReplaceTocEntry(entry2);

        assertEquals(entry0, toc.getEntry(new QualifiedNameType("MotorPolicy", IpsObjectType.PRODUCT_CMPT)));
        assertEquals(entry1, toc.getEntry(new QualifiedNameType("HomePolicy", IpsObjectType.PRODUCT_CMPT)));
        assertEquals(entry2, toc.getEntry(new QualifiedNameType("Other", IpsObjectType.PRODUCT_CMPT)));
        assertNull(toc.getEntry(new QualifiedNameType("Unkown", IpsObjectType.POLICY_CMPT_TYPE)));
    }

    @Test
    public void testGetProductCmptTocEntries() {
        assertEquals(0, toc.getEntries().size());
        TocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry0);
        assertEquals(entry0, toc.getEntries().iterator().next());

        TocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry1);
        Iterator<TocEntryObject> iterator = toc.getEntries().iterator();
        assertEquals(entry1, iterator.next());
        assertEquals(entry0, iterator.next());
    }

    @Test
    public void testAddOrReplaceTocEntry_TestCase() {
        TocEntryObject entry0 = new TestCaseTocEntry("TestCaseId", "TestCaseName", "TestCase.xml", "TestCase");
        boolean changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(toc.isModified());
        assertEquals(1, toc.getEntries().size());
        assertEquals(entry0, toc.getEntries().iterator().next());
    }

    @Test
    public void testAddOrReplaceTypedTocEntry() {
        @SuppressWarnings("unchecked")
        CustomTocEntryObject<IProductComponent> entry0 = mock(CustomTocEntryObject.class);
        when(entry0.getIpsObjectTypeId()).thenReturn(IpsObjectType.PRODUCT_CMPT.getId());
        when(entry0.getIpsObjectQualifiedName()).thenReturn("qualifiedName");
        boolean changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(toc.isModified());
        assertEquals(1, toc.getEntries().size());
        assertEquals(entry0, toc.getEntries().iterator().next());
    }

    @Test
    public void testAddOrReplaceTocEntry() {
        TocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        boolean changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(toc.isModified());
        assertEquals(1, toc.getEntries().size());
        assertEquals(entry0, toc.getEntries().iterator().next());

        toc.resetModified();
        TocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        assertTrue(changed);
        changed = toc.addOrReplaceTocEntry(entry1);
        assertTrue(toc.isModified());
        assertEquals(2, toc.getEntries().size());
        Iterator<TocEntryObject> iterator = toc.getEntries().iterator();
        assertEquals(entry1, iterator.next());
        assertEquals(entry0, iterator.next());

        // replace Motor with product component class name changed
        toc.resetModified();
        entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "org.sample.MotorPolicyPkAnpStufe",
                new DateTime(2010, 1, 1));
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(toc.isModified());
        assertEquals(2, toc.getEntries().size());
        iterator = toc.getEntries().iterator();
        assertEquals(entry1, iterator.next());
        assertEquals(entry0, iterator.next());

        // replace Motor with policy component class name changed
        toc.resetModified();
        entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk2", "org.sample.MotorPolicyPkAnpStufe",
                new DateTime(2010, 1, 1));
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(toc.isModified());
        assertEquals(2, toc.getEntries().size());
        iterator = toc.getEntries().iterator();
        assertEquals(entry1, iterator.next());
        assertEquals(entry0, iterator.next());

        // replace Motor with xml resource name changed
        toc.resetModified();
        entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "org/sample/MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk",
                "org.sample.MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(toc.isModified());
        assertEquals(2, toc.getEntries().size());
        iterator = toc.getEntries().iterator();
        assertEquals(entry1, iterator.next());
        assertEquals(entry0, iterator.next());

        // replace but without changing
        toc.resetModified();
        new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "org/sample/MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk",
                "org.sample.MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        changed = toc.addOrReplaceTocEntry(entry0);
        assertFalse(changed);
        assertFalse(toc.isModified());
        assertEquals(2, toc.getEntries().size());
        iterator = toc.getEntries().iterator();
        assertEquals(entry1, iterator.next());// !! still old entry0 !!
        assertEquals(entry0, iterator.next());

        toc.resetModified();
        TocEntryObject tocEntry = new EnumXmlAdapterTocEntry("org.faktorips.AnEnum", "org.faktorips.AnEnum",
                "org.faktorips.AnEnum");
        changed = toc.addOrReplaceTocEntry(tocEntry);
        assertTrue(changed);
        assertTrue(toc.isModified());
        assertTrue(toc.getEntries().contains(tocEntry));

        toc.resetModified();
        changed = toc.addOrReplaceTocEntry(tocEntry);
        assertFalse(changed);
        assertFalse(toc.isModified());

    }

    @Test
    public void testRemoveEntry() {
        toc.removeEntry(new QualifiedNameType("MotorProduct", IpsObjectType.PRODUCT_CMPT));
        assertFalse(toc.isModified());

        TocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        TocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        TocEntryObject entry2 = new TableContentTocEntry("RateTable", "RateTable", "RateTable.ipstablecontents",
                "RateTable");
        TocEntryObject entry3 = new TableContentTocEntry("TestTable", "TestTable", "TestTable.ipstablecontents",
                "TestTable");

        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        toc.addOrReplaceTocEntry(entry2);
        toc.addOrReplaceTocEntry(entry3);

        assertTrue(toc.isModified());
        toc.resetModified();
        toc.removeEntry(new QualifiedNameType("Unknown Product", IpsObjectType.ENUM_CONTENT));
        assertFalse(toc.isModified());

        toc.removeEntry(new QualifiedNameType(entry1.getIpsObjectQualifiedName(), IpsObjectType.PRODUCT_CMPT));
        assertTrue(toc.isModified());
        toc.resetModified();
        assertEquals(3, toc.getEntries().size());
        assertEquals(entry0, toc.getEntry(new QualifiedNameType("MotorPolicy", IpsObjectType.PRODUCT_CMPT)));

        toc.resetModified();
        toc.removeEntry(new QualifiedNameType(entry0.getIpsObjectQualifiedName(), IpsObjectType.PRODUCT_CMPT));
        assertTrue(toc.isModified());
        assertEquals(2, toc.getEntries().size());

        assertNotNull(toc.getEntry(new QualifiedNameType("TestTable", IpsObjectType.TABLE_CONTENTS)));
        toc.resetModified();
        toc.removeEntry(new QualifiedNameType(entry3.getIpsObjectQualifiedName(), IpsObjectType.TABLE_CONTENTS));
        assertTrue(toc.isModified());
        assertNull(toc.getEntry(new QualifiedNameType("TestTable", IpsObjectType.TABLE_CONTENTS)));

        assertNotNull(toc.getEntry(new QualifiedNameType("RateTable", IpsObjectType.TABLE_CONTENTS)));
        toc.resetModified();
        toc.removeEntry(new QualifiedNameType(entry2.getIpsObjectQualifiedName(), IpsObjectType.TABLE_CONTENTS));
        assertTrue(toc.isModified());
        assertNull(toc.getEntry(new QualifiedNameType("RateTable", IpsObjectType.TABLE_CONTENTS)));

        TocEntryObject tocEntry = new EnumXmlAdapterTocEntry("org.faktorips.AnEnum", "org.faktorips.AnEnum",
                "org.faktorips.AnEnum");
        toc.addOrReplaceTocEntry(tocEntry);
        toc.resetModified();
        toc.removeEntry(new QualifiedNameType(tocEntry.getIpsObjectQualifiedName(), IpsObjectType.ENUM_TYPE));
        assertTrue(toc.isModified());
        assertEquals(0, toc.getEntries().size());

    }

    @Test
    public void testToXml() throws Exception {
        TocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        TocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        TocEntryObject entry2 = new TestCaseTocEntry("TestCaseId", "TestCase", "TestCase.xml", "TestCase");
        TocEntryObject entry3 = new TableContentTocEntry("TableId", "Table", "Table.xml", "Table");
        TocEntryObject entry4 = new EnumXmlAdapterTocEntry("AnEnum", "AnEnum", "AnEnum");
        TocEntryObject entry5 = new MyDummyTypedTocEntryObject("qualifiedName");
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        toc.addOrReplaceTocEntry(entry2);
        toc.addOrReplaceTocEntry(entry3);
        toc.addOrReplaceTocEntry(entry4);
        toc.addOrReplaceTocEntry(entry5);

        Element tocElement = toc.toXml(DefaultVersion.EMPTY_VERSION, newDocument());
        assertNotNull(tocElement);
        AbstractReadonlyTableOfContents readOnlyToc = new ReadonlyTableOfContents(
                MyDummyTypedTocEntryObject.class.getClassLoader());
        readOnlyToc.initFromXml(tocElement);
        List<ProductCmptTocEntry> entries = readOnlyToc.getProductCmptTocEntries();
        assertEquals(2, entries.size());
        List<TestCaseTocEntry> testEntries = readOnlyToc.getTestCaseTocEntries();
        assertEquals(1, testEntries.size());
        List<TableContentTocEntry> tableEntries = readOnlyToc.getTableTocEntries();
        assertEquals(1, tableEntries.size());
        Set<EnumXmlAdapterTocEntry> xmlAdapterEntries = readOnlyToc.getEnumXmlAdapterTocEntries();
        assertEquals(1, xmlAdapterEntries.size());
        List<CustomTocEntryObject<MyDummyRuntimeObject>> typedTocEntries = readOnlyToc
                .getTypedTocEntries(MyDummyRuntimeObject.class);
        assertEquals(1, typedTocEntries.size());
    }

    @Test
    public void testToXml_EntriesAreOrdered() throws Exception {
        // test if the two xml representations are identical regradless of the order in that the
        // entries
        // where added
        // to do so we have to make sure, we have to entries that are stored in the same bucket in
        // the map
        String s1 = "" + (char)1 + (char)0;
        String s2 = "" + (char)0 + (char)31;
        assertEquals(s1.hashCode(), s2.hashCode()); // so they must habe the same hashcode
        toc = new TableOfContent();
        TocEntryObject entry0 = new ProductCmptTocEntry(s1, "Entry0", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpstufe", new DateTime(2010, 1, 1));
        TocEntryObject entry1 = new ProductCmptTocEntry(s2, "Entry1", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        String tocString = XmlUtil.nodeToString(toc.toXml(DefaultVersion.EMPTY_VERSION, newDocument()), "UTF-8");
        TableOfContent toc2 = new TableOfContent();
        toc2.addOrReplaceTocEntry(entry1);
        toc2.addOrReplaceTocEntry(entry0);
        String toc2String = XmlUtil.nodeToString(toc2.toXml(DefaultVersion.EMPTY_VERSION, newDocument()), "UTF-8");

        assertEquals(tocString, toc2String);

        toc2String = XmlUtil.nodeToString(toc2.toXml(new DefaultVersion("other"), newDocument()), "UTF-8");
        assertNotSame(tocString.intern(), toc2String.intern());

        Pattern versionPattern = Pattern.compile("productDataVersion=\".*?\"\\s*");
        String tocWithoutVersion = versionPattern.matcher(tocString).replaceFirst("");
        String toc2WithoutVersion = versionPattern.matcher(toc2String).replaceFirst("");

        assertEquals(tocWithoutVersion, toc2WithoutVersion);
    }

}
