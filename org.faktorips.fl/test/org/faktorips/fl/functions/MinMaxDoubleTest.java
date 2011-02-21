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

public class MinMaxDoubleTest extends FunctionAbstractTest {
    @Test
    public void testCompile() throws Exception {
        registerFunction(new MinMaxDouble("MAX", "", true));
        execAndTestSuccessfull("MAX(a; b)", new Double(4.4), new String[] { "a", "b" }, new Datatype[] {
                Datatype.DOUBLE, Datatype.DOUBLE }, new Object[] { new Double(4.4), new Double(3.5) }, Datatype.DOUBLE);
        execAndTestSuccessfull("MAX(a; b)", new Double(4.4), new String[] { "a", "b" }, new Datatype[] {
                Datatype.DOUBLE, Datatype.DOUBLE }, new Object[] { new Double(3.5), new Double(4.4) }, Datatype.DOUBLE);

        registerFunction(new MinMaxDouble("MIN", "", false));
        execAndTestSuccessfull("MIN(a; b)", new Double(3.5), new String[] { "a", "b" }, new Datatype[] {
                Datatype.DOUBLE, Datatype.DOUBLE }, new Object[] { new Double(4.4), new Double(3.5) }, Datatype.DOUBLE);
        execAndTestSuccessfull("MIN(a; b)", new Double(3.5), new String[] { "a", "b" }, new Datatype[] {
                Datatype.DOUBLE, Datatype.DOUBLE }, new Object[] { new Double(3.5), new Double(4.4) }, Datatype.DOUBLE);

    }

}
