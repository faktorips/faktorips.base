/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype;

import junit.framework.TestCase;

public class GenericEnumDatatypeTest extends TestCase {

    private DefaultGenericEnumDatatype datatype;
    
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
        String[] valueIds = datatype.getAllValueIds();
        assertEquals(2, valueIds.length);
        assertEquals(PaymentMode.MONTHLY.getId(), valueIds[0]);
        assertEquals(PaymentMode.ANNUAL.getId(), valueIds[1]);
    }

    /*
     * Test method for 'org.faktorips.datatype.GenericEnumDatatype.getGetAllValuesMethod()'
     */
    public void testGetGetAllValuesMethod() {
        datatype.setGetAllValuesMethodName("getAllPaymentModes");
        assertNotNull(datatype.getGetAllValuesMethod());
        datatype.setGetAllValuesMethodName("unkownMethod");
        try {
            datatype.getGetAllValuesMethod();
            fail();
        } catch (RuntimeException e) {
        }
        
    }

    public void testGetNameMethod(){
        datatype.setGetAllValuesMethodName("getAllPaymentModes");
        datatype.setToStringMethodName("getId");
        datatype.setGetNameMethodName("getName");
        datatype.setValueOfMethodName("getPaymentMode");
        datatype.setIsSupportingNames(true);
        assertEquals("Annual Payment", datatype.getValueName(PaymentMode.ANNUAL.getId()));
    }
}
