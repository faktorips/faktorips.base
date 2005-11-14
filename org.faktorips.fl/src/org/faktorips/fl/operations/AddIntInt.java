package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the addition of two ints. 
 */
public class AddIntInt extends AbstractBinaryOperation {

    public AddIntInt() {
        super("+", Datatype.PRIMITIVE_INT, Datatype.PRIMITIVE_INT);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(" + ");
        lhs.add(rhs);
        return lhs;
    }

}
