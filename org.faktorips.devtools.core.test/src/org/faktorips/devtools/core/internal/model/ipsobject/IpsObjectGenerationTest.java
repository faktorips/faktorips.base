/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IpsObjectGenerationTest extends AbstractIpsPluginTest {

    private ITimedIpsObject timedObj;
    private IIpsObjectGeneration generation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // we use the ProductCmptImpl to test the TimedIpsObject class
        // because TimedIpsObject is abstract.
        IIpsProject ipsProject = newIpsProject();
        timedObj = newProductCmpt(ipsProject, "MyProduct");
        generation = timedObj.newGeneration();
    }

    @Test
    public void testGetGenerationNo() {
        assertEquals(1, generation.getGenerationNo());

        // create a new generation that begins before the generation => generation number does not
        // changes!
        generation.setValidFrom(new GregorianCalendar(2005, 1, 1));
        IIpsObjectGeneration gen2 = timedObj.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2004, 1, 1));

        assertEquals(2, generation.getGenerationNo());
        assertEquals(1, gen2.getGenerationNo());
    }

    @Test
    public void testSetValidFrom() {
        GregorianCalendar date = new GregorianCalendar(2005, 0, 1);
        generation.setValidFrom(date);
        assertEquals(date, generation.getValidFrom());
        assertTrue(timedObj.getIpsSrcFile().isDirty());
    }

    @Test
    public void testIsValidFromInPast() {
        GregorianCalendar now = new GregorianCalendar();
        generation.setValidFrom(now);
        assertFalse(generation.isValidFromInPast().booleanValue());

        GregorianCalendar yesterday = now;
        yesterday.set(Calendar.DATE, now.get(Calendar.DATE) - 1);
        generation.setValidFrom(yesterday);
        assertTrue(generation.isValidFromInPast().booleanValue());

        generation.setValidFrom(null);
        assertNull(generation.isValidFromInPast());
    }

    @Test
    public void testRemove() {
        generation.delete();
        assertEquals(0, timedObj.getNumOfGenerations());
    }

    @Test
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        generation.initFromXml(doc.getDocumentElement());
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());
    }

    @Test
    public void testToXmlDocument() {
        generation.setValidFrom(new GregorianCalendar(2005, 0, 1));
        Element element = generation.toXml(newDocument());

        generation.setValidFrom(new GregorianCalendar(2006, 0, 1));
        generation.initFromXml(element);
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());
    }

    @Test
    public void testGetValidTo() {
        generation.setValidFrom(new GregorianCalendar(2005, 1, 1));
        GregorianCalendar validTo = generation.getValidTo();
        assertNull(validTo);

        IIpsObjectGeneration gen2 = timedObj.newGeneration();
        validTo = generation.getValidTo();
        assertNull(validTo);

        GregorianCalendar date = new GregorianCalendar(2006, 1, 1);
        gen2.setValidFrom(date);

        validTo = generation.getValidTo();
        assertFalse(date.equals(validTo));

        date.setTimeInMillis(date.getTimeInMillis() - 1);
        assertEquals(date, validTo);
    }

    @Test
    public void testGetNext() throws Exception {
        IIpsObjectGeneration gen1 = timedObj.newGeneration();
        IIpsObjectGeneration gen2 = timedObj.newGeneration();
        IIpsObjectGeneration gen3 = timedObj.newGeneration();

        assertSame(gen2, gen1.getNextByValidDate());
        assertSame(gen3, gen2.getNextByValidDate());
        assertNull(gen3.getNextByValidDate());
    }

    @Test
    public void testGetPrevious() throws Exception {
        IIpsObjectGeneration gen1 = timedObj.newGeneration();
        IIpsObjectGeneration gen2 = timedObj.newGeneration();
        IIpsObjectGeneration gen3 = timedObj.newGeneration();

        assertSame(gen2, gen3.getPreviousByValidDate());
        assertSame(gen1, gen2.getPreviousByValidDate());
        assertSame(generation, gen1.getPreviousByValidDate());
        assertNull(generation.getPreviousByValidDate());
    }

}
