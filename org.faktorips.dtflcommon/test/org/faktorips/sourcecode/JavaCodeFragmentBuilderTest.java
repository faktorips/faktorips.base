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

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.JavaCodeFragmentBuilder;


/**
 * 
 * @author Jan Ortmann
 */
public class JavaCodeFragmentBuilderTest extends TestCase
{
	/**
	 * Constructor for JavaCodeFragmentBuilderTest.
	 */
	public JavaCodeFragmentBuilderTest(String name)
	{
		super(name);
	}
	
	public void testOpenCloseBracket()
	{
		JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
		assertEquals(0, builder.getFragment().getIndentationLevel());
		builder.openBracket();
		assertEquals(1, builder.getFragment().getIndentationLevel());
		builder.openBracket();
		assertEquals(2, builder.getFragment().getIndentationLevel());
		builder.append("blabla");
		builder.closeBracket();
		assertEquals(1, builder.getFragment().getIndentationLevel());
		builder.closeBracket();
		assertEquals(0, builder.getFragment().getIndentationLevel());
		String expected = "{" + SystemUtils.LINE_SEPARATOR 
						+ "    {" + SystemUtils.LINE_SEPARATOR
						+ "        blabla" + SystemUtils.LINE_SEPARATOR
						+ "    }" + SystemUtils.LINE_SEPARATOR
						+ "}" + SystemUtils.LINE_SEPARATOR;
		assertEquals(expected, builder.getFragment().getSourcecode());
	}
}
