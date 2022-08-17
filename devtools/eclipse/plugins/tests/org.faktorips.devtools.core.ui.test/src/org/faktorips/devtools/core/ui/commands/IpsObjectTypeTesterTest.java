/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.junit.Test;

public class IpsObjectTypeTesterTest {

    @Test
    public void testIsIpsObjectType() throws Exception {
        IpsObjectTypeTester tester = new IpsObjectTypeTester();
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        assertTrue(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_IPS_OBJECT_TYPE, null,
                IpsObjectType.PRODUCT_CMPT.getId()));
        assertFalse(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_IPS_OBJECT_TYPE, null, "FooBar"));
    }

    @Test
    public void testIsType() throws Exception {
        IpsObjectTypeTester tester = new IpsObjectTypeTester();
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT_TYPE);

        assertTrue(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_TYPE, null, null));

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        assertTrue(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_TYPE, null, null));

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        assertFalse(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_TYPE, null, null));

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.ENUM_TYPE);

        assertFalse(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_TYPE, null, null));
    }

    @Test
    public void testIsMetaType() throws Exception {
        IpsObjectTypeTester tester = new IpsObjectTypeTester();
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT_TYPE);

        assertTrue(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_META_TYPE, null, null));

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.ENUM_TYPE);

        assertTrue(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_META_TYPE, null, null));

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_STRUCTURE);

        assertTrue(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_META_TYPE, null, null));

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.TEST_CASE_TYPE);

        assertTrue(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_META_TYPE, null, null));

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        assertFalse(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_META_TYPE, null, null));

        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        assertFalse(tester.test(ipsSrcFile, IpsObjectTypeTester.PROPERTY_IS_META_TYPE, null, null));
    }

    @Test
    public void testUndefinedProperty() throws Exception {
        IpsObjectTypeTester tester = new IpsObjectTypeTester();
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        doThrow(new AssertionError("getIpsObjectType() should not be called if it is not relevant")).when(ipsSrcFile)
                .getIpsObjectType();

        assertFalse(tester.test(ipsSrcFile, "SomeProperty", null, null));
    }

}
