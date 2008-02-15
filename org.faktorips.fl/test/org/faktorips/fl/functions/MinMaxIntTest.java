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

public class MinMaxIntTest extends FunctionAbstractTest {

    public final void testCompile() throws Exception {
        registerFunction(new MinMaxInt("MAX", "", true));
        execAndTestSuccessfull("MAX(3; 4)", new Integer(4), Datatype.INTEGER);
        execAndTestSuccessfull("MAX(4; 3)", new Integer(4), Datatype.INTEGER);

        registerFunction(new MinMaxInt("MIN", "", false));
        execAndTestSuccessfull("MIN(3; 4)", new Integer(3), Datatype.INTEGER);
        execAndTestSuccessfull("MIN(4; 3)", new Integer(3), Datatype.INTEGER);
    }

}
