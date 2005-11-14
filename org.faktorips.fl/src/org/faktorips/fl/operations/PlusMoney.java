package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Plus (+) operator for datatype Decimal.
 */
public class PlusMoney extends AbstractUnaryOperation {

    public PlusMoney() {
        super(Datatype.MONEY, "+");
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.UnaryOperation#generate(org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl arg) {
        return arg;
    }

}
