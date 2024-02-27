/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.faktorips.runtime.DummyTocEntryFactory;
import org.faktorips.runtime.DummyTocEntryFactory.DummyTypedTocEntryObject;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Test;

/**
 *
 * @author Jan Ortmann
 */
public class ReadonlyTableOfContentsTest extends XmlAbstractTestCase {

    @Test
    public void testInitFromXml() {
        AbstractReadonlyTableOfContents toc = new ReadonlyTableOfContents();

        toc.initFromXml(getTestDocument().getDocumentElement());

        assertEquals(1, toc.getProductCmptTocEntries().size());
        assertEquals(2, toc.getModelTypeTocEntries().size());
        assertEquals(3, toc.getEnumContentTocEntries().size());
        assertEquals("org/faktorips/sample/OptionContent2.xml",
                toc.getEnumContentTocEntry("org.faktorips.sample.Option2").getXmlResourceName());
        assertEquals("org/faktorips/sample/OptionContent3.xml",
                toc.getEnumContentTocEntry("org.faktorips.sample.Option3").getXmlResourceName());
    }

    @Test
    public void testGetProductDataVersion() {
        AbstractReadonlyTableOfContents toc = new ReadonlyTableOfContents();

        toc.initFromXml(getTestDocument("ReadonlyTableOfContentsTest_withVersion.xml").getDocumentElement());
        assertThat(toc.getProductDataVersion(), is("1.0.1"));
    }

    @Test
    public void testGetProductDataVersion_Maven() {
        AbstractReadonlyTableOfContents toc = new ReadonlyTableOfContents();

        toc.initFromXml(getTestDocument("ReadonlyTableOfContentsTest_withMaven.xml").getDocumentElement());
        assertThat(toc.getProductDataVersion(), is("47.11"));
    }

    @Test
    public void testGetProductDataVersion_MavenFallback() {
        AbstractReadonlyTableOfContents toc = new ReadonlyTableOfContents();

        toc.initFromXml(getTestDocument("ReadonlyTableOfContentsTest_withMavenNoPom.xml").getDocumentElement());
        assertThat(toc.getProductDataVersion(), startsWith("0.0.0.local"));
    }

    @Test
    public void testGetProductDataVersion_NoVersion() {
        AbstractReadonlyTableOfContents toc = new ReadonlyTableOfContents();

        toc.initFromXml(getTestDocument().getDocumentElement());
        assertThat(toc.getProductDataVersion(), is(""));
    }

    @Test
    public void testGetEntries() {
        ReadonlyTableOfContents toc = new ReadonlyTableOfContents();

        toc.initFromXml(getTestDocument("ReadonlyTableOfContentsTest_wrongOrder.xml").getDocumentElement());

        List<TocEntryObject> entries = toc.getEntries();

        assertEquals("Coverage", entries.get(0).getIpsObjectQualifiedName());
        assertEquals("Product", entries.get(1).getIpsObjectQualifiedName());
        assertEquals("ProductWithGen", entries.get(2).getIpsObjectQualifiedName());
        assertEquals("a.Enum", entries.get(3).getIpsObjectQualifiedName());
        assertEquals("a.Enum2", entries.get(4).getIpsObjectQualifiedName());
        assertEquals("b.Product", entries.get(5).getIpsObjectQualifiedName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInternalRemoveEntry_NotModifiable() {
        ReadonlyTableOfContents toc = new ReadonlyTableOfContents();
        toc.initFromXml(getTestDocument().getDocumentElement());

        toc.internalRemoveEntry(toc.getProductCmptTocEntry("motor.MotorProduct2005"));
    }

    @Test
    public void testInternalRemoveEntry() {
        ReadonlyTableOfContents toc = new ReadonlyTableOfContents() {
            @Override
            protected boolean isModifiable() {
                return true;
            }
        };
        toc.initFromXml(getTestDocument("ReadonlyTableOfContentsTest_allTypes.xml").getDocumentElement());
        ProductCmptTocEntry productCmptTocEntry = toc.getProductCmptTocEntry("motor.MotorProduct2005");
        EnumContentTocEntry enumContentTocEntry = toc.getEnumContentTocEntry("org.faktorips.sample.Option1");
        Map<String, ModelTypeTocEntry> modelTypeTocEntries = asMap(toc.getModelTypeTocEntries());
        ModelTypeTocEntry policyCmptTypeTocEntry = modelTypeTocEntries.get("sample:MotorPolicy");
        ModelTypeTocEntry productCmptTypeTocEntry = modelTypeTocEntries.get("sample:MotorProduct");
        EnumXmlAdapterTocEntry enumXmlAdapterTocEntry = asMap(toc.getEnumXmlAdapterTocEntries())
                .get("enums.boundary.EnumWithLongBoundary");
        TableContentTocEntry tableContentTocEntry = toc
                .getTableTocEntryByQualifiedTableName("tables.TableContentsWithNull");
        TestCaseTocEntry testCaseTocEntry = toc.getTestCaseTocEntryByQName("testcaseTests.TcProductAndCoverageTest1");
        DummyTypedTocEntryObject customTocEntry = new DummyTocEntryFactory.DummyTypedTocEntryObject("foo.bar");
        toc.internalAddEntry(customTocEntry);

        assertTrue(toc.internalRemoveEntry(productCmptTocEntry));
        assertTrue(toc.internalRemoveEntry(enumContentTocEntry));
        assertTrue(toc.internalRemoveEntry(policyCmptTypeTocEntry));
        assertTrue(toc.internalRemoveEntry(productCmptTypeTocEntry));
        assertTrue(toc.internalRemoveEntry(enumXmlAdapterTocEntry));
        assertTrue(toc.internalRemoveEntry(tableContentTocEntry));
        assertTrue(toc.internalRemoveEntry(testCaseTocEntry));
        assertTrue(toc.internalRemoveEntry(customTocEntry));

        assertFalse(toc.internalRemoveEntry(productCmptTocEntry));
        assertFalse(toc.internalRemoveEntry(enumContentTocEntry));
        assertFalse(toc.internalRemoveEntry(policyCmptTypeTocEntry));
        assertFalse(toc.internalRemoveEntry(productCmptTypeTocEntry));
        assertFalse(toc.internalRemoveEntry(enumXmlAdapterTocEntry));
        assertFalse(toc.internalRemoveEntry(tableContentTocEntry));
        assertFalse(toc.internalRemoveEntry(testCaseTocEntry));
        assertFalse(toc.internalRemoveEntry(customTocEntry));

    }

    @Test
    public void testInternalRemoveEntry_UnknownCustomType() {
        ReadonlyTableOfContents toc = new ReadonlyTableOfContents() {
            @Override
            protected boolean isModifiable() {
                return true;
            }
        };
        toc.initFromXml(getTestDocument().getDocumentElement());
        DummyTypedTocEntryObject customTocEntry = new DummyTocEntryFactory.DummyTypedTocEntryObject("foo.bar");

        assertFalse(toc.internalRemoveEntry(customTocEntry));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInternalRemoveEntry_UnknownEntryType() {
        ReadonlyTableOfContents toc = new ReadonlyTableOfContents() {
            @Override
            protected boolean isModifiable() {
                return true;
            }
        };
        toc.initFromXml(getTestDocument().getDocumentElement());
        TocEntryObject unknownTocEntryType = new TocEntryObject("id", "qName", "xml", "class") {

            @Override
            protected String getXmlElementTag() {
                return "X";
            }
        };

        toc.internalRemoveEntry(unknownTocEntryType);
    }

    private <T extends TocEntryObject> Map<String, T> asMap(Set<T> tocEntries) {
        return tocEntries.stream().collect(Collectors.toMap(TocEntryObject::getIpsObjectId, Function.identity()));
    }

}
