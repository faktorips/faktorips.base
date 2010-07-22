/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.sourcecode;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.ImportDeclaration;

/**
 * 
 * @author Jan Ortmann
 */
public class ImportDeclarationTest extends TestCase {

    public ImportDeclarationTest(String name) {
        super(name);
    }

    public void testImportDeclaration() {
        ImportDeclaration id = new ImportDeclaration();
        assertEquals(0, id.getNoOfImports());
    }

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

    public void testToString() {
        ImportDeclaration id = new ImportDeclaration();
        id.add("java.util.List"); //$NON-NLS-1$
        id.add("java.util.Iterator"); //$NON-NLS-1$

        String expected = "import java.util.List;" + SystemUtils.LINE_SEPARATOR + "import java.util.Iterator;" //$NON-NLS-1$ //$NON-NLS-2$
                + SystemUtils.LINE_SEPARATOR;

        assertEquals(expected, id.toString());
    }

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
