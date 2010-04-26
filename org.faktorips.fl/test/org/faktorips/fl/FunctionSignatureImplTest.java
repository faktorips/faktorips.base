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

package org.faktorips.fl;

import junit.framework.TestCase;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;

/**
 *
 */
public class FunctionSignatureImplTest extends TestCase {

    public void testMatch() {
        Datatype[] argList = new Datatype[] { Datatype.DECIMAL, Datatype.MONEY };
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

    public void testToString() {
        FunctionSignatureImpl fct = new FunctionSignatureImpl("function1", Datatype.VOID, new Datatype[] {});
        assertEquals("void function1()", fct.toString());

        fct = new FunctionSignatureImpl("function1", Datatype.DECIMAL, new Datatype[] { Datatype.DECIMAL,
                Datatype.MONEY });
        assertEquals("Decimal function1(Decimal, Money)", fct.toString());
    }

}
