package org.faktorips.devtools.core.internal.model;

import org.faktorips.runtime.ReadonlyTableOfContents;
import org.faktorips.runtime.ReadonlyTableOfContentsImpl;
import org.faktorips.runtime.TocEntryObject;
import org.faktorips.util.XmlAbstractTestCase;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class MutableClProductCmptRegistryTocTest extends XmlAbstractTestCase {

    private MutableClRuntimeRepositoryToc toc;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        toc = new MutableClRuntimeRepositoryToc();
    }
    
    public void testGetTocEntry() {
        assertNull(toc.getProductCmptTocEntry("Unkown"));
        
        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy");
        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy");
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);

        assertEquals(entry0, toc.getProductCmptTocEntry("MotorPolicy"));
        assertEquals(entry1, toc.getProductCmptTocEntry("HomePolicy"));
        assertNull(toc.getProductCmptTocEntry("Unkown"));
    }

    public void testGetProductCmptTocEntries() {
        assertEquals(0, toc.getProductCmptTocEntries().length);
        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy");
        toc.addOrReplaceTocEntry(entry0);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);

        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy");
        toc.addOrReplaceTocEntry(entry1);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
    }

    public void testAddOrReplaceTocEntry() {
        long modStamp = toc.getModificationStamp(); 
        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy");
        boolean changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(1, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);

        modStamp = toc.getModificationStamp(); 
        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy");
        assertTrue(changed);
        changed = toc.addOrReplaceTocEntry(entry1);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
        
        // replace Motor with product component class name changed
        modStamp = toc.getModificationStamp(); 
        entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "MotorPolicy");
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
        
        // replace Motor with policy component class name changed
        modStamp = toc.getModificationStamp(); 
        entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "org.sample.MotorPolicy");
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
        
        // replace Motor with xml resource name changed
        modStamp = toc.getModificationStamp(); 
        entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "org/sample/MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "org.sample.MotorPolicy");
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
        
        // replace but without changing
        modStamp = toc.getModificationStamp(); 
        TocEntryObject.createProductCmptTocEntry("MotorPolicy", "org/sample/MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "org.sample.MotorPolicy");
        changed = toc.addOrReplaceTocEntry(entry0);
        assertFalse(changed);
        assertEquals(modStamp, toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]); // !! still old entry0 !!
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
    }

    public void testRemoveEntry() {
        long modStamp = toc.getModificationStamp(); 
        toc.removeEntry(TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy"));
        assertEquals(modStamp, toc.getModificationStamp());

        
        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy");
        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy");
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);

        modStamp = toc.getModificationStamp(); 
        toc.removeEntry(TocEntryObject.createProductCmptTocEntry("Unknown", "Unknown.ipsproduct", "Unknown", "Unknown"));
        assertEquals(modStamp, toc.getModificationStamp());
        
        modStamp = toc.getModificationStamp(); 
        toc.removeEntry(TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy"));
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(1, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntry("MotorPolicy"));
        
        modStamp = toc.getModificationStamp(); 
        toc.removeEntry(TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy"));
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(0, toc.getProductCmptTocEntries().length);
    }
    
    public void testToXml() {
        TocEntryObject entry0 = TocEntryObject.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy");
        TocEntryObject entry1 = TocEntryObject.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy");
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        Element tocElement = toc.toXml(newDocument());
        assertNotNull(tocElement);
        ReadonlyTableOfContents readOnlyToc = new ReadonlyTableOfContentsImpl();
        readOnlyToc.initFromXml(tocElement);
        TocEntryObject[] entries = readOnlyToc.getProductCmptTocEntries();
        assertEquals(2, entries.length);
    }

}
