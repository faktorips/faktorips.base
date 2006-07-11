/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.faktorips.util.StringUtil;

import junit.framework.TestCase;

public class MessageFragmentTest extends TestCase {

	/*
	 * Test method for 'org.faktorips.devtools.core.builder.MessageFragment.createMessageFragment(String)'
	 */
	public void testCreateMessageFragment() {
		MessageFragment msgFrag = MessageFragment.createMessageFragment("The value a=[attribute1] is not b=[attribute2]");
		String sep = StringUtil.getSystemLineSeparator();
		StringBuffer buf = new StringBuffer();
		buf.append(sep);
		buf.append("StringBuffer text = new StringBuffer();");
		buf.append(sep);
		buf.append("text.append(\"The value a=\");");
		buf.append(sep);
		buf.append("text.append(param0);");
		buf.append(sep);
		buf.append("text.append(\" is not b=\");");
		buf.append(sep);
		buf.append("text.append(param1);");
		buf.append(sep);
		System.out.print(msgFrag.getFrag().toString());

		assertEquals(buf.toString(), msgFrag.getFrag().toString());
		String[] paraNames = msgFrag.getParameterNames();
		assertEquals(2, paraNames.length);
		assertEquals("param0", paraNames[0]);
		assertEquals("param1", paraNames[1]);
		String[] paraValues = msgFrag.getParameterValues();
		assertEquals("attribute1", paraValues[0]);
		assertEquals("attribute2", paraValues[1]);
	}

}
