/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
