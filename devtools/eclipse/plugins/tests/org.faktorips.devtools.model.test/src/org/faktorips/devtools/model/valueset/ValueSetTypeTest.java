/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.valueset;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.faktorips.devtools.model.internal.valueset.DerivedValueSet;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.model.util.XmlUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ValueSetTypeTest {


    private static final String MY_ID = "myId";
    @Mock
    private IValueSetOwner mockOwner;

    private MockitoSession mockito;

    @BeforeEach
    void setUp() {
        mockito = createMocks(this);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }



    @Test
    public void testUNRESTRICTEDNewValueSet() throws Exception {
        IValueSet newValueSet = ValueSetType.UNRESTRICTED.newValueSet(mockOwner, MY_ID);

        assertTrue(newValueSet instanceof UnrestrictedValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testENUMNewValueSet() throws Exception {
        IValueSet newValueSet = ValueSetType.ENUM.newValueSet(mockOwner, MY_ID);

        assertTrue(newValueSet instanceof EnumValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testRANGENewValueSet() throws Exception {
        IValueSet newValueSet = ValueSetType.RANGE.newValueSet(mockOwner, MY_ID);

        assertTrue(newValueSet instanceof RangeValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testDERIVEDNewValueSet() throws Exception {
        IValueSet newValueSet = ValueSetType.DERIVED.newValueSet(mockOwner, MY_ID);

        assertTrue(newValueSet instanceof DerivedValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testNewValueSet_UNRESTRICTED() throws Exception {
        IValueSet dummyValueSet = ValueSetType.UNRESTRICTED.newValueSet(mockOwner, MY_ID);
        Document document = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element xml = dummyValueSet.toXml(document);

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertTrue(newValueSet instanceof UnrestrictedValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testNewValueSet_ENUM() throws Exception {
        IValueSet dummyValueSet = ValueSetType.ENUM.newValueSet(mockOwner, MY_ID);
        Document document = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element xml = dummyValueSet.toXml(document);

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertTrue(newValueSet instanceof EnumValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testNewValueSet_RANGE() throws Exception {
        IValueSet dummyValueSet = ValueSetType.RANGE.newValueSet(mockOwner, MY_ID);
        Document document = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element xml = dummyValueSet.toXml(document);

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertTrue(newValueSet instanceof RangeValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testNewValueSet_DERIVED() throws Exception {
        IValueSet dummyValueSet = ValueSetType.DERIVED.newValueSet(mockOwner, MY_ID);
        Document document = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element xml = dummyValueSet.toXml(document);

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertTrue(newValueSet instanceof DerivedValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testNewValueSet_None() throws Exception {
        Document document = XmlUtil.getDefaultDocumentBuilder().newDocument();
        Element xml = document.createElement("foo");

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertNull(newValueSet);
    }

}
