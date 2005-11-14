package org.faktorips.fl;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.Decimal;


/**
 *
 */
public class ParanthesisTest extends AbstractCompilerTest {
    
    public void test() throws Exception {
        execAndTestSuccessfull("3.0 + 2.0 * 5.0", Decimal.valueOf("13.00"), Datatype.DECIMAL);
        execAndTestSuccessfull("(3.0 + 2.0) * 5.0", Decimal.valueOf("25.00"), Datatype.DECIMAL);        
    }

    
}
