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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TimedIpsObjectTest extends AbstractIpsPluginTest {

    private ITimedIpsObject timedObject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // we use the ProductCmptImpl to test the TimedIpsObject class
        // because TimedIpsObject is abstract.
        IIpsProject project = newIpsProject("TestProject");
        timedObject = newProductCmpt(project, "Product");
        ((ProductCmpt)timedObject).setRuntimeId("abc");
    }

    public void testGetChildren() throws CoreException {
        assertEquals(0, timedObject.getChildren().length);
        IIpsObjectGeneration gen = timedObject.newGeneration();
        assertEquals(1, timedObject.getChildren().length);
        assertEquals(gen, timedObject.getChildren()[0]);

        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        assertEquals(2, timedObject.getChildren().length);
        assertEquals(gen, timedObject.getChildren()[0]);
        assertEquals(gen2, timedObject.getChildren()[1]);
    }

    public void testChangesOn() {
        GregorianCalendar date = new GregorianCalendar(2005, 0, 1);
        assertFalse(timedObject.changesOn(date));
        timedObject.newGeneration().setValidFrom(new GregorianCalendar(2004, 0, 1));
        assertFalse(timedObject.changesOn(date));
        timedObject.newGeneration().setValidFrom(new GregorianCalendar(2005, 0, 1));
        assertTrue(timedObject.changesOn(date));
    }

    public void testGetGenerations() {
        IIpsObjectGeneration[] generations = timedObject.getGenerationsOrderedByValidDate();
        assertEquals(0, generations.length);

        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 1, 1));
        generations = timedObject.getGenerationsOrderedByValidDate();
        assertEquals(1, generations.length);
        assertEquals(gen1, generations[0]);

        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 1, 1));
        generations = timedObject.getGenerationsOrderedByValidDate();
        assertEquals(2, generations.length);
        assertEquals(gen1, generations[0]);
        assertEquals(gen2, generations[1]);

        // change gen2 valid from date, so that now gen2 should come first
        gen2.setValidFrom(new GregorianCalendar(2003, 1, 1));
        generations = timedObject.getGenerationsOrderedByValidDate();
        assertEquals(2, generations.length);
        assertEquals(gen2, generations[0]);
        assertEquals(gen1, generations[1]);

        // gen2 has valid from date null => is should come last
        gen2.setValidFrom(null);
        generations = timedObject.getGenerationsOrderedByValidDate();
        assertEquals(2, generations.length);
        assertEquals(gen1, generations[0]);
        assertEquals(gen2, generations[1]);

        // now also gen1 has valid from date null
        gen1.setValidFrom(null);
        generations = timedObject.getGenerationsOrderedByValidDate();
        assertEquals(2, generations.length);
        assertEquals(gen1, generations[0]);
        assertEquals(gen2, generations[1]);

    }

    public void testFindGenerationEffectiveOn() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 0, 1));

        IIpsObjectGeneration genFound = timedObject.findGenerationEffectiveOn(new GregorianCalendar(2004, 0, 1));
        assertEquals(gen1, genFound);

        genFound = timedObject.findGenerationEffectiveOn(new GregorianCalendar(2004, 3, 1));
        assertEquals(gen1, genFound);

        genFound = timedObject.findGenerationEffectiveOn(new GregorianCalendar(2005, 0, 1));
        assertEquals(gen2, genFound);

        genFound = timedObject.findGenerationEffectiveOn(new GregorianCalendar(2003, 0, 1));
        assertNull(genFound);

        genFound = timedObject.findGenerationEffectiveOn(null);
        assertNull(genFound);
    }

    public void testGetGenerationByEffectiveDate() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2004, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2005, 0, 1));

        IIpsObjectGeneration genFound = timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2004, 0, 1));
        assertEquals(gen1, genFound);

        genFound = timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2004, 3, 1));
        assertNull(genFound);

        genFound = timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2005, 0, 1));
        assertEquals(gen2, genFound);

        assertNull(timedObject.getGenerationByEffectiveDate(null));

        gen1.setValidFrom(null);
        assertNull(timedObject.getGenerationByEffectiveDate(null));
        assertNull(timedObject.getGenerationByEffectiveDate(new GregorianCalendar(2004, 0, 1)));
    }

    public void testNewGeneration() {
        IIpsObjectGeneration gen = timedObject.newGeneration();
        assertEquals(timedObject, gen.getParent());
        assertEquals(1, timedObject.getNumOfGenerations());
        assertEquals(gen, timedObject.getGenerationsOrderedByValidDate()[0]);
    }

    public void testInitFromXml() {
        Document doc = getTestDocument();
        timedObject.initFromXml(doc.getDocumentElement());
        assertEquals(2, timedObject.getNumOfGenerations());
    }

    public void testToXml() {
        IIpsObjectGeneration gen1 = timedObject.newGeneration();
        gen1.setValidFrom(new GregorianCalendar(2005, 0, 1));
        IIpsObjectGeneration gen2 = timedObject.newGeneration();
        gen2.setValidFrom(new GregorianCalendar(2006, 0, 1));

        Element element = timedObject.toXml(newDocument());
        gen1.delete();
        gen2.delete();
        timedObject.initFromXml(element);
        assertEquals(2, timedObject.getNumOfGenerations());
    }
}
