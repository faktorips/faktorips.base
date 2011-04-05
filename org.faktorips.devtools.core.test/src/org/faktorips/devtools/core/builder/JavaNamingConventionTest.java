/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JavaNamingConventionTest {

    @Test
    public void testGetEnumLiteral() {
        String literal = JavaNamingConvention.ECLIPSE_STANDARD.getEnumLiteral("foo");
        assertEquals("FOO", literal);
    }

    @Test
    public void testGetEnumLiteralInvalidCharacters() {
        String literal = JavaNamingConvention.ECLIPSE_STANDARD.getEnumLiteral("foo //%bar");
        assertEquals("FOO____BAR", literal);
    }

    @Test
    public void testGetEnumLiteralUmlaut() {
        String literal = JavaNamingConvention.ECLIPSE_STANDARD.getEnumLiteral("fooÄbar");
        assertEquals("FOOAEBAR", literal);

        literal = JavaNamingConvention.ECLIPSE_STANDARD.getEnumLiteral("fooäbar");
        assertEquals("FOOAEBAR", literal);

        literal = JavaNamingConvention.ECLIPSE_STANDARD.getEnumLiteral("fooÖbar");
        assertEquals("FOOOEBAR", literal);

        literal = JavaNamingConvention.ECLIPSE_STANDARD.getEnumLiteral("fooöbar");
        assertEquals("FOOOEBAR", literal);

        literal = JavaNamingConvention.ECLIPSE_STANDARD.getEnumLiteral("fooÜbar");
        assertEquals("FOOUEBAR", literal);

        literal = JavaNamingConvention.ECLIPSE_STANDARD.getEnumLiteral("fooübar");
        assertEquals("FOOUEBAR", literal);

        literal = JavaNamingConvention.ECLIPSE_STANDARD.getEnumLiteral("fooßbar");
        assertEquals("FOOSSBAR", literal);
    }

}
