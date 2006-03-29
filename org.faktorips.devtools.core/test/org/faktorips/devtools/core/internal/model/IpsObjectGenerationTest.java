/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.IpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class IpsObjectGenerationTest extends IpsPluginTest {
    
    private ITimedIpsObject timedPdo;
    private IIpsObjectGeneration generation;
    private DefaultTestContent content;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        content = new DefaultTestContent();
        // we use the ProductCmptImpl to test the TimedIpsObject class
        // because TimedIpsObject is abstract.
        timedPdo = content.getBasicCollisionCoverage();
        generation = timedPdo.getGenerations()[0];
    }
    
    public void testGetGenerationNo() {
        assertEquals(1, generation.getGenerationNo());
        
        // create a new generation that begins before the generation => generation no changes!
        generation.setValidFrom(new GregorianCalendar(2005, 1, 1));
        IIpsObjectGeneration gen2 = timedPdo.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2004, 1, 1));
        
        assertEquals(2, generation.getGenerationNo());
        assertEquals(1, gen2.getGenerationNo());
    }
    
    public void testSetValidFrom() {
        GregorianCalendar date = new GregorianCalendar(2005, 0, 1);
        generation.setValidFrom(date);
        assertEquals(date, generation.getValidFrom());
        assertTrue(timedPdo.getIpsSrcFile().isDirty());
    }

    public void testRemove() {
        generation.delete();
        assertNull(generation.getParent());
        assertEquals(0, timedPdo.getNumOfGenerations());
    }

    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        generation.initFromXml((Element)doc.getDocumentElement());
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
    	
        IIpsObjectGeneration gen2 = timedPdo.newGeneration();    	
    	validTo = generation.getValidTo();
    	assertNull(validTo);
    	
    	GregorianCalendar date = new GregorianCalendar(2006, 1, 1);
    	gen2.setValidFrom(date);
    	
    	validTo = generation.getValidTo();
    	assertFalse(date.equals(validTo));

    	date.setTimeInMillis(date.getTimeInMillis()-1);
    	assertEquals(date, validTo);
    }

    public void testGetNext() throws Exception {
    	IIpsObjectGeneration gen1 = timedPdo.newGeneration();
    	IIpsObjectGeneration gen2 = timedPdo.newGeneration();
    	IIpsObjectGeneration gen3 = timedPdo.newGeneration();
    	
    	assertSame(gen2, gen1.getNext());
    	assertSame(gen3, gen2.getNext());
    	assertNull(gen3.getNext());
    }
    
    public void testGetPrevious() throws Exception {
    	IIpsObjectGeneration gen1 = timedPdo.newGeneration();
    	IIpsObjectGeneration gen2 = timedPdo.newGeneration();
    	IIpsObjectGeneration gen3 = timedPdo.newGeneration();
    	
    	assertSame(gen2, gen3.getPrevious());
    	assertSame(gen1, gen2.getPrevious());
    	assertSame(generation, gen1.getPrevious());
    	assertNull(generation.getPrevious());
    }
    
}
