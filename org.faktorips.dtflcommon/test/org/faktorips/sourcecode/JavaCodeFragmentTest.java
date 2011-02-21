/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.sourcecode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.ClassNameUtil;
import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
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
        fragment.appendClassName(this.getClass());
        String pack = ClassNameUtil.getPackageName(JavaCodeFragment.class.getName());
        ImportDeclaration decl = fragment.getImportDeclaration(pack);
        assertEquals(1, decl.getNoOfImports());
        assertTrue(decl.toString().indexOf(getClass().getName()) != -1);
    }

    @Test
    public void testJavaCodeFragment() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        assertEquals("", fragment.getSourcecode()); //$NON-NLS-1$
        assertEquals(new ImportDeclaration(), fragment.getImportDeclaration());
    }

    @Test
    public void testBol() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        assertTrue(fragment.bol());
        fragment.append("blabla"); //$NON-NLS-1$
        assertFalse(fragment.bol());
        fragment.appendln();
        assertTrue(fragment.bol());
    }

    @Test
    public void testAppend_String() {
        JavaCodeFragment fragment = new JavaCodeFragment(true);
        fragment.append("blabla"); //$NON-NLS-1$
        assertEquals("blabla", fragment.getSourcecode()); //$NON-NLS-1$

        fragment = new JavaCodeFragment(true);
        fragment.incIndentationLevel();
        fragment.append("blabla"); //$NON-NLS-1$
        assertEquals("    blabla", fragment.getSourcecode()); //$NON-NLS-1$

        fragment = new JavaCodeFragment(true);
        fragment.incIndentationLevel();
        fragment.append("bla"); //$NON-NLS-1$
        fragment.append("bla"); //$NON-NLS-1$
        assertEquals("    blabla", fragment.getSourcecode()); //$NON-NLS-1$

        fragment.appendln();
        fragment.append("line2"); //$NON-NLS-1$
        String expected = "    blabla" + SystemUtils.LINE_SEPARATOR + "    line2"; //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(expected, fragment.getSourcecode());
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
        StringTokenizer tokenizer = new StringTokenizer(result, SystemUtils.LINE_SEPARATOR);
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

}
