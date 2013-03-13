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

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Locale;

import org.faktorips.values.InternationalString;
import org.faktorips.values.LocalizedString;
import org.junit.Test;

public class InternationalStringXmlReaderWriterTest {

    @Test
    public void testFromXml_XmlSnippet() throws Exception {
        String xmlSnippet = "<InternationalString>\n" //
                + "<LocalizedString locale=\"de\" text=\"blabla\"/>\n" //
                + "<LocalizedString locale=\"en\" text=\"english\"/>\n" //
                + "</InternationalString>";

        InternationalString internationalString = InternationalStringXmlReaderWriter.fromXml(xmlSnippet);

        assertThat(internationalString.getLocalizedStrings(), hasItem(new LocalizedString(Locale.GERMAN, "blabla")));
        assertThat(internationalString.getLocalizedStrings(), hasItem(new LocalizedString(Locale.ENGLISH, "english")));
    }

}
