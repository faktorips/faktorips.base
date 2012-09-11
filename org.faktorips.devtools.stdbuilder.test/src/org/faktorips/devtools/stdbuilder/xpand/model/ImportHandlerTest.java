/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import org.junit.Before;
import org.junit.Test;

public class ImportHandlerTest {

    private ImportHandler importHandler;

    @Before
    public void createImportHandler() {
        importHandler = new ImportHandler("my.package");
    }

    @Test
    public void testAdd() throws Exception {
        importHandler.add("test.pack.TestClass");
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("test.pack.TestClass")));
    }

    @Test
    public void testAddDefaultPackageImport() {
        importHandler.add("SomeJavaClass");
        importHandler.add("AnotherJavaClass");
        assertTrue(importHandler.getImports().isEmpty());
    }

    @Test
    public void testAddPrimitiveImport() {
        importHandler.add("boolean");
        importHandler.add("int");
        assertTrue(importHandler.getImports().isEmpty());
    }

    @Test
    public void testAddJavaLang() {
        importHandler.add(String.class.getName());
        assertTrue(importHandler.getImports().isEmpty());
    }

    @Test
    public void testAddOwnPackage() {
        importHandler.add("my.package.TestClass");
        assertTrue(importHandler.getImports().isEmpty());
    }

    @Test
    public void testJavaLangPackage_NoImport() {
        importHandler.add("java.lang.Integer");
        assertTrue(importHandler.getImports().isEmpty());
    }

    @Test
    public void testClassNameCollisionImport_JavaLang() {
        importHandler.add("Integer");
        assertTrue(importHandler.getImports().isEmpty());
        importHandler.add("java.lang.Integer");
        assertTrue(importHandler.getImports().isEmpty());
        importHandler.add("org.faktorips.model.internal.Integer");
        assertFalse(importHandler.getImports().contains(new ImportStatement("org.faktorips.model.internal.Integer")));
        assertTrue(importHandler
                .requiresQualifiedClassName(new ImportStatement("org.faktorips.model.internal.Integer")));
    }

    @Test
    public void testClassNameCollisionImport_Arbitrary() {
        importHandler.add("some.package.ArbitraryClass");
        assertEquals(1, importHandler.getImports().size());
        assertTrue(importHandler.getImports().contains(new ImportStatement("some.package.ArbitraryClass")));

        importHandler.add("another.package.internal.model.ArbitraryClass");
        assertEquals(1, importHandler.getImports().size());
        assertTrue(importHandler.getImports().contains(new ImportStatement("some.package.ArbitraryClass")));
        assertFalse(importHandler.getImports().contains(
                new ImportStatement("another.package.internal.model.ArbitraryClass")));
    }

    @Test
    public void testRequiresQualifiedClassName_JavaLang() {
        ImportStatement importStatement = importHandler.add("java.lang.Integer");
        assertFalse(importHandler.requiresQualifiedClassName(importStatement));
    }

    @Test
    public void testRequiresQualifiedClassName_ImportedClass() {
        ImportStatement importStatement = importHandler.add("some.package.SomeClass");
        assertFalse(importHandler.requiresQualifiedClassName(importStatement));
    }

    @Test
    public void testRequiresQualifiedClassName_Collision_JavaLangFirst() {
        ImportStatement importStatement = importHandler.add("java.lang.Integer");
        ImportStatement secondImportStatement = importHandler.add("some.package.Integer");
        assertFalse(importHandler.requiresQualifiedClassName(importStatement));
        assertTrue(importHandler.requiresQualifiedClassName(secondImportStatement));
    }

    @Test
    public void testRequiresQualifiedClassName_Collision_SamePackageFirst() {
        ImportStatement importStatement = importHandler.add("my.package.SomeClass");
        ImportStatement secondImportStatement = importHandler.add("some.other.package.SomeClass");
        assertFalse(importHandler.requiresQualifiedClassName(importStatement));
        assertTrue(importHandler.requiresQualifiedClassName(secondImportStatement));
    }

    @Test
    public void testRequiresQualifiedClassName_Collision_OtherClassFirst() {
        ImportStatement importStatement = importHandler.add("some.package.Integer");
        ImportStatement javaLangImportStatement = importHandler.add("java.lang.Integer");
        ImportStatement unqualifiedJavLangImportStatement = importHandler.add("Integer");
        assertFalse(importHandler.requiresQualifiedClassName(importStatement));
        assertTrue(importHandler.requiresQualifiedClassName(javaLangImportStatement));
        assertTrue(importHandler.requiresQualifiedClassName(unqualifiedJavLangImportStatement));
    }

    @Test
    public void testRequiresQualifiedClassName_Collision_OtherClassFirst2() {
        ImportStatement importStatement = importHandler.add("some.package.SomeClass");
        ImportStatement myPackageImportStatement = importHandler.add("my.package.SomeClass");
        ImportStatement unqualifiedMayPackageImportStatement = importHandler.add("SomeClass");
        assertFalse(importHandler.requiresQualifiedClassName(importStatement));
        assertTrue(importHandler.requiresQualifiedClassName(myPackageImportStatement));
        assertTrue(importHandler.requiresQualifiedClassName(unqualifiedMayPackageImportStatement));
    }

    @Test
    public void testRequiresQualifiedClassName_NotImportedClass() {
        ImportStatement importStatement = new ImportStatement("some.package.SomeClass");
        assertTrue(importHandler.requiresQualifiedClassName(importStatement));
    }

}
