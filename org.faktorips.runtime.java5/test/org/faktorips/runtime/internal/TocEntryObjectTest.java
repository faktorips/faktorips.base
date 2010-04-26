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

package org.faktorips.runtime.internal;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.runtime.XmlAbstractTestCase;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class TocEntryObjectTest extends XmlAbstractTestCase {

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCreateFromXml() {
        Element element = getTestDocument().getDocumentElement();
        TocEntryObject entry = TocEntryObject.createFromXml(element);
        assertEquals("motor.MotorPlus", entry.getIpsObjectId());
        assertEquals("MotorPlus", entry.getKindId());
        assertEquals("2005-01", entry.getVersionId());
        assertEquals("motor.MotorPlus", entry.getIpsObjectId());
        assertEquals("org/faktorips/samples/motor/MotorPlus.ipsproduct", entry.getXmlResourceName());
        assertEquals("org.faktorips.sample.motor.MotorPolicyPk", entry.getImplementationClassName());
        assertEquals(new DateTime(2010, 1, 16), entry.getValidTo());

        List<TocEntryGeneration> genEntries = entry.getGenerationEntries();
        assertEquals(2, genEntries.size());
        assertEquals(new DateTime(2006, 1, 1), genEntries.get(0).getValidFrom());
        assertEquals("GenerationClass", genEntries.get(0).getImplementationClassName());
        assertEquals("GenerationRessource.xml", genEntries.get(0).getXmlResourceName());
        assertEquals(new DateTime(2005, 1, 1), genEntries.get(1).getValidFrom());
    }

    public void testToXml() {
        TocEntryObject entry = TocEntryObject
                .createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                        "org/samples/MotorPolice.ipsproduct", "org.samples.MotorPolicyPk", new DateTime(2010, 1, 18));
        Element element = entry.toXml(newDocument());
        entry = TocEntryObject.createFromXml(element);
        assertEquals("MotorPolicy", entry.getIpsObjectId());
        assertEquals("MotorProduct", entry.getKindId());
        assertEquals("2005-01", entry.getVersionId());
        assertEquals("MotorPolicy", entry.getIpsObjectId());
        assertEquals("org/samples/MotorPolice.ipsproduct", entry.getXmlResourceName());
        assertEquals("org.samples.MotorPolicyPk", entry.getImplementationClassName());
        assertEquals(0, entry.getGenerationEntries().size());
        assertEquals(new DateTime(2010, 1, 18), entry.getValidTo());

        // with generation entries
        entry = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct", "2005-01",
                "org/samples/MotorPolice.ipsproduct", "org.samples.MotorPolicyPk", new DateTime(2010, 1, 1));
        TocEntryGeneration genEntry0 = new TocEntryGeneration(entry, new DateTime(2006, 1, 1), "class", "resource");
        TocEntryGeneration genEntry1 = new TocEntryGeneration(entry, new DateTime(2005, 1, 1), "class", "resource");
        entry.setGenerationEntries(Arrays.asList(genEntry0, genEntry1));

        element = entry.toXml(newDocument());
        entry = TocEntryObject.createFromXml(element);
        assertEquals(2, entry.getGenerationEntries().size());
        assertEquals(new DateTime(2006, 1, 1), entry.getGenerationEntries().get(0).getValidFrom());
        assertEquals("class", entry.getGenerationEntries().get(0).getImplementationClassName());
        assertEquals("resource", entry.getGenerationEntries().get(0).getXmlResourceName());
        assertEquals(new DateTime(2005, 1, 1), entry.getGenerationEntries().get(1).getValidFrom());
    }

    public void testGetGenerationEntry() {
        TocEntryObject entry = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorPolicy", "MotorProduct",
                "2005-01", "MotorPolice.ipsproduct", "java.lang.String", new DateTime(2010, 1, 1));
        Calendar effectiveDate = new GregorianCalendar(2005, 0, 1);
        assertNull(entry.getGenerationEntry(effectiveDate));
        TocEntryGeneration genEntry0 = new TocEntryGeneration(entry, new DateTime(2005, 1, 1), "class", "resource");
        TocEntryGeneration genEntry1 = new TocEntryGeneration(entry, new DateTime(2006, 1, 1), "class", "resource");
        entry.setGenerationEntries(Arrays.asList(genEntry0, genEntry1));
        assertNull(entry.getGenerationEntry(new GregorianCalendar(2004, 11, 31)));
        assertSame(genEntry0, entry.getGenerationEntry(new GregorianCalendar(2005, 0, 1)));
        assertSame(genEntry0, entry.getGenerationEntry(new GregorianCalendar(2005, 11, 31)));
        assertSame(genEntry1, entry.getGenerationEntry(new GregorianCalendar(2006, 0, 1)));
    }

    public void testToString() {
        TocEntryObject entry = TocEntryObject.createProductCmptTocEntry("MotorProduct 2005-01",
                "motor.MotorProduct 2005-01", "MotorProduct", "2005-01", "MotorPolice.ipsproduct", "java.lang.String",
                new DateTime(2010, 1, 1));
        assertEquals("TocEntry(productComponent:MotorProduct 2005-01)", entry.toString());
    }

    public void testCreateTableTocEntry() {
        TocEntryObject toc = TocEntryObject.createTableTocEntry("RateTable", "RateTable", "RateTable.ipstablecontents",
                "RateTable");
        assertEquals("RateTable", toc.getIpsObjectId());
        assertEquals("RateTable.ipstablecontents", toc.getXmlResourceName());
        assertEquals("RateTable", toc.getImplementationClassName());
        assertTrue(toc.isTableTocEntry());
    }
}
