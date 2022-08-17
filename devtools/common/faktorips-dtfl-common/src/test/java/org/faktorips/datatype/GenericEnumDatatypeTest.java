/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import static org.faktorips.datatype.GenericValueDatatype.MSGCODE_PREFIX_GET_ALL_VALUES_METHOD;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCodeThat;
import static org.faktorips.testsupport.IpsMatchers.hasMessageThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.values.NullObjectSupport;
import org.junit.Before;
import org.junit.Test;

public class GenericEnumDatatypeTest {

    private DefaultGenericEnumDatatype datatype;

    @Before
    public void setUp() throws Exception {
        datatype = new DefaultGenericEnumDatatype(PaymentMode.class);
    }

    @Test
    public void testGetAllValueIds_FromArray() {
        datatype.setAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        datatype.setNullObjectDefined(false);
        String[] valueIds = datatype.getAllValueIds(false);
        assertEquals(2, valueIds.length);
        assertEquals(PaymentMode.MONTHLY.getId(), valueIds[0]);
        assertEquals(PaymentMode.ANNUAL.getId(), valueIds[1]);

        valueIds = datatype.getAllValueIds(true);
        assertEquals(3, valueIds.length);
        assertEquals(null, valueIds[2]);

        // now do the same tests, but with caching enabled.
        datatype.setCacheData(true);
        valueIds = datatype.getAllValueIds(false);
        assertEquals(2, valueIds.length);
        assertEquals(PaymentMode.MONTHLY.getId(), valueIds[0]);
        assertEquals(PaymentMode.ANNUAL.getId(), valueIds[1]);

        valueIds = datatype.getAllValueIds(true);
        assertEquals(3, valueIds.length);
        assertEquals(null, valueIds[2]);
    }

    @Test
    public void testGetAllValueIds_FromList() {
        datatype = new DefaultGenericEnumDatatype(TestValueClassWithListOfAllValues.class);
        datatype.setAllValuesMethodName("getAllValues"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        datatype.setNullObjectDefined(false);
        String[] valueIds = datatype.getAllValueIds(false);
        assertEquals(3, valueIds.length);
        assertEquals("1", valueIds[0]); //$NON-NLS-1$
        assertEquals("2", valueIds[1]); //$NON-NLS-1$
        assertEquals("3", valueIds[2]); //$NON-NLS-1$

        valueIds = datatype.getAllValueIds(true);
        assertEquals(4, valueIds.length);
        assertEquals("1", valueIds[0]); //$NON-NLS-1$
        assertEquals("2", valueIds[1]); //$NON-NLS-1$
        assertEquals("3", valueIds[2]); //$NON-NLS-1$
        assertEquals(null, valueIds[3]);
    }

    @Test
    public void testGetAllValueIds_FromSet() {
        datatype = new DefaultGenericEnumDatatype(TestValueClassWithSetOfAllValuesAndNullObject.class);
        datatype.setAllValuesMethodName("getAllValues"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        datatype.setNullObjectDefined(true);
        datatype.setNullObjectId("0"); //$NON-NLS-1$
        String[] valueIds = datatype.getAllValueIds(false);
        assertEquals(3, valueIds.length);
        assertEquals("1", valueIds[0]); //$NON-NLS-1$
        assertEquals("2", valueIds[1]); //$NON-NLS-1$
        assertEquals("3", valueIds[2]); //$NON-NLS-1$

        valueIds = datatype.getAllValueIds(true);
        assertEquals(4, valueIds.length);
        assertEquals("1", valueIds[0]); //$NON-NLS-1$
        assertEquals("0", valueIds[1]); //$NON-NLS-1$
        assertEquals("2", valueIds[2]); //$NON-NLS-1$
        assertEquals("3", valueIds[3]); //$NON-NLS-1$
    }

    @Test
    public void testGetGetAllValuesMethod() {
        datatype.setAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
        assertThat(datatype.checkReadyToUse(),
                not(
                        hasMessageThat(
                                hasMessageCodeThat(
                                        startsWith(MSGCODE_PREFIX_GET_ALL_VALUES_METHOD)))));
        datatype.setAllValuesMethodName("unknownMethod"); //$NON-NLS-1$
        assertThat(datatype.checkReadyToUse(),
                hasMessageThat(
                        hasMessageCodeThat(
                                startsWith(MSGCODE_PREFIX_GET_ALL_VALUES_METHOD))));
    }

    @Test
    public void testGetValueName() {
        datatype.setAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        datatype.setGetNameMethodName("getName"); //$NON-NLS-1$
        datatype.setValueOfMethodName("getPaymentMode"); //$NON-NLS-1$
        datatype.setIsSupportingNames(true);
        datatype.setCacheData(false);
        assertEquals("Annual Payment", datatype.getValueName(PaymentMode.ANNUAL.getId())); //$NON-NLS-1$

        datatype.setCacheData(true);
        assertEquals("Annual Payment", datatype.getValueName(PaymentMode.ANNUAL.getId())); //$NON-NLS-1$
    }

    @Test
    public void testGetValue() {
        datatype.setAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        datatype.setValueOfMethodName("getPaymentMode"); //$NON-NLS-1$
        datatype.setCacheData(false);
        assertEquals(PaymentMode.ANNUAL, datatype.getValue(PaymentMode.ANNUAL.getId()));

        datatype.setCacheData(true);
        assertEquals(PaymentMode.ANNUAL, datatype.getValue(PaymentMode.ANNUAL.getId()));
    }

    @Test
    public void testIsParsable() {
        datatype.setAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        datatype.setValueOfMethodName("getPaymentMode"); //$NON-NLS-1$
        datatype.setCacheData(false);
        assertTrue(datatype.isParsable(PaymentMode.ANNUAL.getId()));
        assertFalse(datatype.isParsable("unknownId")); //$NON-NLS-1$

        datatype.setCacheData(true);
        assertTrue(datatype.isParsable(PaymentMode.ANNUAL.getId()));
        assertFalse(datatype.isParsable("unknownId")); //$NON-NLS-1$
    }

    @Test
    public void testGetValueIdsFromCache() throws Exception {
        datatype.setAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$

        String[] ids = datatype.getAllValueIdsFromCache();
        assertNull(ids);

        datatype.setCacheData(true);
        ids = datatype.getAllValueIdsFromCache();
        assertEquals(2, ids.length);
        assertEquals(PaymentMode.MONTHLY.getId(), ids[0]);
        assertEquals(PaymentMode.ANNUAL.getId(), ids[1]);

        datatype.setCacheData(false);
        ids = datatype.getAllValueIdsFromCache();
        assertNull(ids); // should have cleared the cached
    }

    @Test
    public void testGetValueNamesFromCache() throws Exception {
        datatype.setAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
        datatype.setValueOfMethodName("getPaymentMode"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$

        datatype.setIsSupportingNames(false);
        try {
            datatype.getAllValueNamesFromCache();
            fail();
        } catch (RuntimeException e) {
            // data type does not support names
        }

        datatype.setIsSupportingNames(true);
        datatype.setGetNameMethodName("getName"); //$NON-NLS-1$
        String[] names = datatype.getAllValueNamesFromCache();
        assertNull(names);

        datatype.setCacheData(true);
        names = datatype.getAllValueNamesFromCache();
        assertEquals(2, names.length);
        assertEquals(PaymentMode.MONTHLY.getName(), names[0]);
        assertEquals(PaymentMode.ANNUAL.getName(), names[1]);

        datatype.setCacheData(false);
        names = datatype.getAllValueNamesFromCache();
        assertNull(names); // should have cleared the cached
    }

    public static class TestValueClassWithListOfAllValues {
        private final int value;

        private TestValueClassWithListOfAllValues(int value) {
            this.value = value;
        }

        public static TestValueClassWithListOfAllValues of(String id) {
            return new TestValueClassWithListOfAllValues(Integer.parseInt(id));
        }

        public static List<TestValueClassWithListOfAllValues> getAllValues() {
            return List.of(of("1"), of("2"), of("3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        public String getId() {
            return Integer.toString(value);
        }
    }

    public static class TestValueClassWithSetOfAllValuesAndNullObject implements NullObjectSupport {

        public static TestValueClassWithSetOfAllValuesAndNullObject NULL = new TestValueClassWithSetOfAllValuesAndNullObject(
                0) {
            @Override
            public boolean isNull() {
                return true;
            }

            @Override
            public boolean isNotNull() {
                return false;
            }
        };

        private final int value;

        private TestValueClassWithSetOfAllValuesAndNullObject(int value) {
            this.value = value;
        }

        public static TestValueClassWithSetOfAllValuesAndNullObject of(String id) {
            return new TestValueClassWithSetOfAllValuesAndNullObject(Integer.parseInt(id));
        }

        public static Set<TestValueClassWithSetOfAllValuesAndNullObject> getAllValues() {
            return new LinkedHashSet<>(List.of(of("1"), NULL, of("2"), of("3"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        public String getId() {
            return Integer.toString(value);
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public boolean isNotNull() {
            return true;
        }

    }

}
