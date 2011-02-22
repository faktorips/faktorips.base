/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;
import org.junit.Test;

public class MinMaxLongTest extends FunctionAbstractTest {
    @Test
    public void testCompile() throws Exception {
        registerFunction(new MinMaxLong("MAX", "", true));
        execAndTestSuccessfull("MAX(a; b)", new Long(4), new String[] { "a", "b" }, new Datatype[] { Datatype.LONG,
                Datatype.LONG }, new Object[] { new Long(4), new Long(3) }, Datatype.LONG);
        execAndTestSuccessfull("MAX(a; b)", new Long(4), new String[] { "a", "b" }, new Datatype[] { Datatype.LONG,
                Datatype.LONG }, new Object[] { new Long(3), new Long(4) }, Datatype.LONG);
        execAndTestSuccessfull("MAX(a; b)", new Long(4), new String[] { "a", "b" }, new Datatype[] { Datatype.LONG,
                Datatype.PRIMITIVE_LONG }, new Object[] { new Long(3), new Long(4) }, Datatype.LONG);
        execAndTestSuccessfull("MAX(a; b)", new Long(4), new String[] { "a", "b" }, new Datatype[] {
                Datatype.PRIMITIVE_LONG, Datatype.LONG }, new Object[] { new Long(3), new Long(4) }, Datatype.LONG);

        registerFunction(new MinMaxLong("MIN", "", false));
        execAndTestSuccessfull("MIN(a; b)", new Long(3), new String[] { "a", "b" }, new Datatype[] { Datatype.LONG,
                Datatype.LONG }, new Object[] { new Long(4), new Long(3) }, Datatype.LONG);
        execAndTestSuccessfull("MIN(a; b)", new Long(3), new String[] { "a", "b" }, new Datatype[] { Datatype.LONG,
                Datatype.LONG }, new Object[] { new Long(3), new Long(4) }, Datatype.LONG);
        execAndTestSuccessfull("MIN(a; b)", new Long(3), new String[] { "a", "b" }, new Datatype[] {
                Datatype.PRIMITIVE_LONG, Datatype.LONG }, new Object[] { new Long(3), new Long(4) }, Datatype.LONG);
        execAndTestSuccessfull("MIN(a; b)", new Long(3), new String[] { "a", "b" }, new Datatype[] { Datatype.LONG,
                Datatype.PRIMITIVE_LONG }, new Object[] { new Long(3), new Long(4) }, Datatype.LONG);

    }

}
