/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.datatype;

import junit.framework.TestCase;

public class GenericEnumDatatypeTest extends TestCase {

    private DefaultGenericEnumDatatype datatype;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        datatype = new DefaultGenericEnumDatatype(PaymentMode.class);
    }

    /*
     * Test method for 'org.faktorips.datatype.GenericEnumDatatype.getAllValueIds()'
     */
    public void testGetAllValueIds() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes");
        datatype.setToStringMethodName("getId");
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

    /*
     * Test method for 'org.faktorips.datatype.GenericEnumDatatype.getGetAllValuesMethod()'
     */
    public void testGetGetAllValuesMethod() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes");
        assertNotNull(datatype.getGetAllValuesMethod());
        datatype.setGetAllValuesMethodName("unknownMethod");
        try {
            datatype.getGetAllValuesMethod();
            fail();
        } catch (RuntimeException e) {
        }
    }

    public void testGetValueName() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes");
        datatype.setToStringMethodName("getId");
        datatype.setGetNameMethodName("getName");
        datatype.setValueOfMethodName("getPaymentMode");
        datatype.setIsSupportingNames(true);
        datatype.setCacheData(false);
        assertEquals("Annual Payment", datatype.getValueName(PaymentMode.ANNUAL.getId()));

        datatype.setCacheData(true);
        assertEquals("Annual Payment", datatype.getValueName(PaymentMode.ANNUAL.getId()));
    }

    public void testGetValue() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes");
        datatype.setToStringMethodName("getId");
        datatype.setValueOfMethodName("getPaymentMode");
        datatype.setCacheData(false);
        assertEquals(PaymentMode.ANNUAL, datatype.getValue(PaymentMode.ANNUAL.getId()));

        datatype.setCacheData(true);
        assertEquals(PaymentMode.ANNUAL, datatype.getValue(PaymentMode.ANNUAL.getId()));
    }

    public void testIsParsable() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes");
        datatype.setToStringMethodName("getId");
        datatype.setValueOfMethodName("getPaymentMode");
        datatype.setCacheData(false);
        assertTrue(datatype.isParsable(PaymentMode.ANNUAL.getId()));
        assertFalse(datatype.isParsable("unknownId"));

        datatype.setCacheData(true);
        assertTrue(datatype.isParsable(PaymentMode.ANNUAL.getId()));
        assertFalse(datatype.isParsable("unknownId"));
    }

    public void testGetValueIdsFromCache() throws Exception {
        datatype.setGetAllValuesMethodName("getAllPaymentModes");
        datatype.setToStringMethodName("getId");

        String[] ids = datatype.getAllValueIdsFromCache();
        assertNull(ids);

        datatype.setCacheData(true);
        ids = datatype.getAllValueIdsFromCache();
        assertEquals(2, ids.length);
        assertEquals(PaymentMode.MONTHLY.getId(), ids[0]);
        assertEquals(PaymentMode.ANNUAL.getId(), ids[1]);

        datatype.setCacheData(false);
        ids = datatype.getAllValueIdsFromCache();
        assertNull(ids); // should have cleared the cachde
    }

    public void testGetValueNamesFromCache() throws Exception {
        datatype.setGetAllValuesMethodName("getAllPaymentModes");
        datatype.setValueOfMethodName("getPaymentMode");
        datatype.setToStringMethodName("getId");

        datatype.setIsSupportingNames(false);
        try {
            datatype.getAllValueNamesFromCache();
            fail();
        } catch (RuntimeException e) {
            // datatype doesn not support names
        }

        datatype.setIsSupportingNames(true);
        datatype.setGetNameMethodName("getName");
        String[] names = datatype.getAllValueNamesFromCache();
        assertNull(names);

        datatype.setCacheData(true);
        names = datatype.getAllValueNamesFromCache();
        assertEquals(2, names.length);
        assertEquals(PaymentMode.MONTHLY.getName(), names[0]);
        assertEquals(PaymentMode.ANNUAL.getName(), names[1]);

        datatype.setCacheData(false);
        names = datatype.getAllValueNamesFromCache();
        assertNull(names); // should have cleared the cachde
    }

}
