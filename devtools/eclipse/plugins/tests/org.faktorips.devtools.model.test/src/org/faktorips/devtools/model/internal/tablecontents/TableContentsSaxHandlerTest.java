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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.Attributes;

@RunWith(MockitoJUnitRunner.class)
public class TableContentsSaxHandlerTest {

    private static final String MY_ID = "myID";

    @Mock
    private TableContents tableContents;

    private TableContentsSaxHandler tableContentsSaxHandler;

    @Before
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
