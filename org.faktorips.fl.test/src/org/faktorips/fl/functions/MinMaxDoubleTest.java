/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
