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

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProductComponentTest {

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

    @Test(expected = RuntimeException.class)
    public void testGetLinks() {
        ProductComponent cmpt = mock(ProductComponent.class, CALLS_REAL_METHODS);
        // base implementation throws exception
        cmpt.getLinks();
    }

    @Test(expected = RuntimeException.class)
    public void testGetLinkForName() {
        ProductComponent cmpt = mock(ProductComponent.class, CALLS_REAL_METHODS);
        // base implementation throws exception
        cmpt.getLink("", null);
    }
}
