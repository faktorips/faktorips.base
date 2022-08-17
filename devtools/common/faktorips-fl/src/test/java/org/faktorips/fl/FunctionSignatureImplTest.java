/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.junit.Test;

/**
 *
 */
public class FunctionSignatureImplTest {
    @Test
    public void testMatch() {
        Datatype[] argList = { Datatype.DECIMAL, Datatype.MONEY };
        FunctionSignatureImpl fct1 = new FunctionSignatureImpl("Function1", Datatype.DECIMAL, argList);

        // different name
        assertFalse(fct1.match("differentName", argList));

        // different number of arguments
        assertFalse(fct1.match("Function1", new Datatype[] { Datatype.DECIMAL, Datatype.MONEY, Datatype.BOOLEAN }));

        // different argument types
        assertFalse(fct1.match("Function1", new Datatype[] { Datatype.DECIMAL, Datatype.BOOLEAN }));

        // match
        assertTrue(fct1.match("Function1", argList));

        // test datatype any
        argList = new Datatype[] { AnyDatatype.INSTANCE, Datatype.MONEY };
        fct1 = new FunctionSignatureImpl("Function1", AnyDatatype.INSTANCE, argList);
        assertTrue(fct1.match("Function1", new Datatype[] { Datatype.DECIMAL, Datatype.MONEY }));
    }

    @Test
    public void testIsSame() {
        FunctionSignatureImpl fct1 = new FunctionSignatureImpl("Function1", Datatype.DECIMAL, new Datatype[] {
                Datatype.DECIMAL, Datatype.MONEY });

        // different name
        FunctionSignatureImpl fct2 = new FunctionSignatureImpl("Function2", Datatype.DECIMAL, new Datatype[] {
                Datatype.DECIMAL, Datatype.MONEY });
        assertFalse(fct1.isSame(fct2));

        // different return type
        FunctionSignatureImpl fct3 = new FunctionSignatureImpl("Function1", Datatype.MONEY, new Datatype[] {
                Datatype.DECIMAL, Datatype.MONEY });
        assertFalse(fct1.isSame(fct3));

        // different number of arguments
        FunctionSignatureImpl fct4 = new FunctionSignatureImpl("Function1", Datatype.MONEY, new Datatype[] {
                Datatype.DECIMAL, Datatype.MONEY, Datatype.BOOLEAN });
        assertFalse(fct1.isSame(fct4));

        // different argument types
        FunctionSignatureImpl fct5 = new FunctionSignatureImpl("Function1", Datatype.MONEY, new Datatype[] {
                Datatype.DECIMAL, Datatype.BOOLEAN });
        assertFalse(fct1.isSame(fct5));

        // same
        FunctionSignatureImpl fct6 = new FunctionSignatureImpl("Function1", Datatype.DECIMAL, new Datatype[] {
                Datatype.DECIMAL, Datatype.MONEY });
        assertTrue(fct1.isSame(fct6));
    }

    @Test
    public void testToString() {
        FunctionSignatureImpl fct = new FunctionSignatureImpl("function1", Datatype.VOID, new Datatype[] {});
        assertEquals("void function1()", fct.toString());

        fct = new FunctionSignatureImpl("function1", Datatype.DECIMAL, new Datatype[] { Datatype.DECIMAL,
                Datatype.MONEY });
        assertEquals("Decimal function1(Decimal, Money)", fct.toString());
    }

}
