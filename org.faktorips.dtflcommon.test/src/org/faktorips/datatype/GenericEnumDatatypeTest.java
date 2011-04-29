/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.datatype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class GenericEnumDatatypeTest {

    private DefaultGenericEnumDatatype datatype;

    @Before
    public void setUp() throws Exception {
        datatype = new DefaultGenericEnumDatatype(PaymentMode.class);
    }

    @Test
    public void testGetAllValueIds() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
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
    public void testGetGetAllValuesMethod() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
        assertNotNull(datatype.getGetAllValuesMethod());
        datatype.setGetAllValuesMethodName("unknownMethod"); //$NON-NLS-1$
        try {
            datatype.getGetAllValuesMethod();
            fail();
        } catch (RuntimeException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetValueName() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
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
        datatype.setGetAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        datatype.setValueOfMethodName("getPaymentMode"); //$NON-NLS-1$
        datatype.setCacheData(false);
        assertEquals(PaymentMode.ANNUAL, datatype.getValue(PaymentMode.ANNUAL.getId()));

        datatype.setCacheData(true);
        assertEquals(PaymentMode.ANNUAL, datatype.getValue(PaymentMode.ANNUAL.getId()));
    }

    @Test
    public void testIsParsable() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
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
        datatype.setGetAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
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
        datatype.setGetAllValuesMethodName("getAllPaymentModes"); //$NON-NLS-1$
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

}
