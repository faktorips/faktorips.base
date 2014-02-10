/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
