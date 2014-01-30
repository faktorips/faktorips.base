/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.joda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.codegen.JavaCodeFragment;
import org.junit.Test;

/**
 * Test class for {@link BaseJodaDatatypeHelper}.
 */
public class BaseJodaDatatypeHelperTest {

    @Test
    public void testValueOfExpression() {
        BaseJodaDatatypeHelper baseJodaDatatypeHelper = new BaseJodaDatatypeHelper("myParseMethod");
        JavaCodeFragment valueOfExpression = baseJodaDatatypeHelper.valueOfExpression("myExpression");
        assertTrue(valueOfExpression.getImportDeclaration().isCovered(
                BaseJodaDatatypeHelper.ORG_FAKTORIPS_UTIL_JODA_UTIL));
        assertTrue(valueOfExpression.getSourcecode().contains("JodaUtil"));
        assertTrue(valueOfExpression.getSourcecode().contains("myParseMethod"));
        assertTrue(valueOfExpression.getSourcecode().contains("myExpression"));
    }

    @Test
    public void testNewInstance() {
        BaseJodaDatatypeHelper baseJodaDatatypeHelper = new BaseJodaDatatypeHelper("myParseMethod");
        JavaCodeFragment newInstance = baseJodaDatatypeHelper.newInstance("myExpression");
        assertTrue(newInstance.getImportDeclaration().isCovered(BaseJodaDatatypeHelper.ORG_FAKTORIPS_UTIL_JODA_UTIL));
        assertEquals("JodaUtil.myParseMethod(\"myExpression\")", newInstance.getSourcecode());
    }

    @Test
    public void testNewInstance_Null() {
        JavaCodeFragment newInstance = new BaseJodaDatatypeHelper("myParseMethod").newInstance(null);
        assertEquals("null", newInstance.getSourcecode());
    }

    @Test
    public void testNewInstance_EmptyString() {
        JavaCodeFragment newInstance = new BaseJodaDatatypeHelper("myParseMethod").newInstance("");
        assertEquals("null", newInstance.getSourcecode());
    }

}
