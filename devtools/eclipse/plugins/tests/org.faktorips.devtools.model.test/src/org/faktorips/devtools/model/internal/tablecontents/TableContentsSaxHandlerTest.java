/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.xml.sax.Attributes;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TableContentsSaxHandlerTest {

    private static final String MY_ID = "myID";

    @Mock
    private TableContents tableContents;

    private TableContentsSaxHandler tableContentsSaxHandler;

    @BeforeEach
    public void createTableContentsSaxHandler() throws Exception {
        tableContentsSaxHandler = new TableContentsSaxHandler(tableContents, true);
    }

    @Test
    public void testEndElement_extensionProperty() throws Exception {
        tableContentsSaxHandler.startElement("", "", TableRows.getXmlExtPropertiesElementName(), null);
        Attributes attributes = mock(Attributes.class);
        when(attributes.getValue(TableRows.getXmlAttributeExtpropertyid())).thenReturn(MY_ID);
        tableContentsSaxHandler.startElement("", "", TableRows.getXmlValueElement(), attributes);

        tableContentsSaxHandler.endElement("", "", TableRows.getXmlValueElement());

        verify(tableContents).addExtensionProperty(MY_ID, "");
    }

}
