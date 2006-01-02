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
