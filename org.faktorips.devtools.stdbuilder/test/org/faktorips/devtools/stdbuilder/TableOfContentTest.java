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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.EnumXmlAdapterTocEntry;
import org.faktorips.runtime.internal.toc.IEnumXmlAdapterTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.faktorips.runtime.internal.toc.ITocEntryObject;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TestCaseTocEntry;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class TableOfContentTest extends XmlAbstractTestCase {

    private TableOfContent toc;

    @Override
    protected void setUp() throws Exception {
        toc = new TableOfContent();
    }

    public void testGetTocEntry() {
        assertNull(toc.getEntry(new QualifiedNameType("Unkown", IpsObjectType.POLICY_CMPT_TYPE)));

        ITocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPk", new DateTime(2010, 1, 1));
        ITocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPk", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);

        assertEquals(entry0, toc.getEntry(new QualifiedNameType("MotorPolicy", IpsObjectType.PRODUCT_CMPT)));
        assertEquals(entry1, toc.getEntry(new QualifiedNameType("HomePolicy", IpsObjectType.PRODUCT_CMPT)));
        assertNull(toc.getEntry(new QualifiedNameType("Unkown", IpsObjectType.POLICY_CMPT_TYPE)));
    }

    public void testGetProductCmptTocEntries() {
        assertEquals(0, toc.getEntries().size());
        ITocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry0);
        assertEquals(entry0, toc.getEntries().iterator().next());

        ITocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry1);
        Iterator<ITocEntryObject> iterator = toc.getEntries().iterator();
        assertEquals(entry1, iterator.next());
        assertEquals(entry0, iterator.next());
    }

    public void testAddOrReplaceTocEntry_TestCase() {
        ITocEntryObject entry0 = new TestCaseTocEntry("TestCaseId", "TestCaseName", "TestCase.xml", "TestCase");
        boolean changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(toc.isModified());
        assertEquals(1, toc.getEntries().size());
        assertEquals(entry0, toc.getEntries().iterator().next());
    }

    public void testAddOrReplaceTocEntry() {
        ITocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        boolean changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(toc.isModified());
        assertEquals(1, toc.getEntries().size());
        assertEquals(entry0, toc.getEntries().iterator().next());

        toc.resetModified();
        ITocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        assertTrue(changed);
        changed = toc.addOrReplaceTocEntry(entry1);
        assertTrue(toc.isModified());
        assertEquals(2, toc.getEntries().size());
        Iterator<ITocEntryObject> iterator = toc.getEntries().iterator();
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
                "MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "org.sample.MotorPolicyPkAnpStufe",
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
        ITocEntryObject tocEntry = new EnumXmlAdapterTocEntry("org.faktorips.AnEnum", "org.faktorips.AnEnum",
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

    public void testRemoveEntry() {
        toc.removeEntry(new QualifiedNameType("MotorProduct", IpsObjectType.PRODUCT_CMPT));
        assertFalse(toc.isModified());

        ITocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        ITocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        ITocEntryObject entry2 = new TableContentTocEntry("RateTable", "RateTable", "RateTable.ipstablecontents",
                "RateTable");
        ITocEntryObject entry3 = new TableContentTocEntry("TestTable", "TestTable", "TestTable.ipstablecontents",
                "TestTable");

        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        toc.addOrReplaceTocEntry(entry2);
        toc.addOrReplaceTocEntry(entry3);

        assertTrue(toc.isModified());
        toc.resetModified();
        toc.removeEntry(new QualifiedNameType("Unknown Product", IpsObjectType.BUSINESS_FUNCTION));
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

        ITocEntryObject tocEntry = new EnumXmlAdapterTocEntry("org.faktorips.AnEnum", "org.faktorips.AnEnum",
                "org.faktorips.AnEnum");
        toc.addOrReplaceTocEntry(tocEntry);
        toc.resetModified();
        toc.removeEntry(new QualifiedNameType(tocEntry.getIpsObjectQualifiedName(), IpsObjectType.ENUM_TYPE));
        assertTrue(toc.isModified());
        assertEquals(0, toc.getEntries().size());

    }

    public void testToXml() throws Exception {
        ITocEntryObject entry0 = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpStufe", new DateTime(2010, 1, 1));
        ITocEntryObject entry1 = new ProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        ITocEntryObject entry2 = new TestCaseTocEntry("TestCaseId", "TestCase", "TestCase.xml", "TestCase");
        ITocEntryObject entry3 = new TableContentTocEntry("TableId", "Table", "Table.xml", "Table");
        ITocEntryObject entry4 = new EnumXmlAdapterTocEntry("AnEnum", "AnEnum", "AnEnum");
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        toc.addOrReplaceTocEntry(entry2);
        toc.addOrReplaceTocEntry(entry3);
        toc.addOrReplaceTocEntry(entry4);

        Element tocElement = toc.toXml(newDocument());
        assertNotNull(tocElement);
        AbstractReadonlyTableOfContents readOnlyToc = new ReadonlyTableOfContents();
        readOnlyToc.initFromXml(tocElement);
        List<IProductCmptTocEntry> entries = readOnlyToc.getProductCmptTocEntries();
        assertEquals(2, entries.size());
        List<ITestCaseTocEntry> testEntries = readOnlyToc.getTestCaseTocEntries();
        assertEquals(1, testEntries.size());
        List<ITableContentTocEntry> tableEntries = readOnlyToc.getTableTocEntries();
        assertEquals(1, tableEntries.size());
        Set<IEnumXmlAdapterTocEntry> xmlAdapterEntries = readOnlyToc.getEnumXmlAdapterTocEntries();
        assertEquals(1, xmlAdapterEntries.size());
    }

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
        ITocEntryObject entry0 = new ProductCmptTocEntry(s1, "Entry0", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicyPkAnpstufe", new DateTime(2010, 1, 1));
        ITocEntryObject entry1 = new ProductCmptTocEntry(s2, "Entry1", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicyPkAnpStufe", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        String tocString = XmlUtil.nodeToString(toc.toXml(newDocument()), "UTF-8");
        TableOfContent toc2 = new TableOfContent();
        toc2.addOrReplaceTocEntry(entry1);
        toc2.addOrReplaceTocEntry(entry0);
        String toc2String = XmlUtil.nodeToString(toc2.toXml(newDocument()), "UTF-8");
        assertEquals(tocString, toc2String);
    }

}
