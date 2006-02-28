package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;

public class AndTest extends FunctionAbstractTest {

    protected void setUp() throws Exception {
        super.setUp();
        registerFunction(new And("AND", ""));
        compiler.setEnsureResultIsObject(false);
    }

    public void testCompile() throws Exception{
        //test if multiple arguments work
        execAndTestSuccessfull("AND(true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(true; true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(true; true; false)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
        
        //test if the result of an expression as argument for the OR-function works
        execAndTestSuccessfull("AND(1=1)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("AND(1!=1)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
    }
}
