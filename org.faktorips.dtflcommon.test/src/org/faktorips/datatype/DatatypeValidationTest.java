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

import static org.faktorips.testsupport.IpsMatchers.containsNoErrorMessage;
import static org.faktorips.testsupport.IpsMatchers.hasErrorMessage;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Method;
import java.util.Currency;

import org.faktorips.runtime.MessageList;
import org.junit.Test;

public class DatatypeValidationTest {

    @Test
    public void testCheckMethod() throws Exception {
        Method method = getClass().getMethod("methodToTest", String.class);
        MessageList ml = new MessageList();

        DatatypeValidation.checkMethod(ml, method, false, String.class);

        assertThat(ml, containsNoErrorMessage());
    }

    @Test
    public void testCheckMethod_wrongReturnType() throws Exception {
        Method method = getClass().getMethod("methodToTest", String.class);
        MessageList ml = new MessageList();

        DatatypeValidation.checkMethod(ml, method, false, Boolean.class);

        assertThat(ml, hasErrorMessage(DatatypeValidation.MSGCODE_METHOD_NOT_FOUND));
    }

    @Test
    public void testCheckMethod_notStatic() throws Exception {
        Method method = getClass().getMethod("methodToTest", String.class);
        MessageList ml = new MessageList();

        DatatypeValidation.checkMethod(ml, method, true, String.class);

        assertThat(ml, hasErrorMessage(DatatypeValidation.MSGCODE_METHOD_NOT_STATIC));
    }

    @Test
    public void testCheckMethod_static() throws Exception {
        Method method = getClass().getMethod("staticMethodToTest");
        MessageList ml = new MessageList();

        DatatypeValidation.checkMethod(ml, method, true, Currency.class);

        assertThat(ml, containsNoErrorMessage());
    }

    @Test
    public void testCheckMethod_primitive() throws Exception {
        Method method = getClass().getMethod("primitiveMethodToTest");
        MessageList ml = new MessageList();

        DatatypeValidation.checkMethod(ml, method, false, Boolean.class);

        assertThat(ml, containsNoErrorMessage());
    }

    @Test
    public void testCheckMethod_primitiveAndprimitive() throws Exception {
        Method method = getClass().getMethod("primitiveMethodToTest");
        MessageList ml = new MessageList();

        DatatypeValidation.checkMethod(ml, method, false, boolean.class);

        assertThat(ml, containsNoErrorMessage());
    }

    @Test
    public void testCheckMethod_noMethod() {
        Method method = null;
        MessageList ml = new MessageList();

        DatatypeValidation.checkMethod(ml, method, false, boolean.class);

        assertThat(ml, containsNoErrorMessage());
    }

    @Test
    public void testCheckMethod_moreReturnTypes() throws Exception {
        Method method = getClass().getMethod("methodToTest", String.class);
        MessageList ml = new MessageList();

        DatatypeValidation.checkMethod(ml, method, false, Integer.class, String.class);

        assertThat(ml, containsNoErrorMessage());
    }

    @Test
    public void testCheckMethod_moreWrongReturnTypes() throws Exception {
        Method method = getClass().getMethod("methodToTest", String.class);
        MessageList ml = new MessageList();

        DatatypeValidation.checkMethod(ml, method, false, Integer.class, Double.class);

        assertThat(ml, hasErrorMessage(DatatypeValidation.MSGCODE_METHOD_NOT_FOUND));
    }

    public String methodToTest(String string) {
        return string;
    }

    public boolean primitiveMethodToTest() {
        return false;
    }

    public static Currency staticMethodToTest() {
        return Currency.getInstance("EUR");
    }
}
