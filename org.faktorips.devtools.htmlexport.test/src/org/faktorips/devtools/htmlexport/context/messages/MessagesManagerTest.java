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
    private DocumentationContext context;
    private static final String KEY = "ProjectOverviewPageElement_project";

    @Before
    public void setUp() throws Exception {
        MockPluginResourcesFacade pluginResources = new MockPluginResourcesFacade();
        Properties enMessages = new Properties();
        enMessages.put(KEY, "Project");
        pluginResources.putMessageProperties("org/faktorips/devtools/htmlexport/context/messages/messages.properties",
                enMessages);

        Properties deMessages = new Properties();
        deMessages.put(KEY, "Projekt");
        pluginResources.putMessageProperties(
                "org/faktorips/devtools/htmlexport/context/messages/messages_de.properties", deMessages);

        context = new DocumentationContext(pluginResources);
    }

    @Test
    public void testProjectOverviewPageElementProjectEn() {
        context.setDescriptionLocale(Locale.UK);

        MessagesManager manager = new MessagesManager(context);

        assertEquals("Project", manager.getMessage(KEY));
        assertTrue(context.getExportStatus().isOK());
    }

    @Test
    public void testProjectOverviewPageElementProjectDe() {
        context.setDescriptionLocale(Locale.GERMAN);

        MessagesManager manager = new MessagesManager(context);

        assertEquals("Projekt", manager.getMessage(KEY));
        assertTrue(context.getExportStatus().isOK());
    }

    @Test
    public void testProjectOverviewPageElementProjectNotUsedLocaleWithFallback() {
        context.setDescriptionLocale(Locale.TRADITIONAL_CHINESE);

        MessagesManager manager = new MessagesManager(context);

        assertFalse(context.getExportStatus().isOK());

        String expectedResult = "Project";

        assertEquals(expectedResult, manager.getMessage(KEY));
    }

    @Test
    public void testProjectOverviewPageElementProjectNotUsedKey() {
        String wrongKey = "definitiv ungültiger name für eine property";
        context.setDescriptionLocale(Locale.UK);

        MessagesManager manager = new MessagesManager(context);

        assertTrue(context.getExportStatus().getMessage(), context.getExportStatus().isOK());
        assertEquals(wrongKey, manager.getMessage(wrongKey));
        assertFalse(context.getExportStatus().getMessage(), context.getExportStatus().isOK());
    }
}
