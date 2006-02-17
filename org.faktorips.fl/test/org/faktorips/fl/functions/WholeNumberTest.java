package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;

/**
 *
 */
public class WholeNumberTest extends FunctionAbstractTest {

    public void test() throws Exception {
        registerFunction(new WholeNumber("WHOLENUMBER", ""));
        execAndTestSuccessfull("WHOLENUMBER(3.24)", new Integer(3), Datatype.INTEGER);
        execAndTestSuccessfull("WHOLENUMBER(-3.24)", new Integer(-3), Datatype.INTEGER);
    }
}
