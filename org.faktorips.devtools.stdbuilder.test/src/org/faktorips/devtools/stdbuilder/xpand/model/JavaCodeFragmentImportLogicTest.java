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

import org.faktorips.codegen.JavaCodeFragment;
import org.junit.Before;
import org.junit.Test;

public class JavaCodeFragmentImportLogicTest {

    private JavaCodeFragment importHandler;

    @Before
    public void createImportHandler() {
        importHandler = new JavaCodeFragment();
    }

    @Test
    public void testAdd() throws Exception {
        importHandler.appendClassName("test.pack.TestClass");
        assertThat(importHandler.getImportDeclaration().getImports(), hasItem("test.pack.TestClass"));
    }

    @Test
    public void testAddDefaultPackageImport() {
        importHandler.appendClassName("SomeJavaClass");
        importHandler.appendClassName("AnotherJavaClass");
        assertTrue(importHandler.getImportDeclaration().getImports().isEmpty());
    }

    @Test
    public void testAddPrimitiveImport() {
        importHandler.appendClassName("boolean");
        importHandler.appendClassName("int");
        assertTrue(importHandler.getImportDeclaration().getImports().isEmpty());
    }

    @Test
    public void testAddJavaLang() {
        importHandler.appendClassName(String.class.getName());
        assertTrue(importHandler.getImportDeclaration().getImports().isEmpty());
    }

    @Test
    public void testAddOwnPackage() {
        importHandler.appendClassName("my.package.TestClass");
        assertTrue(importHandler.getImportDeclaration().getImports().isEmpty());
    }

    @Test
    public void testJavaLangPackage_NoImport() {
        importHandler.appendClassName("java.lang.Integer");
        assertTrue(importHandler.getImportDeclaration().getImports().isEmpty());
    }

    @Test
    public void testClassNameCollisionImport_JavaLang() {
        importHandler.appendClassName("Integer");
        assertTrue(importHandler.getImportDeclaration().getImports().isEmpty());
        importHandler.appendClassName("java.lang.Integer");
        assertTrue(importHandler.getImportDeclaration().getImports().isEmpty());
        importHandler.appendClassName("org.faktorips.model.internal.Integer");
        assertThat(importHandler.getImportDeclaration().getImports(), hasItem("org.faktorips.model.internal.Integer"));
    }

    @Test
    public void testClassNameCollisionImport_Arbitrary() {
        importHandler.appendClassName("some.package.ArbitraryClass");
        assertEquals(1, importHandler.getImportDeclaration().getImports().size());
        assertThat(importHandler.getImportDeclaration().getImports(), hasItem("some.package.ArbitraryClass"));

        importHandler.appendClassName("another.package.internal.model.ArbitraryClass");
        assertEquals(1, importHandler.getImportDeclaration().getImports().size());
        assertThat(importHandler.getImportDeclaration().getImports(), hasItem("some.package.ArbitraryClass"));
        assertFalse(importHandler.getImportDeclaration().getImports()
                .contains("another.package.internal.model.ArbitraryClass"));
    }

}
