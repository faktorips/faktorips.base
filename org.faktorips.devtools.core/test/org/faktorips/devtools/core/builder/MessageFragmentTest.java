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

package org.faktorips.devtools.core.builder;

import junit.framework.TestCase;

import org.faktorips.util.StringUtil;

public class MessageFragmentTest extends TestCase {

    /*
     * Test method for
     * 'org.faktorips.devtools.core.builder.MessageFragment.createMessageFragment(String)'
     */
    public void testCreateMessageFragment() {
        MessageFragment msgFrag = MessageFragment
                .createMessageFragment("The value a={attribute1} is not b={attribute2}"); //$NON-NLS-1$
        String sep = StringUtil.getSystemLineSeparator();
        StringBuffer buf = new StringBuffer();
        buf.append(sep);
        buf.append("StringBuffer text = new StringBuffer();"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(\"The value a=\");"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(param0);"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(\" is not b=\");"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(param1);"); //$NON-NLS-1$
        buf.append(sep);
        System.out.print(msgFrag.getFrag().toString());

        assertEquals(buf.toString(), msgFrag.getFrag().toString());
        String[] paraNames = msgFrag.getParameterNames();
        assertEquals(2, paraNames.length);
        assertEquals("param0", paraNames[0]); //$NON-NLS-1$
        assertEquals("param1", paraNames[1]); //$NON-NLS-1$
        String[] paraValues = msgFrag.getParameterValues();
        assertEquals("attribute1", paraValues[0]); //$NON-NLS-1$
        assertEquals("attribute2", paraValues[1]); //$NON-NLS-1$
    }

    public void testCreateMessageFragment2() {
        MessageFragment msgFrag = MessageFragment.createMessageFragment(
                "The value a={attribute1} is not b={attribute2}", MessageFragment.VALUES_AS_PARAMETER_NAMES); //$NON-NLS-1$
        String sep = StringUtil.getSystemLineSeparator();
        StringBuffer buf = new StringBuffer();
        buf.append(sep);
        buf.append("StringBuffer text = new StringBuffer();"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(\"The value a=\");"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(attribute1);"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(\" is not b=\");"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(attribute2);"); //$NON-NLS-1$
        buf.append(sep);
        System.out.print(msgFrag.getFrag().toString());

        assertEquals(buf.toString(), msgFrag.getFrag().toString());
        String[] paraNames = msgFrag.getParameterNames();
        assertEquals(2, paraNames.length);
        assertEquals("attribute1", paraNames[0]); //$NON-NLS-1$
        assertEquals("attribute2", paraNames[1]); //$NON-NLS-1$
        String[] paraValues = msgFrag.getParameterValues();
        assertEquals("attribute1", paraValues[0]); //$NON-NLS-1$
        assertEquals("attribute2", paraValues[1]); //$NON-NLS-1$
    }

    public void testCreateMessageFragment3() {
        MessageFragment msgFrag = MessageFragment.createMessageFragment(
                "The value a={0} is not b={1}", MessageFragment.VALUES_AS_PARAMETER_NAMES); //$NON-NLS-1$
        String sep = StringUtil.getSystemLineSeparator();
        StringBuffer buf = new StringBuffer();
        buf.append(sep);
        buf.append("StringBuffer text = new StringBuffer();"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(\"The value a=\");"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(p0);"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(\" is not b=\");"); //$NON-NLS-1$
        buf.append(sep);
        buf.append("text.append(p1);"); //$NON-NLS-1$
        buf.append(sep);
        System.out.print(msgFrag.getFrag().toString());

        assertEquals(buf.toString(), msgFrag.getFrag().toString());
        String[] paraNames = msgFrag.getParameterNames();
        assertEquals(2, paraNames.length);
        assertEquals("p0", paraNames[0]); //$NON-NLS-1$
        assertEquals("p1", paraNames[1]); //$NON-NLS-1$

    }

}
