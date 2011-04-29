/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class JavaCodeFragmentBuilderTest {

    @Test
    public void testOpenCloseBracket() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder(true);
        assertEquals(0, builder.getFragment().getIndentationLevel());
        builder.openBracket();
        assertEquals(1, builder.getFragment().getIndentationLevel());
        builder.openBracket();
        assertEquals(2, builder.getFragment().getIndentationLevel());
        builder.append("blabla"); //$NON-NLS-1$
        builder.closeBracket();
        assertEquals(1, builder.getFragment().getIndentationLevel());
        builder.closeBracket();
        assertEquals(0, builder.getFragment().getIndentationLevel());
        String expected = "{" + SystemUtils.LINE_SEPARATOR + "    {" + SystemUtils.LINE_SEPARATOR + "        blabla" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + SystemUtils.LINE_SEPARATOR + "    }" + SystemUtils.LINE_SEPARATOR + "}" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(expected, builder.getFragment().getSourcecode());
    }

}
