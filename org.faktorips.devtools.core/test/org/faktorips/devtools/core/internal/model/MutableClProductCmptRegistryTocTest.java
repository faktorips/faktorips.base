package org.faktorips.devtools.core.internal.model;

import org.faktorips.runtime.ReadonlyTableOfContents;
import org.faktorips.runtime.ReadonlyTableOfContentsImpl;
import org.faktorips.runtime.TocEntry;
import org.faktorips.util.XmlTestCase;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class MutableClProductCmptRegistryTocTest extends XmlTestCase {

    private MutableClRuntimeRepositoryToc toc;
    private ClassLoader cl = MutableClProductCmptRegistryTocTest.class.getClassLoader();
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        toc = new MutableClRuntimeRepositoryToc();
    }
    
    public void testGetTocEntry() {
        assertNull(toc.getProductCmptTocEntry("Unkown"));
        
        TocEntry entry0 = TocEntry.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy", cl);
        TocEntry entry1 = TocEntry.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy", cl);
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);

        assertEquals(entry0, toc.getProductCmptTocEntry("MotorPolicy"));
        assertEquals(entry1, toc.getProductCmptTocEntry("HomePolicy"));
        assertNull(toc.getProductCmptTocEntry("Unkown"));
    }

    public void testGetProductCmptTocEntries() {
        assertEquals(0, toc.getProductCmptTocEntries().length);
        TocEntry entry0 = TocEntry.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy", cl);
        toc.addOrReplaceTocEntry(entry0);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);

        TocEntry entry1 = TocEntry.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy", cl);
        toc.addOrReplaceTocEntry(entry1);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
    }

    public void testAddOrReplaceTocEntry() {
        long modStamp = toc.getModificationStamp(); 
        TocEntry entry0 = TocEntry.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy", cl);
        boolean changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(1, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);

        modStamp = toc.getModificationStamp(); 
        TocEntry entry1 = TocEntry.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy", cl);
        assertTrue(changed);
        changed = toc.addOrReplaceTocEntry(entry1);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
        
        // replace Motor with product component class name changed
        modStamp = toc.getModificationStamp(); 
        entry0 = TocEntry.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "MotorPolicy", cl);
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
        
        // replace Motor with policy component class name changed
        modStamp = toc.getModificationStamp(); 
        entry0 = TocEntry.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "org.sample.MotorPolicy", cl);
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
        
        // replace Motor with xml resource name changed
        modStamp = toc.getModificationStamp(); 
        entry0 = TocEntry.createProductCmptTocEntry("MotorPolicy", "org/sample/MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "org.sample.MotorPolicy", cl);
        changed = toc.addOrReplaceTocEntry(entry0);
        assertTrue(changed);
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]);
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
        
        // replace but without changing
        modStamp = toc.getModificationStamp(); 
        TocEntry.createProductCmptTocEntry("MotorPolicy", "org/sample/MotorProduct2005.ipsproduct", "org.sample.MotorPolicyPk", "org.sample.MotorPolicy", cl);
        changed = toc.addOrReplaceTocEntry(entry0);
        assertFalse(changed);
        assertEquals(modStamp, toc.getModificationStamp());
        assertEquals(2, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntries()[0]); // !! still old entry0 !!
        assertEquals(entry1, toc.getProductCmptTocEntries()[1]);
    }

    public void testRemoveEntry() {
        long modStamp = toc.getModificationStamp(); 
        toc.removeEntry(TocEntry.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy", cl));
        assertEquals(modStamp, toc.getModificationStamp());

        
        TocEntry entry0 = TocEntry.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy", cl);
        TocEntry entry1 = TocEntry.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy", cl);
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);

        modStamp = toc.getModificationStamp(); 
        toc.removeEntry(TocEntry.createProductCmptTocEntry("Unknown", "Unknown.ipsproduct", "Unknown", "Unknown", cl));
        assertEquals(modStamp, toc.getModificationStamp());
        
        modStamp = toc.getModificationStamp(); 
        toc.removeEntry(TocEntry.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy", cl));
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(1, toc.getProductCmptTocEntries().length);
        assertEquals(entry0, toc.getProductCmptTocEntry("MotorPolicy"));
        
        modStamp = toc.getModificationStamp(); 
        toc.removeEntry(TocEntry.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy", cl));
        assertTrue(modStamp!=toc.getModificationStamp());
        assertEquals(0, toc.getProductCmptTocEntries().length);
    }
    
    public void testToXml() {
        TocEntry entry0 = TocEntry.createProductCmptTocEntry("MotorPolicy", "MotorProduct2005.ipsproduct", "MotorPolicyPk", "MotorPolicy", cl);
        TocEntry entry1 = TocEntry.createProductCmptTocEntry("HomePolicy", "HomeProduct2005.ipsproduct", "HomePolicyPk", "HomePolicy", cl);
        toc.addOrReplaceTocEntry(entry0);
        toc.addOrReplaceTocEntry(entry1);
        Element tocElement = toc.toXml(newDocument());
        assertNotNull(tocElement);
        ReadonlyTableOfContents readOnlyToc = new ReadonlyTableOfContentsImpl();
        readOnlyToc.initFromXml(tocElement, cl);
        TocEntry[] entries = readOnlyToc.getProductCmptTocEntries();
        assertEquals(2, entries.length);
    }

}
