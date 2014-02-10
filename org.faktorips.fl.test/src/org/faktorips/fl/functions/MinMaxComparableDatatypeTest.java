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

public class MinMaxComparableDatatypeTest extends FunctionAbstractTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        registerFunction(new MinMaxComparableDatatypes("MAX", "", true, Datatype.STRING));
        registerFunction(new MinMaxComparableDatatypes("MIN", "", false, Datatype.STRING));
    }

    @Test
    public void testCompile_max() throws Exception {
        execAndTestSuccessfull("MAX(\"aaa\"; \"zzz\")", "zzz", Datatype.STRING);
        execAndTestSuccessfull("MAX(\"zzz\"; \"aaa\")", "zzz", Datatype.STRING);
        execAndTestSuccessfull("MAX(\"1\"; \"1\")", "1", Datatype.STRING);
    }

    @Test
    public void testCompile_min() throws Exception {
        execAndTestSuccessfull("MIN(\"aaa\"; \"zzz\")", "aaa", Datatype.STRING);
        execAndTestSuccessfull("MIN(\"zzz\"; \"aaa\")", "aaa", Datatype.STRING);
        execAndTestSuccessfull("MIN(\"1\"; \"1\")", "1", Datatype.STRING);
    }

    @Test
    public void testFail() throws Exception {
        execAndTestFail("MAX(10)", "FLC-WrongArgumentTypes");
        execAndTestFail("MIN(\"aaa\")", "FLC-WrongArgumentTypes");
    }

}
