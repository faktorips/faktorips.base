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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.xml.sax.Attributes;

public class TableContentsSaxHandlerTest {


    private static final String MY_ID = "myID";

    @Mock
    private TableContents tableContents;

    private MockitoSession mockito;

    private TableContentsSaxHandler tableContentsSaxHandler;

    @BeforeEach
    public void createTableContentsSaxHandler() throws Exception {
        mockito = createMocks(this);
        tableContentsSaxHandler = new TableContentsSaxHandler(tableContents, true);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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
