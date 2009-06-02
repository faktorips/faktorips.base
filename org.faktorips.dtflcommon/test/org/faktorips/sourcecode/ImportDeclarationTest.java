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

    /**
     * Constructor for ImportDeclarationTest.
     * 
     * @param name
     */
    public ImportDeclarationTest(String name) {
        super(name);
    }

    public void testImportDeclaration() {
        ImportDeclaration id = new ImportDeclaration();
        assertEquals(0, id.getNoOfImports());
    }

    public void testImportDeclaration_ImportDeclaration_PackageName() {
        ImportDeclaration id = new ImportDeclaration();
        id.add("moredummy.DummyClass");
        id.add("dummy.*");

        assertEquals(2, new ImportDeclaration(id, "differentPackage").getNoOfImports());

        ImportDeclaration newId = new ImportDeclaration(id, "dummy");
        assertEquals(1, newId.getNoOfImports());
        assertEquals("moredummy.DummyClass", newId.iterator().next());

        newId = new ImportDeclaration(id, "moredummy");
        assertEquals(1, newId.getNoOfImports());
        assertEquals("dummy.*", newId.iterator().next());
    }

    public void testAdd() {
        ImportDeclaration id = new ImportDeclaration();
        id.iterator();
        id.getNoOfImports();

        id.add("java.util.List");
        assertEquals(1, id.getNoOfImports());

        id.add("java.util.ArrayList");
        assertEquals(2, id.getNoOfImports());

        id.add("org.faktorips.*");
        assertEquals(3, id.getNoOfImports());

        id.add("java.util.*");
        assertEquals(2, id.getNoOfImports());

        id.add("java.util.Map");
        assertEquals(2, id.getNoOfImports());
        id.add("java.lang.Integer");
        assertEquals(2, id.getNoOfImports());
        id.add("java.lang.*");
        assertEquals(2, id.getNoOfImports());
        id.add("boolean");
        id.add("long");
        id.add("int");
        id.add("double");
        assertEquals(2, id.getNoOfImports());

        Iterator it = id.iterator();
        assertEquals("org.faktorips.*", it.next());
        assertEquals("java.util.*", it.next());
        assertFalse(it.hasNext());
    }

    public void testToString() {
        ImportDeclaration id = new ImportDeclaration();
        id.add("java.util.List");
        id.add("java.util.Iterator");

        String expected = "import java.util.List;" + SystemUtils.LINE_SEPARATOR + "import java.util.Iterator;"
                + SystemUtils.LINE_SEPARATOR;

        assertEquals(expected, id.toString());
    }

    public void testEquals() {
        ImportDeclaration id = new ImportDeclaration();
        id.add("java.util.List");
        id.add("java.io.*");

        ImportDeclaration id2 = new ImportDeclaration();
        id2.add("java.util.List");
        id2.add("java.io.*");
        assertEquals(id, id2);

        id2 = new ImportDeclaration();
        id2.add("java.util.List");
        assertFalse(id.equals(id2));

        id2 = new ImportDeclaration();
        id2.add("java.io.*");
        assertFalse(id.equals(id2));
    }

    public void testGetUncoveredImports() {
        ImportDeclaration decl = new ImportDeclaration();
        decl.add("java.util.List");
        decl.add("java.io.*");

        ImportDeclaration toTest = new ImportDeclaration();
        toTest.add("java.io.File");
        ImportDeclaration uncovered = decl.getUncoveredImports(toTest);
        assertEquals(0, uncovered.getNoOfImports());

        toTest.add("java.util.List");
        uncovered = decl.getUncoveredImports(toTest);
        assertEquals(0, uncovered.getNoOfImports());

        toTest.add("java.math.Math");
        uncovered = decl.getUncoveredImports(toTest);
        assertEquals(1, uncovered.getNoOfImports());

        toTest.add("java.util.ArrayList");
        uncovered = decl.getUncoveredImports(toTest);
        assertEquals(2, uncovered.getNoOfImports());

        uncovered = decl.getUncoveredImports(decl);
        assertEquals(0, uncovered.getNoOfImports());

        uncovered = decl.getUncoveredImports(null);
        assertEquals(0, uncovered.getNoOfImports());

    }
}
