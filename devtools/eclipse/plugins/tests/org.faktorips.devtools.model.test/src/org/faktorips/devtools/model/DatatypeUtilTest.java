/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.PrimitiveIntegerDatatype;
import org.faktorips.datatype.PrimitiveLongDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.DatatypeUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatatypeUtilTest {

    private static final String NULL_VALUE = "NULL_VALUE";

    @Mock
    private IEnumContent enumContent;

    @Mock
    private IEnumType enumType;

    @Mock
    private IEnumType superEnumType;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ValueDatatype valueDatatype;

    private EnumTypeDatatypeAdapter enumDatatype;

    private EnumTypeDatatypeAdapter superenumDatatype;

    @Before
    public void setUp() {
        enumDatatype = new EnumTypeDatatypeAdapter(enumType, enumContent);
        superenumDatatype = new EnumTypeDatatypeAdapter(superEnumType, enumContent);
        when(enumType.getIpsProject()).thenReturn(ipsProject);
        when(superEnumType.getIpsProject()).thenReturn(ipsProject);
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

    @Test
    public void testIsNullValue() throws Exception {
        when(valueDatatype.isNull(NULL_VALUE)).thenReturn(true);

        assertThat(DatatypeUtil.isNullValue(valueDatatype, null), is(true));
        assertThat(DatatypeUtil.isNullValue(valueDatatype, NULL_VALUE), is(true));
        assertThat(DatatypeUtil.isNullValue(valueDatatype, "abc"), is(false));
        assertThat(DatatypeUtil.isNullValue(valueDatatype, ""), is(false));
        assertThat(DatatypeUtil.isNullValue(valueDatatype, "<null>"), is(false));
    }

    @Test
    public void testIsPrimitiveNullValue() throws Exception {
        PrimitiveIntegerDatatype primIntDatatype = new PrimitiveIntegerDatatype();
        assertThat(DatatypeUtil.isPrimitiveNullValue(primIntDatatype, ""), is(false));
        assertThat(DatatypeUtil.isPrimitiveNullValue(primIntDatatype, "0"), is(true));

        PrimitiveLongDatatype primLongDatatype = new PrimitiveLongDatatype();
        assertThat(DatatypeUtil.isPrimitiveNullValue(primLongDatatype, ""), is(false));
        assertThat(DatatypeUtil.isPrimitiveNullValue(primLongDatatype, "0"), is(true));

        PrimitiveBooleanDatatype primBooleanDatatype = new PrimitiveBooleanDatatype();
        assertThat(DatatypeUtil.isPrimitiveNullValue(primBooleanDatatype, ""), is(false));
        assertThat(DatatypeUtil.isPrimitiveNullValue(primBooleanDatatype, "false"), is(true));
    }

    @Test
    public void testIsNonNullValue() throws Exception {
        when(valueDatatype.isNull(NULL_VALUE)).thenReturn(true);

        assertThat(DatatypeUtil.isNonNull(valueDatatype, (String)null), is(false));
        assertThat(DatatypeUtil.isNonNull(valueDatatype, "", "abc", NULL_VALUE), is(false));
        assertThat(DatatypeUtil.isNonNull(valueDatatype, "asd", "", null, "123"), is(false));
        assertThat(DatatypeUtil.isNonNull(valueDatatype, NULL_VALUE), is(false));
        assertThat(DatatypeUtil.isNonNull(valueDatatype, "abc"), is(true));
        assertThat(DatatypeUtil.isNonNull(valueDatatype, ""), is(true));
        assertThat(DatatypeUtil.isNonNull(valueDatatype, "<null>"), is(true));
    }

}
