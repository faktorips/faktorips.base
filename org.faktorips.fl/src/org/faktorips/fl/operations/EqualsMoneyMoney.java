package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


/**
 * Operation for the equality check of two money objects. 
 */
public class EqualsMoneyMoney extends AbstractBinaryOperation {

    public EqualsMoneyMoney() {
        super("=", Datatype.MONEY, Datatype.MONEY);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs,
            CompilationResultImpl rhs) {
        lhs.getCodeFragment().append(".equals(");
        lhs.add(rhs);
        lhs.getCodeFragment().append(')');
        lhs.setDatatype(Datatype.PRIMITIVE_BOOLEAN);
        return lhs;
    }

}
