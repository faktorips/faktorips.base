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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SupportedLanguageTest extends AbstractIpsPluginTest {

    private ISupportedLanguage englishLanguage;

    private ISupportedLanguage germanLanguage;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        englishLanguage = new SupportedLanguage(Locale.ENGLISH);
        ((SupportedLanguage)englishLanguage).setDefaultLanguage(true);
        germanLanguage = new SupportedLanguage(Locale.GERMAN);
    }

    public void testGetLocale() {
        assertEquals(Locale.ENGLISH, englishLanguage.getLocale());
        assertEquals(Locale.GERMAN, germanLanguage.getLocale());
    }

    public void testGetLanguageName() {
        assertEquals(Locale.ENGLISH.getDisplayLanguage(), englishLanguage.getLanguageName());
        assertEquals(Locale.GERMAN.getDisplayLanguage(), germanLanguage.getLanguageName());
    }

    public void testIsDefaultLanguage() {
        assertTrue(englishLanguage.isDefaultLanguage());
        assertFalse(germanLanguage.isDefaultLanguage());
    }

    public void testEquals() {
        assertEquals(englishLanguage, new SupportedLanguage(Locale.ENGLISH));
        assertEquals(germanLanguage, new SupportedLanguage(Locale.GERMAN));
        assertFalse(englishLanguage.equals(germanLanguage));
    }

    public void testInitFromXml() {
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();

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

    public void testToXml() {
        Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();

        Element elementEnglishLanguage = englishLanguage.toXml(doc);
        assertEquals(Locale.ENGLISH.getLanguage(), elementEnglishLanguage.getAttribute("locale"));
        assertEquals("true", elementEnglishLanguage.getAttribute("defaultLanguage"));

        Element elementGermanLanguage = germanLanguage.toXml(doc);
        assertEquals(Locale.GERMAN.getLanguage(), elementGermanLanguage.getAttribute("locale"));
        assertEquals("", elementGermanLanguage.getAttribute("defaultLanguage"));
    }

}
