/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.internal.toc.AbstractReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.ReadonlyTableOfContents;
import org.faktorips.runtime.internal.toc.TocEntryObject;
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

}
