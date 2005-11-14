package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Plus (+) operator for datatype Decimal.
 */
public class MinusDecimal extends AbstractUnaryOperation {

    public MinusDecimal() {
        super(Datatype.DECIMAL, "-");
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.UnaryOperation#generate(org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        arg.getCodeFragment().append(".multiply(-1)");
        return arg;
    }

}
