/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.faktorips.runtime.internal.XmlUtil;
import org.faktorips.runtime.testrepository.testenum.empty.EmptyEnum;
import org.faktorips.runtime.testrepository.testenum.modell.AbstractModellEnum;
import org.faktorips.runtime.testrepository.testenum.modell.ModellEnum;
import org.faktorips.runtime.testrepository.testenum.modell.SuperModellEnum;
import org.faktorips.runtime.testrepository.testenum.produkt.ExtensibleEnum;
import org.faktorips.runtime.testrepository.testenum.produkt.ExtensibleEnumWithModell;
import org.faktorips.runtime.testrepository.testenum.produkt.SuperExtensibleEnum;
import org.faktorips.runtime.testrepository.testenum.produkt.SuperExtensibleEnumWithModell;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IpsEnumToXmlWriterTest {

    private DocumentBuilder builder;
    private ClassloaderRuntimeRepository repository;

    @Before
    public void setup() throws Exception {
        builder = XmlUtil.getDocumentBuilder();

        repository = ClassloaderRuntimeRepository
                .create("org/faktorips/runtime/testrepository/testenum/internal/faktorips-repository-toc.xml");

    }

    @Test
    public void testModellEnum() throws Exception {
        assertThrows(UnsupportedOperationException.class,
                () -> new IpsEnumToXmlWriter(repository, ModellEnum.class).toXml(builder.newDocument()));
    }

    @Test
    public void testAbstractModellEnum() throws Exception {
        assertThrows(UnsupportedOperationException.class,
                () -> new IpsEnumToXmlWriter(repository, AbstractModellEnum.class).toXml(builder.newDocument()));
    }

    @Test
    public void testSuperModellEnum() throws Exception {
        assertThrows(UnsupportedOperationException.class,
                () -> new IpsEnumToXmlWriter(repository, SuperModellEnum.class).toXml(builder.newDocument()));
    }

    @Test
    public void testExtensibleEnum() throws Exception {
        Document doc = builder.newDocument();

        Element enumXml = new IpsEnumToXmlWriter(repository, ExtensibleEnum.class).toXml(doc);

        doc.appendChild(enumXml);
        String xml = xmlToString(doc);
        assertThat(xml, containsString("enumType=\"model.ExtensibleEnum\""));
        assertThat(xml, containsString("<Description locale=\"en\">The Description</Description>"));
        assertThat(xml, containsString("<Description locale=\"de\">Die Beschreibung</Description>"));
        assertThat(xml, containsString("<EnumAttributeValue>1</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeValue>extensible_enum_1</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeValue>2</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeValue>extensible_enum_2</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"id\"/>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"name\"/>"));
    }

    @Test
    public void testExtensibleEnumWithModel() throws Exception {
        Document doc = builder.newDocument();

        Element enumXml = new IpsEnumToXmlWriter(repository, ExtensibleEnumWithModell.class).toXml(doc);

        doc.appendChild(enumXml);
        String xml = xmlToString(doc);
        assertThat(xml, containsString("enumType=\"model.ExtensibleEnumWithModell\""));
        assertThat(xml, containsString("<Description locale=\"en\">The Description</Description>"));
        assertThat(xml, containsString("<Description locale=\"de\">Die Beschreibung</Description>"));
        assertThat(xml, not(containsString("<EnumAttributeValue>1</EnumAttributeValue>")));
        assertThat(xml, not(containsString("<EnumAttributeValue>extensible_enum_1</EnumAttributeValue>")));
        assertThat(xml, containsString("<EnumAttributeValue>2</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeValue>extensible_enum_2</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"id\"/>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"name\"/>"));
    }

    @Test
    public void testSuperExtensibleEnum() throws Exception {
        Document doc = builder.newDocument();

        Element enumXml = new IpsEnumToXmlWriter(repository, SuperExtensibleEnum.class).toXml(doc);

        doc.appendChild(enumXml);
        String xml = xmlToString(doc);
        assertThat(xml, containsString("enumType=\"model.SuperExtensibleEnum\""));
        assertThat(xml, containsString("<Description locale=\"en\">The Description</Description>"));
        assertThat(xml, containsString("<Description locale=\"de\">Die Beschreibung</Description>"));
        assertThat(xml, containsString("<EnumAttributeValue>1</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeValue>super_extensible_enum_1</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeValue>2</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeValue>super_extensible_enum_2</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"id\"/>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"name\"/>"));
    }

    @Test
    public void testSuperExtensibleWithModellEnum() throws Exception {
        Document doc = builder.newDocument();

        Element enumXml = new IpsEnumToXmlWriter(repository, SuperExtensibleEnumWithModell.class).toXml(doc);

        doc.appendChild(enumXml);
        String xml = xmlToString(doc);
        assertThat(xml, containsString("enumType=\"model.SuperExtensibleEnumWithModell\""));
        assertThat(xml, containsString("<Description locale=\"en\">The Description</Description>"));
        assertThat(xml, containsString("<Description locale=\"de\">Die Beschreibung</Description>"));
        assertThat(xml, not(containsString("<EnumAttributeValue>1</EnumAttributeValue>")));
        assertThat(xml, not(containsString("<EnumAttributeValue>super_extensible_enum_1</EnumAttributeValue>")));
        assertThat(xml, containsString("<EnumAttributeValue>2</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeValue>super_extensible_enum_2</EnumAttributeValue>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"id\"/>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"name\"/>"));
    }

    @Test
    public void testEmptyEnum() throws Exception {
        Document doc = builder.newDocument();

        Element enumXml = new IpsEnumToXmlWriter(repository, EmptyEnum.class).toXml(doc);

        doc.appendChild(enumXml);
        String xml = xmlToString(doc);
        assertThat(xml, containsString("enumType=\"empty.EmptyEnum\""));
        assertThat(xml, containsString("<Description locale=\"en\"/>"));
        assertThat(xml, containsString("<Description locale=\"de\"/>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"id\"/>"));
        assertThat(xml, containsString("<EnumAttributeReference name=\"name\"/>"));
    }

    private String xmlToString(Document doc) {
        try (StringWriter writer = new StringWriter();) {
            XmlUtil.writeXMLtoResult(new StreamResult(writer), doc, null, 0, "UTF-8");
            return writer.toString();
        } catch (TransformerException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
