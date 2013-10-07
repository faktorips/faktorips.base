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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.values.InternationalString;
import org.faktorips.values.LocalizedString;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class InternationalStringXmlReaderWriterTest {

    @Test
    public void testFromXml_XmlSnippet() throws Exception {
        String xmlSnippet = "<InternationalString>\n" //
                + "<LocalizedString locale=\"de\" text=\"blabla\"/>\n" //
                + "<LocalizedString locale=\"en\" text=\"english\"/>\n" //
                + "</InternationalString>";

        InternationalString internationalString = fromXml(xmlSnippet);

        assertThat(internationalString.getLocalizedStrings(), hasItem(new LocalizedString(Locale.GERMAN, "blabla")));
        assertThat(internationalString.getLocalizedStrings(), hasItem(new LocalizedString(Locale.ENGLISH, "english")));
    }

    /**
     * Reads an {@link InternationalString} from an XML snippet represented by the String parameter.
     * 
     * @param xmlSnippet The XML representation of the {@link InternationalString}
     * @return An {@link InternationalString} loaded from the XML snippet
     */
    public static InternationalString fromXml(String xmlSnippet) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlSnippet.getBytes()));
            Element rootElement = document.getDocumentElement();
            Collection<LocalizedString> collection = InternationalStringXmlReaderWriter.fromXml(rootElement);
            return new InternationalString(collection);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
