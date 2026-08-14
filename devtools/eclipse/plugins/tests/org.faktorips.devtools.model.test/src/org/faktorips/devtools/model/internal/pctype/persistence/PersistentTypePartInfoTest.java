/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo.PersistentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PersistentTypePartInfoTest extends PersistenceIpsTest {

    private IPolicyCmptTypeAttribute pcTypePart;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        policyCmptType.getPersistenceTypeInfo().setPersistentType(PersistentType.ENTITY);
        pcTypePart = policyCmptType.newPolicyCmptTypeAttribute();
        pcTypePart.getPersistenceAttributeInfo().setTransient(false);
        pcTypePart.setName("attr1");
    }

    @Test
    public void testInitFromXml() {
        NodeList nodeList = getTestDocument().getElementsByTagName(IPersistentAttributeInfo.XML_TAG);
        assertEquals(1, nodeList.getLength());

        Element element = (Element)nodeList.item(0);
        IPersistentAttributeInfo persistenceAttributeInfo = pcTypePart.getPersistenceAttributeInfo();
        persistenceAttributeInfo.initFromXml(element);

        assertFalse(persistenceAttributeInfo.isTransient());
        assertEquals("premiumIndex", persistenceAttributeInfo.getIndexName());
    }

    @Test
    public void testToXml() {
        IPersistentAttributeInfo persistenceAttributeInfo = pcTypePart.getPersistenceAttributeInfo();
        persistenceAttributeInfo.setTransient(true);
        persistenceAttributeInfo.setIndexName("XYZ");
        Element element = policyCmptType.toXml(newDocument());

        PolicyCmptType copyOfPcType = (PolicyCmptType)newIpsObject(ipsProject, IpsObjectType.POLICY_CMPT_TYPE, "Copy");
        copyOfPcType.initFromXml(element);

        assertEquals(1, copyOfPcType.getPolicyCmptTypeAttributes().size());
        IPersistentAttributeInfo persistenceAttributeInfoCopy = copyOfPcType.getPolicyCmptTypeAttributes().get(0)
                .getPersistenceAttributeInfo();

        assertTrue(persistenceAttributeInfoCopy.isTransient());
        assertEquals("XYZ", persistenceAttributeInfo.getIndexName());
    }

}
