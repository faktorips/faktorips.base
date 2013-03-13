/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.util;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessagesHelperTest {

    private final Locale defaultLocale = Locale.ENGLISH;

    private ClassLoader loader;

    @Before
    public void setUp() throws Exception {
        loader = getClass().getClassLoader();
    }

    @Test
    public void testGetMessage_simpleGetGerman() throws Exception {
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessage("test", Locale.GERMAN);

        assertEquals(message, "testmessageInDe");
    }

    @Test
    public void testGetMessage_getSystemDefault() throws Exception {
        Locale.setDefault(Locale.GERMAN);
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessage("test", Locale.CHINESE);

        assertEquals(message, "testmessageInDe");
    }

    @Test
    public void testGetMessage_getOurDefault() throws Exception {
        Locale.setDefault(Locale.FRENCH);
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessage("test", Locale.CHINESE);

        assertEquals(message, "testmessageInEn");
    }

}
