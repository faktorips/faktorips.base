/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.joda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.codegen.JavaCodeFragment;
import org.junit.Test;

/**
 * Test class for {@link BaseJodaDatatypeHelper}.
 */
public class BaseJodaDatatypeHelperTest {

    @Test
    public void testValueOfExpression() {
        BaseJodaDatatypeHelper baseJodaDatatypeHelper = new BaseJodaDatatypeHelper(
                LocalDateHelper.ORG_JODA_TIME_LOCAL_DATE, "myParseMethod");
        JavaCodeFragment valueOfExpression = baseJodaDatatypeHelper.valueOfExpression("myExpression");
        assertTrue(valueOfExpression.getImportDeclaration().isCovered(
                BaseJodaDatatypeHelper.ORG_FAKTORIPS_UTIL_JODA_UTIL));
        assertTrue(valueOfExpression.getSourcecode().contains("JodaUtil"));
        assertTrue(valueOfExpression.getSourcecode().contains("myParseMethod"));
        assertTrue(valueOfExpression.getSourcecode().contains("myExpression"));
    }

    @Test
    public void testGetToStringExpression() {
        BaseJodaDatatypeHelper baseJodaDatatypeHelper = new BaseJodaDatatypeHelper(null, null);
        JavaCodeFragment toStringExpression = baseJodaDatatypeHelper.getToStringExpression("myField");
        assertTrue(toStringExpression.getSourcecode().contains("myField"));
        assertTrue(toStringExpression.getSourcecode().contains("toString()"));
    }

    @Test
    public void testGetToStringExpression_withParam() {
        BaseJodaDatatypeHelper baseJodaDatatypeHelper = new BaseJodaDatatypeHelper(null, null) {
            @Override
            protected void appendToStringParameter(JavaCodeFragment fragment) {
                fragment.addImport(BaseJodaDatatypeHelperTest.class.getName());
                fragment.append("FooBar");
            }
        };
        JavaCodeFragment toStringExpression = baseJodaDatatypeHelper.getToStringExpression("myField");
        assertTrue(toStringExpression.getImportDeclaration().isCovered(BaseJodaDatatypeHelperTest.class));
        assertTrue(toStringExpression.getSourcecode().contains("myField"));
        assertTrue(toStringExpression.getSourcecode().contains("toString"));
        assertTrue(toStringExpression.getSourcecode().contains("FooBar"));
        assertFalse(toStringExpression.getSourcecode().contains("toString()"));
    }

    @Test
    public void testNewInstance() {
        BaseJodaDatatypeHelper baseJodaDatatypeHelper = new BaseJodaDatatypeHelper(
                LocalDateHelper.ORG_JODA_TIME_LOCAL_DATE, "myParseMethod");
        JavaCodeFragment newInstance = baseJodaDatatypeHelper.newInstance("myExpression");
        assertTrue(newInstance.getImportDeclaration().isCovered(BaseJodaDatatypeHelper.ORG_FAKTORIPS_UTIL_JODA_UTIL));
        assertEquals("JodaUtil.myParseMethod(\"myExpression\")", newInstance.getSourcecode());
    }

    @Test
    public void testNewInstance_Null() {
        JavaCodeFragment newInstance = new BaseJodaDatatypeHelper(LocalDateHelper.ORG_JODA_TIME_LOCAL_DATE,
                "myParseMethod").newInstance(null);
        assertEquals("null", newInstance.getSourcecode());
    }

    @Test
    public void testNewInstance_EmptyString() {
        JavaCodeFragment newInstance = new BaseJodaDatatypeHelper(LocalDateHelper.ORG_JODA_TIME_LOCAL_DATE,
                "myParseMethod").newInstance("");
        assertEquals("null", newInstance.getSourcecode());
    }

}
