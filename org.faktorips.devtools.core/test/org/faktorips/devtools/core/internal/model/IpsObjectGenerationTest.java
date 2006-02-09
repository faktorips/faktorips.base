package org.faktorips.devtools.core.internal.model;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class IpsObjectGenerationTest extends IpsObjectTestCase {
    
    private ITimedIpsObject timedPdo;
    private IIpsObjectGeneration generation;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.PRODUCT_CMPT);
    }
    
    protected void createObjectAndPart() {
        // we use the ProductCmptImpl to test the TimedIpsObject class
        // because TimedIpsObject is abstract.
        timedPdo = new ProductCmpt(pdSrcFile);
        generation = timedPdo.newGeneration();
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
        assertTrue(pdSrcFile.isDirty());
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

}
