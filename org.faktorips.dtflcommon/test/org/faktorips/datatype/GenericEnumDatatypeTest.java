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

}
