/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import java.util.Locale;

import junit.framework.TestCase;

import org.faktorips.runtime.ValidationContext;
import org.faktorips.values.Money;

public class ValidationContextTest extends TestCase {

    public void testGetLocale() {
        ValidationContext context = new ValidationContext();
        assertEquals(Locale.getDefault(), context.getLocale());

        context = new ValidationContext(Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, context.getLocale());
    }

    public void testGetValue() {
        ValidationContext context = new ValidationContext();
        context.setValue("a", Money.euro(100));
        context.setValue("b", new Integer(1));
        assertEquals(Money.euro(100), context.getValue("a"));
    }
}
