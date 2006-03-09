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

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.ImportDeclaration;


/**
 * 
 * @author Jan Ortmann
 */
public class ImportDeclarationTest extends TestCase
{

	/**
	 * Constructor for ImportDeclarationTest.
	 * @param name
	 */
	public ImportDeclarationTest(String name)
	{
		super(name);
	}
	
	public void testImportDeclaration()
	{
		ImportDeclaration id = new ImportDeclaration();
		assertEquals(0, id.getNoOfImports());
	}
	
	public void testImportDeclaration_ImportDeclaration_PackageName()
	{
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
    
	public void testAdd()
	{
        ImportDeclaration id = new ImportDeclaration();
        id.iterator();
        id.getNoOfImports();
        
        id.add("java.util.List");
        assertEquals(1, id.getNoOfImports());
        
        id.add("java.util.ArrayList");
        assertEquals(2, id.getNoOfImports());
        
        id.add("com.fja.pm.*");
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
        assertEquals("com.fja.pm.*", it.next());
        assertEquals("java.util.*", it.next());
        assertFalse(it.hasNext());
	}
	
	public void testToString()
	{
		ImportDeclaration id = new ImportDeclaration();
		id.add("java.util.List");
		id.add("java.util.Iterator");
		
		String expected = "import java.util.List;" + SystemUtils.LINE_SEPARATOR
			+ "import java.util.Iterator;" + SystemUtils.LINE_SEPARATOR;
		
		assertEquals(expected, id.toString());
	}
	
	public void testEquals()
	{
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

}
