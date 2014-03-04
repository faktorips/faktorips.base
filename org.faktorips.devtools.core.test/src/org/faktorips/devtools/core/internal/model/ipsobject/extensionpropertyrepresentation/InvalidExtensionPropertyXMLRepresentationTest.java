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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@RunWith(MockitoJUnitRunner.class)
public class InvalidExtensionPropertyXMLRepresentationTest {

    @Mock
    private Element valueElement;

    @Mock
    private Element extPropertiesEl;

    @Mock
    private Node importedElement;

    @Mock
    private Document document;

    @InjectMocks
    private InvalidExtensionPropertyXMLRepresentation invalidExtensionProperty;

    @Test
    public void testSaveElementInXML() throws Exception {
        when(extPropertiesEl.getOwnerDocument()).thenReturn(document);
        when(document.importNode(valueElement, true)).thenReturn(importedElement);

        invalidExtensionProperty.saveElementInXML(extPropertiesEl);

        verify(extPropertiesEl).appendChild(importedElement);
    }

}
