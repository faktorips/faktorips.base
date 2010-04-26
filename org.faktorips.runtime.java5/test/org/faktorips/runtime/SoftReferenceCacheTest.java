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

package org.faktorips.runtime;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Class to test the SoftReferenceCache.
 * 
 * @see #testDummy()
 * 
 * @author Joerg Ortmann
 */
public class SoftReferenceCacheTest extends TestCase {
    private final static int DUMMY_BLOCK_SIZE_IN_MB = 42;
    private final static int ENTRY_SIZE_IN_MB = 3;

    public void testDummy() {
        // Remark: the test methods are not active, because the asserts depends on the JVM space
        // properties (e.g. -Xmx).
        // If the SoftReferenceCache should be tested then the test methods could be activated by
        // renaming
        // the methods from _test<MethodName> to test<MethodName> and manualy adapt the space usage
        // constants
        // DUMMY_BLOCK_SIZE_IN_MB and ENTRY_SIZE_IN_MB or set the java max heap size (e.g. -Xmx63m).
    }

    /*
     * Test the SoftReferenceMap several times to increase the base memory usage of the JVM, thus
     * this base memory usage shouldn't influence the tests.
     */
    public void _testASoftReferenceMap0() {
        // no OutOfMemoryError expected by using the SoftReferenceCache
        testMap(new SoftReferenceCache(5));
        System.out.println("=> Ok, no OutOfMemoryError by using SoftReferenceCache <=");
    }

    public void _testBSoftReferenceMap1() {
        // no OutOfMemoryError expected by using the SoftReferenceCache
        testMap(new SoftReferenceCache(5));
        System.out.println("=> Ok, no OutOfMemoryError by using SoftReferenceCache <=");
    }

    public void _testCHashMap() {
        boolean errorCatched = false;

        try {
            testMap(new HashMap<String, BigEntry>(5));
        } catch (Error err) {
            // Expected OutOfMemoryError error
            if (err instanceof OutOfMemoryError) {
                errorCatched = true;
                System.out.println("=> Ok, OutOfMemoryError by using HashMap<=");
            }
        }
        if (!errorCatched) {
            fail("Warning: OutOfMemoryError expected by using HashMap, but wasn't thrown. "
                    + "Decrease the JVM heap size or increase the constants in this class");
        }
    }

    public void _testDSoftReferenceMap() {
        // no OutOfMemoryError expected by using the SoftReferenceCache
        testMap(new SoftReferenceCache(60));
        System.out.println("=> Ok, no OutOfMemoryError by using SoftReferenceCache <=");
    }

    public void _testESoftReferenceMap() {
        // no OutOfMemoryError expected by using the SoftReferenceCache
        // the last 5 entries will be stored as hard references, and not removed
        // by the garbage collector
        testMap(new SoftReferenceCache(60, 5));
        System.out.println("=> Ok, no OutOfMemoryError by using SoftReferenceCache <=");
    }

    private static void print(Map<String, BigEntry> map) {
        System.out.println("One=" + map.get("One"));
        System.out.println("Two=" + map.get("Two"));
        System.out.println("Three=" + map.get("Three"));
        System.out.println("Four=" + map.get("Four"));
        System.out.println("Five=" + map.get("Five"));
        System.out.println("Six=" + map.get("Six"));
    }

    private static void print(ICache map) {
        System.out.println("One=" + map.getObject("One"));
        System.out.println("Two=" + map.getObject("Two"));
        System.out.println("Three=" + map.getObject("Three"));
        System.out.println("Four=" + map.getObject("Four"));
        System.out.println("Five=" + map.getObject("Five"));
        System.out.println("Six=" + map.getObject("Six"));
    }

    private static void testMap(Map<String, BigEntry> map) {
        System.out.println("Testing: " + map.getClass());
        map.put("One", new BigEntry(1));
        map.get("One");
        map.put("Two", new BigEntry(2));
        map.get("Two");
        map.put("Three", new BigEntry(3));
        map.get("Three");
        map.put("Four", new BigEntry(4));
        map.get("Four");
        map.put("Five", new BigEntry(5));
        map.get("Five");
        map.put("Six", new BigEntry(6));
        map.get("Six");
        print(map);
        byte[] block = new byte[DUMMY_BLOCK_SIZE_IN_MB * 1024 * 1024];
        print(map);
        block[0] = block[0];
    }

    private static void testMap(ICache map) {
        System.out.println("Testing: " + map.getClass());
        map.put("One", new BigEntry(1));
        map.getObject("One");
        map.put("Two", new BigEntry(2));
        map.getObject("Two");
        map.put("Three", new BigEntry(3));
        map.getObject("Three");
        map.put("Four", new BigEntry(4));
        map.getObject("Four");
        map.put("Five", new BigEntry(5));
        map.getObject("Five");
        map.put("Six", new BigEntry(6));
        map.getObject("Six");
        print(map);
        byte[] block = new byte[DUMMY_BLOCK_SIZE_IN_MB * 1024 * 1024];
        print(map);
        block[0] = block[0];
    }

    private static class BigEntry {
        byte[] block = new byte[ENTRY_SIZE_IN_MB * 1024 * 1024];
        private final Integer value;

        public BigEntry(int value) {
            this.value = new Integer(value);
        }

        @Override
        public String toString() {
            return "" + value;
        }
    }
}
