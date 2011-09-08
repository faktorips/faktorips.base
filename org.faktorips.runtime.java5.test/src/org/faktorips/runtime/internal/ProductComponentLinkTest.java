/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.IProductComponent;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductComponentLinkTest {

    private ProductComponentLink<IProductComponent> linkSpy;
    private Element linkElement;
    private Document docMock;

    @Before
    public void setUp() throws Exception {
        ProductComponentGeneration generationMock = mock(ProductComponentGeneration.class);
        ProductComponentLink<IProductComponent> link = new ProductComponentLink<IProductComponent>(generationMock);
        linkSpy = spy(link);
        when(linkSpy.getTargetId()).thenReturn(null);
        when(linkSpy.getAssociationName()).thenReturn("associationName");

        linkElement = mock(Element.class);
        docMock = mock(Document.class);
        when(docMock.createElement("Link")).thenReturn(linkElement);
    }

    @Test
    public void testToXmlWithNullValues() {
        when(linkSpy.getCardinality()).thenReturn(new CardinalityRange(0, 1, 1));
        linkSpy.toXml(docMock);

        verify(linkElement).setAttribute("association", "associationName");
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
        verify(linkElement).setAttribute("targetRuntimeId", null);
        verify(linkElement).setAttribute("minCardinality", "0");
        verify(linkElement).setAttribute("maxCardinality", "*");
        verify(linkElement).setAttribute("defaultCardinality", "1");
    }

}
