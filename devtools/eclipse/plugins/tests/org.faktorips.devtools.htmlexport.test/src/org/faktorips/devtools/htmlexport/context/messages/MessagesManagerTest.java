/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.Properties;

import org.faktorips.devtools.htmlexport.MockPluginResourcesFacade;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.junit.Before;
import org.junit.Test;

public class MessagesManagerTest {
    private static final String MESSAGE_DE = "Projekt";
    private static final String MESSAGE_EN = "Project";
    private DocumentationContext context;
    private static final String KEY = "ProjectOverviewPageElement_project";

    @Before
    public void setUp() throws Exception {
        MockPluginResourcesFacade pluginResources = new MockPluginResourcesFacade();
        Properties enMessages = new Properties();
        enMessages.put(KEY, MESSAGE_EN);
        pluginResources.putMessageProperties("org/faktorips/devtools/htmlexport/context/messages/messages.properties",
                enMessages);

        Properties deMessages = new Properties();
        deMessages.put(KEY, MESSAGE_DE);
        pluginResources.putMessageProperties(
                "org/faktorips/devtools/htmlexport/context/messages/messages_de.properties", deMessages);

        context = new DocumentationContext(pluginResources);
    }

    @Test
    public void testProjectOverviewPageElementProjectEn() {
        context.setDocumentationLocale(Locale.UK);

        MessagesManager manager = new MessagesManager(context);

        assertEquals(MESSAGE_EN, manager.getMessage(KEY));
        assertTrue(context.getExportStatus().isOK());
    }

    @Test
    public void testProjectOverviewPageElementProjectDe() {
        context.setDocumentationLocale(Locale.GERMAN);

        MessagesManager manager = new MessagesManager(context);

        assertEquals(MESSAGE_DE, manager.getMessage(KEY));
        assertTrue(context.getExportStatus().isOK());
    }

    @Test
    public void testProjectOverviewPageElementProjectNotUsedLocaleWithFallback() {
        context.setDocumentationLocale(Locale.TRADITIONAL_CHINESE);

        MessagesManager manager = new MessagesManager(context);

        String message = manager.getMessage(KEY);
        boolean result = MESSAGE_EN.equals(message) || MESSAGE_DE.equals(message);

        assertTrue(result);
    }

    @Test
    public void testProjectOverviewPageElementProjectNotUsedKey() {
        String wrongKey = "definitiv ungültiger name für eine property";
        context.setDocumentationLocale(Locale.UK);

        MessagesManager manager = new MessagesManager(context);

        assertTrue(context.getExportStatus().getMessage(), context.getExportStatus().isOK());
        assertEquals(wrongKey, manager.getMessage(wrongKey));
        assertFalse(context.getExportStatus().getMessage(), context.getExportStatus().isOK());
    }
}
