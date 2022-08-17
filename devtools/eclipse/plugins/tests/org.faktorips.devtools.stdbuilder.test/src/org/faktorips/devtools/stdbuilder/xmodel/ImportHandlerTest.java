/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class ImportHandlerTest {

    private ImportHandler importHandler;

    @Before
    public void createImportHandler() {
        importHandler = new ImportHandler("my.package", Collections.emptySet());
        // Use to run testcases against JavaCodeFragment's import logic
        // importHandler = new DelegateImportHandler();
    }

    @Test
    public void testAddImportAndReturnClassName_easyCase() {
        String className = importHandler.addImportAndReturnClassName("some.package.SomeClass");

        assertEquals("SomeClass", className);
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("some.package.SomeClass")));
    }

    @Test
    public void testAddImportAndReturnClassName_samePackageTwice() {
        importHandler.addImportAndReturnClassName("some.package.SomeClass");
        String otherClassName = importHandler.addImportAndReturnClassName("some.other.package.SomeClass");

        assertEquals("some.other.package.SomeClass", otherClassName);
    }

    @Test
    public void testAddImportAndReturnClassName_withGenerics() {
        String className = importHandler.addImportAndReturnClassName("some.package.SomeClass<org.any.Gen>");

        assertEquals("SomeClass<Gen>", className);
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("some.package.SomeClass")));
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("org.any.Gen")));
    }

    @Test
    public void testAddImportAndReturnClassName_withGenericsAndDuplTypePackage() {
        importHandler.addImportAndReturnClassName("some.any.SomeClass");
        String className = importHandler.addImportAndReturnClassName("some.package.SomeClass<org.any.Gen>");

        assertEquals("some.package.SomeClass<Gen>", className);
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("org.any.Gen")));
    }

    @Test
    public void testAddImportAndReturnClassName_withGenericsAndDuplGenPackage() {
        importHandler.addImportAndReturnClassName("any.other.Gen");
        String className = importHandler.addImportAndReturnClassName("some.package.SomeClass<org.any.Gen>");

        assertEquals("SomeClass<org.any.Gen>", className);
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("some.package.SomeClass")));
    }

    @Test
    public void testAddImportAndReturnClassName_withGenericsAndDuplPackage() {
        importHandler.addImportAndReturnClassName("any.other.SomeClass");
        importHandler.addImportAndReturnClassName("any.other.Gen");
        String className = importHandler.addImportAndReturnClassName("some.package.SomeClass<org.any.Gen>");

        assertEquals("some.package.SomeClass<org.any.Gen>", className);
    }

    @Test
    public void testAddImportAndReturnClassName_withMultipleGenerics() {
        String className = importHandler
                .addImportAndReturnClassName("some.package.SomeClass<org.any.Gen, org.anyother.Gen2>");

        assertEquals("SomeClass<Gen, Gen2>", className);
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("some.package.SomeClass")));
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("org.any.Gen")));
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("org.anyother.Gen2")));
    }

    @Test
    public void testAddImportAndReturnClassName_withNestedGenerics() {
        String className = importHandler
                .addImportAndReturnClassName("some.package.SomeClass<org.any.Gen<a.b.Nested>, org.anyother.Gen2>");

        assertEquals("SomeClass<Gen<Nested>, Gen2>", className);
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("some.package.SomeClass")));
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("org.any.Gen")));
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("a.b.Nested")));
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("org.anyother.Gen2")));
    }

    @Test
    public void testAdd() {
        importHandler.add("test.pack.TestClass");
        assertThat(importHandler.getImports(), hasItem(new ImportStatement("test.pack.TestClass")));
    }

    @Test
    public void testAddStatic() {
        importHandler.addStatic("test.pack.TestClass", "FOO");

        assertThat(importHandler.getImports(), not(hasItem(new ImportStatement("test.pack.TestClass"))));
        assertThat(importHandler.getStaticImports(), hasItem(new StaticImportStatement("test.pack.TestClass", "FOO")));
    }

    @Test
    public void testAddStatic_MultipleConstantsFromSameClass() {
        importHandler.addStatic("test.pack.TestClass", "FOO");
        importHandler.addStatic("test.pack.TestClass", "BAR");

        assertThat(importHandler.getImports(), not(hasItem(new ImportStatement("test.pack.TestClass"))));
        assertThat(importHandler.getStaticImports(), hasItem(new StaticImportStatement("test.pack.TestClass", "FOO")));
        assertThat(importHandler.getStaticImports(), hasItem(new StaticImportStatement("test.pack.TestClass", "BAR")));
    }

    @Test
    public void testAddStatic_Superclass() {
        importHandler = new ImportHandler("test.pack",
                new HashSet<>(Arrays.asList("test.pack.AClass", "a.SuperClass")));

        importHandler.addStatic("a.SuperClass", "FOO");

        assertThat(importHandler.getImports(), not(hasItem(new ImportStatement("a.SuperClass"))));
        assertThat(importHandler.getStaticImports(), not(hasItem(new StaticImportStatement("a.SuperClass", "FOO"))));
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
        // importHandler.add("Integer");
        // assertTrue(importHandler.getImports().isEmpty());
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
        assertFalse(importHandler.requiresQualifiedClassName(importStatement));
        ImportStatement secondImportStatement = importHandler.add("some.package.Integer");
        assertTrue(importHandler.requiresQualifiedClassName(secondImportStatement));
    }

    @Test
    public void testRequiresQualifiedClassName_Collision_SamePackageFirst() {
        ImportStatement importStatement = importHandler.add("my.package.SomeClass");
        assertFalse(importHandler.requiresQualifiedClassName(importStatement));
        ImportStatement secondImportStatement = importHandler.add("some.other.package.SomeClass");
        assertTrue(importHandler.requiresQualifiedClassName(secondImportStatement));
    }

    @Test
    public void testRequiresQualifiedClassName_Collision_OtherClassFirst() {
        ImportStatement importStatement = importHandler.add("some.package.Integer");
        assertFalse(importHandler.requiresQualifiedClassName(importStatement));

        ImportStatement javaLangImportStatement = importHandler.add("java.lang.Integer");
        assertTrue(importHandler.requiresQualifiedClassName(javaLangImportStatement));

        ImportStatement unqualifiedJavLangImportStatement = importHandler.add("Integer");
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
