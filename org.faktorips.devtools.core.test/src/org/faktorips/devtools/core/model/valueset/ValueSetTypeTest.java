/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.valueset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.DerivedValueSet;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class ValueSetTypeTest {

    private static final String MY_ID = "myId";
    @Mock
    private IValueSetOwner mockOwner;

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
        Document document = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element xml = dummyValueSet.toXml(document);

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertTrue(newValueSet instanceof UnrestrictedValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testNewValueSet_ENUM() throws Exception {
        IValueSet dummyValueSet = ValueSetType.ENUM.newValueSet(mockOwner, MY_ID);
        Document document = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element xml = dummyValueSet.toXml(document);

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertTrue(newValueSet instanceof EnumValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testNewValueSet_RANGE() throws Exception {
        IValueSet dummyValueSet = ValueSetType.RANGE.newValueSet(mockOwner, MY_ID);
        Document document = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element xml = dummyValueSet.toXml(document);

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertTrue(newValueSet instanceof RangeValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testNewValueSet_DERIVED() throws Exception {
        IValueSet dummyValueSet = ValueSetType.DERIVED.newValueSet(mockOwner, MY_ID);
        Document document = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element xml = dummyValueSet.toXml(document);

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertTrue(newValueSet instanceof DerivedValueSet);
        assertEquals(MY_ID, newValueSet.getId());
        assertEquals(mockOwner, newValueSet.getParent());
    }

    @Test
    public void testNewValueSet_None() throws Exception {
        Document document = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element xml = document.createElement("foo");

        IValueSet newValueSet = ValueSetType.newValueSet(xml, mockOwner, MY_ID);

        assertNull(newValueSet);
    }

}
