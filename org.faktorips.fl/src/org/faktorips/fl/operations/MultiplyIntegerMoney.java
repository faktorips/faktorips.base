package org.faktorips.fl.operations;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;


public class MultiplyIntegerMoney extends AbstractBinaryOperation {

    public MultiplyIntegerMoney() {
        super("*", Datatype.INTEGER, Datatype.MONEY);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#generate(org.faktorips.fl.CompilationResultImpl, org.faktorips.fl.CompilationResultImpl)
     */
    public CompilationResultImpl generate(CompilationResultImpl lhs, CompilationResultImpl rhs) {
        JavaCodeFragment fragment = rhs.getCodeFragment();
        fragment.append(".multiply(");
        fragment.append(lhs.getCodeFragment());
        fragment.append(")");
        CompilationResultImpl result = new CompilationResultImpl(fragment, Datatype.MONEY);
        result.addMessages(lhs.getMessages());
        result.addMessages(rhs.getMessages());
        return result;
    }

}
