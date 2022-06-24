/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.junit.Before;
import org.junit.Test;

public class CurrencyHelperTest {

    private CurrencyHelper helper;
    private ImportDeclaration importDecl;

    @Before
    public void setUp() {
        helper = new CurrencyHelper();
        importDecl = new ImportDeclaration();
        importDecl.add("java.util.Currency"); //$NON-NLS-1$
    }

    @Test
    public void testCurrencyNewInstance() {
        assertThat(helper.newInstance("EUR"), is(new JavaCodeFragment("Currency.getInstance(\"EUR\")", importDecl))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testCurrencyNewInstance_Null() {
        assertThat(helper.newInstance(""), is(new JavaCodeFragment("null"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testCurrencyValueOf() {
        assertThat(helper.valueOfExpression("String.valueOf(\"EUR\")"), //$NON-NLS-1$
                is(new JavaCodeFragment("Currency.getInstance(String.valueOf(\"EUR\"))", importDecl))); //$NON-NLS-1$
    }

    @Test
    public void testCurrencyValueOf_Null() {
        assertThat(helper.valueOfExpression(null), is(new JavaCodeFragment("null"))); //$NON-NLS-1$
    }
}
