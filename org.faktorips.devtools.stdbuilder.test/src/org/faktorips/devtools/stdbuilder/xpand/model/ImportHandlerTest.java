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

}
