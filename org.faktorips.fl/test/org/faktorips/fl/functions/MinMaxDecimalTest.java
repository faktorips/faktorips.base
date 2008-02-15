/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;

public class MinMaxDecimalTest extends FunctionAbstractTest {

    public final void testCompile() throws Exception {
        registerFunction(new MinMaxDecimal("MAX", "", true));
        execAndTestSuccessfull("MAX(3.0; 4.0)", Decimal.valueOf("4.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MAX(4.0; 3.0)", Decimal.valueOf("4.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MAX(4; 3)", Decimal.valueOf("4"), Datatype.DECIMAL);

        registerFunction(new MinMaxDecimal("MIN", "", false));
        execAndTestSuccessfull("MIN(3.0; 4.0)", Decimal.valueOf("3.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MIN(4.0; 3.0)", Decimal.valueOf("3.0"), Datatype.DECIMAL);
        execAndTestSuccessfull("MIN(4; 3)", Decimal.valueOf("3"), Datatype.DECIMAL);
    }

}
