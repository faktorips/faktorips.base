/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentLink;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProductComponentTest {

    @SuppressWarnings("unchecked")
    // the verify for the parameterized map cannot be type safe
    @Test
    public void testCallInitReferencesOnInitFromXML() {
        ProductComponent cmpt = mock(ProductComponent.class, CALLS_REAL_METHODS);
        Element element = setUpElement();

        cmpt.initFromXml(element);

        verify(cmpt).doInitReferencesFromXml(anyMap());
    }

    private Element setUpElement() {
        Element element = mock(Element.class);
        Element validToElement = mock(Element.class);
        NodeList nodeList = mock(NodeList.class);
        NodeList emptyNodeList = mock(NodeList.class);

        when(element.getElementsByTagName(anyString())).thenReturn(nodeList);
        when(nodeList.item(0)).thenReturn(validToElement);
        when(element.getChildNodes()).thenReturn(emptyNodeList);

        return element;
    }

    @Test
    public void testGetLinks() {
        ProductComponent cmpt = mock(ProductComponent.class, CALLS_REAL_METHODS);
        List<IProductComponentLink<? extends IProductComponent>> links = cmpt.getLinks();

        assertEquals(0, links.size());
    }

    @Test
    public void testGetLinkForName() {
        ProductComponent cmpt = mock(ProductComponent.class, CALLS_REAL_METHODS);
        IProductComponentLink<? extends IProductComponent> link = cmpt.getLink("", null);

        assertNull(link);
    }
}
