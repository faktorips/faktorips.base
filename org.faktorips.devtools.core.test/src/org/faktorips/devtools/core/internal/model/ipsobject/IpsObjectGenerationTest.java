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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class IpsObjectGenerationTest extends AbstractIpsPluginTest {

    private ITimedIpsObject timedObj;
    private IIpsObjectGeneration generation;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // we use the ProductCmptImpl to test the TimedIpsObject class
        // because TimedIpsObject is abstract.
        IIpsProject ipsProject = newIpsProject();
        timedObj = newProductCmpt(ipsProject, "MyProduct");
        generation = timedObj.newGeneration();
    }

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

    public void testSetValidFrom() {
        GregorianCalendar date = new GregorianCalendar(2005, 0, 1);
        generation.setValidFrom(date);
        assertEquals(date, generation.getValidFrom());
        assertTrue(timedObj.getIpsSrcFile().isDirty());
    }

    public void testIsValidFromInPast() {
        GregorianCalendar now = new GregorianCalendar();
        generation.setValidFrom(now);
        assertFalse(generation.isValidFromInPast().booleanValue());

        GregorianCalendar yesterday = now;
        yesterday.set(GregorianCalendar.DATE, now.get(GregorianCalendar.DATE) - 1);
        generation.setValidFrom(yesterday);
        assertTrue(generation.isValidFromInPast().booleanValue());

        generation.setValidFrom(null);
        assertNull(generation.isValidFromInPast());
    }

    public void testRemove() {
        generation.delete();
        assertEquals(0, timedObj.getNumOfGenerations());
    }

    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        generation.initFromXml(doc.getDocumentElement());
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        generation.setValidFrom(new GregorianCalendar(2005, 0, 1));
        Element element = generation.toXml(newDocument());

        generation.setValidFrom(new GregorianCalendar(2006, 0, 1));
        generation.initFromXml(element);
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());
    }

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

    public void testGetNext() throws Exception {
        IIpsObjectGeneration gen1 = timedObj.newGeneration();
        IIpsObjectGeneration gen2 = timedObj.newGeneration();
        IIpsObjectGeneration gen3 = timedObj.newGeneration();

        assertSame(gen2, gen1.getNextByValidDate());
        assertSame(gen3, gen2.getNextByValidDate());
        assertNull(gen3.getNextByValidDate());
    }

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
