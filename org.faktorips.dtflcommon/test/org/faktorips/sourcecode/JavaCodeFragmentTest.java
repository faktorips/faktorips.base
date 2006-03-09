/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.sourcecode;

import java.util.*;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.*;


/**
 * 
 * @author Jan Ortmann
 */
public class JavaCodeFragmentTest extends TestCase
{
	/**
	 * Constructor for JavaCodeFragmentTest.
	 * @param name
	 */
	public JavaCodeFragmentTest(String name)
	{
		super(name);
	}
	
	public void testJavaCodeFragment() {
	    JavaCodeFragment fragment = new JavaCodeFragment();
	    assertEquals("", fragment.getSourcecode());
	    assertEquals(new ImportDeclaration(), fragment.getImportDeclaration());
	}
	
	public void testBol() {
	    JavaCodeFragment fragment = new JavaCodeFragment();
	    assertTrue(fragment.bol());
	    fragment.append("blabla");
	    assertFalse(fragment.bol());
	    fragment.appendln();
	    assertTrue(fragment.bol());
	}
	
	public void testAppend_String() {
	    JavaCodeFragment fragment = new JavaCodeFragment();
	    fragment.append("blabla");
	    assertEquals("blabla", fragment.getSourcecode());
	    
	    fragment = new JavaCodeFragment();
	    fragment.incIndentationLevel();
	    fragment.append("blabla");
	    assertEquals("    blabla", fragment.getSourcecode());
	    
	    fragment = new JavaCodeFragment();
	    fragment.incIndentationLevel();
	    fragment.append("bla");
	    fragment.append("bla");
	    assertEquals("    blabla", fragment.getSourcecode());
	    
	    fragment.appendln();
	    fragment.append("line2");
	    String expected = "    blabla" + SystemUtils.LINE_SEPARATOR + "    line2";
	    assertEquals(expected, fragment.getSourcecode());
	}
	
    public void testAppendClassName() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(List.class);
        assertEquals("List", fragment.getSourcecode());
        assertEquals(1, fragment.getImportDeclaration().getNoOfImports());
        assertTrue(fragment.getImportDeclaration().isCovered(List.class));
    }
    
    public void testAppendInnerClassName() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendInnerClassName(Entry.class);
        assertEquals("Entry", fragment.getSourcecode());
        assertEquals(1, fragment.getImportDeclaration().getNoOfImports());
        assertTrue(fragment.getImportDeclaration()
                .isCovered(Entry.class.getName().replaceAll("\\$", "\\.")));
    }
    
	public void testAppend_Fragment() {
		JavaCodeFragmentBuilder builder1 = new JavaCodeFragmentBuilder();
		builder1.openBracket();
		builder1.appendClassName(Map.class);
		builder1.appendln();
		builder1.closeBracket();
		JavaCodeFragment fragment1 = builder1.getFragment(); 
		  
		JavaCodeFragmentBuilder builder2 = new JavaCodeFragmentBuilder();
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
		assertEquals("{", tokenizer.nextToken());
		assertEquals("    List", tokenizer.nextToken());
		assertEquals("    {", tokenizer.nextToken());
		assertEquals("        Map", tokenizer.nextToken());
		assertEquals("    }", tokenizer.nextToken());
		assertEquals("}", tokenizer.nextToken());
		
		// test if insert does not introduce any new lineSeparators.
		JavaCodeFragment f1 = new JavaCodeFragment("Hello ");
		JavaCodeFragment f2 = new JavaCodeFragment("world!");
		JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
		builder.append(f1); 
		builder.append(f2);
		assertEquals("Hello world!", builder.getFragment().getSourcecode()); 
	}
	
	
	
	public void testEquals()
	{
		ImportDeclaration id1 = new ImportDeclaration();
		ImportDeclaration id2 = new ImportDeclaration();
		JavaCodeFragment fragment1 = new JavaCodeFragment("return", id1);
		JavaCodeFragment fragment2 = new JavaCodeFragment("return", id2);
		
		assertEquals(fragment1, fragment2);
		assertFalse(fragment1.equals(new JavaCodeFragment("blabla", id2)));
		
		id2.add("java.util.*");
		assertFalse(fragment1.equals(new JavaCodeFragment("return", id2)));
	}
}
