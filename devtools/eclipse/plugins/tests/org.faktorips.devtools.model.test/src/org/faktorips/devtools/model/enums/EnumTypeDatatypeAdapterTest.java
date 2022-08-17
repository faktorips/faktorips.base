/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.IValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumTypeDatatypeAdapterTest {

    @Mock
    private IEnumContent covariantEnumContent;

    @Mock
    private IEnumType covariantEnumType;

    @Mock
    private IEnumType covariantSuperEnumType;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IEnumType enumType;
    @Mock
    private IEnumType enumType2;

    @Mock
    private IEnumContent enumContent;

    @Mock
    private IEnumValue enumValue;

    @Mock
    private IEnumAttribute displayNameAttribute;

    @Mock
    private IEnumAttributeValue enumAttributeValue;

    @Mock
    private IValue<IInternationalString> resultValue;

    private EnumTypeDatatypeAdapter covariantEnumDatatype;

    private EnumTypeDatatypeAdapter covariantSuperenumDatatype;

    private EnumTypeDatatypeAdapter adapter;

    private EnumTypeDatatypeAdapter adapterWithContent;

    @Before
    public void setUp() {
        covariantEnumDatatype = new EnumTypeDatatypeAdapter(covariantEnumType, covariantEnumContent);
        covariantSuperenumDatatype = new EnumTypeDatatypeAdapter(covariantSuperEnumType, covariantEnumContent);
        when(covariantEnumType.getIpsProject()).thenReturn(ipsProject);
        when(covariantSuperEnumType.getIpsProject()).thenReturn(ipsProject);
        when(covariantEnumType.isSubEnumTypeOrSelf(covariantEnumType, ipsProject)).thenReturn(true);
        when(covariantSuperEnumType.isSubEnumTypeOrSelf(covariantSuperEnumType, ipsProject)).thenReturn(true);
        when(covariantEnumType.isSubEnumTypeOrSelf(covariantSuperEnumType, ipsProject)).thenReturn(true);

        adapter = new EnumTypeDatatypeAdapter(enumType, null);
        adapterWithContent = new EnumTypeDatatypeAdapter(enumType, enumContent);
    }

    @Test
    public void testIsCovariantEnumTypeDatatypeAdapter_same() throws Exception {
        assertTrue(covariantEnumDatatype.isCovariant(covariantEnumDatatype));
        assertTrue(covariantSuperenumDatatype.isCovariant(covariantSuperenumDatatype));
    }

    @Test
    public void testIsCovariantEnumTypeDatatypeAdapter_subType() throws Exception {
        assertTrue(covariantEnumDatatype.isCovariant(covariantSuperenumDatatype));
        assertFalse(covariantSuperenumDatatype.isCovariant(covariantEnumDatatype));
    }

    @Test
    public void testIsCovariantEnumTypeDatatypeAdapter_otherType() throws Exception {
        assertFalse(covariantSuperenumDatatype.isCovariant(mock(ValueDatatype.class)));
    }

    @Test
    public void testCompare_shouldReturn0IfBothStringValuesAreNull() {
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        assertEquals(0, adapter.compare(null, null));
    }

    @Test
    public void testCompare_shouldReturn0IfBothEnumValuesAreNull() {
        setUpEnumValue("A", null);
        setUpEnumValue("B", null);

        assertEquals(0, adapter.compare("A", "B"));
    }

    @Test
    public void testCompare_shouldReturnLessThan0IfFirstStringValueIsNullAndSecondNotNull() {
        IEnumValue valueB = Mockito.mock(IEnumValue.class);
        setUpEnumValue("B", valueB);

        assertTrue(adapter.compare(null, "B") > 0);
    }

    @Test
    public void testCompare_shouldReturnLessThan0IfFirstEnumValueIsNullAndSecondNotNull() {
        setUpEnumValue("unresolvedA", null);
        IEnumValue valueB = Mockito.mock(IEnumValue.class);
        setUpEnumValue("B", valueB);

        assertTrue(adapter.compare("unresolvedA", "B") > 0);
    }

    @Test
    public void testCompare_shouldReturnGreaterThan0IfFirstStringValueIsNotNullAndSecondIsNull() {
        IEnumValue valueA = Mockito.mock(IEnumValue.class);
        setUpEnumValue("A", valueA);

        assertTrue(adapter.compare("A", null) < 0);
    }

    @Test
    public void testCompare_shouldReturnGreaterThan0IfFirstEnumValueIsNotNullSndSecondIsNull() {

        IEnumValue valueA = Mockito.mock(IEnumValue.class);
        setUpEnumValue("A", valueA);

        assertTrue(adapter.compare("A", "unresolvedB") < 0);
    }

    @Test
    public void testCompare_firstValueInEnumIsGreaterThenSecondValueInEnum() {
        IEnumValue valueA = Mockito.mock(IEnumValue.class);
        setUpEnumValueWithIndex("A", valueA, 1);

        IEnumValue valueB = Mockito.mock(IEnumValue.class);
        setUpEnumValueWithIndex("B", valueB, 2);

        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        assertTrue(adapter.compare("A", "B") < 0);
        assertTrue(adapter.compare("B", "A") > 0);
    }

    @Test
    public void testCompare_sortList() {
        IEnumValue valueA = Mockito.mock(IEnumValue.class);
        IEnumValue valueB = Mockito.mock(IEnumValue.class);
        IEnumValue valueC = Mockito.mock(IEnumValue.class);

        setUpEnumValueWithIndex("A", valueA, 0);
        setUpEnumValueWithIndex("B", valueB, 1);
        setUpEnumValueWithIndex("C", valueC, 2);

        List<String> list = new ArrayList<>();
        list.add("C");
        list.add("B");
        list.add(null);
        list.add("A");

        Collections.sort(list, new TestComparator(adapter));

        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertNull(list.get(3));
    }

    public void initEnumContentAndType() {
        when(enumContent.getIpsProject()).thenReturn(ipsProject);
        when(enumType.getIpsProject()).thenReturn(ipsProject);
        when(enumContent.findEnumValue("testValue", ipsProject)).thenReturn(enumValue);
    }

    @Test
    public void test_getValueName_NULL() {
        assertTrue(adapter.getValueName(null) == null);

        initEnumContentAndType();
        when(enumType.findUsedAsNameInFaktorIpsUiAttribute(ipsProject)).thenReturn(displayNameAttribute);
        when(enumValue.getEnumAttributeValue(displayNameAttribute)).thenReturn(null);

        assertTrue(adapterWithContent.getValueName("testValue") == null);
    }

    @Test
    public void test_getValue() {
        initEnumContentAndType();
        assertEquals(enumValue, adapterWithContent.getValue("testValue"));
    }

    @Test
    public void test_getValue_Null() {
        initEnumContentAndType();
        when(enumContent.findEnumValue("testValue", ipsProject)).thenReturn(null);
        assertEquals(null, adapterWithContent.getValue("testValue"));
    }

    @Test
    public void test_getAllValueIds_EnumContentNotNull_ResultContainsNull() {
        List<String> list = new ArrayList<>();
        list.add("result1");
        list.add("result2");

        when(enumContent.getIpsProject()).thenReturn(ipsProject);
        when(enumContent.findAllIdentifierAttributeValues(ipsProject)).thenReturn(list);
        String[] result = adapterWithContent.getAllValueIds(true);

        assertEquals(3, result.length);
        assertEquals("result1", result[0]);
    }

    @Test
    public void test_getAllValueIds_EnumContentNotNull() {

        List<String> list = new ArrayList<>();
        list.add("result1");

        when(enumContent.getIpsProject()).thenReturn(ipsProject);
        when(enumContent.findAllIdentifierAttributeValues(ipsProject)).thenReturn(list);

        String[] result = adapterWithContent.getAllValueIds(false);
        assertEquals(1, result.length);
        assertEquals("result1", result[0]);
    }

    private static class TestComparator implements Comparator<String> {

        private final EnumTypeDatatypeAdapter enumDatatypeAdapter;

        public TestComparator(EnumTypeDatatypeAdapter enumDatatypeAdapter) {
            this.enumDatatypeAdapter = enumDatatypeAdapter;
        }

        @Override
        public int compare(String enumValue1, String enumValue2) {

            return enumDatatypeAdapter.compare(enumValue1, enumValue2);
        }
    }

    private void setUpEnumValue(String enumName, IEnumValue value) {
        Mockito.when(enumType.findEnumValue(enumName, null)).thenReturn(value);
    }

    private void setUpEnumValueWithIndex(String enumName, IEnumValue value, int index) {
        setUpEnumValue(enumName, value);
        Mockito.when(enumType.getIndexOfEnumValue(value)).thenReturn(index);
    }

    @Test
    public void testEquals() {
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, enumContent);
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType, enumContent);

        assertReflexiveEqual(adapter1, adapter2);
    }

    @Test
    public void testEquals_nullContent() {
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, enumContent);
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType, null);

        assertReflexiveUnequal(adapter1, adapter2);
    }

    @Test
    public void testEquals_bothNullContents() {
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, null);
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType, null);

        assertReflexiveEqual(adapter1, adapter2);
    }

    @Test
    public void testEquals_differentEnumTypes() {
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, null);
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType2, null);

        assertReflexiveUnequal(adapter1, adapter2);
    }

    @Test
    public void testEquals_differentEnumTypes_sameContents() {
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, enumContent);
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType2, enumContent);

        assertReflexiveUnequal(adapter1, adapter2);
    }

    @Test
    public void testEquals_differentEnumTypes_nullContent() {
        EnumTypeDatatypeAdapter adapter1 = new EnumTypeDatatypeAdapter(enumType, null);
        EnumTypeDatatypeAdapter adapter2 = new EnumTypeDatatypeAdapter(enumType2, enumContent);

        assertReflexiveUnequal(adapter1, adapter2);
    }

    private void assertReflexiveEqual(EnumTypeDatatypeAdapter adapter1, EnumTypeDatatypeAdapter adapter2) {
        assertTrue(adapter1.equals(adapter2));
        assertTrue(adapter2.equals(adapter1));
    }

    private void assertReflexiveUnequal(EnumTypeDatatypeAdapter adapter1, EnumTypeDatatypeAdapter adapter2) {
        assertFalse(adapter1.equals(adapter2));
        assertFalse(adapter2.equals(adapter1));
    }

}
