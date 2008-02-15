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

public class MinMaxLongTest extends FunctionAbstractTest {

    public final void testCompile() throws Exception {
        registerFunction(new MinMaxLong("MAX", "", true));
        execAndTestSuccessfull("MAX(a; b)", new Long(4), new String[]{"a", "b"}, new Datatype[]{Datatype.LONG, Datatype.LONG}, new Object[]{new Long(4), new Long(3)}, Datatype.LONG);
        execAndTestSuccessfull("MAX(a; b)", new Long(4), new String[]{"a", "b"}, new Datatype[]{Datatype.LONG, Datatype.LONG}, new Object[]{new Long(3), new Long(4)}, Datatype.LONG);

        registerFunction(new MinMaxLong("MIN", "", false));
        execAndTestSuccessfull("MIN(a; b)", new Long(3), new String[]{"a", "b"}, new Datatype[]{Datatype.LONG, Datatype.LONG}, new Object[]{new Long(4), new Long(3)}, Datatype.LONG);
        execAndTestSuccessfull("MIN(a; b)", new Long(3), new String[]{"a", "b"}, new Datatype[]{Datatype.LONG, Datatype.LONG}, new Object[]{new Long(3), new Long(4)}, Datatype.LONG);

    }

}
