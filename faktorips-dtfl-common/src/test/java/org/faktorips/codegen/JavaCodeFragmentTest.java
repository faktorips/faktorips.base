/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class JavaCodeFragmentTest {

    @Test
    public void testGetImportDeclaration() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(JavaCodeFragment.class);
        fragment.appendClassName(Map.class);
        String pack = ClassNameUtil.getPackageName(JavaCodeFragment.class.getName());
        ImportDeclaration decl = fragment.getImportDeclaration(pack);
        assertEquals(1, decl.getNoOfImports());
        assertThat(decl.getImports(), hasItem(Map.class.getName()));
    }

    @Test
    public void testJavaCodeFragment() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        assertEquals("", fragment.getSourcecode()); //$NON-NLS-1$
        assertEquals(new ImportDeclaration(), fragment.getImportDeclaration());
    }

    @Test
    public void testAppendClassName() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(List.class);
        assertEquals("List", fragment.getSourcecode()); //$NON-NLS-1$
        assertEquals(1, fragment.getImportDeclaration().getNoOfImports());
        assertTrue(fragment.getImportDeclaration().isCovered(List.class));

        fragment = new JavaCodeFragment();
        fragment.appendClassName(List.class.getName() + "[]"); //$NON-NLS-1$
        assertEquals("List[]", fragment.getSourcecode()); //$NON-NLS-1$
        assertEquals(1, fragment.getImportDeclaration().getNoOfImports());
        assertTrue(fragment.getImportDeclaration().isCovered(List.class));

        fragment = new JavaCodeFragment();
        fragment.appendClassName(List.class.getName() + "[][]"); //$NON-NLS-1$
        assertEquals("List[][]", fragment.getSourcecode()); //$NON-NLS-1$
        assertEquals(1, fragment.getImportDeclaration().getNoOfImports());
        assertTrue(fragment.getImportDeclaration().isCovered(List.class));
    }

    @Test
    public void testAppendClassName_oneGeneric() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName("any.package.Type<any.other.GenType>"); //$NON-NLS-1$
        assertEquals("Type<GenType>", fragment.getSourcecode()); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.package.Type")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.other.GenType")); //$NON-NLS-1$
    }

    @Test
    public void testAppendClassName_twoGeneric() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName("any.package.Type<any.other.GenType, another.GenType2>"); //$NON-NLS-1$
        assertEquals("Type<GenType, GenType2>", fragment.getSourcecode()); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.package.Type")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.other.GenType")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("another.GenType2")); //$NON-NLS-1$
    }

    @Test
    public void testAppendClassName_twoGenericSameClassDifferentPackage() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName("any.package.Type<any.other.GenType, another.GenType>"); //$NON-NLS-1$
        assertEquals("Type<GenType, another.GenType>", fragment.getSourcecode()); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.package.Type")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.other.GenType")); //$NON-NLS-1$
    }

    @Test
    public void testAppendClassName_onlyGenerics() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName("<any.other.GenType, another.GenType>"); //$NON-NLS-1$
        assertEquals("<GenType, another.GenType>", fragment.getSourcecode()); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.other.GenType")); //$NON-NLS-1$
    }

    @Test
    public void testAppendClassName_nestedGeneric() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(
                "any.package.Type<any.other.GenType, another.GenType2<xy.A, xy.B<any.other.GenType>>>"); //$NON-NLS-1$
        assertEquals("Type<GenType, GenType2<A, B<GenType>>>", fragment.getSourcecode()); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.package.Type")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.other.GenType")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("another.GenType2")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("xy.A")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("xy.B")); //$NON-NLS-1$
    }

    @Test
    public void testAppendClassName_nestedGeneric2() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(
                "any.package.Type<any.other.GenType<ab.X,ab.Y>, another.GenType2<xy.A, xy.B<any.other.GenType>, F>, H>"); //$NON-NLS-1$
        assertEquals("Type<GenType<X, Y>, GenType2<A, B<GenType>, F>, H>", fragment.getSourcecode()); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.package.Type")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("any.other.GenType")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("another.GenType2")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("xy.A")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("xy.B")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("ab.X")); //$NON-NLS-1$
        assertThat(fragment.getImportDeclaration().getImports(), hasItem("ab.Y")); //$NON-NLS-1$
    }

    @Test
    public void testAppendInnerClassName() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendInnerClassName(Entry.class);
        assertEquals("Entry", fragment.getSourcecode()); //$NON-NLS-1$
        assertEquals(1, fragment.getImportDeclaration().getNoOfImports());
        assertTrue(fragment.getImportDeclaration().isCovered(Entry.class.getName().replaceAll("\\$", "\\."))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testAppend_Fragment() {
        JavaCodeFragmentBuilder builder1 = new JavaCodeFragmentBuilder(true);
        builder1.openBracket();
        builder1.appendClassName(Map.class);
        builder1.appendln();
        builder1.closeBracket();
        JavaCodeFragment fragment1 = builder1.getFragment();

        JavaCodeFragmentBuilder builder2 = new JavaCodeFragmentBuilder(true);
        builder2.openBracket();
        builder2.appendClassName(List.class);
        builder2.appendln();
        builder2.append(fragment1);
        builder2.closeBracket();
        JavaCodeFragment fragment2 = builder2.getFragment();

        assertEquals(2, fragment2.getImportDeclaration().getNoOfImports());
        assertTrue(fragment2.getImportDeclaration().isCovered(Map.class.getName()));
        assertTrue(fragment2.getImportDeclaration().isCovered(List.class.getName()));

        String result = fragment2.getSourcecode();
        StringTokenizer tokenizer = new StringTokenizer(result, System.lineSeparator());
        assertEquals("{", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("    List", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("    {", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("        Map", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("    }", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("}", tokenizer.nextToken()); //$NON-NLS-1$

        // test if insert does not introduce any new lineSeparators.
        JavaCodeFragment f1 = new JavaCodeFragment(true).append("Hello "); //$NON-NLS-1$
        JavaCodeFragment f2 = new JavaCodeFragment(true).append("world!"); //$NON-NLS-1$
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.append(f1);
        builder.append(f2);
        assertEquals("Hello world!", builder.getFragment().getSourcecode()); //$NON-NLS-1$
    }

    @Test
    public void testEquals() {
        ImportDeclaration id1 = new ImportDeclaration();
        ImportDeclaration id2 = new ImportDeclaration();
        JavaCodeFragment fragment1 = new JavaCodeFragment("return", id1); //$NON-NLS-1$
        JavaCodeFragment fragment2 = new JavaCodeFragment("return", id2); //$NON-NLS-1$

        assertEquals(fragment1, fragment2);
        assertFalse(fragment1.equals(new JavaCodeFragment("blabla", id2))); //$NON-NLS-1$

        id2.add("java.util.*"); //$NON-NLS-1$
        assertFalse(fragment1.equals(new JavaCodeFragment("return", id2))); //$NON-NLS-1$
    }

    @Test
    public void testAddImport() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.addImport(Calendar.class.getName());
        assertEquals(1, fragment.getImportDeclaration().getNoOfImports());
        assertTrue(fragment.getImportDeclaration().isCovered(Calendar.class));
    }

    @Test
    public void testSplitToplevelGenericTypesByColon_noSplit() throws Exception {
        String input = "any.package.Type<any.other.GenType<ab.X,ab.Y>, another.GenType2<xy.A, xy.B<any.other.GenType>, F>, H>"; //$NON-NLS-1$

        List<String> typeParts = new JavaCodeFragment().splitToplevelGenericTypesByColon(input);

        assertEquals(
                "any.package.Type<any.other.GenType<ab.X,ab.Y>, another.GenType2<xy.A, xy.B<any.other.GenType>, F>, H>", //$NON-NLS-1$
                typeParts.get(0));
    }

    @Test
    public void testSplitToplevelGenericTypesByColon_splits() throws Exception {
        String input = "any.other.GenType<ab.X,ab.Y>, another.GenType2<xy.A, xy.B<any.other.GenType>, F>, H"; //$NON-NLS-1$

        List<String> typeParts = new JavaCodeFragment().splitToplevelGenericTypesByColon(input);

        assertEquals("any.other.GenType<ab.X,ab.Y>", typeParts.get(0)); //$NON-NLS-1$
        assertEquals("another.GenType2<xy.A, xy.B<any.other.GenType>, F>", typeParts.get(1)); //$NON-NLS-1$
        assertEquals("H", typeParts.get(2)); //$NON-NLS-1$
    }

}
