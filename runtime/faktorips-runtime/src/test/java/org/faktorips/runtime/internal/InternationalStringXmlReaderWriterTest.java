/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;

import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.LocalizedString;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class InternationalStringXmlReaderWriterTest {

    private static final String XML = """
            <InternationalString defaultLocale="de">\
            <LocalizedString locale="de" text="blabla"/>\
            <LocalizedString locale="en" text="english"/>\
            </InternationalString>""";

    private static final String WRAPPED_XML = """
            <Parent><Child><InternationalString defaultLocale="de">\
            <LocalizedString locale="de" text="blabla"/>\
            <LocalizedString locale="en" text="english"/>\
            </InternationalString></Child></Parent>""";

    @Test
    public void testFromXml() throws Exception {
        Collection<LocalizedString> localizedStrings = InternationalStringXmlReaderWriter.fromXml(asElement(XML));
        assertThat(localizedStrings, hasItem(new LocalizedString(Locale.GERMAN, "blabla")));
        assertThat(localizedStrings, hasItem(new LocalizedString(Locale.ENGLISH, "english")));
    }

    @Test
    public void testFromXml_WrappedElement() throws Exception {
        Collection<LocalizedString> localizedStrings = InternationalStringXmlReaderWriter.fromXml(
                asElement(WRAPPED_XML), "Child");
        assertThat(localizedStrings, hasItem(new LocalizedString(Locale.GERMAN, "blabla")));
        assertThat(localizedStrings, hasItem(new LocalizedString(Locale.ENGLISH, "english")));
    }

    @Test
    public void testDefaultLocaleFromXml() throws Exception {
        Locale l = InternationalStringXmlReaderWriter.defaultLocaleFromXml(asElement(XML));
        assertThat(l, is(Locale.GERMAN));
    }

    @Test
    public void testDefaultLocaleFromXml_WrappedElement() throws Exception {
        Locale l = InternationalStringXmlReaderWriter.defaultLocaleFromXml(asElement(WRAPPED_XML), "Child");
        assertThat(l, is(Locale.GERMAN));
    }

    @Test
    public void testToXml() throws Exception {
        Collection<LocalizedString> localizedString = new ArrayList<>();
        localizedString.add(new LocalizedString(Locale.GERMAN, "bla"));
        localizedString.add(new LocalizedString(Locale.ENGLISH, "blubb"));
        DefaultInternationalString internationalString = new DefaultInternationalString(localizedString, Locale.GERMAN);
        Element element = InternationalStringXmlReaderWriter.toXml(newDocument(), internationalString);
        assertThat(element.getAttribute(InternationalStringXmlReaderWriter.XML_ATTR_DEFAULT_LOCALE), is("de"));
        assertThat(element.getChildNodes().getLength(), is(2));
        assertThat(element.getChildNodes().item(0).getNodeName(),
                is(InternationalStringXmlReaderWriter.XML_ELEMENT_LOCALIZED_STRING));
        assertThat(element.getChildNodes().item(1).getNodeName(),
                is(InternationalStringXmlReaderWriter.XML_ELEMENT_LOCALIZED_STRING));
    }

    @Test
    public void testSetDefaultLocaleInXml() {
        Document doc = newDocument();
        Element element = doc.createElement("someElement");
        InternationalStringXmlReaderWriter.setDefaultLocaleInXml(element, Locale.FRENCH);
        assertThat(element.getAttribute(InternationalStringXmlReaderWriter.XML_ATTR_DEFAULT_LOCALE), is("fr"));
    }

    @Test
    public void testSetDefaultLocaleInXml_NonElement() {
        Document doc = newDocument();
        Attr attr = doc.createAttribute("someAttribute");
        InternationalStringXmlReaderWriter.setDefaultLocaleInXml(attr, Locale.FRENCH);
        // Nothing to verify, non-element node should just be ignored without throwing an exception
    }

    private static Element asElement(String xmlSnippet) {
        try {
            DocumentBuilder builder = XmlUtil.getDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlSnippet.getBytes()));
            return document.getDocumentElement();
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Document newDocument() {
        DocumentBuilder builder = XmlUtil.getDocumentBuilder();
        return builder.newDocument();
    }

}
