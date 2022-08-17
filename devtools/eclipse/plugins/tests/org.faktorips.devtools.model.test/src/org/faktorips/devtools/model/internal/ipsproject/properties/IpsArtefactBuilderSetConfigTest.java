/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.properties;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class IpsArtefactBuilderSetConfigTest extends AbstractIpsPluginTest {

    @Test
    public void testInitFromXml() throws Exception {
        IpsArtefactBuilderSetConfigModel config = new IpsArtefactBuilderSetConfigModel();
        Document doc = getTestDocument();
        config.initFromXml(doc.getDocumentElement());

        assertEquals("one", config.getPropertyValue("prop1"));
        assertEquals("two", config.getPropertyValue("prop2"));
    }

    @Test
    public void testToXml() throws Exception {
        IpsArtefactBuilderSetConfigModel config = new IpsArtefactBuilderSetConfigModel();
        Document doc = getTestDocument();
        config.initFromXml(doc.getDocumentElement());

        Element el = config.toXml(doc);
        IpsArtefactBuilderSetConfigModel newConfig = new IpsArtefactBuilderSetConfigModel();
        newConfig.initFromXml(el);

        assertEquals("one", newConfig.getPropertyValue("prop1"));
        assertEquals("two", newConfig.getPropertyValue("prop2"));
    }
}
