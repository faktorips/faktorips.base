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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProductComponentLinkTest extends XmlAbstractTestCase {

    private ProductComponentLink<IProductComponent> linkSpy;
    private Element linkElement;
    private Document docMock;
    private ProductComponentLink<IProductComponent> link;
    private NodeList linkListInXml;
    private Element docElement;

    @Before
    public void setUp() throws Exception {
        ProductComponentGeneration generationMock = mock(ProductComponentGeneration.class);
        link = new ProductComponentLink<>(generationMock);
        linkListInXml = getTestDocument().getDocumentElement().getElementsByTagName("Link");
        linkSpy = spy(link);
        when(linkSpy.getTargetId()).thenReturn(null);
        when(linkSpy.getAssociationName()).thenReturn("associationName");

        linkElement = mock(Element.class);
        docElement = mock(Element.class);
        docMock = mock(Document.class);
        when(docMock.createElement("Link")).thenReturn(linkElement);
        when(linkElement.getOwnerDocument()).thenReturn(docMock);
        when(docMock.createElement("Description")).thenReturn(docElement);
    }

    @Test
    public void testToXmlWithNullValues() {
        when(linkSpy.getCardinality()).thenReturn(new CardinalityRange(0, 1, 1));
        linkSpy.toXml(docMock);

        verify(linkElement).setAttribute("association", "associationName");
        verify(linkElement).setAttribute("target", null);
        verify(linkElement).setAttribute("targetRuntimeId", null);
        verify(linkElement).setAttribute("minCardinality", "0");
        verify(linkElement).setAttribute("maxCardinality", "1");
        verify(linkElement).setAttribute("defaultCardinality", "1");
    }

    @Test
    public void testToXmlWithUnlimitedUpperBound() {
        when(linkSpy.getCardinality()).thenReturn(new CardinalityRange(0, Integer.MAX_VALUE, 1));
        linkSpy.toXml(docMock);

        verify(linkElement).setAttribute("association", "associationName");
        verify(linkElement).setAttribute("target", null);
        verify(linkElement).setAttribute("targetRuntimeId", null);
        verify(linkElement).setAttribute("minCardinality", "0");
        verify(linkElement).setAttribute("maxCardinality", "*");
        verify(linkElement).setAttribute("defaultCardinality", "1");
    }

    @Test
    public void testToXmlWithExluded() {
        when(linkSpy.getCardinality()).thenReturn(CardinalityRange.EXCLUDED);
        linkSpy.toXml(docMock);

        verify(linkElement).setAttribute("association", "associationName");
        verify(linkElement).setAttribute("targetRuntimeId", null);
        verify(linkElement).setAttribute("minCardinality", "0");
        verify(linkElement).setAttribute("maxCardinality", "0");
        verify(linkElement).setAttribute("defaultCardinality", "0");
    }

    @Test
    public void testToXmlWriteDescription() {
        link.initFromXml((Element)linkListInXml.item(0));

        link.toXml(docMock);

        verify(docElement).setAttribute("locale", "de");
        verify(docElement).setTextContent("Die Beschreibung 1");
        verify(docElement).setAttribute("locale", "en");
        verify(docElement).setTextContent("The Description 1");
    }

    @Test
    public void testInitFromXml() {
        link.initFromXml((Element)linkListInXml.item(0));
        CardinalityRange cardinality = link.getCardinality();

        assertEquals(Integer.valueOf(2), cardinality.getLowerBound());
        assertEquals(Integer.valueOf(3), cardinality.getUpperBound());
        assertEquals(Integer.valueOf(2), cardinality.getDefaultCardinality());
    }

    @Test
    public void testInitFromXml_UnlimitedUpperBound() {
        link.initFromXml((Element)linkListInXml.item(1));
        CardinalityRange cardinality = link.getCardinality();

        assertEquals(Integer.valueOf(1), cardinality.getLowerBound());
        assertEquals((Integer)Integer.MAX_VALUE, cardinality.getUpperBound());
        assertEquals(Integer.valueOf(1), cardinality.getDefaultCardinality());
    }

    @Test
    public void testInitFromXml_Excluded() {
        link.initFromXml((Element)linkListInXml.item(2));
        CardinalityRange cardinality = link.getCardinality();

        assertEquals(CardinalityRange.EXCLUDED, cardinality);
    }

    @Test
    public void testInitFromXml_GetDescription() {
        link.initFromXml((Element)linkListInXml.item(0));

        assertThat(link.getDescription(Locale.GERMAN), is("Die Beschreibung 1"));
        assertThat(link.getDescription(Locale.ENGLISH), is("The Description 1"));
        // default locale
        assertThat(link.getDescription(Locale.CHINESE), is("Die Beschreibung 1"));
    }
}
