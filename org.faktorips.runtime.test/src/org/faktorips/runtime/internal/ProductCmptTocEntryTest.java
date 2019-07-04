/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.internal.toc.AbstractTocEntryFactory;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.ProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.TableContentTocEntry;
import org.faktorips.runtime.internal.toc.TocEntryObject;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTocEntryTest extends XmlAbstractTestCase {

    @Test
    public void testCreateFromXml() {
        Element element = getTestDocument().getDocumentElement();
        ProductCmptTocEntry entry = new AbstractTocEntryFactory.ProductCmptTocEntryFactory().createFromXml(element);
        assertEquals("motor.MotorPlus", entry.getIpsObjectId());
        assertEquals("MotorPlus", entry.getKindId());
        assertEquals("2005-01", entry.getVersionId());
        assertEquals("motor.MotorPlus", entry.getIpsObjectId());
        assertEquals("org/faktorips/samples/motor/MotorPlus.ipsproduct", entry.getXmlResourceName());
        assertEquals("org.faktorips.sample.motor.MotorPolicyPk", entry.getImplementationClassName());
        assertEquals(new DateTime(2010, 1, 16), entry.getValidTo());

        List<GenerationTocEntry> genEntries = entry.getGenerationEntries();
        assertEquals(2, genEntries.size());
        assertEquals(new DateTime(2006, 1, 1), genEntries.get(0).getValidFrom());
        assertEquals("GenerationClass", genEntries.get(0).getImplementationClassName());
        assertEquals("GenerationRessource.xml", genEntries.get(0).getXmlResourceName());
        assertEquals(new DateTime(2005, 1, 1), genEntries.get(1).getValidFrom());
    }

    @Test
    public void testToXml_WithoutGenerationEntry() {
        ProductCmptTocEntry entry = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "org/samples/MotorPolice.ipsproduct", "org.samples.MotorPolicyPk", "org.samples.MotorPolicyPkAnpStufe",
                new DateTime(2010, 1, 18));
        Element element = entry.toXml(newDocument());
        AbstractTocEntryFactory.ProductCmptTocEntryFactory productCmptTocEntryFactory = new AbstractTocEntryFactory.ProductCmptTocEntryFactory();
        entry = productCmptTocEntryFactory.createFromXml(element);
        assertEquals("MotorPolicy", entry.getIpsObjectId());
        assertEquals("MotorProduct", entry.getKindId());
        assertEquals("2005-01", entry.getVersionId());
        assertEquals("MotorPolicy", entry.getIpsObjectId());
        assertEquals("org/samples/MotorPolice.ipsproduct", entry.getXmlResourceName());
        assertEquals("org.samples.MotorPolicyPk", entry.getImplementationClassName());
        assertEquals("", entry.getGenerationImplClassName());
        assertEquals(0, entry.getGenerationEntries().size());
        assertEquals(new DateTime(2010, 1, 18), entry.getValidTo());
    }

    @Test
    public void testToXml_WithGenerationEntry() {
        ProductCmptTocEntry entry = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "org/samples/MotorPolice.ipsproduct", "org.samples.MotorPolicyPk", "org.samples.MotorPolicyPk",
                new DateTime(2010, 1, 1));
        GenerationTocEntry genEntry0 = new GenerationTocEntry(entry, new DateTime(2006, 1, 1), "class", "resource");
        GenerationTocEntry genEntry1 = new GenerationTocEntry(entry, new DateTime(2005, 1, 1), "class", "resource");
        entry.setGenerationEntries(Arrays.asList(genEntry0, genEntry1));

        Element element = entry.toXml(newDocument());
        AbstractTocEntryFactory.ProductCmptTocEntryFactory productCmptTocEntryFactory = new AbstractTocEntryFactory.ProductCmptTocEntryFactory();
        entry = productCmptTocEntryFactory.createFromXml(element);
        assertEquals(2, entry.getGenerationEntries().size());
        assertEquals(new DateTime(2006, 1, 1), entry.getGenerationEntries().get(0).getValidFrom());
        assertEquals("class", entry.getGenerationEntries().get(0).getImplementationClassName());
        assertEquals("resource", entry.getGenerationEntries().get(0).getXmlResourceName());
        assertEquals(new DateTime(2005, 1, 1), entry.getGenerationEntries().get(1).getValidFrom());
    }

    @Test
    public void testGetGenerationEntry() {
        ProductCmptTocEntry entry = new ProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "MotorPolice.ipsproduct", "java.lang.String", "java.lang.String", new DateTime(2010, 1, 1));
        Calendar effectiveDate = new GregorianCalendar(2005, 0, 1);
        assertNull(entry.getGenerationEntry(effectiveDate));
        GenerationTocEntry genEntry0 = new GenerationTocEntry(entry, new DateTime(2005, 1, 1), "class", "resource");
        GenerationTocEntry genEntry1 = new GenerationTocEntry(entry, new DateTime(2006, 1, 1), "class", "resource");
        entry.setGenerationEntries(Arrays.asList(genEntry0, genEntry1));
        assertNull(entry.getGenerationEntry(new GregorianCalendar(2004, 11, 31)));
        assertSame(genEntry0, entry.getGenerationEntry(new GregorianCalendar(2005, 0, 1)));
        assertSame(genEntry0, entry.getGenerationEntry(new GregorianCalendar(2005, 11, 31)));
        assertSame(genEntry1, entry.getGenerationEntry(new GregorianCalendar(2006, 0, 1)));
    }

    @Test
    public void testToString() {
        TocEntryObject entry = new ProductCmptTocEntry("MotorProduct 2005-01", "motor.MotorProduct 2005-01",
                "MotorProduct", "2005-01", "MotorPolice.ipsproduct", "java.lang.String", "java.lang.String",
                new DateTime(2010, 1, 1));
        assertEquals("TocEntry(ProductComponent:MotorProduct 2005-01)", entry.toString());
    }

    @Test
    public void testCreateTableTocEntry() {
        TocEntryObject toc = new TableContentTocEntry("RateTable", "RateTable", "RateTable.ipstablecontents",
                "RateTable");
        assertEquals("RateTable", toc.getIpsObjectId());
        assertEquals("RateTable.ipstablecontents", toc.getXmlResourceName());
        assertEquals("RateTable", toc.getImplementationClassName());
        assertTrue(toc instanceof TableContentTocEntry);
    }

}
