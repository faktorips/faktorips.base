/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

import org.faktorips.values.DefaultInternationalString;
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

        DefaultInternationalString internationalString = fromXml(xmlSnippet);

        assertThat(internationalString.getLocalizedStrings(), hasItem(new LocalizedString(Locale.GERMAN, "blabla")));
        assertThat(internationalString.getLocalizedStrings(), hasItem(new LocalizedString(Locale.ENGLISH, "english")));
    }

    /**
     * Reads an {@link DefaultInternationalString} from an XML snippet represented by the String parameter.
     * 
     * @param xmlSnippet The XML representation of the {@link DefaultInternationalString}
     * @return An {@link DefaultInternationalString} loaded from the XML snippet
     */
    public static DefaultInternationalString fromXml(String xmlSnippet) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlSnippet.getBytes()));
            Element rootElement = document.getDocumentElement();
            Collection<LocalizedString> collection = InternationalStringXmlReaderWriter.fromXml(rootElement);
            return new DefaultInternationalString(collection);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
