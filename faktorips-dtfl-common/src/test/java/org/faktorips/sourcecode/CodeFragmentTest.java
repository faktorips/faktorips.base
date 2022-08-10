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
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.faktorips.codegen.CodeFragment;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class CodeFragmentTest {

    @Test
    public void testCodeFragment() {
        CodeFragment fragment = new CodeFragment();
        assertEquals("", fragment.getSourcecode()); //$NON-NLS-1$
    }

    @Test
    public void testBol() {
        CodeFragment fragment = new CodeFragment();
        assertTrue(fragment.bol());
        fragment.append("blabla"); //$NON-NLS-1$
        assertFalse(fragment.bol());
        fragment.appendln();
        assertTrue(fragment.bol());
    }

    @Test
    public void testAppend_String() {
        CodeFragment fragment = new CodeFragment(true);
        fragment.append("blabla"); //$NON-NLS-1$
        assertEquals("blabla", fragment.getSourcecode()); //$NON-NLS-1$

        fragment = new CodeFragment(true);
        fragment.incIndentationLevel();
        fragment.append("blabla"); //$NON-NLS-1$
        assertEquals("    blabla", fragment.getSourcecode()); //$NON-NLS-1$

        fragment = new CodeFragment(true);
        fragment.incIndentationLevel();
        fragment.append("bla"); //$NON-NLS-1$
        fragment.append("bla"); //$NON-NLS-1$
        assertEquals("    blabla", fragment.getSourcecode()); //$NON-NLS-1$

        fragment.appendln();
        fragment.append("line2"); //$NON-NLS-1$
        String expected = "    blabla" + System.lineSeparator() + "    line2"; //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(expected, fragment.getSourcecode());
    }

    @Test
    public void testAppend_Fragment() {
        CodeFragment fragment1 = new CodeFragment(true);
        fragment1.append('{');
        fragment1.appendln();
        fragment1.incIndentationLevel();
        fragment1.append(Map.class.getSimpleName());
        fragment1.appendln();
        fragment1.decIndentationLevel();
        fragment1.append('}');

        CodeFragment fragment2 = new CodeFragment(true);
        fragment2.append('{');
        fragment2.appendln();
        fragment2.incIndentationLevel();
        fragment2.append(List.class.getSimpleName());
        fragment2.appendln();
        fragment2.append(fragment1);
        fragment2.appendln();
        fragment2.decIndentationLevel();
        fragment2.append('}');

        String result = fragment2.getSourcecode();
        StringTokenizer tokenizer = new StringTokenizer(result, System.lineSeparator());
        assertEquals("{", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("    List", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("    {", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("        Map", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("    }", tokenizer.nextToken()); //$NON-NLS-1$
        assertEquals("}", tokenizer.nextToken()); //$NON-NLS-1$

        // test if insert does not introduce any new lineSeparators.
        CodeFragment f1 = new CodeFragment(true).append("Hello "); //$NON-NLS-1$
        CodeFragment f2 = new CodeFragment(true).append("world!"); //$NON-NLS-1$
        CodeFragment f = new CodeFragment();
        f.append(f1);
        f.append(f2);
        assertEquals("Hello world!", f.getSourcecode()); //$NON-NLS-1$
    }

    @Test
    public void testEquals() {
        CodeFragment fragment1 = new CodeFragment("return"); //$NON-NLS-1$
        CodeFragment fragment2 = new CodeFragment("return"); //$NON-NLS-1$

        assertEquals(fragment1, fragment2);
        assertFalse(fragment1.equals(new CodeFragment("blabla"))); //$NON-NLS-1$
    }

}
