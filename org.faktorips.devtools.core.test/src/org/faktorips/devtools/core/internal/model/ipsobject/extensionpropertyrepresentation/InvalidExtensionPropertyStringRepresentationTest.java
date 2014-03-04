/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsobject.extensionpropertyrepresentation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@RunWith(MockitoJUnitRunner.class)
public class InvalidExtensionPropertyStringRepresentationTest {

    private String ID = "myId";

    private static final String VALUE = "myValue";

    @Mock
    private Element extPropertiesEl;

    @Mock
    private Element valueElement;

    @Mock
    private Document document;

    @Mock
    private CDATASection cdata;

    @Captor
    private ArgumentCaptor<Node> childCaptor;

    private InvalidExtensionPropertyStringRepresentation invalidExtensionPropertyStringRepresentation = new InvalidExtensionPropertyStringRepresentation(
            ID, VALUE);

    @Test
    public void testSaveElementInXML() throws Exception {
        when(extPropertiesEl.getOwnerDocument()).thenReturn(document);
        when(valueElement.getOwnerDocument()).thenReturn(document);
        when(document.createElement("Value")).thenReturn(valueElement);
        when(document.createCDATASection(VALUE)).thenReturn(cdata);

        invalidExtensionPropertyStringRepresentation.saveElementInXML(extPropertiesEl);

        verify(valueElement).setAttribute("id", ID);
        verify(valueElement).setAttribute("isNull", "false");
        verify(valueElement).appendChild(cdata);
    }
}
