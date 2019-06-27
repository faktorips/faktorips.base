/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SupportedLanguageTest extends AbstractIpsPluginTest {

    private ISupportedLanguage englishLanguage;

    private ISupportedLanguage germanLanguage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        englishLanguage = new SupportedLanguage(Locale.ENGLISH);
        ((SupportedLanguage)englishLanguage).setDefaultLanguage(true);
        germanLanguage = new SupportedLanguage(Locale.GERMAN);
    }

    @Test
    public void testGetLocale() {
        assertEquals(Locale.ENGLISH, englishLanguage.getLocale());
        assertEquals(Locale.GERMAN, germanLanguage.getLocale());
    }

    @Test
    public void testGetLanguageName() {
        assertEquals(Locale.ENGLISH.getDisplayLanguage(), englishLanguage.getLanguageName());
        assertEquals(Locale.GERMAN.getDisplayLanguage(), germanLanguage.getLanguageName());
    }

    @Test
    public void testIsDefaultLanguage() {
        assertTrue(englishLanguage.isDefaultLanguage());
        assertFalse(germanLanguage.isDefaultLanguage());
    }

    @Test
    public void testEquals() {
        assertEquals(englishLanguage, new SupportedLanguage(Locale.ENGLISH));
        assertEquals(germanLanguage, new SupportedLanguage(Locale.GERMAN));
        assertFalse(englishLanguage.equals(germanLanguage));
    }

    @Test
    public void testInitFromXml() {
        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();

        ISupportedLanguage supportedLanguage = new SupportedLanguage();
        Element elementEnglishLanguage = englishLanguage.toXml(doc);
        supportedLanguage.initFromXml(elementEnglishLanguage);
        assertEquals(Locale.ENGLISH, supportedLanguage.getLocale());
        assertTrue(supportedLanguage.isDefaultLanguage());

        supportedLanguage = new SupportedLanguage();
        Element elementGermanLanguage = germanLanguage.toXml(doc);
        supportedLanguage.initFromXml(elementGermanLanguage);
        assertEquals(Locale.GERMAN, supportedLanguage.getLocale());
        assertFalse(supportedLanguage.isDefaultLanguage());
    }

    @Test
    public void testToXml() {
        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();

        Element elementEnglishLanguage = englishLanguage.toXml(doc);
        assertEquals(Locale.ENGLISH.getLanguage(), elementEnglishLanguage.getAttribute("locale"));
        assertEquals("true", elementEnglishLanguage.getAttribute("defaultLanguage"));

        Element elementGermanLanguage = germanLanguage.toXml(doc);
        assertEquals(Locale.GERMAN.getLanguage(), elementGermanLanguage.getAttribute("locale"));
        assertEquals("", elementGermanLanguage.getAttribute("defaultLanguage"));
    }

}
