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
