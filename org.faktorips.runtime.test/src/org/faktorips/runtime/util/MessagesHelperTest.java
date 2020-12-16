/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.util;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
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

        assertEquals("testmessageInDe", message);
    }

    @Test
    public void testGetMessage_getSystemDefault() throws Exception {
        Locale.setDefault(Locale.GERMAN);
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessage("test", Locale.CHINESE);

        assertEquals("testmessageInDe", message);
    }

    @Test
    public void testGetMessage_getOurDefault() throws Exception {
        Locale.setDefault(Locale.FRENCH);
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessage("test", Locale.CHINESE);

        assertEquals("testmessageInEn", message);
    }

    @Test
    public void testGetMessage_nullObject() throws Exception {
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessage("testReplace", Locale.GERMAN, Decimal.NULL);

        assertEquals("test null abc", message);
    }

    @Test
    public void testGetMessage_nullObjects() throws Exception {
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessage("testReplaces", Locale.GERMAN, Decimal.NULL, null);

        assertEquals("test null abc null", message);
    }

    @Test
    public void testGetMessage_nullObjects_2() throws Exception {
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessage("testReplaces", Locale.GERMAN, 1, Money.NULL);

        assertEquals("test 1 abc null", message);
    }

    @Test
    public void testGetMessageOr_FoundMessage() throws Exception {
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessageOr("test", Locale.GERMAN, "fallback");

        assertEquals("testmessageInDe", message);
    }

    @Test
    public void testGetMessageOr_FoundLocaleFallback() throws Exception {
        Locale.setDefault(Locale.ROOT);
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessageOr("test", Locale.CHINESE, "fallback");

        assertEquals("testmessageInEn", message);
    }

    @Test
    public void testGetMessageOr_Fallback() throws Exception {
        String qualifiedName = getClass().getName();
        MessagesHelper messagesHelper = new MessagesHelper(qualifiedName, loader, defaultLocale);

        String message = messagesHelper.getMessageOr("n o t", Locale.CHINESE, "fallback");

        assertEquals("fallback", message);
    }

}
