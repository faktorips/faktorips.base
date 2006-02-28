package org.faktorips.fl.functions;

import org.faktorips.datatype.Datatype;

public class OrTest extends FunctionAbstractTest {

    protected void setUp() throws Exception {
        super.setUp();
        registerFunction(new Or("OR", ""));
        compiler.setEnsureResultIsObject(false);
    }

    public void testCompile() throws Exception{
        //test if multiple arguments work
        execAndTestSuccessfull("OR(true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; true; true)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(true; true; false)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(false; false; false)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
        
        //test if the result of an expression as argument for the OR-function works
        execAndTestSuccessfull("OR(1=1)", Boolean.TRUE, Datatype.PRIMITIVE_BOOLEAN);
        execAndTestSuccessfull("OR(1!=1)", Boolean.FALSE, Datatype.PRIMITIVE_BOOLEAN);
    }
}
