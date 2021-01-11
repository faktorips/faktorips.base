/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.sourcecode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.faktorips.codegen.ImportDeclaration;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ImportDeclarationTest {

    @Test
    public void testImportDeclaration() {
        ImportDeclaration id = new ImportDeclaration();
        assertEquals(0, id.getNoOfImports());
    }

    @Test
    public void testImportDeclaration_ImportDeclaration_PackageName() {
        ImportDeclaration id = new ImportDeclaration();
        id.add("moredummy.DummyClass"); //$NON-NLS-1$
        id.add("dummy.*"); //$NON-NLS-1$

        assertEquals(2, new ImportDeclaration(id, "differentPackage").getNoOfImports()); //$NON-NLS-1$

        ImportDeclaration newId = new ImportDeclaration(id, "dummy"); //$NON-NLS-1$
        assertEquals(1, newId.getNoOfImports());
        assertEquals("moredummy.DummyClass", newId.iterator().next()); //$NON-NLS-1$

        newId = new ImportDeclaration(id, "moredummy"); //$NON-NLS-1$
        assertEquals(1, newId.getNoOfImports());
        assertEquals("dummy.*", newId.iterator().next()); //$NON-NLS-1$
    }

    @Test
    public void testAdd() {
        ImportDeclaration id = new ImportDeclaration();
        id.iterator();
        id.getNoOfImports();

        id.add("java.util.List"); //$NON-NLS-1$
        assertEquals(1, id.getNoOfImports());

        id.add("java.util.ArrayList"); //$NON-NLS-1$
        assertEquals(2, id.getNoOfImports());

        id.add("org.faktorips.*"); //$NON-NLS-1$
        assertEquals(3, id.getNoOfImports());

        id.add("java.util.*"); //$NON-NLS-1$
        assertEquals(2, id.getNoOfImports());

        id.add("java.util.Map"); //$NON-NLS-1$
        assertEquals(2, id.getNoOfImports());
        id.add("java.lang.Integer"); //$NON-NLS-1$
        assertEquals(2, id.getNoOfImports());
        id.add("java.lang.*"); //$NON-NLS-1$
        assertEquals(2, id.getNoOfImports());
        id.add("boolean"); //$NON-NLS-1$
        id.add("long"); //$NON-NLS-1$
        id.add("int"); //$NON-NLS-1$
        id.add("double"); //$NON-NLS-1$
        assertEquals(2, id.getNoOfImports());

        Iterator<String> it = id.iterator();
        assertEquals("org.faktorips.*", it.next()); //$NON-NLS-1$
        assertEquals("java.util.*", it.next()); //$NON-NLS-1$
        assertFalse(it.hasNext());
    }

    @Test
    public void testToString() {
        ImportDeclaration id = new ImportDeclaration();
        id.add("java.util.List"); //$NON-NLS-1$
        id.add("java.util.Iterator"); //$NON-NLS-1$

        String expected = "import java.util.List;" + System.lineSeparator() + "import java.util.Iterator;" //$NON-NLS-1$ //$NON-NLS-2$
                + System.lineSeparator();

        assertEquals(expected, id.toString());
    }

    @Test
    public void testEquals() {
        ImportDeclaration id = new ImportDeclaration();
        id.add("java.util.List"); //$NON-NLS-1$
        id.add("java.io.*"); //$NON-NLS-1$

        ImportDeclaration id2 = new ImportDeclaration();
        id2.add("java.util.List"); //$NON-NLS-1$
        id2.add("java.io.*"); //$NON-NLS-1$
        assertEquals(id, id2);

        id2 = new ImportDeclaration();
        id2.add("java.util.List"); //$NON-NLS-1$
        assertFalse(id.equals(id2));

        id2 = new ImportDeclaration();
        id2.add("java.io.*"); //$NON-NLS-1$
        assertFalse(id.equals(id2));
    }

    @Test
    public void testGetUncoveredImports() {
        ImportDeclaration decl = new ImportDeclaration();
        decl.add("java.util.List"); //$NON-NLS-1$
        decl.add("java.io.*"); //$NON-NLS-1$

        ImportDeclaration toTest = new ImportDeclaration();
        toTest.add("java.io.File"); //$NON-NLS-1$
        ImportDeclaration uncovered = decl.getUncoveredImports(toTest);
        assertEquals(0, uncovered.getNoOfImports());

        toTest.add("java.util.List"); //$NON-NLS-1$
        uncovered = decl.getUncoveredImports(toTest);
        assertEquals(0, uncovered.getNoOfImports());

        toTest.add("java.math.Math"); //$NON-NLS-1$
        uncovered = decl.getUncoveredImports(toTest);
        assertEquals(1, uncovered.getNoOfImports());

        toTest.add("java.util.ArrayList"); //$NON-NLS-1$
        uncovered = decl.getUncoveredImports(toTest);
        assertEquals(2, uncovered.getNoOfImports());

        uncovered = decl.getUncoveredImports(decl);
        assertEquals(0, uncovered.getNoOfImports());

        uncovered = decl.getUncoveredImports(null);
        assertEquals(0, uncovered.getNoOfImports());
    }

}
