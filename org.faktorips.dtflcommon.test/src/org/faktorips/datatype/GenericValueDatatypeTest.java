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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class GenericValueDatatypeTest {

    private DefaultGenericValueDatatype datatype;

    @Before
    public void setUp() throws Exception {
        datatype = new DefaultGenericValueDatatype(PaymentMode.class);
    }

    @Test
    public void testValidate_ClassNotFound() {
        GenericValueDatatype type = new InvalidType();
        MessageList list = type.checkReadyToUse();
        assertEquals(1, list.size());
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_JAVACLASS_NOT_FOUND));
    }

    @Test
    public void testValidate_InvalidMethods() {
        datatype.setIsParsableMethodName("unknownMethod"); //$NON-NLS-1$
        datatype.setToStringMethodName("unknownMethod"); //$NON-NLS-1$
        datatype.setValueOfMethodName("unknownMethod"); //$NON-NLS-1$
        datatype.setNullObjectDefined(false);
        MessageList list = datatype.checkReadyToUse();
        assertEquals(3, list.size());
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_GETVALUE_METHOD_NOT_FOUND));
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_ISPARSABLE_METHOD_NOT_FOUND));
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_TOSTRING_METHOD_NOT_FOUND));
    }

    @Test
    public void testValidate_InvalidSpecialCaseNull() {
        datatype.setIsParsableMethodName("isParsable"); //$NON-NLS-1$
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        datatype.setValueOfMethodName("getPaymentMode"); //$NON-NLS-1$
        datatype.setNullObjectDefined(true);
        datatype.setNullObjectId("unknownValue"); //$NON-NLS-1$
        MessageList list = datatype.checkReadyToUse();
        assertEquals(1, list.size());
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_SPECIALCASE_NULL_NOT_FOUND));

        datatype.setNullObjectId(PaymentMode.ANNUAL.getId());
        list = datatype.checkReadyToUse();
        assertNotNull(list.getMessageByCode(GenericValueDatatype.MSGCODE_SPECIALCASE_NULL_IS_NOT_NULL));
    }

    @Test
    public void testIsParsable() {
        datatype.setIsParsableMethodName("isParsable"); //$NON-NLS-1$
        assertTrue(datatype.isParsable(PaymentMode.ANNUAL.getId()));
        assertFalse(datatype.isParsable("unknownId")); //$NON-NLS-1$
        assertTrue(datatype.isParsable(null));

        datatype = new DefaultGenericValueDatatype(TestValueClass.class);
        datatype.setValueOfMethodName("getInteger"); //$NON-NLS-1$
        datatype.setIsParsableMethodName("isInteger"); //$NON-NLS-1$
        assertTrue(datatype.isParsable("42")); //$NON-NLS-1$
        assertTrue(datatype.isParsable(null));
        assertFalse(datatype.isParsable("abc")); //$NON-NLS-1$
    }

    @Test
    public void testGetIsParsableMethod() {
        assertNotNull(datatype.getIsParsableMethod());
        datatype.setIsParsableMethodName("unknownMethod"); //$NON-NLS-1$
        try {
            datatype.getIsParsableMethod();
            fail();
        } catch (RuntimeException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetValue() {
        datatype.setValueOfMethodName("getPaymentMode"); //$NON-NLS-1$
        assertEquals(PaymentMode.ANNUAL, datatype.getValue(PaymentMode.ANNUAL.getId()));

        datatype = new DefaultGenericValueDatatype(TestValueClass.class);
        datatype.setValueOfMethodName("getInteger"); //$NON-NLS-1$
        assertEquals(new Integer(42), datatype.getValue("42")); //$NON-NLS-1$
        assertNull(datatype.getValue(null));
    }

    @Test
    public void testGetValueOfMethod() {
        datatype.setValueOfMethodName("getPaymentMode"); //$NON-NLS-1$
        assertNotNull(datatype.getValueOfMethod());
        datatype.setValueOfMethodName("unknownMethod"); //$NON-NLS-1$
        try {
            datatype.getValueOfMethod();
            fail();
        } catch (RuntimeException e) {
            // Expected exception
        }
    }

    @Test
    public void testValueToString() {
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        assertEquals(PaymentMode.ANNUAL.getId(), datatype.valueToString(PaymentMode.ANNUAL));
    }

    @Test
    public void testGetToStringMethod() {
        datatype.setToStringMethodName("getId"); //$NON-NLS-1$
        assertNotNull(datatype.getToStringMethod());
        datatype.setToStringMethodName("unknownMethod"); //$NON-NLS-1$
        try {
            datatype.getValueOfMethod();
            fail();
        } catch (RuntimeException e) {
            // Expected exception
        }
        // Payment hasn't got a special toString method, but the super type
        datatype.setToStringMethodName("toString"); //$NON-NLS-1$
        assertNotNull(datatype.getToStringMethod());
    }

    @Test
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
        paymentMode2.setQualifiedName("PaymentMode"); //$NON-NLS-1$
        assertEquals(datatype, paymentMode2);
    }

    @Test
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
        paymentMode2.setQualifiedName("PaymentMode"); //$NON-NLS-1$
        assertEquals(datatype.hashCode(), paymentMode2.hashCode());
    }

    @Test
    public void testIsNull() {
        datatype.setValueOfMethodName("getPaymentMode"); //$NON-NLS-1$
        assertFalse(datatype.isNull(PaymentMode.ANNUAL.getId()));
        assertTrue(datatype.isNull(null));
    }

    @Test
    public void shouldCompareValuesOfEqualClasses() {
        datatype = spy(new DefaultGenericValueDatatype());

        doReturn(new Integer(5)).when(datatype).getValue("5");
        doReturn(new Integer(0)).when(datatype).getValue("0");
        doReturn(true).when(datatype).supportsCompare();
        int compare = datatype.compare("5", "0");
        assertEquals(1, compare);

        verify(datatype, times(2)).getValue(anyString());
    }

    @Test(expected = ClassCastException.class)
    public void shouldNotCompareValuesOfDifferentClasses() {
        datatype = spy(new DefaultGenericValueDatatype());

        doReturn(new Integer(5)).when(datatype).getValue("5");
        doReturn(new Double(0)).when(datatype).getValue("0");
        doReturn(true).when(datatype).supportsCompare();
        datatype.compare("5", "0");
    }

    private class InvalidType extends GenericValueDatatype {

        @Override
        public Class getAdaptedClass() {
            return null;
        }

        @Override
        public String getAdaptedClassName() {
            return "UnknownClass"; //$NON-NLS-1$
        }

    }

}
