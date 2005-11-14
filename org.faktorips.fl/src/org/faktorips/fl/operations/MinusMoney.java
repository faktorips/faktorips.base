package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Minus (-) operator for datatype Money.
 */
public class MinusMoney extends AbstractUnaryOperation {

    public MinusMoney() {
        super(Datatype.MONEY, "-");
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.UnaryOperation#generate(org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl result) {
        result.getCodeFragment().append(".multiply(-1)");
        return result;
    }

}
