/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumTypeDatatypeAdapterTest {

    @Mock
    private IEnumType enumType;
    private EnumTypeDatatypeAdapter adapter;

    @Before
    public void setUp() {
        adapter = new EnumTypeDatatypeAdapter(enumType, null);
    }

    @Test
    public void testCompare_shouldReturn0IfBothStringValuesAreNull() {
        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        assertEquals(0, adapter.compare(null, null));
    }

    @Test
    public void testCompare_shouldReturn0IfBothEnumValuesAreNull() throws CoreException {
        setUpEnumValue("A", null);
        setUpEnumValue("B", null);

        assertEquals(0, adapter.compare("A", "B"));
    }

    @Test
    public void testCompare_shouldReturnLessThan0IfFirstStringValueIsNullAndSecondNotNull() throws CoreException {
        IEnumValue valueB = Mockito.mock(IEnumValue.class);
        setUpEnumValue("B", valueB);

        assertTrue(adapter.compare(null, "B") > 0);
    }

    @Test
    public void testCompare_shouldReturnLessThan0IfFirstEnumValueIsNullAndSecondNotNull() throws CoreException {
        setUpEnumValue("unresolvedA", null);
        IEnumValue valueB = Mockito.mock(IEnumValue.class);
        setUpEnumValue("B", valueB);

        assertTrue(adapter.compare("unresolvedA", "B") > 0);
    }

    @Test
    public void testCompare_shouldReturnGreaterThan0IfFirstStringValueIsNotNullAndSecondIsNull() throws CoreException {
        IEnumValue valueA = Mockito.mock(IEnumValue.class);
        setUpEnumValue("A", valueA);

        assertTrue(adapter.compare("A", null) < 0);
    }

    @Test
    public void testCompare_shouldReturnGreaterThan0IfFirstEnumValueIsNotNullSndSecondIsNull() throws CoreException {

        IEnumValue valueA = Mockito.mock(IEnumValue.class);
        setUpEnumValue("A", valueA);

        assertTrue(adapter.compare("A", "unresolvedB") < 0);
    }

    @Test
    public void testCompare_firstValueInEnumIsGreaterThenSecondValueInEnum() throws CoreException {
        IEnumValue valueA = Mockito.mock(IEnumValue.class);
        setUpEnumValueWithIndex("A", valueA, 1);

        IEnumValue valueB = Mockito.mock(IEnumValue.class);
        setUpEnumValueWithIndex("B", valueB, 2);

        EnumTypeDatatypeAdapter adapter = new EnumTypeDatatypeAdapter(enumType, null);
        assertTrue(adapter.compare("A", "B") < 0);
        assertTrue(adapter.compare("B", "A") > 0);
    }

    @Test
    public void testCompare_sortList() throws CoreException {
        IEnumValue valueA = Mockito.mock(IEnumValue.class);
        IEnumValue valueB = Mockito.mock(IEnumValue.class);
        IEnumValue valueC = Mockito.mock(IEnumValue.class);

        setUpEnumValueWithIndex("A", valueA, 0);
        setUpEnumValueWithIndex("B", valueB, 1);
        setUpEnumValueWithIndex("C", valueC, 2);

        List<String> list = new ArrayList<String>();
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

    private void setUpEnumValue(String enumName, IEnumValue value) throws CoreException {
        Mockito.when(enumType.findEnumValue(enumName, null)).thenReturn(value);
    }

    private void setUpEnumValueWithIndex(String enumName, IEnumValue value, int index) throws CoreException {
        setUpEnumValue(enumName, value);
        Mockito.when(enumType.getIndexOfEnumValue(value)).thenReturn(index);
    }

}
