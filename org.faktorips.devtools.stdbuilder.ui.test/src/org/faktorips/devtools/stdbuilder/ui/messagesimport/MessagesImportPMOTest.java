/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.ui.messagesimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Locale;

import org.faktorips.devtools.core.internal.model.ipsproject.SupportedLanguage;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.junit.Test;

public class MessagesImportPMOTest {

    @Test
    public void testSetAvailableLocales() throws Exception {
        MessagesImportPMO messagesImportPMO = new MessagesImportPMO();

        assertNull(messagesImportPMO.getLocale());

        HashSet<ISupportedLanguage> supportedLanguages = new HashSet<ISupportedLanguage>();
        SupportedLanguage deLang = new SupportedLanguage(Locale.GERMAN);
        supportedLanguages.add(deLang);
        messagesImportPMO.setAvailableLocales(supportedLanguages);

        assertEquals(deLang, messagesImportPMO.getLocale());

        supportedLanguages = new HashSet<ISupportedLanguage>();
        supportedLanguages.add(new SupportedLanguage(Locale.ENGLISH));
        messagesImportPMO.setAvailableLocales(supportedLanguages);

        assertEquals(deLang, messagesImportPMO.getLocale());
    }

}
