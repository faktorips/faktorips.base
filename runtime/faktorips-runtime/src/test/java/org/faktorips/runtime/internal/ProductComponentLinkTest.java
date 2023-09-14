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
        ProductComponentLink<IProductComponent> link = new ProductComponentLink<>(generationMock);
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

}
