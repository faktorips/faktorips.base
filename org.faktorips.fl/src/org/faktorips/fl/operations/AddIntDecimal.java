package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the addition of two decimals. 
 */
public class AddIntDecimal extends AbstractBinaryOperation {

    public AddIntDecimal() {
        super("+", Datatype.PRIMITIVE_INT, Datatype.DECIMAL);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        rhs.getCodeFragment().append(".add(");
        rhs.add(lhs);
        rhs.getCodeFragment().append(')');
        return rhs;
    }

}
