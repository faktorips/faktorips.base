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

public class MinMaxDoubleTest extends FunctionAbstractTest {

    public final void testCompile() throws Exception {
        registerFunction(new MinMaxDouble("MAX", "", true));
        execAndTestSuccessfull("MAX(a; b)", new Double(4.4), new String[]{"a", "b"}, new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE}, new Object[]{new Double(4.4), new Double(3.5)}, Datatype.DOUBLE);
        execAndTestSuccessfull("MAX(a; b)", new Double(4.4), new String[]{"a", "b"}, new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE}, new Object[]{new Double(3.5), new Double(4.4)}, Datatype.DOUBLE);

        registerFunction(new MinMaxDouble("MIN", "", false));
        execAndTestSuccessfull("MIN(a; b)", new Double(3.5), new String[]{"a", "b"}, new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE}, new Object[]{new Double(4.4), new Double(3.5)}, Datatype.DOUBLE);
        execAndTestSuccessfull("MIN(a; b)", new Double(3.5), new String[]{"a", "b"}, new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE}, new Object[]{new Double(3.5), new Double(4.4)}, Datatype.DOUBLE);

    }

}
