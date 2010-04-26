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

import org.faktorips.util.message.MessageList;

public class GenericValueDatatypeTest extends TestCase {

    private DefaultGenericValueDatatype datatype;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        datatype = new DefaultGenericValueDatatype(PaymentMode.class);
    }

    public void testValidate_ClassNotFound() {
        GenericValueDatatype type = new InvalidType();
        MessageList list = type.checkReadyToUse();
        assertEquals(1, list.getNoOfMessages());
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_JAVACLASS_NOT_FOUND));
    }

    public void testValidate_InvalidMethods() {
        datatype.setIsParsableMethodName("unknwonMethod");
        datatype.setToStringMethodName("unknwonMethod");
        datatype.setValueOfMethodName("unknwonMethod");
        datatype.setNullObjectDefined(false);
        MessageList list = datatype.checkReadyToUse();
        assertEquals(3, list.getNoOfMessages());
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_GETVALUE_METHOD_NOT_FOUND));
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_ISPARSABLE_METHOD_NOT_FOUND));
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_TOSTRING_METHOD_NOT_FOUND));
    }

    public void testValidate_InvalidSpecialCaseNull() {
        datatype.setIsParsableMethodName("isParsable");
        datatype.setToStringMethodName("getId");
        datatype.setValueOfMethodName("getPaymentMode");
        datatype.setNullObjectDefined(true);
        datatype.setNullObjectId("unknownValue");
        MessageList list = datatype.checkReadyToUse();
        assertEquals(1, list.getNoOfMessages());
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_SPECIALCASE_NULL_NOT_FOUND));

        datatype.setNullObjectId(PaymentMode.ANNUAL.getId());
        list = datatype.checkReadyToUse();
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_SPECIALCASE_NULL_IS_NOT_NULL));
    }

    /*
     * Test method for 'org.faktorips.datatype.GenericValueDatatype.isParsable(String)'
     */
    public void testIsParsable() {
        datatype.setIsParsableMethodName("isParsable");
        assertTrue(datatype.isParsable(PaymentMode.ANNUAL.getId()));
        assertFalse(datatype.isParsable("unknownId"));
        assertTrue(datatype.isParsable(null));

        datatype = new DefaultGenericValueDatatype(TestValueClass.class);
        datatype.setValueOfMethodName("getInteger");
        datatype.setIsParsableMethodName("isInteger");
        assertTrue(datatype.isParsable("42"));
        assertTrue(datatype.isParsable(null));
        assertFalse(datatype.isParsable("abc"));
    }

    /*
     * Test method for 'org.faktorips.datatype.GenericValueDatatype.getIsParsableMethod()'
     */
    public void testGetIsParsableMethod() {
        assertNotNull(datatype.getIsParsableMethod());
        datatype.setIsParsableMethodName("unknownMethod");
        try {
            datatype.getIsParsableMethod();
            fail();
        } catch (RuntimeException e) {
        }
    }

    /*
     * Test method for 'org.faktorips.datatype.GenericValueDatatype.getValue(String)'
     */
    public void testGetValue() {
        datatype.setValueOfMethodName("getPaymentMode");
        assertEquals(PaymentMode.ANNUAL, datatype.getValue(PaymentMode.ANNUAL.getId()));

        datatype = new DefaultGenericValueDatatype(TestValueClass.class);
        datatype.setValueOfMethodName("getInteger");
        assertEquals(new Integer(42), datatype.getValue("42"));
        assertNull(datatype.getValue(null));
    }

    /*
     * Test method for 'org.faktorips.datatype.GenericValueDatatype.getValueOfMethod()'
     */
    public void testGetValueOfMethod() {
        datatype.setValueOfMethodName("getPaymentMode");
        assertNotNull(datatype.getValueOfMethod());
        datatype.setValueOfMethodName("unknownMethod");
        try {
            datatype.getValueOfMethod();
            fail();
        } catch (RuntimeException e) {
        }
    }

    /*
     * Test method for 'org.faktorips.datatype.GenericValueDatatype.valueToString(Object)'
     */
    public void testValueToString() {
        datatype.setToStringMethodName("getId");
        assertEquals(PaymentMode.ANNUAL.getId(), datatype.valueToString(PaymentMode.ANNUAL));
    }

    /*
     * Test method for 'org.faktorips.datatype.GenericValueDatatype.getValueToStringMethod()'
     */
    public void testGetToStringMethod() {
        datatype.setToStringMethodName("getId");
        assertNotNull(datatype.getToStringMethod());
        datatype.setToStringMethodName("unknownMethod");
        try {
            datatype.getValueOfMethod();
            fail();
        } catch (RuntimeException e) {
        }
        // Payment hasn't got a special toString method, but the supertype
        datatype.setToStringMethodName("toString");
        assertNotNull(datatype.getToStringMethod());

    }

    public void testEquals() {
        assertEquals(datatype, datatype);
        assertFalse(datatype.equals(Datatype.INTEGER));
        GenericValueDatatype paymentMode2 = new GenericValueDatatype() {

            @Override
            public Class getAdaptedClass() {
                return null;
            }

            @Override
            public String getAdaptedClassName() {
                return null;
            }

        };
        paymentMode2.setQualifiedName("PaymentMode");
        assertEquals(datatype, paymentMode2);

    }

    public void testHashCode() {
        assertEquals(datatype.hashCode(), datatype.hashCode());
        assertFalse(datatype.hashCode() == Datatype.INTEGER.hashCode());
        GenericValueDatatype paymentMode2 = new GenericValueDatatype() {

            @Override
            public Class getAdaptedClass() {
                return null;
            }

            @Override
            public String getAdaptedClassName() {
                return null;
            }

        };
        paymentMode2.setQualifiedName("PaymentMode");
        assertEquals(datatype.hashCode(), paymentMode2.hashCode());

    }

    /*
     * Test method for 'org.faktorips.datatype.GenericValueDatatype.isNull(Object)'
     */
    public void testIsNull() {
        datatype.setValueOfMethodName("getPaymentMode");
        assertFalse(datatype.isNull(PaymentMode.ANNUAL.getId()));
        assertTrue(datatype.isNull(null));
    }

    private class InvalidType extends GenericValueDatatype {

        @Override
        public Class getAdaptedClass() {
            return null;
        }

        @Override
        public String getAdaptedClassName() {
            return "UnknownClass";
        }

    }

}
