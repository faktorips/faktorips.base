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

package org.faktorips.util;
import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.util.StringUtil;

/**
 * @author Jan Ortmann
 */
public class StringUtilTest extends TestCase {

	/**
	 * Constructor for StringUtilTest.
	 * @param name
	 */
	public StringUtilTest(String name) {
		super(name);
	}

	public void testQuote() {
		assertEquals("\"hello\"", StringUtil.quote("hello"));
		assertNull(StringUtil.quote(null));
	}

	public void testUnqualifiedClassName() {
		assertEquals("Test", StringUtil.unqualifiedName("com.Test"));
		assertEquals("Test", StringUtil.unqualifiedName("com.ips.Test"));
		assertEquals("Test", StringUtil.unqualifiedName("Test"));
	}

	public void testGetLine() {
	    String lineSeparator = SystemUtils.LINE_SEPARATOR;
	    String text = "blabla";
	    assertEquals("blabla", StringUtil.getLine(text, 0, lineSeparator));
	    
	    text = "blabla" + lineSeparator
	    	 + "2.line";
        
	    assertEquals("blabla", StringUtil.getLine(text, 0, lineSeparator));
	    assertEquals("2.line", StringUtil.getLine(text, 6 + SystemUtils.LINE_SEPARATOR.getBytes().length, lineSeparator));
	}
	
	public void testGetLines() {
		String[] result;
		
		result = StringUtil.getLines("blabla", SystemUtils.LINE_SEPARATOR);
		assertEquals(1, result.length);
	    assertEquals("blabla", result[0]);
	    
		result = StringUtil.getLines("blabla" + SystemUtils.LINE_SEPARATOR, SystemUtils.LINE_SEPARATOR);
		assertEquals(2, result.length);
	    assertEquals("blabla", result[0]);
	    assertEquals("", result[1]);
	    
		result = StringUtil.getLines("blabla" + SystemUtils.LINE_SEPARATOR + "2.line", SystemUtils.LINE_SEPARATOR);
		assertEquals(2, result.length);
	    assertEquals("blabla", result[0]);
	    assertEquals("2.line", result[1]);

		result = StringUtil.getLines("blabla" + SystemUtils.LINE_SEPARATOR + "2.line" + SystemUtils.LINE_SEPARATOR, SystemUtils.LINE_SEPARATOR);
		assertEquals(3, result.length);
	    assertEquals("blabla", result[0]);
	    assertEquals("2.line", result[1]);
	    assertEquals("", result[2]);
	}
	
	public void testGetSystemLineseparator(){
	    assertEquals(System.getProperty("line.separator"), StringUtil.getSystemLineSeparator());
	}
}
