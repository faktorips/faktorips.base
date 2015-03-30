/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatatypeUtilTest {

    @Mock
    private IEnumContent enumContent;

    @Mock
    private IEnumType enumType;

    @Mock
    private IEnumType superEnumType;

    @Mock
    private IIpsProject ipsProject;

    private EnumTypeDatatypeAdapter enumDatatype;

    private EnumTypeDatatypeAdapter superenumDatatype;

    @Before
    public void setUp() {
        enumDatatype = new EnumTypeDatatypeAdapter(enumType, enumContent);
        superenumDatatype = new EnumTypeDatatypeAdapter(superEnumType, enumContent);
        when(enumType.getIpsProject()).thenReturn(ipsProject);
        when(superEnumType.getIpsProject()).thenReturn(ipsProject);
        when(enumType.isSubEnumTypeOrSelf(enumType, ipsProject)).thenReturn(true);
        when(superEnumType.isSubEnumTypeOrSelf(superEnumType, ipsProject)).thenReturn(true);
        when(enumType.isSubEnumTypeOrSelf(superEnumType, ipsProject)).thenReturn(true);
    }

    @Test
    public void testIsCovariant_same() throws Exception {
        assertTrue(DatatypeUtil.isCovariant(enumDatatype, enumDatatype));
        assertTrue(DatatypeUtil.isCovariant(superenumDatatype, superenumDatatype));
    }

    @Test
    public void testIsCovariant_subType() throws Exception {
        assertTrue(DatatypeUtil.isCovariant(enumDatatype, superenumDatatype));
        assertFalse(DatatypeUtil.isCovariant(superenumDatatype, enumDatatype));
    }

    @Test
    public void testIsCovariant_otherType() throws Exception {
        assertFalse(DatatypeUtil.isCovariant(superenumDatatype, mock(ValueDatatype.class)));
        assertFalse(DatatypeUtil.isCovariant(mock(ValueDatatype.class), superenumDatatype));
    }

    @Test
    public void testIsCovariant_null() throws Exception {
        assertFalse(DatatypeUtil.isCovariant(null, enumDatatype));
        assertFalse(DatatypeUtil.isCovariant(superenumDatatype, null));
        assertFalse(DatatypeUtil.isCovariant(null, null));
    }

}
