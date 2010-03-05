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

import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.test.XmlAbstractTestCase;
import org.faktorips.runtime.internal.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.DateTime;
import org.faktorips.runtime.internal.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.TocEntryObject;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class MutableClRuntimeRepositoryTocTest extends XmlAbstractTestCase {

    private MutableClRuntimeRepositoryToc toc;

    @Override
    protected void setUp() throws Exception {
        toc = new MutableClRuntimeRepositoryToc();
    }

    public void testGetTocEntry() {
        assertNull(toc.getProductCmptTocEntry("Unkown"));

        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct",
                "2005-01", "MotorProduct2005.ipsproduct", "MotorPolicyPk", new DateTime(2010, 1, 1));
        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct",
                "2005-01", "HomeProduct2005.ipsproduct", "HomePolicyPk", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);

        assertEquals(entry0, toc.getProductCmptTocEntry("MotorPolicy"));
        assertEquals(entry1, toc.getProductCmptTocEntry("HomePolicy"));
        assertNull(toc.getProductCmptTocEntry("Unkown"));
    }

    public void testGetProductCmptTocEntries() {
        assertEquals(0, toc.getProductCmptTocEntries().size());
        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct",
                "2005-01", "MotorProduct2005.ipsproduct", "MotorPolicyPk", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry0);
        assertEquals(entry0, toc.getProductCmptTocEntries().get(0));

        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct",
                "2005-01", "HomeProduct2005.ipsproduct", "HomePolicyPk", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry1);
        assertEquals(entry0, toc.getProductCmptTocEntries().get(0));
        assertEquals(entry1, toc.getProductCmptTocEntries().get(1));
    }

    public void testAddOrReplaceTocEntry_TestCase() {
        long modStamp = toc.getModificationStamp();
        TocEntryObject entry0 = TocEntryObject.createTestCaseTocEntry("TestCaseId", "TestCaseName", "TestCase.xml",
                "TestCase");
        boolean changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp != toc.getModificationStamp());
        assertEquals(1, toc.getTestCaseTocEntries().size());
        assertEquals(entry0, toc.getTestCaseTocEntries().get(0));
    }

    public void testAddOrReplaceTocEntry() {
        long modStamp = toc.getModificationStamp();
        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct",
                "2005-01", "MotorProduct2005.ipsproduct", "MotorPolicyPk", new DateTime(2010, 1, 1));
        boolean changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp != toc.getModificationStamp());
        assertEquals(1, toc.getProductCmptTocEntries().size());
        assertEquals(entry0, toc.getProductCmptTocEntries().get(0));

        modStamp = toc.getModificationStamp();
        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct",
                "2005-01", "HomeProduct2005.ipsproduct", "HomePolicyPk", new DateTime(2010, 1, 1));
        assertTrue(changed);
        changed = toc.addOrReplaceTocEntry(entry1);
        assertTrue(modStamp != toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().size());
        assertEquals(entry0, toc.getProductCmptTocEntries().get(0));
        assertEquals(entry1, toc.getProductCmptTocEntries().get(1));

        // replace Motor with product component class name changed
        modStamp = toc.getModificationStamp();
        entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", new DateTime(2010, 1, 1));
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp != toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().size());
        assertEquals(entry0, toc.getProductCmptTocEntries().get(0));
        assertEquals(entry1, toc.getProductCmptTocEntries().get(1));

        // replace Motor with policy component class name changed
        modStamp = toc.getModificationStamp();
        entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", new DateTime(2010, 1, 1));
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp != toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().size());
        assertEquals(entry0, toc.getProductCmptTocEntries().get(0));
        assertEquals(entry1, toc.getProductCmptTocEntries().get(1));

        // replace Motor with xml resource name changed
        modStamp = toc.getModificationStamp();
        entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "org/sample/MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", new DateTime(2010, 1, 1));
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp != toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().size());
        assertEquals(entry0, toc.getProductCmptTocEntries().get(0));
        assertEquals(entry1, toc.getProductCmptTocEntries().get(1));

        // replace but without changing
        modStamp = toc.getModificationStamp();
        TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "org/sample/MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", new DateTime(2010, 1, 1));
        changed = toc.addOrReplaceTocEntry(entry0);
        assertFalse(changed);
        assertEquals(modStamp, toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().size());
        assertEquals(entry0, toc.getProductCmptTocEntries().get(0)); // !! still old entry0 !!
        assertEquals(entry1, toc.getProductCmptTocEntries().get(1));

        modStamp = toc.getModificationStamp();
        TocEntryObject tocEntry = TocEntryObject.createEnumXmlAdapterTocEntry("org.faktorips.AnEnum",
                "org.faktorips.AnEnum");
        changed = toc.addOrReplaceTocEntry(tocEntry);
        assertTrue(changed);
        assertTrue(modStamp != toc.getModificationStamp());
        assertTrue(toc.getEnumXmlAdapterTocEntries().contains(tocEntry));

        modStamp = toc.getModificationStamp();
        changed = toc.addOrReplaceTocEntry(tocEntry);
        assertFalse(changed);
        assertEquals(modStamp, toc.getModificationStamp());

    }

    public void testRemoveEntry() {
        long modStamp = toc.getModificationStamp();
        toc.removeEntry("MotorProduct");
        assertEquals(modStamp, toc.getModificationStamp());

        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct",
                "2005-01", "MotorProduct2005.ipsproduct", "MotorPolicyPk", new DateTime(2010, 1, 1));
        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct",
                "2005-01", "HomeProduct2005.ipsproduct", "HomePolicyPk", new DateTime(2010, 1, 1));
        TocEntryObject entry2 = TocEntryObject.createTableTocEntry("RateTable", "RateTable",
                "RateTable.ipstablecontents", "RateTable");
        TocEntryObject entry3 = TocEntryObject.createTableTocEntry("TestTable", "TestTable",
                "TestTable.ipstablecontents", "TestTable");

        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        toc.addOrReplaceTocEntry(entry2);
        toc.addOrReplaceTocEntry(entry3);

        modStamp = toc.getModificationStamp();
        toc.removeEntry("Unknown Product");
        assertEquals(modStamp, toc.getModificationStamp());

        modStamp = toc.getModificationStamp();
        toc.removeEntry(entry1.getIpsObjectQualifiedName());
        assertTrue(modStamp != toc.getModificationStamp());
        assertEquals(1, toc.getProductCmptTocEntries().size());
        assertEquals(entry0, toc.getProductCmptTocEntry("MotorPolicy"));

        modStamp = toc.getModificationStamp();
        toc.removeEntry(entry0.getIpsObjectId());
        assertTrue(modStamp != toc.getModificationStamp());
        assertEquals(0, toc.getProductCmptTocEntries().size());

        assertNotNull(toc.getTableTocEntryByQualifiedTableName("TestTable"));
        modStamp = toc.getModificationStamp();
        toc.removeEntry(entry3.getIpsObjectId());
        assertTrue(modStamp != toc.getModificationStamp());
        assertNull(toc.getTableTocEntryByQualifiedTableName("TestTable"));

        assertNotNull(toc.getTableTocEntryByClassname("RateTable"));
        modStamp = toc.getModificationStamp();
        toc.removeEntry(entry2.getIpsObjectId());
        assertTrue(modStamp != toc.getModificationStamp());
        assertNull(toc.getTableTocEntryByClassname("RateTable"));

        TocEntryObject tocEntry = TocEntryObject.createEnumXmlAdapterTocEntry("org.faktorips.AnEnum",
                "org.faktorips.AnEnum");
        toc.addOrReplaceTocEntry(tocEntry);
        modStamp = toc.getModificationStamp();
        toc.removeEntry(tocEntry.getIpsObjectId());
        assertTrue(modStamp != toc.getModificationStamp());
        assertEquals(0, toc.getEnumXmlAdapterTocEntries().size());

    }

    public void testToXml() throws Exception {
        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct",
                "2005-01", "MotorProduct2005.ipsproduct", "MotorPolicyPk", new DateTime(2010, 1, 1));
        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomePolicy", "MotorProduct",
                "2005-01", "HomeProduct2005.ipsproduct", "HomePolicyPk", new DateTime(2010, 1, 1));
        TocEntryObject entry2 = TocEntryObject.createTestCaseTocEntry("TestCaseId", "TestCase", "TestCase.xml",
                "TestCase");
        TocEntryObject entry3 = TocEntryObject.createTableTocEntry("TableId", "Table", "Table.xml", "Table");
        TocEntryObject entry4 = TocEntryObject.createEnumXmlAdapterTocEntry("AnEnum", "AnEnum");
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        toc.addOrReplaceTocEntry(entry2);
        toc.addOrReplaceTocEntry(entry3);
        toc.addOrReplaceTocEntry(entry4);

        Element tocElement = toc.toXml(newDocument());
        assertNotNull(tocElement);
        AbstractReadonlyTableOfContents readOnlyToc = new ReadonlyTableOfContents();
        readOnlyToc.initFromXml(tocElement);
        List<TocEntryObject> entries = readOnlyToc.getProductCmptTocEntries();
        assertEquals(2, entries.size());
        entries = readOnlyToc.getTestCaseTocEntries();
        assertEquals(1, entries.size());
        entries = readOnlyToc.getTableTocEntries();
        assertEquals(1, entries.size());
        Set<TocEntryObject> xmlAdapterEntries = readOnlyToc.getEnumXmlAdapterTocEntries();
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
        toc = new MutableClRuntimeRepositoryToc();
        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry(s1, "Entry0", "MotorProduct", "2005-01",
                "MotorProduct2005.ipsproduct", "MotorPolicyPk", new DateTime(2010, 1, 1));
        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry(s2, "Entry1", "MotorProduct", "2005-01",
                "HomeProduct2005.ipsproduct", "HomePolicyPk", new DateTime(2010, 1, 1));
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        String tocString = XmlUtil.nodeToString(toc.toXml(newDocument()), "UTF-8");
        MutableClRuntimeRepositoryToc toc2 = new MutableClRuntimeRepositoryToc();
        toc2.addOrReplaceTocEntry(entry1);
        toc2.addOrReplaceTocEntry(entry0);
        String toc2String = XmlUtil.nodeToString(toc2.toXml(newDocument()), "UTF-8");
        assertEquals(tocString, toc2String);
    }

}
